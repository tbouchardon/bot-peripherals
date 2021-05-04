package net.ddns.ksuto.prh.helpers;

import net.ddns.ksuto.prh.entities.LocatedObject;
import net.ddns.ksuto.prh.entities.Parameter;
import net.ddns.ksuto.prh.entities.Position;
import net.ddns.ksuto.prh.entities.SearchHistory;
import net.ddns.ksuto.prh.peripherals.Mouse;
import net.ddns.ksuto.prh.peripherals.Screen;
import net.ddns.ksuto.prh.properties.Constants;
import net.ddns.ksuto.prh.tools.SearchHistoryDatabase;
import net.ddns.ksuto.prh.tools.ShowObjects;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

@SuppressWarnings({"UnusedReturnValue"})
public abstract class AbstractSeeker<S extends AbstractSeeker<S, T>, T extends LocatedObject> {
    
    //    private static final Dimension dim_D         = new Dimension(Toolkit.getDefaultToolkit().getScreenSize());
    //    private static final int       SCREEN_WIDTH  = (int) dim_D.getWidth();
    //    private static final int       SCREEN_HEIGHT = (int) dim_D.getHeight();
    private static final int NOX_MIN_X = 247;
    private static final int NOX_MIN_Y = 162;
    private static final int NOX_MAX_X = 1572;
    private static final int NOX_MAX_Y = 917;
    
    public SearchHistoryDatabase searchHistoryDatabase = new SearchHistoryDatabase("prh");
    public SearchHistory         searchHistory;
    
    public  boolean           optimizing                 = false;
    public  boolean           learning                   = false;
    public  Integer           precision                  = null;
    public  Integer           expectedResults            = null;
    public  Double            allowedErrorRate           = null;
    public  int               exclusiveZone              = 0;
    public  Robot             robot                      = new Robot();
    public  int               clickDelay                 = Constants.i_DELAY;
    public  int               searchDelay                = 0;
    public  boolean           isTracking                 = false;
    public  java.util.List<T> objects                    = new ArrayList<>();
    public  int               maximumMovement            = 20;
    public  Screen.Zone       searchZone                 = Screen.Zone.ALL;
    private ShowObjects<T>    showObjects;
    private S                 seeker;
    private boolean           showTargets                = false;
    private boolean           debug                      = false;
    private boolean           clickUntilDisappear        = false;
    private int               iterationsBeforeOptimizing = 10;
    private int               iterationsToKeep           = 50;
    
    public AbstractSeeker() throws AWTException {
    
    }
    
    public AbstractSeeker<S, T> debug() {
        
        this.debug = true;
        showObjects = initShowObjects(false);
        return this;
    }
    
    public ShowObjects<T> initShowObjects(boolean clean) {
        
        if (clean && showObjects != null) {
            showObjects.clean();
            showObjects = null;
        }
        
        if (showObjects != null) { return showObjects; }
        
        showObjects = new ShowObjects<>(searchZone, objects.stream().map(LocatedObject::getHash).collect(Collectors.joining(", ")));
        
        return showObjects;
    }
    
    public AbstractSeeker<S, T> reintialize() {
        
        objects.clear();
        
        return (AbstractSeeker<S, T>) this;
    }
    
    public AbstractSeeker<S, T> clearResults() {
        
        objects.forEach(o -> {
            o.setPositions(new ArrayList<>());
            o.setPresent(false);
        });
        
        return this;
    }
    
    public AbstractSeeker<S, T> clearAreaOptimization() {
    
        searchHistoryDatabase.clearAreaOptimization(getObjectsHash());
        
        return this;
    }
    
    public AbstractSeeker<S, T> clearParamtersOptimization() {
    
        searchHistoryDatabase.clearSearchOptimization(getObjectsHash());
        
        return this;
    }
    
    public AbstractSeeker<S, T> clearObjectOptimizations() {
    
        searchHistoryDatabase.clearObjectOptimization(getObjectsHash());
    
        return this;
    }
    
    public boolean hasAnyResults() {
        
        return objects.stream().anyMatch(LocatedObject::isPresent);
    }
    
    public boolean hasAnyResults(String hash) {
        
        T object = getObjectByHash(hash);
        return object.isPresent();
    }
    
    public boolean waitAndClick() {
        
        return waitAndClick(15000);
    }
    
    public boolean waitAndClick(int milliseconds) {
        
        return waitAndClick(milliseconds, false);
    }
    
    public boolean waitAndClickFirstMatch() {
        
        return waitAndClickFirstMatch(15000);
    }
    
    public boolean waitAndClickFirstMatch(int milliseconds) {
        
        return waitAndClick(milliseconds, true);
    }
    
    public AbstractSeeker<S, T> click() {
        
        do {
            if (hasAnyResults()) {
                objects.forEach(object -> object.getPositions().forEach(position -> clickRandomlyInObject(position, object)));
                if (clickUntilDisappear) { robot.delay(1000); }
            }
        }
        while (clickUntilDisappear && clearResults().search().hasAnyResults());
    
        return this;
    }
    
    public AbstractSeeker<S, T> clickNth(int index) {
        
        T object = getFirstResult();
        
        if (hasAnyResults()) { clickRandomlyInObject(object.getPositions().get(index - 1), object); }
        
        return this;
    }
    
    public AbstractSeeker<S, T> clickNth(int index, String hash) {
        
        T object = getObjectByHash(hash);
        
        if (hasAnyResults()) { clickRandomlyInObject(object.getPositions().get(index - 1), object); }
        
        return this;
    }
    
    public AbstractSeeker<S, T> clickFirst() {
        
        return clickNth(1);
    }
    
    public AbstractSeeker<S, T> clickFirst(String hash) {
        
        return clickNth(1, hash);
    }
    
    public boolean waitAndClick(int milliseconds, boolean clickFirstResultOnly) {
        
        if (clickUntilDisappear) { clearResults(); }
        
        await(milliseconds);
        
        boolean found;
        
        do {
            found = expectedResults == null ? getNumbreOfResults() >= 1 : getNumbreOfResults() == expectedResults;
            
            if (found) {
                if (clickFirstResultOnly) {
    
                    T object = getFirstResult();
                    clickRandomlyInObject(object.getPositions().get(0), object);
                }
                else {
                    objects.forEach(object -> object.getPositions().forEach(position -> clickRandomlyInObject(position, object)));
                }
            }
        }
        while (clickUntilDisappear && clearResults().search().hasAnyResults());
        
        clean();
        
        return found;
    }
    
    public AbstractSeeker<S, T> await() {
        
        return await(15000);
    }
    
    public AbstractSeeker<S, T> await(int milliseconds) {
        
        long until = System.currentTimeMillis() + milliseconds;
        System.out.println("[TRACE] PRH : Awaiting " + getObjectsHash());
        while (System.currentTimeMillis() < until) {
            System.out.print("\r[TRACE] PRH : Time's up : " + (int) Math.floor((until - System.currentTimeMillis()) / 1000f) + "s    ");
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
        
        if (showObjects != null) { showObjects.clean(); }
        
        return this;
    }
    
    public AbstractSeeker<S, T> search() {
        
        return search(false);
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
        }
        else {
            parameter = parameters.get(0);
        }
        return parameter;
    }
    
    public AbstractSeeker<S, T> learn(int expectedResults) {
        
        this.learning = true;
        this.expectedResults = expectedResults;
        setShowTargets(true);
    
        String hash = getObjectsHash();
        
        this.searchHistory = searchHistoryDatabase.selectSearchHistory(hash, true, false, true, true);
        
        return this;
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
            if (debug || showTargets) { initShowObjects(true); }
        }
        
        return this;
    }
    
    public AbstractSeeker<S, T> showObjects() {
        
        if (showObjects != null) { showObjects.setVisible(true); }
        return this;
    }
    
    public AbstractSeeker<S, T> hideObjects() {
        
        if (showObjects != null) { showObjects.setVisible(false); }
        return this;
    }
    
    public AbstractSeeker<S, T> clickUntilDisappear() {
        
        this.clickUntilDisappear = true;
        AbstractSeeker<S, T> seeker = click();
        this.clickUntilDisappear = false;
        
        return seeker;
    }
    
    public T getObjectByHash(String hash) {
        
        for (T object : getObjects()) {
            if (object.getHash().equals(hash)) { return object; }
        }
        
        return null;
    }
    
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
    
    boolean isMatch(int capturedRGB, int refRGB) {
        
        boolean match = capturedRGB == refRGB;
        
        if (precision != 0) {
            
            int imgB = (capturedRGB) & 0xFF;
            int imgG = (capturedRGB >> 8) & 0xFF;
            int imgR = (capturedRGB >> 16) & 0xFF;
            
            int refB = (refRGB) & 0xFF;
            int refG = (refRGB >> 8) & 0xFF;
            int refR = (refRGB >> 16) & 0xFF;
            
            match = imgR > refR - precision && imgR < refR + precision &&
                    imgG > refG - precision && imgG < refG + precision &&
                    imgB > refB - precision && imgB < refB + precision;
        }
        
        return match;
    }
    
    abstract boolean searchObject(BufferedImage capturedScreen, Position currentPosition, T object);
    
    abstract boolean isObjectFound(BufferedImage capturedScreen, Position currentPosition, T object);
    
    void updatePositions(BufferedImage capturedScreen) {
        
        for (T object : objects) {
            
            Iterator<Position> positionsIterator = object.getPositions().iterator();
            
            while (positionsIterator.hasNext()) {
                
                Position position = positionsIterator.next();
                
                Position currentPosition = new Position(position.getX() - exclusiveZone - maximumMovement, position.getY() - exclusiveZone - maximumMovement);
                
                boolean objectFound = false;
                
                for (; currentPosition.getY() < position.getY() + object.getHeight() + exclusiveZone + maximumMovement &&
                       currentPosition.getY() < capturedScreen.getHeight(); currentPosition.incY()) {
                    currentPosition.setX(position.getX() - exclusiveZone - maximumMovement);
                    for (; currentPosition.getX() < position.getX() + object.getWidth() + exclusiveZone + maximumMovement &&
                           currentPosition.getX() < capturedScreen.getWidth(); currentPosition.incX()) {
                        
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
    
    private AbstractSeeker<S, T> clickRandomlyInObject(Position position, T object) {
        
        return clickWithOffset(position, (int) Math.floor(Math.random() * object.getWidth()), (int) Math.floor(Math.random() * object.getHeight()));
    }
    
    private AbstractSeeker<S, T> clickWithOffset(Position position, int offsetX, int offsetY) {
        
        try {
            robot.delay(clickDelay);
            (new Mouse()).move(position.getX() + offsetX, position.getY() + offsetY);
            robot.mousePress(Mouse.LEFT);
            robot.delay(Constants.i_DELAY);
            robot.mouseRelease(Mouse.LEFT);
        }
        catch (AWTException e) {
            e.printStackTrace();
        }
        
        return this;
    }
    
    private AbstractSeeker<S, T> learningSearch() {
        
        return search(true);
    }
    
    private AbstractSeeker<S, T> search(boolean learningSearch) {
        
        robot.delay(searchDelay);
        
        boolean hidedObjects = false;
        
        if (showObjects != null && showObjects.isVisible()) {
            //                showObjects.setVisible(false);
            hidedObjects = true;
        }
        
        BufferedImage capturedScreen = robot.createScreenCapture(searchZone.getRectangle());
        
        if (debug) {
            try {
                BufferedWriter writer     = null;
                File           outputfile = new File("image.jpg");
                ImageIO.write(capturedScreen, "png", outputfile);
            }
            catch (IOException e) {
            }
        }
        
        if (hidedObjects) { showObjects.setVisible(true); }
        
        if (isTracking) { updatePositions(capturedScreen); }
        
        if ((learning || precision == null || allowedErrorRate == null) && !learningSearch) {
    
            if (debug && learning) { System.out.println("[TRACE] PRH : >>>> LEARNING <<<<"); }
            
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
                        //                        System.out.println("[TRACE] PRH : overlapping: x =" + currentPosition.getX() +", y =" + currentPosition.getY());
                        currentPosition.setX(currentPosition.getX() + exclusiveZone * 2 + object.getWidth());
                    }
                    
                    boolean found = searchObject(capturedScreen, currentPosition, object);
                    
                    if (found && optimizing && !learningSearch) {
                        
                        if (debug) { System.out.print(" >>>> OPTIMIZING"); }
                        
                        addPositionAndOptimize(currentPosition, true);
                    }
                }
            }
            
            object.getPositions().sort((o1, o2) -> {
                if (o1.getY() == o2.getY()) { return o1.getX() - o2.getX(); }
                else { return o1.getY() - o2.getY(); }
            });
        }
        
        if (debug || showTargets) {showObjects.setLocatedObjects(objects);}
        
        return this;
    }
    
    private void addPositionAndOptimize(Position currentPosition, boolean relative) {
        
        SearchHistory.Position position;
        if (relative) { position = new SearchHistory.Position(currentPosition.getX() + searchZone.getXMin(), currentPosition.getY() + searchZone.getYMin()); }
        else { position = new SearchHistory.Position(currentPosition.getX(), currentPosition.getY()); }
        searchHistoryDatabase.addPosition(position, searchHistory.getHash());
        searchHistoryDatabase.removePositionsOverLimit(searchHistory.getHash(), iterationsToKeep);
        
        searchHistory.setIterations(searchHistoryDatabase.increaseIterations(searchHistory.getHash()));
        
        if (searchHistory.getIterations() >= iterationsBeforeOptimizing) {
            optimiseSearchArea();
        }
    }
    
    private List<Parameter> removeInoperativeParameters(List<Parameter> parameters, int numberOfMatches) {
        
        Iterator<Parameter> iterator = parameters.iterator();
        
        while (iterator.hasNext()) {
            
            Parameter param = iterator.next();
            setPrecision(param.getPrecision());
            setAllowedErrorRate(param.getErrorRate());
            learningSearch();
    
            System.out.println("[TRACE] PRH : param.getErrorRate() = " + param.getErrorRate() + ", param.getPrecision() = " + param.getPrecision());
            
            if (objects.get(0).getPositions().size() != numberOfMatches) { iterator.remove(); }
            else {
                if (debug) { System.out.println("[TRACE] PRH :    > OPTIMIZING <   "); }
                for (Position position : objects.get(0).getPositions()) { addPositionAndOptimize(position, false); }
            }
        }
        
        if (!parameters.isEmpty()) {
            searchHistoryDatabase.updateSearchParameters(searchHistory.getHash(), parameters);
        }
        
        return parameters;
    }
    
    private void optimiseSearchArea() {
        
        SearchHistory locatedObject = searchHistoryDatabase.selectSearchHistory(searchHistory.getHash(), false, true, false, false);
    
        //        Zone de recherche
        int x1 = Integer.MAX_VALUE, x2 = 0, y1 = Integer.MAX_VALUE, y2 = 0;
        for (SearchHistory.Position position : locatedObject.getPositions()) {
    
            if (position.getPosition_x() < x1) { x1 = position.getPosition_x(); }
            if (position.getPosition_x() > x2) { x2 = position.getPosition_x(); }
            if (position.getPosition_y() < y1) { y1 = position.getPosition_y(); }
            if (position.getPosition_y() > y2) { y2 = position.getPosition_y(); }
        }
    
        //        Expansion de la zone de recherche proportionnellement à sa taille (Minimum 0, Maximum screen width)
        int amplitudeX = x2 - x1;
        int amplitudeY = y2 - y1;
        int percent    = 2;
        x1 = x1 - amplitudeX / percent;
        if (x1 < NOX_MIN_X) { x1 = NOX_MIN_X; }
        x2 = x2 + amplitudeX / percent;
        if (x2 > NOX_MAX_X) { x2 = NOX_MAX_X; }
        y1 = y1 - amplitudeY / percent;
        if (y1 < NOX_MIN_Y) { y1 = NOX_MIN_Y; }
        y2 = y2 + amplitudeY / percent;
        if (y2 > NOX_MAX_Y) { y2 = NOX_MAX_Y; }
    
        //        Dimensions maximales des objets
        final int[] maximums = {0, 0};
        objects.forEach(object -> {
            if (object.getWidth() > maximums[0]) { maximums[0] = object.getWidth() + 1; }
            if (object.getHeight() > maximums[1]) { maximums[1] = object.getHeight() + 1; }
        });
    
        //        Expansion de ('growth') de la zone de recherche
        int                growth = 10;
        SearchHistory.Area area   = new SearchHistory.Area(x1 - growth, x2 + maximums[0] + growth, y1 - growth, y2 + maximums[1] + growth);
    
        searchHistoryDatabase.updateOptimisedSearchArea(area, searchHistory.getHash());
        searchHistory.setOptimisedSearchArea(area);
    
        searchHistoryDatabase.resetIterations(locatedObject.getHash());
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
        
        return objects.stream().map(LocatedObject::getHash).collect(Collectors.joining("|"));
    }
    
    /**
     * @param allowedErrorRate entre 0.0 et 1.0
     *
     * @return Seeker
     */
    public AbstractSeeker<S, T> setAllowedErrorRate(double allowedErrorRate) {
        
        if (allowedErrorRate < 0) { allowedErrorRate = 0.0; }
        if (allowedErrorRate > 1) { allowedErrorRate = 1.0; }
        this.allowedErrorRate = allowedErrorRate;
        
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
    
    /**
     * @param precision de préférence < 100
     *
     * @return Seeker
     */
    public AbstractSeeker<S, T> setPrecision(Integer precision) {
        
        this.precision = precision;
        
        return this;
    }
    
    public AbstractSeeker<S, T> setSearchDelay(int searchDelay) {
        
        this.searchDelay = searchDelay;
        return this;
    }
    
    public AbstractSeeker<S, T> setSearchZone(Screen.Zone zone) {
        
        if (searchZone != null == optimizing) { return this; }
        
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
     *
     * @return Seeker
     */
    public AbstractSeeker<S, T> setTracking(boolean tracking) {
        
        isTracking = tracking;
        
        return this;
    }
}
