package fr.ksuto.prh.peripherals;

import fr.ksuto.commons.math.Arithmetic;
import fr.ksuto.logger.ConsoleLogger;
import fr.ksuto.prh.entities.Picture;
import fr.ksuto.prh.entities.Position;
import fr.ksuto.prh.helpers.PictureSearch;
import fr.ksuto.prh.tools.ShowObjects;

import java.awt.*;
import java.awt.event.InputEvent;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.jetbrains.annotations.NotNull;

import com.google.inject.Inject;

/**
 * Created by thomas.bouchardon on 26/11/2015!
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class Mouse extends Peripheral {
    
    public static final  int       LEFT                 = InputEvent.BUTTON1_DOWN_MASK;
    public static final  int       RIGHT                = InputEvent.BUTTON3_DOWN_MASK;
    public static final  double    X_ADJUSTEMENT_FACTOR = 0.0;
    public static final  double    Y_ADJUSTEMENT_FACTOR = 0.0;
    private static final Dimension SCREEN_DIMENSION     = new Dimension(Toolkit.getDefaultToolkit().getScreenSize());
    private static final int       SCREEN_HEIGHT        = (int) SCREEN_DIMENSION.getHeight();
    private static final double    START_Y              = (SCREEN_HEIGHT / 2d);
    private static final int       SCREEN_WIDTH         = (int) SCREEN_DIMENSION.getWidth();
    private static final double    START_X              = (SCREEN_WIDTH / 2d);
    private final        Random    random;
    @Inject
    ConsoleLogger logger;
    @Inject
    private Screen        screen;
    @Inject
    private MousePosition mousePosition;
    private int           dragDelay;
    private int           dragSpace;
    
    public Mouse() throws AWTException, NoSuchAlgorithmException {
        
        super();
        dragSpace = Integer.parseInt(properties.getProperty("ksuto.prh.peripherals.mouse.drag.space", "5"));
        dragDelay = Integer.parseInt(properties.getProperty("ksuto.prh.peripherals.mouse.drag.delay", "10"));
        random = SecureRandom.getInstanceStrong();
    }
    
    public static void main(String[] args) throws AWTException, NoSuchAlgorithmException {
        
        Mouse mouse = new Mouse();
        
        int length = 400;
        
        for (int ignore = 0; ignore < 100; ignore++) {mouse.naturalMoveTo(500 + new Random().nextInt(5), 500 + new Random().nextInt(5));}
    }
    
    public void clickAlongLine(int numberOf, int x1, int y1, int x2, int y2, int iButtonMask) {
        
        mousePosition.waitIfUserActive();
        
        int step = (numberOf == 1 ? 0 : (x2 - x1) / (numberOf - 1));
        int x    = (numberOf == 1 ? ((x2 - x1) / 2 + x1) : x1);
        
        for (int click = 0; click < numberOf; click++) {
            
            int y = (int) (y1 + (((double) y2 - (double) y1) / ((double) x2 - (double) x1)) * ((double) x - (double) x1));
            
            naturalMoveTo(x, y);
            robot.mousePress(iButtonMask);
            robot.mouseRelease(iButtonMask);
            delay(i_DELAY);
            x += step;
        }
        
        mousePosition.updateMousePosition();
    }
    
    public void clickLeft() {
        
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        delay(i_DELAY);
    }
    
    public void clickLeft(int x, int y) {
        
        mousePosition.waitIfUserActive();
        
        logger.sysOutTrace(x + ", " + y + ")");
        
        naturalMoveTo(x, y);
        mousePosition.updateMousePosition();
        clickLeft();
    }
    
    public void clickRight() {
        
        robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
        delay(i_DELAY);
    }
    
    public void clickRight(int x, int y) {
        
        mousePosition.waitIfUserActive();
        
        logger.sysOutTrace(x + ", " + y + ")");
        
        naturalMoveTo(x, y);
        mousePosition.updateMousePosition();
        clickRight();
    }
    
    public boolean clickThing(String[] sImage) {
        
        mousePosition.waitIfUserActive();
        
        logger.sysOutTrace("String[] sImage)");
        
        return clickThing(sImage, 0, 0);
    }
    
    public boolean clickThing(String[] sImage, int xOffset, int yOffset) {
        
        mousePosition.waitIfUserActive();
        
        logger.sysOutTrace("sImage, " + xOffset + ", " + yOffset + ")");
        
        logger.sysOutTrace("Trying to click '" + sImage[0] + "', Offsets : x=" + xOffset + ", y=" + yOffset);
        
        ArrayList<int[]> alFound;
        
        try {
            PictureSearch pictureSearch = (PictureSearch) new PictureSearch()
                                                                  .addPicturesWithUrls(sImage)
                                                                  .search();
            
            if (pictureSearch.hasAnyResults()) {
                for (Picture picture : pictureSearch.getObjects()) {
                    logger.sysOutTrace(picture.getReferenceImage() + " Found");
                    if (picture.isPresent()) {
                        for (Position position : picture.getPositions()) {
                            clickLeft(position.getX() + 3 + xOffset, position.getY() + 3 + yOffset);
                        }
                    }
                }
                return true;
            }
        }
        catch (AWTException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public void drag(int xStart, int xEnd, int yStart, int yEnd, int iButtonMask) {
        
        naturalMoveTo(xStart, yStart);
        press(iButtonMask, 250, true);
        naturalMoveTo(xEnd, yEnd);
        release(iButtonMask, 250, true);
    }
    
    public void dragAngle(int angle, int distance, int iButtonMask) {
        
        double toRadians = Math.toRadians(angle);
        
        double xMod = toRadians % (2 * Math.PI) - Math.PI / 2;
        double yMod = toRadians % (2 * Math.PI) - Math.PI;
        
        double xFactor = -(2 / Math.PI * Math.abs(xMod) - 1);
        double yFactor = 2 / Math.PI * Math.abs(yMod) - 1;
        
        if (xFactor < 0) {distance = (int) (distance * (1 - X_ADJUSTEMENT_FACTOR * (Math.abs(xFactor))));}
        if (yFactor < 0) {distance = (int) (distance * (1 - Y_ADJUSTEMENT_FACTOR * (Math.abs(yFactor))));}
        
        double x = START_X;
        double y = START_Y;
        
        double currentDistance = 0d;
        
        moveAndPress(iButtonMask, Screen.X_START, Screen.Y_START, i_DELAY);
        
        while (currentDistance < distance) {
            
            currentDistance = Math.sqrt(Math.pow(x - START_X, 2) + Math.pow(y - START_Y, 2));
            
            robot.mouseMove((int) x, (int) y);
            delay(dragDelay);
            
            int oldX = (int) x;
            int oldY = (int) y;
            
            while (!minimalDistance(x, y, oldX, oldY)) {
                x -= xFactor;
                y += yFactor;
            }
        }
        
        release(iButtonMask, i_DELAY, true);
    }
    
    public void dragBottom2Top(int iDistance, int iButtonMask) {
        
        moveAndPress(iButtonMask, Screen.X_START, Screen.Y_START, i_DELAY);
        for (int iBT = Screen.Y_START; iBT > Screen.Y_START - iDistance; iBT -= dragSpace) {
            robot.mouseMove(Screen.SCREEN_WIDTH / 2, iBT);
            delay(dragDelay);
        }
        release(iButtonMask, i_DELAY, false);
    }
    
    public void dragLeft2Right(int iDistance, int iButtonMask) {
        
        moveAndPress(iButtonMask, Screen.X_START, Screen.Y_START, i_DELAY);
        for (int iLR = Screen.X_START; iLR < Screen.X_START + iDistance; iLR += dragSpace) {
            robot.mouseMove(iLR, Screen.Y_START);
            delay(dragDelay);
        }
        release(iButtonMask, i_DELAY, false);
    }
    
    public void dragRight2Left(int iDistance, int iButtonMask) {
        
        moveAndPress(iButtonMask, Screen.X_START, Screen.Y_START, i_DELAY);
        for (int iLR = Screen.X_START; iLR > Screen.X_START - iDistance; iLR -= dragSpace) {
            robot.mouseMove(iLR, Screen.Y_START);
            delay(dragDelay);
        }
        release(iButtonMask, i_DELAY, false);
    }
    
    public void dragTop2Bottom(int iDistance, int iButtonMask) {
        
        moveAndPress(iButtonMask, Screen.X_START, Screen.Y_START, i_DELAY);
        for (int iTB = Screen.Y_START; iTB < Screen.Y_START + iDistance; iTB += dragSpace) {
            robot.mouseMove(Screen.X_START, iTB);
            delay(dragDelay);
        }
        release(iButtonMask, i_DELAY, false);
    }
    
    public void move(int x, int y) {
        
        naturalMoveTo(x, y);
        delay(i_DELAY);
    }
    
    public void naturalMoveTo(int xB, int yB) {
        
        double xA = MouseInfo.getPointerInfo().getLocation().getX();
        double yA = MouseInfo.getPointerInfo().getLocation().getY();
        
        boolean isLeftToRight      = xB > xA;
        boolean isTopToBottom      = yB > yA;
        double  distance           = Arithmetic.getDistance(xA, yA, xB, yB);
        int     numberOfOverRun    = (int) Math.floor(random.nextInt(2) + 0.5);
        int     numberOfDeviations = getNumberOfDeviations(distance, 0);
        
        List<Point> pointList = new ArrayList<>();
        pointList.add(new Point((int) xA,
                                (int) yA));
        
        generateDeviationsPoints(xB, yB, xA, yA, isLeftToRight, numberOfDeviations, pointList);
        
        int maximumOverRun = (int) Math.min(150, distance * 0.2 + 1);
        
        for (int i = 0; i < numberOfOverRun; i++) {
            pointList.add(new Point((int) (xB + random.nextInt(maximumOverRun) - maximumOverRun / 2D),
                                    (int) (yB + random.nextInt(maximumOverRun) - maximumOverRun / 2D)));
            maximumOverRun = maximumOverRun / 2;
        }
        
        pointList.add(new Point(xB, yB));
        
        List<Point> mousePositions = generateMousePositions(pointList);
        
        int    slowDownStartingPoint = 666;
        double increment             = 0.0;
        double incrementStep         = 0.000005;
        double duration              = 0.0;
        double threshold             = 1;
        
        if (mousePositions.size() < slowDownStartingPoint) {
            for (int i = mousePositions.size(); i < slowDownStartingPoint; i++) {
                
                increment += incrementStep / 2;
                duration += increment;
            }
        }
        
        for (int i = 0, mousePositionsSize = mousePositions.size(); i < mousePositionsSize; i++) {
            Point point = mousePositions.get(i);
            
            robot.mouseMove((int) point.getX(),
                            (int) point.getY());
            
            if (mousePositions.size() - i < slowDownStartingPoint) {
                
                increment += incrementStep;
                duration += increment;
                if (duration > 1) {duration = 1;}
                
                delay(duration);
            }
        }
    }
    
    public void scrollDown(int scrollDown) {
        
        for (int ignore = 0; ignore < scrollDown; ignore++) {
            robot.mouseWheel(1);
            delay(200);
        }
    }
    
    public void scrollUp(int scrollUp) {
        
        for (int ignore = 0; ignore < scrollUp; ignore++) {
            robot.mouseWheel(-1);
            delay(200);
        }
    }
    
    public void shift(int offsetX, int offsetY) {
        
        Point point = MouseInfo.getPointerInfo().getLocation();
        move((int) point.getX() + offsetX, (int) point.getY() + offsetY);
    }
    
    public Point waitForClick() {
        
        ShowObjects showObjects = new ShowObjects();
        
        while (showObjects.mousePosition == null) {
            robot.delay(i_DELAY);
//            System.out.println(showObjects.mousePosition);
        }
        
        Point point = showObjects.mousePosition;
        showObjects.clean();
        return point;
    }
    
    public void zoomIn(int steps) {
        
        zoom(steps, false);
    }
    
    public void zoomOut(int steps) {
        
        zoom(steps, true);
    }
    
    private void generateDeviationsPoints(int xB, int yB, double xA, double yA, boolean isLeftToRight, int numberOfDeviations, List<Point> pointList) {
        
        double xnA = xA;
        double m   = (yB - yA) / (xB - xA); // y = mx + p
        //        double mp  = -1 / m; // Perpendiculaire
        
        for (int index = 0; index < numberOfDeviations; index++) {
            
            double dxnAxB = Math.abs(xnA - xB);
            
            double xC = ((isLeftToRight ? 1 : -1) * (((dxnAxB - (dxnAxB * 0.25)) * Math.random() * 0.75) + (dxnAxB * 0.25))) + xnA;
            double yC = (m * (xC - xA)) + yA;
            Point  c  = new Point((int) xC, (int) yC);
            pointList.add(c);
            xnA = xC;
        }
    }
    
    @NotNull
    private List<Point> generateMousePositions(List<Point> pointList) {
        
        boolean windowed = properties.getProperty("ksuto.prh.peripherals.screen", "FULLSCREEN").equals("WINDOWED");
        
        double m;
        double p;
        double mPrime;
        double pPrime;
        double theta;
        
        List<Point> mousePositions = new ArrayList<>();
        mousePositions.add(pointList.get(0));
        boolean top = false;
        
        for (int index = 1; index < pointList.size(); index++) {
            
            top = !top;
            
            Point a = pointList.get(index - 1);
            Point b = pointList.get(index); // y = mx + p
            
            if (a.getX() == (int) b.getX()) {b.setLocation(b.getX() + 1, b.getY());}
            if (a.getY() == (int) b.getY()) {b.setLocation(b.getX(), b.getY() + 1);}
            
            m = Arithmetic.getPente(a, b);
            p = Arithmetic.getOrdonnee(a, m);
            mPrime = -1 / m;
            
            if (a.getX() < b.getX()) {
                for (double x = a.getX(); x < b.getX(); x += 0.01) {
                    generatePositions(m, p, mPrime, mousePositions, a, b, x, top);
                }
            }
            else {
                for (double x = a.getX(); x > b.getX(); x -= 0.01) {
                    generatePositions(m, p, mPrime, mousePositions, a, b, x, top);
                }
            }
        }
        
        if (windowed) {
            for (Point position : mousePositions) {
                if (position.getY() > SCREEN_HEIGHT - 50) {position.y = SCREEN_HEIGHT - 50;}
                if (position.getY() < 45) {position.y = 40;}
            }
        }
        
        return mousePositions;
    }
    
    private void generatePositions(double m, double p, double mPrime, List<Point> mousePositions, Point a, Point b, double x, boolean top) {
        
        double y = m * x + p;
        
        Arithmetic.PrecisePoint aPrime = new Arithmetic.PrecisePoint(x, y);
        
        double                  distanceAB                 = Arithmetic.getDistance(a.getX(), a.getY(), b.getX(), b.getY());
        double                  distanceAIntermediatePoint = Arithmetic.getDistance(a.getX(), a.getY(), aPrime.getX(), aPrime.getY());
        double                  distanceFromLine           = Arithmetic.getCircleHeight(distanceAB, distanceAIntermediatePoint, 0.1);
        Arithmetic.PrecisePoint pointAtDistanceOnLine      = Arithmetic.getPointAtDistanceOnLine(aPrime.getX(), aPrime.getY(), mPrime, distanceFromLine, top);
        
        if (mousePositions.get(mousePositions.size() - 1).getX() != (int) Math.floor(pointAtDistanceOnLine.getX()) ||
            mousePositions.get(mousePositions.size() - 1).getY() != (int) Math.floor(pointAtDistanceOnLine.getY())) {
            
            mousePositions.add(pointAtDistanceOnLine.toRoundedPoint());
            if (Double.isNaN(pointAtDistanceOnLine.getX()) || Double.isNaN(pointAtDistanceOnLine.getY())) {
                logger.sysOutError("Position coordinates is Not a Number !");
            }
        }
    }
    
    private int getNumberOfDeviations(double distance, int defaultValue) {
        
        int numberOfDeviations = defaultValue;
        
        if (distance > 500) {numberOfDeviations = Math.random() < 0.2 ? 1 : defaultValue;}
        if (distance > 1000) {numberOfDeviations = Math.random() < 0.2 ? 2 : defaultValue;}
        if (distance > 1500) {numberOfDeviations = Math.random() < 0.2 ? 3 : defaultValue;}
        
        return numberOfDeviations;
    }
    
    private boolean minimalDistance(double x, double y, int oldX, int oldY) {
        
        boolean xCheck = Math.abs(Math.abs(oldX) - Math.abs(x)) >= dragSpace;
        boolean yCheck = Math.abs(Math.abs(oldY) - Math.abs(y)) >= dragSpace;
        
        return xCheck || yCheck;
    }
    
    private void moveAndPress(int buttonMask, int xStart, int yStart, int delay) {
        
        naturalMoveTo(xStart, yStart);
        delay(delay);
        robot.mousePress(buttonMask);
        delay(delay);
    }
    
    private void press(int iButtonMask, int delay, boolean stopMotion) {
        
        if (stopMotion) {delay(250);}
        robot.mousePress(iButtonMask);
        delay(delay);
    }
    
    private void release(int iButtonMask, int delay, boolean stopMotion) {
        
        if (stopMotion) {delay(250);}
        robot.mouseRelease(iButtonMask);
        delay(delay);
    }
    
    private void zoom(int steps, boolean out) {
        
        int wheelAmt = out ? 1 : -1;
        
        for (int j = 1; j <= steps; j++) {
            
            for (int i = 1; i <= 5; i++) {
                
                robot.mouseWheel(wheelAmt);
                delay(20);
            }
        }
    }
}
