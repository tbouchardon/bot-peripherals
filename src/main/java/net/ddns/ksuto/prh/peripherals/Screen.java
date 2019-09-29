package net.ddns.ksuto.prh.peripherals;

import lombok.Data;
import net.ddns.ksuto.prh.entities.ColorBlock;
import net.ddns.ksuto.prh.entities.LocatedObject;
import net.ddns.ksuto.prh.entities.Parameter;
import net.ddns.ksuto.prh.entities.Picture;
import net.ddns.ksuto.prh.entities.Position;
import net.ddns.ksuto.prh.entities.SearchHistory;
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

/**
 * Created by TBO on 15/07/2016.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class Screen extends Peripheral {
    
    public static final Dimension dim_D         = new Dimension(Toolkit.getDefaultToolkit().getScreenSize());
    public static final int       SCREEN_WIDTH  = (int) dim_D.getWidth();
    public static final int       SCREEN_HEIGHT = (int) dim_D.getHeight();
    public final        int       iX_START      = Screen.SCREEN_WIDTH / 2, iY_START = Screen.SCREEN_HEIGHT / 2 - 20;
    private ColorChecker colorChecker;
    
    Screen() throws AWTException {
    
    }
    
    public static void main(String[] args) throws AWTException {
        
        ShowObjects<Picture> showObjects   = new ShowObjects<>();
        PictureSearch        pictureSearch = new PictureSearch();
        int                  precision     = 0;
        pictureSearch.addPictureWithUrl("/test.png")
                .setPrecision(precision)
                .setExclusiveZone(5);
        
        while (pictureSearch.getObjects().get(0).getPositions().size() < 10) {
            pictureSearch.search();
            pictureSearch.robot.delay(1000);
            showObjects.setLocatedObjects(pictureSearch.getObjects());
            precision++;
        }
    }
    
    public void pictureHelper(String url, int numberOfMatches) throws AWTException {
        
        pictureHelper(url, numberOfMatches, new Zone());
    }
    
    public void pictureHelper(String url, int numberOfMatches, Zone searchZone) throws AWTException {
        
        for (int countDown = 5; countDown >= 0; countDown--) {
            System.out.println("countDown = " + countDown);
            robot.delay(1000);
        }
        
        PictureSearch pictureSearch = new PictureSearch()
                                              .addPictureWithUrl(url)
                                              .setSearchZone(searchZone)
                                              .setShowTargets(true)
                                              .setTracking(true)
                                              .debug();
        
        findHelperMatches(numberOfMatches, pictureSearch);
    }
    
    public void waitUntilStopsMoving() {
        
        while (isMoving()) {robot.delay(200);}
    }
    
    private void findHelperMatches(int numberOfMatches, Seeker seeker) {
        
        class Parameter {
            
            int    precision = 0;
            double errorRate = 0.0;
            
            public Parameter(int precision, double errorRate) {
                
                this.precision = precision;
                this.errorRate = errorRate;
            }
        }
        
        List<Parameter> parameters = new ArrayList<>();
        for (int p = 0; p < 66; p += 5) {
            for (double e = 0.0; e <= 0.30; e += 0.05) {
                parameters.add(new Parameter(p, e));
            }
        }
        
        List<LocatedObject> objects = seeker.getObjects();
        
        while (!parameters.isEmpty()) {
            
            System.out.println("------------------------------------------------------------------------------------------------------------------------");
            
            Iterator<Parameter> iterator = parameters.iterator();
            
            while (iterator.hasNext()) {
                
                Parameter param = iterator.next();
                seeker.setPrecision(param.precision);
                seeker.setAllowedErrorRate(param.errorRate);
                seeker.search();
                System.out.println("precision = " + param.precision + " && errorRate = " + param.errorRate + " => " + objects.get(0).getPositions().size() + " matche(s)");
                
                if (objects.get(0).getPositions().size() != numberOfMatches) { iterator.remove(); }
            }
        }
        
        if (seeker.hasAnyResults()) {
            System.out.println("Positions : ");
            objects.get(0).getPositions().forEach(position -> {
                System.out.println(position.getX() + ":" + position.getY());
            });
        }
    }
    
    public boolean isMoving() {
        
        BufferedImage screenCapture = robot.createScreenCapture(new Rectangle(0, 0, Screen.SCREEN_WIDTH, Screen.SCREEN_HEIGHT));
        
        //        try {
        //            BufferedWriter writer     = null;
        //            File           outputfile = new File("moving1.jpg");
        //            ImageIO.write(screenCapture, "png", outputfile);
        //        }
        //        catch (IOException e) {
        //        }
        
        int initialPixelColor1 = screenCapture.getRGB(screenCapture.getWidth() / 2 - 100, screenCapture.getHeight() / 2 - 100);
        int initialPixelColor2 = screenCapture.getRGB(screenCapture.getWidth() / 2, screenCapture.getHeight() / 2);
        int initialPixelColor3 = screenCapture.getRGB(screenCapture.getWidth() / 2 + 100, screenCapture.getHeight() / 2 + 100);
        
        robot.delay(200);
        
        screenCapture = robot.createScreenCapture(new Rectangle(0, 0, Screen.SCREEN_WIDTH, Screen.SCREEN_HEIGHT));
        
        //        try {
        //            BufferedWriter writer     = null;
        //            File           outputfile = new File("moving2.jpg");
        //            ImageIO.write(screenCapture, "png", outputfile);
        //        }
        //        catch (IOException e) {
        //        }
        
        int pixelColor1 = screenCapture.getRGB(screenCapture.getWidth() / 2 - 100, screenCapture.getHeight() / 2 - 100);
        int pixelColor2 = screenCapture.getRGB(screenCapture.getWidth() / 2, screenCapture.getHeight() / 2);
        int pixelColor3 = screenCapture.getRGB(screenCapture.getWidth() / 2 + 100, screenCapture.getHeight() / 2 + 100);
        
        //        System.out.println("px1 " + pixelColor1 + " : " + initialPixelColor1);
        //        System.out.println("px2 " + pixelColor2 + " : " + initialPixelColor2);
        //        System.out.println(" ");
        //        System.out.println("px3 " + pixelColor3 + " : " + initialPixelColor3);
        
        return pixelColor1 != initialPixelColor1 &&
               pixelColor2 != initialPixelColor2; //|| pixelColor3 == initialPixelColor3;
    }
    
    public static class ColorSearch extends Seeker<ColorSearch, ColorBlock> {
        
        public ColorSearch() throws AWTException {
            
            super();
        }
        
        @Override
        boolean searchObject(BufferedImage capturedScreen, Position currentPosition, ColorBlock colorBlock) {
            
            if (isBlockFound(capturedScreen, currentPosition, colorBlock)) {
                
                Position position = new Position(currentPosition.getX() + searchZone.xMin, currentPosition.getY() + searchZone.yMin);
                position.setObject(currentPosition.getObject());
                colorBlock.getPositions().add(position);
                int cote = (int) Math.sqrt(colorBlock.getSize());
                colorBlock.setHeight(colorBlock.getHeight() > cote ? colorBlock.getHeight() : cote);
                colorBlock.setWidth(colorBlock.getWidth() > cote ? colorBlock.getWidth() : cote);
                colorBlock.setPresent(true);
                return true;
            }
            return false;
        }
        
        @Override
        boolean isObjectFound(BufferedImage capturedScreen, Position currentPosition, ColorBlock object) {
            
            return isBlockFound(capturedScreen, currentPosition, object);
        }
        
        public ColorSearch addColorBlock(int red, int green, int blue) {
            
            objects.add(new ColorBlock(red, green, blue));
            
            return this;
        }
        
        public ColorSearch addColorBlock(int red, int green, int blue, int minBlockSize, int maxBlockSize) {
            
            objects.add(new ColorBlock(red, green, blue, minBlockSize, maxBlockSize));
            
            return this;
        }
        
        private boolean isBlockFound(BufferedImage capturedScreen, Position currentPosition, ColorBlock colorBlock) {
            
            if (currentPosition.getY() >= capturedScreen.getHeight()) { return false; }
            if (currentPosition.getX() >= capturedScreen.getWidth()) { return false; }
            
            int b;
            int g;
            int r;
            int xDelta    = 0;
            int yDelta    = 0;
            int blockSize = 0;
            
            while (true) {
                
                int capturedRGB = capturedScreen.getRGB(currentPosition.getX() + xDelta, currentPosition.getY() + yDelta);
                b = (capturedRGB) & 0xFF;
                g = (capturedRGB >> 8) & 0xFF;
                r = (capturedRGB >> 16) & 0xFF;
                
                boolean match;
                if (precision != null) {
                    
                    match = r > colorBlock.getRed() - precision && r < colorBlock.getRed() + precision &&
                            g > colorBlock.getGreen() - precision && g < colorBlock.getGreen() + precision &&
                            b > colorBlock.getBlue() - precision && b < colorBlock.getBlue() + precision;
                }
                else {
                    
                    match = r == colorBlock.getRed() && g == colorBlock.getGreen() && b == colorBlock.getBlue();
                }
                
                if (match) {
                    
                    blockSize += 1;
                    xDelta++;
                    if (currentPosition.getX() + xDelta >= capturedScreen.getWidth()) {
                        xDelta = 0;
                        yDelta++;
                    }
                }
                else {
                    
                    if (xDelta == 0) {
                        break;
                    }
                    
                    xDelta = 0;
                    yDelta++;
                    if (currentPosition.getY() >= capturedScreen.getHeight()) { break; }
                }
            }
            if (blockSize > colorBlock.getSize()) { colorBlock.setSize(blockSize); }
            
            currentPosition.setObject(blockSize);
            
            return blockSize >= colorBlock.getMinBlockSize() && blockSize <= colorBlock.getMaxBlockSize();
        }
    }
    
    public static class PictureSearch extends Seeker<PictureSearch, Picture> {
        
        public PictureSearch() throws AWTException {
            
            super();
        }
        
        @Override
        boolean searchObject(BufferedImage capturedScreen, Position currentPosition, Picture picture) {
            
            if (isPictureFound(capturedScreen, picture, currentPosition)) {
                
                picture.getPositions().add(new Position(currentPosition.getX() + searchZone.xMin, currentPosition.getY() + searchZone.yMin));
                picture.setPresent(true);
                return true;
            }
            return false;
        }
        
        @Override
        boolean isObjectFound(BufferedImage capturedScreen, Position currentPosition, Picture object) {
            
            return isPictureFound(capturedScreen, object, currentPosition);
        }
        
        public PictureSearch addPictureWithUrl(String url, Object o) {
            
            Picture picture = new Picture(url);
            picture.setObject(o);
            
            objects.add(picture);
            
            return this;
        }
        
        public PictureSearch addPictureWithUrl(String url) {
            
            this.objects.add(new Picture(url));
            
            return this;
        }
        
        public PictureSearch addPicturesWithUrls(String[] urls) {
            
            for (String url : urls) { addPictureWithUrl(url); }
            
            return this;
        }
        
        private boolean isPictureFound(BufferedImage capturedScreen, Picture picture, Position currentPosition) {
            
            if (picture.getReferenceImage().getHeight() + currentPosition.getY() >= capturedScreen.getHeight()) { return false; }
            if (picture.getReferenceImage().getWidth() + currentPosition.getX() >= capturedScreen.getWidth()) { return false; }
            
            int    tempCapturedRGB;
            int    refRGB      = picture.getReferenceImage().getRGB(0, 0);
            int    capturedRGB = capturedScreen.getRGB(currentPosition.getX(), currentPosition.getY());
            double area        = picture.getReferenceImage().getHeight() * picture.getReferenceImage().getWidth();
            double errorNumber = 0;
            
            boolean found = false;
            
            if (isMatch(capturedRGB, refRGB)) {
                
                found = true;

yxLoop:
                for (int yRef = 0; yRef < picture.getReferenceImage().getHeight(); yRef++) {
                    
                    for (int xRef = 0; xRef < picture.getReferenceImage().getWidth(); xRef++) {
                        
                        tempCapturedRGB = capturedScreen.getRGB(xRef + currentPosition.getX(), yRef + currentPosition.getY());
                        refRGB = picture.getReferenceImage().getRGB(xRef, yRef);
                        
                        boolean match = isMatch(tempCapturedRGB, refRGB);
                        if (!match) { errorNumber++; }
                        
                        double errorRate = errorNumber / area;
                        
                        if (errorRate > allowedErrorRate) {
                            found = false;
                            break yxLoop;
                        }
                    }
                }
            }
            
            return found;
        }
    }
    
    @Data
    public static class Zone {
        
        //        public static final Dimension dim_D         = new Dimension(Toolkit.getDefaultToolkit().getScreenSize());
        //        public static final int       Screen.SCREEN_WIDTH  = (int) dim_D.getWidth();
        //        public static final int       Screen.SCREEN_HEIGHT = (int) dim_D.getHeight();
        
        public static final Zone TOP          = new Zone(0, Screen.SCREEN_WIDTH, 0, Screen.SCREEN_HEIGHT / 2);
        public static final Zone TOP_LEFT     = new Zone(0, Screen.SCREEN_WIDTH / 2, 0, Screen.SCREEN_HEIGHT / 2);
        public static final Zone TOP_RIGHT    = new Zone(Screen.SCREEN_WIDTH / 2, Screen.SCREEN_WIDTH, 0, Screen.SCREEN_HEIGHT / 2);
        public static final Zone BOTTOM       = new Zone(0, Screen.SCREEN_WIDTH, Screen.SCREEN_HEIGHT / 2, Screen.SCREEN_HEIGHT);
        public static final Zone BOTTOM_LEFT  = new Zone(0, Screen.SCREEN_WIDTH / 2, Screen.SCREEN_HEIGHT / 2, Screen.SCREEN_HEIGHT);
        public static final Zone BOTTOM_RIGHT = new Zone(Screen.SCREEN_WIDTH / 2, Screen.SCREEN_WIDTH, Screen.SCREEN_HEIGHT / 2, Screen.SCREEN_HEIGHT);
        public static final Zone LEFT         = new Zone(0, Screen.SCREEN_WIDTH / 2, 0, Screen.SCREEN_HEIGHT);
        public static final Zone RIGHT        = new Zone(Screen.SCREEN_WIDTH / 2, Screen.SCREEN_WIDTH, 0, Screen.SCREEN_HEIGHT);
        public static final Zone MIDDLE       = new Zone(Screen.SCREEN_WIDTH / 4, Screen.SCREEN_WIDTH - Screen.SCREEN_WIDTH / 4, Screen.SCREEN_HEIGHT / 4,
                                                         Screen.SCREEN_HEIGHT - Screen.SCREEN_HEIGHT / 4);
        public static final Zone ALL          = new Zone(0, Screen.SCREEN_WIDTH, 0, Screen.SCREEN_HEIGHT);
        
        private int xMin   = 0;
        private int xMax   = Screen.SCREEN_WIDTH;
        private int yMin   = 0;
        private int yMax   = Screen.SCREEN_HEIGHT;
        private int width  = Screen.SCREEN_WIDTH;
        private int height = Screen.SCREEN_HEIGHT;
        
        public Zone(int xMin, int xMax, int yMin, int yMax) {
            
            this.xMin = xMin;
            this.xMax = xMax;
            this.yMin = yMin;
            this.yMax = yMax;
            this.width = xMax - xMin;
            this.height = yMax - yMin;
        }
        
        public Zone() {
        
        }
        
        public Rectangle getRectangle() {
            
            return new Rectangle(xMin, yMin, width, height);
        }
    }
    
    /**
     * @param <S>
     * @param <T>
     */
    @SuppressWarnings("unchecked")
    private static abstract class Seeker<S extends Seeker, T extends LocatedObject> {
    
        public SearchHistoryDatabase searchHistoryDatabase = new SearchHistoryDatabase();
        public SearchHistory         searchHistory;
    
        public  boolean        optimizing                 = false;
        public  boolean        learning                   = false;
        public  Integer        precision                  = 0;
        public  Integer        expectedResults            = null;
        public  double         allowedErrorRate           = 0;
        public  int            exclusiveZone              = 0;
        public  Robot          robot                      = new Robot();
        public  int            clickDelay                 = Constants.i_DELAY;
        public  int            searchDelay                = 0;
        public  boolean        isTracking                 = false;
        public  List<T>        objects                    = new ArrayList<>();
        public  int            maximumMovement            = 20;
        public  Zone           searchZone                 = Zone.ALL;
        private ShowObjects<T> showObjects;
        private S              seeker;
        private boolean        showTargets                = false;
        private boolean        debug                      = false;
        private boolean        clickUntilDisappear        = false;
        private int            iterationsBeforeOptimizing = 10;
        private int            iterationsToKeep           = 100;
        
        private Seeker() throws AWTException {
    
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
    
            if (learning) { findMatches(expectedResults); }
            
            for (T object : objects) {
                
                Position currentPosition = new Position(0, 0);
                
                for (; currentPosition.getY() < searchZone.height - (object.getHeight() + exclusiveZone); currentPosition.incY()) {
                    currentPosition.setX(0);
                    for (; currentPosition.getX() < searchZone.width - (object.getWidth() + exclusiveZone); currentPosition.incX()) {
                        
                        while (overlapingExists(currentPosition)) {
                            currentPosition.setX(currentPosition.getX() + exclusiveZone * 2 + object.getWidth());
                        }
    
                        boolean found = searchObject(capturedScreen, currentPosition, object);
    
                        if (found && optimizing) {
        
                            SearchHistory.Position position = new SearchHistory.Position(currentPosition.getX() + searchZone.xMin, currentPosition.getY() + searchZone.yMin);
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
                searchZone = new Zone(area.getX_1(), area.getX_2(), area.getY_1(), area.getY_2());
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
    
        private void findMatches(int numberOfMatches) {
        
            List<Parameter> parameters = Parameter.fromDTOs(searchHistoryDatabase.selectSearchParameters(searchHistory.getHash()));
            if (parameters.isEmpty()) {
                for (int precision = 0; precision < 66; precision += 5) {
                    for (double errorRate = 0.0; errorRate <= 0.30; errorRate += 0.05) {
                        parameters.add(new Parameter(precision, errorRate));
                    }
                }
            }
        
            System.out.println("------------------------------------------------------------------------------------------------------------------------");
        
            Iterator<Parameter> iterator = parameters.iterator();
        
            while (iterator.hasNext()) {
            
                Parameter param = iterator.next();
                setPrecision(param.getPrecision());
                setAllowedErrorRate(param.getErrorRate());
                search();
                System.out.println("precision = " + param.getPrecision() + " && errorRate = " + param.getErrorRate() + " => " + objects.get(0).getPositions().size() + " matche(s)");
            
                if (objects.get(0).getPositions().size() != numberOfMatches) { iterator.remove(); }
            }
        
            if (!parameters.isEmpty()) {
                searchHistoryDatabase.updateSearchParameters(searchHistory.getHash(), parameters);
            }
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
            
            if (currentPosition.getY() + object.getHeight() + exclusiveZone < (objectPosition.getY() - searchZone.yMin)
                || currentPosition.getY() > (objectPosition.getY() - searchZone.yMin) + object.getHeight() + exclusiveZone) {
                return false;
            }
            if (currentPosition.getX() + object.getWidth() + exclusiveZone < (objectPosition.getX() - searchZone.xMin)
                || currentPosition.getX() > (objectPosition.getX() - searchZone.xMin) + object.getWidth() + exclusiveZone) {
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
                           .mapToInt(List::size)
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
        
        public S setSearchZone(Zone zone) {
            
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
    
    public class ColorChecker {
        
        int           x = 1;
        int           y = 1;
        BufferedImage capturedScreen;
        int           color;
        
        public ColorChecker setCoordinates(int x, int y) {
            
            this.x = x;
            this.y = y;
            
            return this;
        }
        
        public boolean check() {
            
            if (capturedScreen == null) { capturedScreen = robot.createScreenCapture(new Rectangle(0, 0, Screen.SCREEN_WIDTH, Screen.SCREEN_HEIGHT)); }
            
            int iCapturedRGB = capturedScreen.getRGB(x, y);
            // Debug.sout("Peripheral > (" + x + ", " + y + ") Searching : " + color + ", found : " + iCapturedRGB + ".");
            return (color == iCapturedRGB);
        }
        
        public ColorChecker setCapturedScreen(BufferedImage biCapturedScreen) {
            
            this.capturedScreen = biCapturedScreen;
            
            return this;
        }
        
        public ColorChecker setColor(int iColor) {
            
            this.color = iColor;
            
            return this;
        }
    }
}
