package fr.ksuto.prh.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fr.ksuto.commons.PropertiesLoader;
import fr.ksuto.commons.helpers.InOut;
import fr.ksuto.prh.capture.Capture;
import fr.ksuto.prh.capture.Frame;
import fr.ksuto.prh.capture.Rgb;
import fr.ksuto.prh.entities.*;
import fr.ksuto.prh.peripherals.Mouse;
import fr.ksuto.prh.peripherals.Peripheral;
import fr.ksuto.prh.peripherals.Screen;
import fr.ksuto.prh.tools.SearchMemory;
import fr.ksuto.prh.tools.ShowObjects;

import java.awt.*;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Recherche d'objets à l'écran (images, blocs de couleur), avec deux mécanismes d'auto-apprentissage, mémorisés dans une
 * base SQLite ({@link SearchMemory}, partagée par tous les bots) :
 * <ul>
 *   <li>{@link #learn(int)} : on connaît le nombre d'objets attendu ; chaque recherche essaie quelques combinaisons de
 *   tolérances sur la même capture, note celles qui trouvent exactement ce nombre, et retient la plus fiable
 *   ({@link ParameterLearning}) ;</li>
 *   <li>{@link #optimize()} : les positions trouvées sont retenues ; une fois assez de trouvailles, la recherche se limite
 *   au rectangle qui les englobe (plus une marge). Si rien n'y est trouvé, elle reprend aussitôt sur toute sa zone, et
 *   après {@value #MISSES_BEFORE_RESET} échecs de suite la zone réduite est oubliée.</li>
 * </ul>
 * La mémoire est rangée par recherche : images cherchées et résolution de l'écran.
 */
@SuppressWarnings({"UnusedReturnValue"})
public abstract class AbstractSeeker<S extends AbstractSeeker<S, T>, T extends LocatedObject> {

    /**
     * Échecs de suite dans la zone réduite avant de l'oublier (l'objet a changé de place : fenêtre déplacée...).
     */
    static final int MISSES_BEFORE_RESET = 3;

    /**
     * Combinaisons de tolérances essayées par recherche en apprentissage : chacune coûte un parcours de la capture.
     */
    static final int DEFAULT_TRIALS_PER_SEARCH = 8;

    private static SearchMemory sharedMemory;

    private final int delay;
    public boolean optimizing = false;
    public boolean learning = false;
    public Integer precision = null;
    public Integer expectedResults = null;
    public Double allowedErrorRate = null;
    public int exclusiveZone = 0;
    public int clickDelay;
    public int searchDelay = 0;
    public boolean isTracking = false;
    public java.util.List<T> objects = new ArrayList<>();
    public int maximumMovement = 20;
    public Screen.Zone searchZone = Screen.Zone.ALL;
    public java.util.List<Screen.Zone> searchZones = new ArrayList<>();
    protected Properties properties;
    final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Zone effectivement capturée et parcourue pendant une recherche : les positions trouvées y sont relatives, et
     * ramenées à l'écran avec son origine.
     */
    Rectangle scanArea = new Rectangle();

    private ShowObjects<T> showObjects;
    private Robot robot;
    private Mouse mouse;
    private SearchMemory memory;
    private Function<Rectangle, Frame> capture = Capture::zone;
    private boolean showTargets = false;
    private boolean debug = false;
    private boolean saveCaptureOnNotFound = false;
    private boolean clickUntilDisappear = false;
    private int iterationsBeforeOptimizing = 10;
    private int iterationsToKeep = 50;
    private int trialsPerSearch = DEFAULT_TRIALS_PER_SEARCH;
    private int missesInReducedArea = 0;

    public AbstractSeeker() throws AWTException {

        this.properties = PropertiesLoader.load("prh");
        this.delay = Integer.parseInt(properties.getProperty("ksuto.prh.peripherals.delay", "100"));
        this.clickDelay = this.delay;
    }

    /**
     * Mémoire partagée par toutes les recherches du programme : {@code ksuto.prh.database.file} (par défaut
     * {@code ~/.ksuto/prh.db}), ou en mémoire seulement si {@code ksuto.prh.database.offline=true} ou si le fichier est
     * inaccessible.
     */
    static synchronized SearchMemory sharedMemory(Properties properties) {

        if (sharedMemory != null) {return sharedMemory;}
        if (Boolean.parseBoolean(properties.getProperty("ksuto.prh.database.offline", "false"))) {
            sharedMemory = SearchMemory.inMemory();
            return sharedMemory;
        }
        Path file = databaseFile(properties);
        try {
            sharedMemory = SearchMemory.open(file);
        }
        catch (SQLException e) {
            LoggerFactory.getLogger(AbstractSeeker.class).warn("Mémoire des recherches inaccessible ({}), apprentissage non conservé : {}", file, e.getMessage());
            sharedMemory = SearchMemory.inMemory();
        }
        return sharedMemory;
    }

    /**
     * Fichier de la mémoire : {@code ksuto.prh.database.file}, où un {@code ~} initial désigne le dossier de
     * l'utilisateur (sous Windows aussi : {@code C:\Users\nom}) ; par défaut {@code ~/.ksuto/prh.db}.
     */
    static Path databaseFile(Properties properties) {

        String file = properties.getProperty("ksuto.prh.database.file", "~/.ksuto/prh.db").trim();
        if (file.equals("~") || file.startsWith("~/") || file.startsWith("~\\")) {
            return Path.of(System.getProperty("user.home"), file.substring(1).replaceFirst("^[/\\\\]", ""));
        }
        return Path.of(file);
    }

    // --- Configuration ---

    public AbstractSeeker<S, T> addSearchZone(Screen.Zone zone) {

        this.searchZones.add(zone);

        return this;
    }

    /**
     * Source des captures d'écran (zone demandée → image) : {@link Capture#zone} par défaut ; une autre pour les tests ou
     * une capture ailleurs que sur l'écran.
     */
    public AbstractSeeker<S, T> setCapture(Function<Rectangle, Frame> capture) {

        this.capture = capture;
        return this;
    }

    /**
     * Mémoire d'apprentissage : la base partagée par défaut ; une autre pour les tests ou pour isoler un bot.
     */
    public AbstractSeeker<S, T> setMemory(SearchMemory memory) {

        this.memory = memory;
        return this;
    }

    /**
     * @param trialsPerSearch combinaisons de tolérances essayées par recherche en apprentissage
     */
    public AbstractSeeker<S, T> setTrialsPerSearch(int trialsPerSearch) {

        this.trialsPerSearch = Math.max(1, trialsPerSearch);
        return this;
    }

    private SearchMemory memory() {

        if (memory == null) {memory = sharedMemory(properties);}
        return memory;
    }

    /**
     * Clé de cette recherche dans la mémoire : les images cherchées et la résolution de l'écran (les positions et les
     * tolérances apprises ne valent que pour elle).
     */
    public String getSearchKey() {

        return getObjectsHash() + "@" + Screen.SCREEN_WIDTH + "x" + Screen.SCREEN_HEIGHT;
    }

    // --- Attente et clics ---

    public AbstractSeeker<S, T> await() {

        return await(15000);
    }

    public AbstractSeeker<S, T> await(int milliseconds) {

        long until = System.currentTimeMillis() + milliseconds;
        logger.debug("Awaiting " + getObjectsHash());
        while (System.currentTimeMillis() < until) {
            logger.debug("Time's up : " + (int) Math.floor((until - System.currentTimeMillis()) / 1000f) + "s    ");
            search();
            if (expectedFound()) {
                return this;
            }
        }

        clean();

        return this;
    }

    public AbstractSeeker<S, T> clean() {

        if (showObjects != null) {
            showObjects.clean();
        }

        return this;
    }

    /**
     * Oublie la zone de recherche réduite et les positions retenues.
     */
    public AbstractSeeker<S, T> clearAreaOptimization() {

        memory().clearArea(getSearchKey());

        return this;
    }

    /**
     * Oublie tout ce qui a été appris sur cette recherche.
     */
    public AbstractSeeker<S, T> clearObjectOptimizations() {

        memory().clear(getSearchKey());

        return this;
    }

    /**
     * Oublie les tolérances apprises.
     */
    public AbstractSeeker<S, T> clearParametersOptimization() {

        memory().clearParameters(getSearchKey());

        return this;
    }

    public AbstractSeeker<S, T> clearResults() {

        objects.forEach(o -> {
            o.setPositions(new ArrayList<>());
            o.setPresent(false);
        });

        return this;
    }

    public AbstractSeeker<S, T> clearSearchZones(Screen.Zone zone) {

        this.searchZones.clear();
        this.searchZone = Screen.Zone.ALL;

        return this;
    }

    public AbstractSeeker<S, T> click() {

        do {
            if (hasAnyResults()) {
                objects.forEach(object -> object.getPositions().forEach(position -> clickRandomlyInObject(position, object)));
                if (clickUntilDisappear) {
                    Peripheral.delay(1000);
                }
            }
        }
        while (clickUntilDisappear && search().hasAnyResults());

        return this;
    }

    public AbstractSeeker<S, T> clickFirst() {

        return clickNth(1);
    }

    public AbstractSeeker<S, T> clickFirst(AbstractPictureEnum pictureEnum) {

        return clickNth(1, pictureEnum);
    }

    public AbstractSeeker<S, T> clickNth(int index) {

        T object = getFirstResult();

        if (hasAnyResults()) {
            clickRandomlyInObject(object.getPositions().get(index - 1), object);
        }

        return this;
    }

    public AbstractSeeker<S, T> clickNth(int index, AbstractPictureEnum pictureEnum) {

        T object = getObject(pictureEnum);

        if (hasAnyResults(pictureEnum)) {
            clickRandomlyInObject(object.getPositions().get(index - 1), object);
        }

        return this;
    }

    public AbstractSeeker<S, T> clickUntilDisappear() {

        this.clickUntilDisappear = true;
        AbstractSeeker<S, T> seeker = click();
        this.clickUntilDisappear = false;

        return seeker;
    }

    public abstract AbstractSeeker<S, T> findWorkingParameters(int numberOfMatches, int numberOfNoChangeLoops);

    public abstract AbstractSeeker<S, T> findWorkingParameters(int numberOfMatches);

    public T getObject(AbstractPictureEnum pictureEnum) {

        for (T object : getObjects()) {
            if (object.getHash().equals(pictureEnum.getHash())) {
                return object;
            }
        }

        return null;
    }

    public boolean hasAnyResults() {

        return objects.stream().anyMatch(LocatedObject::isPresent);
    }

    public boolean hasAnyResults(AbstractPictureEnum pictureEnum) {

        T object = getObject(pictureEnum);
        return object.isPresent();
    }

    public AbstractSeeker<S, T> hideObjects() {

        if (showObjects != null) {
            showObjects.setVisible(false);
        }
        return this;
    }

    public ShowObjects<T> initShowObjects(boolean clean) {

        if (clean && showObjects != null) {
            showObjects.clean();
            showObjects = null;
        }

        if (showObjects != null) {
            return showObjects;
        }

        showObjects = new ShowObjects<>(searchZone, objects.stream().map(LocatedObject::getHash).collect(Collectors.joining(", ")));
        showObjects.setErrorRate(allowedErrorRate);
        showObjects.setPrecision(precision);

        return showObjects;
    }

    /**
     * Apprentissage des tolérances : la recherche doit trouver exactement {@code expectedResults} objets.
     */
    public AbstractSeeker<S, T> learn(int expectedResults) {

        this.learning = true;
        this.expectedResults = expectedResults;

        return this;
    }

    public int numberOfResults() {

        return objects.stream().map(LocatedObject::getNumberOfResults).mapToInt(Integer::intValue).sum();
    }

    public AbstractSeeker<S, T> optimize() {

        return optimize(10);
    }

    public AbstractSeeker<S, T> optimize(int iterationsBeforeOptimizing) {

        return optimize(iterationsBeforeOptimizing, 100);
    }

    /**
     * Réduction de la zone de recherche aux endroits où les objets ont été trouvés.
     *
     * @param iterationsBeforeOptimizing positions retenues nécessaires avant de réduire la zone
     * @param iterationsToKeep           positions retenues au plus (les plus récentes)
     */
    public AbstractSeeker<S, T> optimize(int iterationsBeforeOptimizing, int iterationsToKeep) {

        this.optimizing = true;
        this.iterationsBeforeOptimizing = iterationsBeforeOptimizing;
        this.iterationsToKeep = Math.max(iterationsToKeep, iterationsBeforeOptimizing);

        return this;
    }

    public AbstractSeeker<S, T> reintialize() {

        objects.clear();

        return this;
    }

    // --- Recherche ---

    /**
     * Cherche les objets à l'écran : les résultats précédents sont remplacés (sauf en suivi, où ils sont mis à jour).
     */
    public AbstractSeeker<S, T> search() {

        if (!isTracking) {clearResults();}

        if (!searchZones.isEmpty()) {
            for (Screen.Zone zone : searchZones) {
                searchIn(zone.getRectangle(), false);
            }
        }
        else {
            searchIn(searchZone.getRectangle(), optimizing);
        }

        if (optimizing) {rememberPositions();}

        return this;
    }

    /**
     * Les objets attendus sont trouvés : leur nombre exact s'il est connu, au moins un sinon.
     */
    public boolean expectedFound() {

        return expectedResults == null ? hasAnyResults() : numberOfResults() == expectedResults;
    }

    /**
     * Recherche dans une zone, réduite à la zone apprise si {@code reducible} ; retour à la zone complète si rien n'y est
     * trouvé.
     */
    private void searchIn(Rectangle zone, boolean reducible) {

        long startTime = System.currentTimeMillis();
        Peripheral.delay(searchDelay);

        // Pas de zone réduite en apprentissage : un objet hors de la zone fausserait le jugement des tolérances
        Optional<Rectangle> reduced = reducible && !learning ? memory().area(getSearchKey()).map(area -> area.intersection(zone)).filter(area -> !area.isEmpty())
                                                : Optional.empty();
        Rectangle area = reduced.orElse(zone);

        Frame captured = captureAndScan(area);

        if (reduced.isPresent()) {
            if (expectedFound()) {
                missesInReducedArea = 0;
            }
            else {
                missesInReducedArea++;
                if (missesInReducedArea >= MISSES_BEFORE_RESET) {
                    logger.debug("{} : introuvable {} fois dans sa zone réduite, zone oubliée", getObjectsHash(), missesInReducedArea);
                    memory().clearArea(getSearchKey());
                    missesInReducedArea = 0;
                }
                if (!isTracking) {clearResults();}
                captured = captureAndScan(zone);
            }
        }

        if (debug || showTargets) {
            initShowObjects(false).setLocatedObjects(objects);
        }

        long elapsed = System.currentTimeMillis() - startTime;
        if (elapsed > 1000) {
            logger.warn(getObjectsHash() + " search took " + elapsed + "ms");
        }

        if (!hasAnyResults() && (saveCaptureOnNotFound || Boolean.parseBoolean(properties.getProperty("ksuto.prh.seeker.saveCaptureOnNotFound", "false")))) {
            InOut.writeImage(captured.image(), ".debug/NotFound_" + getObjectsHash() + "_" + System.currentTimeMillis() + ".png");
        }
    }

    private Frame captureAndScan(Rectangle area) {

        Frame captured = capture.apply(area);
        scanArea = new Rectangle(area);

        if (debug) {
            InOut.writeImage(captured.image(), "image");
        }

        if (isTracking) {
            updatePositions(captured);
        }

        if (learning && expectedResults != null) {
            learnOn(captured);
        }
        else if (precision == null || allowedErrorRate == null) {
            // Tolérances non précisées : celles apprises par une recherche précédente, s'il y en a
            ParameterLearning.best(memory().parameterStats(getSearchKey())).ifPresent(this::apply);
        }

        scan(captured);
        return captured;
    }

    /**
     * Essaie quelques combinaisons de tolérances sur cette capture, note celles qui trouvent le nombre d'objets attendu,
     * et applique la meilleure connue.
     */
    private void learnOn(Frame captured) {

        String                                 key      = getSearchKey();
        Map<Parameter, ParameterLearning.Stat> stats    = new HashMap<>(memory().parameterStats(key));
        Map<Parameter, Boolean>                outcomes = new LinkedHashMap<>();

        for (Parameter trial : ParameterLearning.nextTrials(stats, trialsPerSearch)) {
            clearResults(); // chaque combinaison est jugée sur ses seuls résultats
            apply(trial);
            scan(captured);
            boolean success = numberOfResults() == expectedResults;
            outcomes.put(trial, success);
            stats.merge(trial, ParameterLearning.Stat.NONE.plus(success), (old, added) -> old.plus(success));
        }
        memory().recordTrials(key, outcomes);
        clearResults();

        Optional<Parameter> best = ParameterLearning.best(stats);
        if (best.isPresent()) {
            apply(best.get());
        }
        else if (debug) {
            logger.debug("{} : aucune combinaison de tolérances n'a encore fait ses preuves", getObjectsHash());
        }
    }

    private void apply(Parameter parameter) {

        setPrecision(parameter.precision());
        setAllowedErrorRate(parameter.errorRate());
    }

    /**
     * Parcourt la capture et ajoute les objets trouvés (positions à l'écran, origine de {@link #scanArea}).
     */
    private void scan(Frame captured) {

        for (T object : objects) {

            Position currentPosition = new Position(0, 0);

            for (; currentPosition.getY() < captured.height() - (object.getHeight() + exclusiveZone); currentPosition.incY()) {
                currentPosition.setX(0);
                for (; currentPosition.getX() < captured.width() - (object.getWidth() + exclusiveZone); currentPosition.incX()) {

                    while (overlapingExists(currentPosition)) {
                        currentPosition.setX(currentPosition.getX() + exclusiveZone * 2 + object.getWidth());
                    }

                    searchObject(captured, currentPosition, object);
                }
            }

            object.getPositions().sort((o1, o2) -> o1.getY() == o2.getY() ? o1.getX() - o2.getX() : o1.getY() - o2.getY());
        }
    }

    /**
     * Retient les positions trouvées et, une fois assez de positions, réduit la zone de recherche à leur rectangle
     * englobant. Une écriture groupée par recherche.
     */
    private void rememberPositions() {

        List<Point> found = getAllPositions().stream().map(position -> new Point(position.getX(), position.getY())).toList();
        if (found.isEmpty()) {return;}

        String key = getSearchKey();
        memory().addPositions(key, found, iterationsToKeep);
        List<Point> positions = memory().positions(key);
        if (positions.size() >= iterationsBeforeOptimizing) {
            int maxWidth  = objects.stream().mapToInt(LocatedObject::getWidth).max().orElse(0);
            int maxHeight = objects.stream().mapToInt(LocatedObject::getHeight).max().orElse(0);
            memory().setArea(key, areaAround(positions, maxWidth, maxHeight, searchZone.getRectangle()));
        }
    }

    /**
     * Zone de recherche autour des positions trouvées : leur rectangle englobant, élargi de la moitié de son étendue, de
     * la taille du plus grand objet et de 10 pixels, borné à la zone de recherche complète.
     */
    static Rectangle areaAround(List<Point> positions, int objectWidth, int objectHeight, Rectangle bounds) {

        int x1 = positions.stream().mapToInt(p -> p.x).min().orElse(bounds.x);
        int x2 = positions.stream().mapToInt(p -> p.x).max().orElse(bounds.x);
        int y1 = positions.stream().mapToInt(p -> p.y).min().orElse(bounds.y);
        int y2 = positions.stream().mapToInt(p -> p.y).max().orElse(bounds.y);

        int growth = 10;
        int left   = x1 - (x2 - x1) / 2 - growth;
        int top    = y1 - (y2 - y1) / 2 - growth;
        int right  = x2 + (x2 - x1) / 2 + objectWidth + 1 + growth;
        int bottom = y2 + (y2 - y1) / 2 + objectHeight + 1 + growth;

        return new Rectangle(left, top, right - left, bottom - top).intersection(bounds);
    }

    public boolean shouldClose() {

        return showObjects.shouldClose();
    }

    public AbstractSeeker<S, T> showObjects() {

        if (showObjects != null) {
            showObjects.setVisible(true);
        }
        return this;
    }

    public AbstractSeeker<S, T> saveCaptureOnNotFound() {
        this.saveCaptureOnNotFound = true;
        return this;
    }

    public AbstractSeeker<S, T> startDebug() {

        this.debug = true;
        this.showObjects = initShowObjects(false);
        this.showObjects.setShowCount(true);
        return this;
    }

    public AbstractSeeker<S, T> stopDebug() {

        this.debug = false;
        clean();
        this.showObjects = null;
        return this;
    }

    public boolean waitAndClick() {

        return waitAndClick(15000);
    }

    public boolean waitAndClick(int milliseconds) {

        return waitAndClick(milliseconds, false);
    }

    public boolean waitAndClick(int milliseconds, boolean clickFirstResultOnly) {

        await(milliseconds);

        boolean found;

        do {
            found = expectedResults == null ? numberOfResults() >= 1 : numberOfResults() == expectedResults;

            if (found) {
                if (clickFirstResultOnly) {

                    T object = getFirstResult();
                    clickRandomlyInObject(object.getPositions().get(0), object);
                } else {
                    objects.forEach(object -> object.getPositions().forEach(position -> clickRandomlyInObject(position, object)));
                }
            }
        }
        while (clickUntilDisappear && search().hasAnyResults());

        clean();

        return found;
    }

    public boolean waitAndClickFirstMatch() {

        return waitAndClickFirstMatch(15000);
    }

    public boolean waitAndClickFirstMatch(int milliseconds) {

        return waitAndClick(milliseconds, true);
    }

    boolean isMatch(int capturedRGB, int refRGB) {

        if (getPrecision() == 0) {
            return capturedRGB == refRGB;
        }

        return Rgb.isClose(capturedRGB, refRGB, getPrecision());
    }

    abstract boolean isObjectFound(Frame capturedScreen, Position currentPosition, T object);

    /**
     * Un objet déjà trouvé occupe cette position de la capture (positions retenues : à l'écran).
     */
    boolean overlapingExists(Position currentPosition) {

        for (LocatedObject object : objects) {
            for (Position objectPosition : object.getPositions()) {
                if (isOverlaping(currentPosition, objectPosition, object)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Cherche l'objet à cette position de la capture ; s'il y est, l'ajoute à ses positions, ramenée à l'écran avec
     * l'origine de {@link #scanArea}.
     */
    abstract boolean searchObject(Frame capturedScreen, Position currentPosition, T object);

    /**
     * Suivi : chaque objet déjà trouvé est cherché autour de sa dernière position (± {@link #maximumMovement}), et
     * retiré s'il n'y est plus.
     */
    void updatePositions(Frame capturedScreen) {

        for (T object : objects) {

            ListIterator<Position> positionsIterator = object.getPositions().listIterator();

            while (positionsIterator.hasNext()) {

                Position position = positionsIterator.next();
                int      relativeX = position.getX() - scanArea.x;
                int      relativeY = position.getY() - scanArea.y;
                Position moved     = null;

                for (int y = Math.max(0, relativeY - exclusiveZone - maximumMovement);
                     moved == null && y < relativeY + exclusiveZone + maximumMovement && y < capturedScreen.height(); y++) {
                    for (int x = Math.max(0, relativeX - exclusiveZone - maximumMovement);
                         moved == null && x < relativeX + exclusiveZone + maximumMovement && x < capturedScreen.width(); x++) {
                        if (isObjectFound(capturedScreen, new Position(x, y), object)) {
                            moved = new Position(x + scanArea.x, y + scanArea.y);
                            moved.setHasMoved(x != relativeX || y != relativeY);
                        }
                    }
                }

                if (moved == null) {positionsIterator.remove();}
                else {positionsIterator.set(moved);}
            }
            object.setPresent(!object.getPositions().isEmpty());
        }
    }

    private AbstractSeeker<S, T> clickRandomlyInObject(Position position, T object) {

        return clickWithOffset(position, (int) Math.floor(Math.random() * object.getWidth()), (int) Math.floor(Math.random() * object.getHeight()));
    }

    private AbstractSeeker<S, T> clickWithOffset(Position position, int offsetX, int offsetY) {

        try {
            if (robot == null) {robot = new Robot();}
            if (mouse == null) {mouse = new Mouse();}
            Peripheral.delay(clickDelay);
            mouse.move(position.getX() + offsetX, position.getY() + offsetY);
            robot.mousePress(Mouse.LEFT);
            Peripheral.delay(delay);
            robot.mouseRelease(Mouse.LEFT);
        } catch (AWTException e) {
            logger.error("Clic impossible : {}", e.getMessage());
        }
        return this;
    }

    private boolean isOverlaping(Position currentPosition, Position objectPosition, LocatedObject object) {

        int objectX = objectPosition.getX() - scanArea.x;
        int objectY = objectPosition.getY() - scanArea.y;
        if (currentPosition.getY() + object.getHeight() + exclusiveZone <= objectY
                || currentPosition.getY() >= objectY + object.getHeight() + exclusiveZone) {
            return false;
        }
        return currentPosition.getX() + object.getWidth() + exclusiveZone > objectX
               && currentPosition.getX() < objectX + object.getWidth() + exclusiveZone;
    }

    public List<Position> getAllPositions() {

        return getAllResults().stream()
                .map(LocatedObject::getPositions)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public List<T> getAllResults() {

        return objects.stream().filter(LocatedObject::isPresent).collect(Collectors.toList());
    }

    public Double getAllowedErrorRate() {

        double aer = 0.0;
        if (allowedErrorRate != null) {
            aer = allowedErrorRate;
        }

        if (showObjects != null) {
            aer = Math.max(aer + showObjects.errorRateDelta, 0);
        }

        return Math.max(aer, 0);
    }

    /**
     * @param allowedErrorRate entre 0.0 et 1.0
     * @return Seeker
     */
    public AbstractSeeker<S, T> setAllowedErrorRate(double allowedErrorRate) {

        if (allowedErrorRate < 0) {
            allowedErrorRate = 0.0;
        }
        if (allowedErrorRate > 1) {
            allowedErrorRate = 1.0;
        }
        this.allowedErrorRate = allowedErrorRate;

        if (this.showObjects != null) {
            this.showObjects.setErrorRate(allowedErrorRate);
        }

        return this;
    }

    public T getFirstObject() {

        return getObjects().get(0);
    }

    public T getFirstResult() {

        return objects.stream().filter(LocatedObject::isPresent).findFirst().orElse(null);
    }

    public int getNumberOfResults() {

        return numberOfResults();
    }

    public List<T> getObjects() {

        return objects;
    }

    public String getObjectsHash() {

        return objects.stream().map(LocatedObject::getHash).collect(Collectors.joining("|")).replace(".png", "");
    }

    public Integer getPrecision() {

        int p = 0;

        if (precision != null) {
            p = precision;
        }

        if (showObjects != null) {
            p = Math.max(p + showObjects.precisionDelta, 0);
        }

        return Math.max(p, 0);
    }

    /**
     * @param precision tolérance de couleur par canal (0 = pixel identique), de préférence &lt; 100
     * @return Seeker
     */
    public AbstractSeeker<S, T> setPrecision(Integer precision) {

        this.precision = precision;
        if (this.showObjects != null) {
            this.showObjects.setPrecision(precision);
        }

        return this;
    }

    public AbstractSeeker<S, T> setClickDelay(int clickDelay) {

        this.clickDelay = clickDelay;
        return this;
    }

    public AbstractSeeker<S, T> setClickUntilDisappear(boolean clickUntil) {

        this.clickUntilDisappear = clickUntil;

        return this;
    }

    public AbstractSeeker<S, T> setCountDown(int startingFrom) {

        showObjects.countDown(startingFrom);

        return this;
    }

    public AbstractSeeker<S, T> setExclusiveZone(int exclusiveZone) {

        this.exclusiveZone = exclusiveZone;

        return this;
    }

    public AbstractSeeker<S, T> setExpectedResults(int numberOf) {

        this.expectedResults = numberOf;
        return this;
    }

    public AbstractSeeker<S, T> setMaximumMovement(int maximumMovement) {

        this.maximumMovement = maximumMovement;
        return this;
    }

    public AbstractSeeker<S, T> setSearchDelay(int searchDelay) {

        this.searchDelay = searchDelay;
        return this;
    }

    /**
     * Zone de recherche complète ; avec {@link #optimize()}, la recherche se limite à la partie apprise de cette zone.
     */
    public AbstractSeeker<S, T> setSearchZone(Screen.Zone zone) {

        this.searchZone = zone;

        return this;
    }

    public AbstractSeeker<S, T> setShowTargets(boolean showTargets) {

        showObjects = initShowObjects(false);

        this.showTargets = showTargets;

        return this;
    }

    /**
     * @param tracking suivre les objets déjà trouvés d'une recherche à l'autre (défaut false)
     * @return Seeker
     */
    public AbstractSeeker<S, T> setTracking(boolean tracking) {

        isTracking = tracking;

        return this;
    }
}
