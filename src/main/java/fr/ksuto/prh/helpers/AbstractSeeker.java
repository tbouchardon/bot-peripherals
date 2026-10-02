package fr.ksuto.prh.helpers;

import fr.ksuto.commons.PropertiesLoader;
import fr.ksuto.commons.helpers.InOut;
import fr.ksuto.logger.ConsoleLogger;
import fr.ksuto.prh.capture.Capture;
import fr.ksuto.prh.capture.Frame;
import fr.ksuto.prh.capture.Rgb;
import fr.ksuto.prh.entities.*;
import fr.ksuto.prh.peripherals.Mouse;
import fr.ksuto.prh.peripherals.Peripheral;
import fr.ksuto.prh.peripherals.Screen;
import fr.ksuto.prh.tools.SearchHistoryDatabase;
import fr.ksuto.prh.tools.ShowObjects;

import java.awt.*;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings({"UnusedReturnValue"})
public abstract class AbstractSeeker<S extends AbstractSeeker<S, T>, T extends LocatedObject> {

    private static final int NOX_MAX_X = 1572;
    private static final int NOX_MAX_Y = 917;
    //    private static final Dimension dim_D         = new Dimension(Toolkit.getDefaultToolkit().getScreenSize());
    //    private static final int       SCREEN_WIDTH  = (int) dim_D.getWidth();
    //    private static final int       SCREEN_HEIGHT = (int) dim_D.getHeight();
    private static final int NOX_MIN_X = 247;
    private static final int NOX_MIN_Y = 162;
    private final int delay;
    public SearchHistoryDatabase searchHistoryDatabase;
    public SearchHistory searchHistory;
    public boolean optimizing = false;
    public boolean learning = false;
    public Integer precision = null;
    public Integer expectedResults = null;
    public Double allowedErrorRate = null;
    public int exclusiveZone = 0;
    public Robot robot = new Robot();
    public int clickDelay;
    public int searchDelay = 0;
    public boolean isTracking = false;
    public java.util.List<T> objects = new ArrayList<>();
    public int maximumMovement = 20;
    public Screen.Zone searchZone = Screen.Zone.ALL;
    public java.util.List<Screen.Zone> searchZones = new ArrayList<>();
    protected Properties properties;
    ConsoleLogger logger = new ConsoleLogger();
    private ShowObjects<T> showObjects;
    private S seeker;
    private boolean showTargets = false;
    private boolean debug = false;
    private boolean saveCaptureOnNotFound = false;
    private boolean clickUntilDisappear = false;
    private int iterationsBeforeOptimizing = 10;
    private int iterationsToKeep = 50;

    public AbstractSeeker() throws AWTException {

        this.properties = PropertiesLoader.load("prh");
        this.delay = Integer.parseInt(properties.getProperty("ksuto.prh.peripherals.delay", "100"));
        this.clickDelay = this.delay;
        if (properties.getProperty("ksuto.prh.database.offline", "false").equals("false")) {
            searchHistoryDatabase = new SearchHistoryDatabase(properties.getProperty("ksuto.prh.database.name", "prh"));
        }
    }

    public AbstractSeeker<S, T> addSearchZone(Screen.Zone zone) {

        this.searchZones.add(zone);

        return this;
    }

    public AbstractSeeker<S, T> await() {

        return await(15000);
    }

    public AbstractSeeker<S, T> await(int milliseconds) {

        long until = System.currentTimeMillis() + milliseconds;
        logger.sysOutDebug("Awaiting " + getObjectsHash());
        while (System.currentTimeMillis() < until) {
            logger.sysOutDebug("\rTime's up : " + (int) Math.floor((until - System.currentTimeMillis()) / 1000f) + "s    ");
            search();
            if (hasAnyResults() && (expectedResults == null || getNumbreOfResults() == expectedResults)) {
                System.out.println(" ");
                return this;
            }
        }
        System.out.println(" ");

        clean();

        return this;
    }

    public AbstractSeeker<S, T> clean() {

        if (showObjects != null) {
            showObjects.clean();
        }

        return this;
    }

    public AbstractSeeker<S, T> clearAreaOptimization() {

        searchHistoryDatabase.clearAreaOptimization(getObjectsHash());

        return this;
    }

    public AbstractSeeker<S, T> clearObjectOptimizations() {

        searchHistoryDatabase.clearObjectOptimization(getObjectsHash());

        return this;
    }

    public AbstractSeeker<S, T> clearParamtersOptimization() {

        searchHistoryDatabase.clearSearchOptimization(getObjectsHash());

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
        while (clickUntilDisappear && clearResults().search().hasAnyResults());

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

    public Parameter getOptimalSearchParameter(List<Parameter> parameters) {

        if (learning) {

            if (parameters.isEmpty()) {
                for (int precision = 0; precision < 51; precision += 5) {
                    for (double errorRate = 0.0; errorRate <= 0.30; errorRate += 0.05) {
                        parameters.add(new Parameter(precision, errorRate));
                    }
                }
            }

            removeInoperativeParameters(parameters, expectedResults);
        }

        Parameter parameter;
        if (parameters.isEmpty()) {
            parameter = new Parameter(0, 0.0);
        } else {
            parameter = parameters.get(0);
        }
        return parameter;
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

    public AbstractSeeker<S, T> learn(int expectedResults) {

        this.learning = true;
        this.expectedResults = expectedResults;
        setShowTargets(true);

        String hash = getObjectsHash();

        this.searchHistory = searchHistoryDatabase.selectSearchHistory(hash, true, false, true, true);

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

    public AbstractSeeker<S, T> optimize(int iterationsBeforeOptimizing, int iterationsToKeep) {

        this.optimizing = true;
        this.iterationsBeforeOptimizing = iterationsBeforeOptimizing;
        this.iterationsToKeep = iterationsToKeep;

        String hash = getObjectsHash();

        this.searchHistory = searchHistoryDatabase.selectSearchHistory(hash, true, false, true, true);

        if (searchHistory.getOptimisedSearchArea() != null) {

            SearchHistory.Area area = searchHistory.getOptimisedSearchArea();
            searchZone = new Screen.Zone(area.getX_1(), area.getX_2(), area.getY_1(), area.getY_2());
            if (debug || showTargets) {
                initShowObjects(true);
            }
        }

        return this;
    }

    public AbstractSeeker<S, T> reintialize() {

        objects.clear();

        return (AbstractSeeker<S, T>) this;
    }

    public AbstractSeeker<S, T> search() {

        if (!searchZones.isEmpty()) {

            for (Screen.Zone zone : searchZones) {
                searchZone = zone;
                search(false);
            }
        } else {
            search(false);
        }

        return this;
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

        if (clickUntilDisappear) {
            clearResults();
        }

        await(milliseconds);

        boolean found;

        do {
            found = expectedResults == null ? getNumbreOfResults() >= 1 : getNumbreOfResults() == expectedResults;

            if (found) {
                if (clickFirstResultOnly) {

                    T object = getFirstResult();
                    clickRandomlyInObject(object.getPositions().get(0), object);
                } else {
                    objects.forEach(object -> object.getPositions().forEach(position -> clickRandomlyInObject(position, object)));
                }
            }
        }
        while (clickUntilDisappear && clearResults().search().hasAnyResults());

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

    boolean overlapingExists(Position currentPosition) {

        if (!objects.isEmpty()) {
            for (LocatedObject object : objects) {

                for (Position objectPosition : object.getPositions()) {

                    if (isOverlaping(currentPosition, objectPosition, object)) {

                        return true;
                    }
                }
            }
        }
        return false;
    }

    abstract boolean searchObject(Frame capturedScreen, Position currentPosition, T object);

    void updatePositions(Frame capturedScreen) {

        for (T object : objects) {

            Iterator<Position> positionsIterator = object.getPositions().iterator();

            while (positionsIterator.hasNext()) {

                Position position = positionsIterator.next();

                Position currentPosition = new Position(Math.max(0, position.getX() - exclusiveZone - maximumMovement),
                        Math.max(0, position.getY() - exclusiveZone - maximumMovement));

                boolean objectFound = false;

                for (; currentPosition.getY() < position.getY() + object.getHeight() + exclusiveZone + maximumMovement &&
                        currentPosition.getY() < capturedScreen.height(); currentPosition.incY()) {
                    currentPosition.setX(Math.max(0, position.getX() - exclusiveZone - maximumMovement));
                    for (; currentPosition.getX() < position.getX() + object.getWidth() + exclusiveZone + maximumMovement &&
                            currentPosition.getX() < capturedScreen.width(); currentPosition.incX()) {

                        objectFound = isObjectFound(capturedScreen, currentPosition, object);

                        if (objectFound) {
                            boolean hasMoved = position.getY() != currentPosition.getY() ||
                                    position.getX() != currentPosition.getX();
                            position = currentPosition;
                            position.setHasMoved(hasMoved);
                        }
                    }
                }

                if (!objectFound) {
                    positionsIterator.remove();
                }
            }
        }
    }

    private void addPositionAndOptimize(Position currentPosition, boolean relative) {

        SearchHistory.Position position;
        if (relative) {
            position = new SearchHistory.Position(currentPosition.getX() + searchZone.getXMin(), currentPosition.getY() + searchZone.getYMin());
        } else {
            position = new SearchHistory.Position(currentPosition.getX(), currentPosition.getY());
        }
        searchHistoryDatabase.addPosition(position, searchHistory.getHash());
        searchHistoryDatabase.removePositionsOverLimit(searchHistory.getHash(), iterationsToKeep);

        searchHistory.setIterations(searchHistoryDatabase.increaseIterations(searchHistory.getHash()));

        if (searchHistory.getIterations() >= iterationsBeforeOptimizing) {
            optimiseSearchArea();
        }
    }

    private AbstractSeeker<S, T> clickRandomlyInObject(Position position, T object) {

        return clickWithOffset(position, (int) Math.floor(Math.random() * object.getWidth()), (int) Math.floor(Math.random() * object.getHeight()));
    }

    private AbstractSeeker<S, T> clickWithOffset(Position position, int offsetX, int offsetY) {

        try {
            Peripheral.delay(clickDelay);
            (new Mouse()).move(position.getX() + offsetX, position.getY() + offsetY);
            robot.mousePress(Mouse.LEFT);
            Peripheral.delay(delay);
            robot.mouseRelease(Mouse.LEFT);
        } catch (AWTException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return this;
    }

    private boolean isOverlaping(Position currentPosition, Position objectPosition, LocatedObject object) {

        if (currentPosition.getY() + object.getHeight() + exclusiveZone <= (objectPosition.getY() - searchZone.getYMin())
                || currentPosition.getY() >= (objectPosition.getY() - searchZone.getYMin()) + object.getHeight() + exclusiveZone) {
            return false;
        }
        if (currentPosition.getX() + object.getWidth() + exclusiveZone <= (objectPosition.getX() - searchZone.getXMin())
                || currentPosition.getX() >= (objectPosition.getX() - searchZone.getXMin()) + object.getWidth() + exclusiveZone) {
            return false;
        }
        return true;
    }

    private AbstractSeeker<S, T> learningSearch() {

        return search(true);
    }

    private void optimiseSearchArea() {

        SearchHistory locatedObject = searchHistoryDatabase.selectSearchHistory(searchHistory.getHash(), false, true, false, false);

        //        Zone de recherche
        int x1 = Integer.MAX_VALUE, x2 = 0, y1 = Integer.MAX_VALUE, y2 = 0;
        for (SearchHistory.Position position : locatedObject.getPositions()) {

            if (position.getPosition_x() < x1) {
                x1 = position.getPosition_x();
            }
            if (position.getPosition_x() > x2) {
                x2 = position.getPosition_x();
            }
            if (position.getPosition_y() < y1) {
                y1 = position.getPosition_y();
            }
            if (position.getPosition_y() > y2) {
                y2 = position.getPosition_y();
            }
        }

        //        Expansion de la zone de recherche proportionnellement à sa taille (Minimum 0, Maximum screen width)
        int amplitudeX = x2 - x1;
        int amplitudeY = y2 - y1;
        int percent = 2;
        x1 = x1 - amplitudeX / percent;
        if (x1 < NOX_MIN_X) {
            x1 = NOX_MIN_X;
        }
        x2 = x2 + amplitudeX / percent;
        if (x2 > NOX_MAX_X) {
            x2 = NOX_MAX_X;
        }
        y1 = y1 - amplitudeY / percent;
        if (y1 < NOX_MIN_Y) {
            y1 = NOX_MIN_Y;
        }
        y2 = y2 + amplitudeY / percent;
        if (y2 > NOX_MAX_Y) {
            y2 = NOX_MAX_Y;
        }

        //        Dimensions maximales des objets
        final int[] maximums = {0, 0};
        objects.forEach(object -> {
            if (object.getWidth() > maximums[0]) {
                maximums[0] = object.getWidth() + 1;
            }
            if (object.getHeight() > maximums[1]) {
                maximums[1] = object.getHeight() + 1;
            }
        });

        //        Expansion de ('growth') de la zone de recherche
        int growth = 10;
        SearchHistory.Area area = new SearchHistory.Area(x1 - growth, x2 + maximums[0] + growth, y1 - growth, y2 + maximums[1] + growth);

        searchHistoryDatabase.updateOptimisedSearchArea(area, searchHistory.getHash());
        searchHistory.setOptimisedSearchArea(area);

        searchHistoryDatabase.resetIterations(locatedObject.getHash());
    }

    private List<Parameter> removeInoperativeParameters(List<Parameter> parameters, int numberOfMatches) {

        Iterator<Parameter> iterator = parameters.iterator();

        while (iterator.hasNext()) {

            Parameter param = iterator.next();
            setPrecision(param.getPrecision());
            setAllowedErrorRate(param.getErrorRate());
            learningSearch();

            logger.sysOutTrace("param.getErrorRate() = " + param.getErrorRate() + ", param.getPrecision() = " + param.getPrecision());

            if (objects.get(0).getPositions().size() != numberOfMatches) {
                iterator.remove();
            } else {
                if (debug) {
                    logger.sysOutTrace("   > OPTIMIZING <   ");
                }
                for (Position position : objects.get(0).getPositions()) {
                    addPositionAndOptimize(position, false);
                }
            }
        }

        if (!parameters.isEmpty()) {
            searchHistoryDatabase.updateSearchParameters(searchHistory.getHash(), parameters);
        }

        return parameters;
    }

    private AbstractSeeker<S, T> search(boolean learningSearch) {

        long startTime = System.currentTimeMillis();

        Peripheral.delay(searchDelay);

        boolean hidedObjects = false;

        if (showObjects != null && showObjects.isVisible()) {
            //                showObjects.setVisible(false);
            hidedObjects = true;
        }

        Frame capturedScreen = Capture.zone(searchZone.getRectangle());

        if (debug) {
            InOut.writeImage(capturedScreen.image(), "image");
        }

        if (hidedObjects) {
            showObjects.setVisible(true);
        }

        if (isTracking) {
            updatePositions(capturedScreen);
        }

        if ((learning || precision == null || allowedErrorRate == null) && !learningSearch) {

            if (debug && learning) {
                logger.sysOutTrace(">>>> LEARNING <<<<");
            }

            java.util.List<Parameter> parameters = Parameter.fromDTOs(searchHistoryDatabase.selectSearchParameters(searchHistory.getHash()));

            Parameter parameter = getOptimalSearchParameter(parameters);

            precision = parameter.getPrecision();
            allowedErrorRate = parameter.getErrorRate();
        }

        for (T object : objects) {

            Position currentPosition = new Position(0, 0);

            for (; currentPosition.getY() < searchZone.getHeight() - (object.getHeight() + exclusiveZone); currentPosition.incY()) {
                currentPosition.setX(0);
                for (; currentPosition.getX() < searchZone.getWidth() - (object.getWidth() + exclusiveZone); currentPosition.incX()) {

                    while (overlapingExists(currentPosition)) {
                        //                        logger.sysOutTrace("overlapping: x =" + currentPosition.getX() +", y =" + currentPosition.getY());
                        currentPosition.setX(currentPosition.getX() + exclusiveZone * 2 + object.getWidth());
                    }

                    boolean found = searchObject(capturedScreen, currentPosition, object);

                    if (found && optimizing && !learningSearch) {

                        if (debug) {
                            System.out.print(" >>>> OPTIMIZING");
                        }

                        addPositionAndOptimize(currentPosition, true);
                    }
                }
            }

            object.getPositions().sort((o1, o2) -> {
                if (o1.getY() == o2.getY()) {
                    return o1.getX() - o2.getX();
                } else {
                    return o1.getY() - o2.getY();
                }
            });
        }

        if (debug || showTargets) {
            showObjects.setLocatedObjects(objects);
        }

        long elapsed = System.currentTimeMillis() - startTime;
        if (elapsed > 1000) {
            logger.sysOutWarning(getObjectsHash() + " search took " + elapsed + "ms");
        }

        if (properties.getProperty("ksuto.prh.seeker.saveCaptureOnNotFound", "false").equals("true") || saveCaptureOnNotFound) {
            InOut.writeImage(capturedScreen.image(), ".debug/NotFound_" + getObjectsHash() + "_" + System.currentTimeMillis() + ".png");
        }

        return this;
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

        int numberOfResults = 0;

        for (T o : objects) {
            numberOfResults += o.getPositions().size();
        }

        return numberOfResults;
    }

    //    TODO : remove !
    private int getNumbreOfResults() {

        return objects.stream()
                .map(LocatedObject::getPositions)
                .filter(Objects::nonNull)
                .mapToInt(java.util.List::size)
                .sum();
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
     * @param precision de préférence < 100
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

    public AbstractSeeker<S, T> setSearchZone(Screen.Zone zone) {

        if (searchZone != null == optimizing) {
            return this;
        }

        this.searchZone = zone;

        return this;
    }

    public AbstractSeeker<S, T> setShowTargets(boolean showTargets) {

        showObjects = initShowObjects(false);

        this.showTargets = showTargets;

        return this;
    }

    /**
     * @param tracking Permet d'afficher ou non une JFrame encadrant les résultats (défaut false)
     * @return Seeker
     */
    public AbstractSeeker<S, T> setTracking(boolean tracking) {

        isTracking = tracking;

        return this;
    }
}
