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

public abstract class AbstractSeeker<S extends AbstractSeeker, T extends LocatedObject> {
    
    public SearchHistoryDatabase searchHistoryDatabase = new SearchHistoryDatabase();
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
    private int               iterationsToKeep           = 100;
    
    public AbstractSeeker() throws AWTException {
        
        //            if (Constants.DEBUG) { showObjects = new ShowObjects<>(searchZone); }
    }
    
    public S debug() {
        
        this.debug = true;
        showObjects = new ShowObjects<>(searchZone, objects.stream().map(t -> t.getHash()).collect(Collectors.joining(", ")));
        return (S) this;
    }
    
    public S reintialize() {
        
        objects.clear();
        
        return (S) this;
    }
    
    public S clearResults() {
        
        objects.forEach(o -> {
            o.setPositions(new ArrayList<>());
            o.setPresent(false);
        });
        
        return (S) this;
    }
    
    public boolean hasAnyResults() {
        
        return objects.stream().anyMatch(LocatedObject::isPresent);
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
    
    public S click() {
        
        do {
            if (hasAnyResults()) {
                objects.forEach(object -> object.getPositions().forEach(this::click));
                if (clickUntilDisappear) { robot.delay(1000); }
            }
        }
        while (clickUntilDisappear && clearResults().search().hasAnyResults());
        
        return (S) this;
    }
    
    public S clickNth(int index) {
        
        if (hasAnyResults()) { click(getFirstResult().getPositions().get(index - 1)); }
        
        return (S) this;
    }
    
    public S clickFirst() {
        
        return clickNth(1);
    }
    
    public S click(Position position) {
        
        robot.delay(clickDelay);
        robot.mouseMove(position.getX(), position.getY());
        robot.delay(Constants.i_DELAY);
        robot.mousePress(Mouse.LEFT);
        robot.delay(Constants.i_DELAY);
        robot.mouseRelease(Mouse.LEFT);
        
        return (S) this;
    }
    
    public boolean waitAndClick(int milliseconds, boolean clickFirstResultOnly) {
        
        if (clickUntilDisappear) { clearResults(); }
        
        await(milliseconds);
        
        boolean found;
        
        do {
            found = expectedResults == null ? getNumbreOfResults() >= 1 : getNumbreOfResults() == expectedResults;
            
            if (found) {
                if (clickFirstResultOnly) {
                    click(getFirstResult().getPositions().get(0));
                }
                else {
                    objects.forEach(object -> object.getPositions().forEach(this::click));
                }
            }
        }
        while (clickUntilDisappear && clearResults().search().hasAnyResults());
        
        clean();
        
        return found;
    }
    
    public S await() {
        
        return await(15000);
    }
    
    public S await(int milliseconds) {
        
        long until = System.currentTimeMillis() + milliseconds;
        while (System.currentTimeMillis() < until) {
            System.out.print("\rTime's up : " + Math.floor((until - System.currentTimeMillis()) / 1000) + "...     ");
            search();
            if (hasAnyResults() && (expectedResults == null || getNumbreOfResults() == expectedResults)) {
                System.out.println(" ");
                return (S) this;
            }
        }
        System.out.println(" ");
        
        clean();
        
        return (S) this;
    }
    
    public S clean() {
        
        if (showObjects != null) { showObjects.clean(); }
        
        return (S) this;
    }
    
    public S search() {
        
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
        
        if (learning || precision == null || allowedErrorRate == null) {
            
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
                        currentPosition.setX(currentPosition.getX() + exclusiveZone * 2 + object.getWidth());
                    }
                    
                    boolean found = searchObject(capturedScreen, currentPosition, object);
                    
                    if (found && optimizing) {
                        
                        SearchHistory.Position position = new SearchHistory.Position(currentPosition.getX() + searchZone.getXMin(), currentPosition.getY() + searchZone.getYMin());
                        searchHistoryDatabase.addPosition(position, searchHistory.getHash());
                        searchHistoryDatabase.removePositionsOverLimit(searchHistory.getHash(), iterationsToKeep);
                        
                        searchHistory.setIterations(searchHistoryDatabase.increaseIterations(searchHistory.getHash()));
                        
                        if (searchHistory.getIterations() >= iterationsBeforeOptimizing) {
                            optimiseSearchArea();
                        }
                    }
                }
            }
            
            object.getPositions().sort((o1, o2) -> {
                if (o1.getY() == o2.getY()) { return o1.getX() - o2.getX(); }
                else { return o1.getY() - o2.getY(); }
            });
        }
        
        if (debug || showTargets) {showObjects.setLocatedObjects(objects);}
        
        return (S) this;
    }
    
    public Parameter getOptimalSearchParameter(List<Parameter> parameters) {
        
        if (learning) {
            if (parameters.isEmpty()) {
                for (int precision = 0; precision < 66; precision += 5) {
                    for (double errorRate = 0.0; errorRate <= 0.30; errorRate += 0.05) {
                        parameters.add(new Parameter(precision, errorRate));
                    }
                }
            }
            
            findWorkingParameters(parameters, expectedResults);
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
    
    public S learn(int expectedResults) {
        
        this.learning = true;
        this.expectedResults = expectedResults;
        setShowTargets(true);
        
        String hash = objects.stream().map(LocatedObject::getHash).collect(Collectors.joining("|"));
        
        this.searchHistory = searchHistoryDatabase.selectSearchHistory(hash, true, false, true, true);
        
        return (S) this;
    }
    
    public S optimize() {
        
        return optimize(10);
    }
    
    public S optimize(int iterationsBeforeOptimizing) {
        
        return optimize(iterationsBeforeOptimizing, 100);
    }
    
    public S optimize(int iterationsBeforeOptimizing, int iterationsToKeep) {
        
        this.optimizing = true;
        this.iterationsBeforeOptimizing = iterationsBeforeOptimizing;
        this.iterationsToKeep = iterationsToKeep;
        
        String hash = objects.stream().map(LocatedObject::getHash).collect(Collectors.joining("|"));
        
        this.searchHistory = searchHistoryDatabase.selectSearchHistory(hash, true, false, true, true);
        
        if (searchHistory.getOptimisedSearchArea() != null) {
            
            SearchHistory.Area area = searchHistory.getOptimisedSearchArea();
            searchZone = new Screen.Zone(area.getX_1(), area.getX_2(), area.getY_1(), area.getY_2());
        }
        
        return (S) this;
    }
    
    public S showObjects() {
        
        if (showObjects != null) { showObjects.setVisible(true); }
        return (S) this;
    }
    
    public S hideObjects() {
        
        if (showObjects != null) { showObjects.setVisible(false); }
        return (S) this;
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
    
    private List<Parameter> findWorkingParameters(List<Parameter> parameters, int numberOfMatches) {
        
        Iterator<Parameter> iterator = parameters.iterator();
        
        while (iterator.hasNext()) {
            
            Parameter param = iterator.next();
            setPrecision(param.getPrecision());
            setAllowedErrorRate(param.getErrorRate());
            search();
            if (objects.get(0).getPositions().size() != numberOfMatches) { iterator.remove(); }
        }
        
        if (!parameters.isEmpty()) {
            searchHistoryDatabase.updateSearchParameters(searchHistory.getHash(), parameters);
        }
        
        return parameters;
    }
    
    private void optimiseSearchArea() {
        
        SearchHistory locatedObject = searchHistoryDatabase.selectSearchHistory(searchHistory.getHash(), false, true, false, false);
        
        int x1 = Integer.MAX_VALUE, x2 = 0, y1 = Integer.MAX_VALUE, y2 = 0;
        for (SearchHistory.Position position : locatedObject.getPositions()) {
            
            if (position.getPosition_x() < x1) { x1 = position.getPosition_x(); }
            if (position.getPosition_x() > x2) { x2 = position.getPosition_x(); }
            if (position.getPosition_y() < y1) { y1 = position.getPosition_y(); }
            if (position.getPosition_y() > y2) { y2 = position.getPosition_y(); }
        }
        
        final int[] maximums = {0, 0};
        
        objects.forEach(object -> {
            if (object.getWidth() > maximums[0]) { maximums[0] = object.getWidth() + 1; }
            if (object.getHeight() > maximums[1]) { maximums[1] = object.getHeight() + 1; }
        });
        
        SearchHistory.Area area = new SearchHistory.Area(x1 > 6 ? x1 - 5 : x1, x2 + maximums[0] + 10, y1 > 6 ? y1 - 5 : y1, y2 + maximums[1] + 10);
        
        searchHistoryDatabase.updateOptimisedSearchArea(area, searchHistory.getHash());
        searchHistory.setOptimisedSearchArea(area);
        
        searchHistoryDatabase.resetIterations(locatedObject.getHash());
    }
    
    private boolean isOverlaping(Position currentPosition, Position objectPosition, LocatedObject object) {
        
        if (currentPosition.getY() + object.getHeight() + exclusiveZone < (objectPosition.getY() - searchZone.getYMin())
            || currentPosition.getY() > (objectPosition.getY() - searchZone.getYMin()) + object.getHeight() + exclusiveZone) {
            return false;
        }
        if (currentPosition.getX() + object.getWidth() + exclusiveZone < (objectPosition.getX() - searchZone.getXMin())
            || currentPosition.getX() > (objectPosition.getX() - searchZone.getXMin()) + object.getWidth() + exclusiveZone) {
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
    
    /**
     * @param allowedErrorRate entre 0.0 et 1.0
     *
     * @return Seeker
     */
    public S setAllowedErrorRate(double allowedErrorRate) {
        
        if (allowedErrorRate < 0) { allowedErrorRate = 0.0; }
        if (allowedErrorRate > 1) { allowedErrorRate = 1.0; }
        this.allowedErrorRate = allowedErrorRate;
        
        return (S) this;
    }
    
    public S setClickDelay(int clickDelay) {
        
        this.clickDelay = clickDelay;
        return (S) this;
    }
    
    public S setClickUntilDisappear(boolean clickUntil) {
        
        this.clickUntilDisappear = clickUntil;
        
        return (S) this;
    }
    
    public S setExclusiveZone(int exclusiveZone) {
        
        this.exclusiveZone = exclusiveZone;
        
        return (S) this;
    }
    
    public S setExpectedResults(int numberOf) {
        
        this.expectedResults = numberOf;
        return (S) this;
    }
    
    public S setMaximumMovement(int maximumMovement) {
        
        this.maximumMovement = maximumMovement;
        return (S) this;
    }
    
    /**
     * @param precision de préférence < 100
     *
     * @return Seeker
     */
    public S setPrecision(Integer precision) {
        
        this.precision = precision;
        
        return (S) this;
    }
    
    public S setSearchDelay(int searchDelay) {
        
        this.searchDelay = searchDelay;
        return (S) this;
    }
    
    public S setSearchZone(Screen.Zone zone) {
        
        this.searchZone = zone;
        
        //            if (debug) { showObjects = new ShowObjects<>(searchZone); }
        
        return (S) this;
    }
    
    public S setShowTargets(boolean showTargets) {
        
        showObjects = new ShowObjects<>(searchZone, objects.stream().map(t -> t.getHash()).collect(Collectors.joining(", ")));
        
        this.showTargets = showTargets;
        
        return (S) this;
    }
    
    /**
     * @param tracking Permet d'afficher ou non une JFrame encadrant les résultats (défaut false)
     *
     * @return Seeker
     */
    public S setTracking(boolean tracking) {
        
        isTracking = tracking;
        
        return (S) this;
    }
}
