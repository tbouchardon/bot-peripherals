package fr.ksuto.prh.peripherals;

import fr.ksuto.prh.entities.Picture;
import fr.ksuto.prh.entities.Position;
import fr.ksuto.prh.helpers.Arithmetic;
import fr.ksuto.prh.helpers.PictureSearch;
import fr.ksuto.prh.properties.Constants;
import fr.ksuto.prh.tools.Debug;

import java.awt.*;
import java.awt.event.InputEvent;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.google.inject.Inject;

/**
 * Created by thomas.bouchardon on 26/11/2015!
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class Mouse extends Peripheral {
    
    public static final int           LEFT                 = InputEvent.BUTTON1_DOWN_MASK;
    public static final int           RIGHT                = InputEvent.BUTTON3_DOWN_MASK;
    public static final double        X_ADJUSTEMENT_FACTOR = 0.0;
    public static final double        Y_ADJUSTEMENT_FACTOR = 0.0;
    public final        Dimension     dim_D                = new Dimension(Toolkit.getDefaultToolkit().getScreenSize());
    public final        int           i_SCREEN_HEIGHT      = (int) dim_D.getHeight();
    private final       double        startY               = (i_SCREEN_HEIGHT / 2d);
    public final        int           i_SCREEN_WIDTH       = (int) dim_D.getWidth();
    private final       double        startX               = (i_SCREEN_WIDTH / 2d);
    @Inject
    private             Screen        screen;
    @Inject
    private             MousePosition mousePosition;
    
    public Mouse() throws AWTException {
    
    }
    
    public static void main(String[] args) throws AWTException {
    
        Mouse mouse = new Mouse();
    
        int length = 400;
    
        for (int ignore = 0; ignore < 100; ignore++) {mouse.naturalMoveTo(500 + (int) (Math.random() * 5), 500 + (int) +(Math.random() * 5));}
    
        //        for (int x = 0; x <= length; x++) {
        //            System.out.println(mouse.getCircleHeight(length, x, 0.1));
        //        }
    }
    
    public void clickAlongLine(int numberOf, int x1, int y1, int x2, int y2, int iButtonMask) {
        
        mousePosition.waitIfUserActive();
        
        int step = (numberOf == 1 ? 0 : (x2 - x1) / (numberOf - 1));
        int x    = (numberOf == 1 ? ((x2 - x1) / 2 + x1) : x1);
        
        for (int click = 0; click < numberOf; click++) {
            
            int y = (int) ((double) y1 + (((double) y2 - (double) y1) / ((double) x2 - (double) x1)) * ((double) x - (double) x1));
            
            naturalMoveTo(x, y);
            robot.mousePress(iButtonMask);
            robot.mouseRelease(iButtonMask);
            delay(Constants.i_DELAY);
            x += step;
        }
        
        mousePosition.updateMousePosition();
    }
    
    public void clickLeft() {
        
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        delay(Constants.i_DELAY);
    }
    
    public void clickLeft(int x, int y) {
        
        mousePosition.waitIfUserActive();
        
        Debug.sout(x + ", " + y + ")");
        
        naturalMoveTo(x, y);
        mousePosition.updateMousePosition();
        clickLeft();
    }
    
    public void clickRight() {
        
        robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
        delay(Constants.i_DELAY);
    }
    
    public void clickRight(int x, int y) {
        
        mousePosition.waitIfUserActive();
        
        Debug.sout(x + ", " + y + ")");
        
        naturalMoveTo(x, y);
        mousePosition.updateMousePosition();
        clickRight();
    }
    
    public boolean clickThing(String[] sImage) {
        
        mousePosition.waitIfUserActive();
        
        Debug.sout("String[] sImage)");
        
        return clickThing(sImage, 0, 0);
    }
    
    public boolean clickThing(String[] sImage, int xOffset, int yOffset) {
        
        mousePosition.waitIfUserActive();
        
        Debug.sout("sImage, " + xOffset + ", " + yOffset + ")");
        
        Debug.sout("Trying to click '" + sImage[0] + "', Offsets : x=" + xOffset + ", y=" + yOffset);
        
        ArrayList<int[]> alFound;
        
        try {
            PictureSearch pictureSearch = (PictureSearch) new PictureSearch()
                    .addPicturesWithUrls(sImage)
                    .search();
            
            if (pictureSearch.hasAnyResults()) {
                for (Picture picture : pictureSearch.getObjects()) {
                    Debug.sout(picture.getReferenceImage() + " Found");
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
            // TODO : Catcher cette exception correctement !
            e.printStackTrace();
        }
        return false;
    }
    
    public void drag(int iX_start, int iX_end, int iY_start, int iY_end, int iButtonMask) {
        
        moveAndPress(iButtonMask, iX_start, iY_start, 1000);
        while ((iX_start != iX_end) || (iY_start != iY_end)) {
            if (iX_start < iX_end) {iX_start++;}
            if (iX_start > iX_end) {iX_start--;}
            if (iY_start < iY_end) {iY_start++;}
            if (iY_start > iY_end) {iY_start--;}
            robot.mouseMove(iX_start, iY_start);
            Debug.sout(iX_start + " " + iY_start);
            delay(Constants.i_DRAG_DELAY);
        }
        release(iButtonMask, 1000, false);
    }
    
    public void dragAngle(int angle, int distance, int iButtonMask) {
        
        //        System.out.println("[TRACE] PRH : angle = " + angle);
        double toRadians = Math.toRadians(angle);
        //        System.out.println("[TRACE] PRH : toRadians = " + toRadians);
        
        double xMod = toRadians % (2 * Math.PI) - Math.PI / 2;
        //        System.out.println("[TRACE] PRH : xMod = " + xMod);
        double yMod = toRadians % (2 * Math.PI) - Math.PI;
        //        System.out.println("[TRACE] PRH : yMod = " + yMod);
        
        double xFactor = -(2 / Math.PI * Math.abs(xMod) - 1);
        //        System.out.println("[TRACE] PRH : xFactor = " + xFactor);
        double yFactor = 2 / Math.PI * Math.abs(yMod) - 1;
        //        System.out.println("[TRACE] PRH : yFactor = " + yFactor);
        
        if (xFactor < 0) {distance = (int) (distance * (1 - X_ADJUSTEMENT_FACTOR * (Math.abs(xFactor))));}
        if (yFactor < 0) {distance = (int) (distance * (1 - Y_ADJUSTEMENT_FACTOR * (Math.abs(yFactor))));}
        
        double x = startX;
        double y = startY;
        
        double currentDistance = 0d;
        
        moveAndPress(iButtonMask, screen.iX_START, screen.iY_START, Constants.i_DELAY);
        
        while (currentDistance < distance) {
            
            currentDistance = Math.sqrt(Math.pow(x - startX, 2) + Math.pow(y - startY, 2));
            
            robot.mouseMove((int) x, (int) y);
            delay(Constants.i_DRAG_DELAY);
            
            int oldX = (int) x, oldY = (int) y;
            
            while (!minimalDistance(x, y, oldX, oldY)) {
                x -= xFactor;
                y += yFactor;
            }
        }
        
        release(iButtonMask, Constants.i_DELAY, true);
    }
    
    public void dragBottom2Top(int iDistance, int iButtonMask) {
        
        moveAndPress(iButtonMask, screen.iX_START, screen.iY_START, Constants.i_DELAY);
        for (int iBT = screen.iY_START; iBT > screen.iY_START - iDistance; iBT -= Constants.i_DRAG_SPACE) {
            robot.mouseMove(Screen.SCREEN_WIDTH / 2, iBT);
            delay(Constants.i_DRAG_DELAY);
        }
        release(iButtonMask, Constants.i_DELAY, false);
    }
    
    public void dragLeft2Right(int iDistance, int iButtonMask) {
        
        moveAndPress(iButtonMask, screen.iX_START, screen.iY_START, Constants.i_DELAY);
        for (int iLR = screen.iX_START; iLR < screen.iX_START + iDistance; iLR += Constants.i_DRAG_SPACE) {
            robot.mouseMove(iLR, screen.iY_START);
            delay(Constants.i_DRAG_DELAY);
        }
        release(iButtonMask, Constants.i_DELAY, false);
    }
    
    public void dragRight2Left(int iDistance, int iButtonMask) {
        
        moveAndPress(iButtonMask, screen.iX_START, screen.iY_START, Constants.i_DELAY);
        for (int iLR = screen.iX_START; iLR > screen.iX_START - iDistance; iLR -= Constants.i_DRAG_SPACE) {
            robot.mouseMove(iLR, screen.iY_START);
            delay(Constants.i_DRAG_DELAY);
        }
        release(iButtonMask, Constants.i_DELAY, false);
    }
    
    public void dragTop2Bottom(int iDistance, int iButtonMask) {
        
        moveAndPress(iButtonMask, screen.iX_START, screen.iY_START, Constants.i_DELAY);
        for (int iTB = screen.iY_START; iTB < screen.iY_START + iDistance; iTB += Constants.i_DRAG_SPACE) {
            robot.mouseMove(screen.iX_START, iTB);
            delay(Constants.i_DRAG_DELAY);
        }
        release(iButtonMask, Constants.i_DELAY, false);
    }
    
    public void move(int x, int y) {
    
        naturalMoveTo(x, y);
        delay(Constants.i_DELAY);
    }
    
    public void naturalMoveTo(int xB, int yB) {
    
        //        Painter painter = new Painter();
    
        int maximumOverRun = 150;
    
        double xA = MouseInfo.getPointerInfo().getLocation().getX();
        double yA = MouseInfo.getPointerInfo().getLocation().getY();
    
        boolean isLeftToRight      = xB > xA;
        boolean isTopToBottom      = yB > yA;
        double  distance           = Arithmetic.getDistance(xA, yA, xB, yB);
        int     numberOfOverRun    = (int) Math.floor(Math.random() * 2 + 0.5);
        int     numberOfDeviations = 0;//(int) Math.floor(0.001 * Math.exp(Math.random() * 8.5)); //Excel = ARRONDI.INF(0,001*EXP(B1*8,5);0) => 0 à 5 avec 80% de probabilité 1
        if (distance > 500) {numberOfDeviations = Math.random() < 0.2 ? 1 : numberOfDeviations;}
        if (distance > 1000) {numberOfDeviations = Math.random() < 0.2 ? 2 : numberOfDeviations;}
        if (distance > 1500) {numberOfDeviations = Math.random() < 0.2 ? 3 : numberOfDeviations;}
    
        //        System.out.println("numberOfDeviations = " + numberOfDeviations);
    
        List<Point> pointList = new ArrayList<>();
        pointList.add(new Point((int) xA,
                                (int) yA));
    
        generateDeviationsPoints(xB, yB, xA, yA, isLeftToRight, numberOfDeviations, pointList);
    
        //        System.out.println("numberOfOverRun = " + numberOfOverRun);
    
        for (int i = 0; i < numberOfOverRun; i++) {
            pointList.add(new Point((int) (xB + (Math.random() * maximumOverRun) - maximumOverRun / 2),
                                    (int) (yB + (Math.random() * maximumOverRun) - maximumOverRun / 2)));
            maximumOverRun = maximumOverRun / 2;
        }
    
        pointList.add(new Point(xB, yB));
    
        //        System.out.println("pointList = {" + pointList.stream().map(point -> "[" + point.getX() + ", " + point.getY() + "]").collect(Collectors.joining(", ")) + "}");
    
        List<Point> mousePositions = generateMousePositions(pointList);
    
        //        System.out.println("mousePositions.size() = " + mousePositions.size());
    
        int     slowDownStartingPoint = 666;
        double  increment             = 0.0;
        double  incrementStep         = 0.001;
        double  counter               = 0.0;
        double  threshold             = 1;
        boolean shouldSleep;
    
        if (mousePositions.size() < slowDownStartingPoint) {
            for (int i = mousePositions.size(); i < slowDownStartingPoint; i++) {
            
                increment += incrementStep;
                counter += increment;
                shouldSleep = counter > threshold;
                if (shouldSleep) {counter = 0;}
            }
        }
    
        for (int i = 0, mousePositionsSize = mousePositions.size(); i < mousePositionsSize; i++) {
            Point point = mousePositions.get(i);
        
            robot.mouseMove((int) point.getX(),
                            (int) point.getY());
        
            if (mousePositions.size() - i < slowDownStartingPoint) {
            
                increment += incrementStep;
                counter += increment;
                shouldSleep = counter > threshold;
                if (shouldSleep) {counter = 0;}
            
                if (shouldSleep) {
                    try {
                        Thread.sleep(1);
                    }
                    catch (InterruptedException e) {
                        System.out.println("[ERROR] " + e.getMessage());
                    }
                }
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
        
        double      m, p, mPrime, pPrime, theta;
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
        return mousePositions;
    }
    
    private void generatePositions(double m, double p, double mPrime, List<Point> mousePositions, Point a, Point b, double x, boolean top) {
        
        double y = m * x + p;
        
        Arithmetic.PrecisePoint aPrime = new Arithmetic.PrecisePoint(x, y);
        
        double                  distanceAB                 = Arithmetic.getDistance(a.getX(), a.getY(), b.getX(), b.getY());
        double                  distanceAIntermediatePoint = Arithmetic.getDistance(a.getX(), a.getY(), aPrime.getX(), aPrime.getY());
        double                  distanceFromLine           = Arithmetic.getCircleHeight(distanceAB, distanceAIntermediatePoint, 0.1);
        Arithmetic.PrecisePoint pointAtDistanceOnLine      = Arithmetic.getPointAtDistanceOnLine(aPrime.getX(), aPrime.getY(), mPrime, distanceFromLine, top);
        
        //        System.out.println("m = " + m);
        //        System.out.println("mPrime = " + mPrime);
        //        System.out.println("m * mPrime = " + m * mPrime);
    
        if (mousePositions.get(mousePositions.size() - 1).getX() != (int) Math.floor(pointAtDistanceOnLine.getX()) ||
            mousePositions.get(mousePositions.size() - 1).getY() != (int) Math.floor(pointAtDistanceOnLine.getY())) {
        
            mousePositions.add(pointAtDistanceOnLine.toRoundedPoint());
            if (Double.isNaN(pointAtDistanceOnLine.getX()) || Double.isNaN(pointAtDistanceOnLine.getY())) {
                System.out.println("[ERROR] Position coordinates is Not a Number !");
            }
        }
    }
    
    private boolean minimalDistance(double x, double y, int oldX, int oldY) {
        
        boolean xCheck = Math.abs(Math.abs(oldX) - Math.abs(x)) >= Constants.i_DRAG_SPACE;
        boolean yCheck = Math.abs(Math.abs(oldY) - Math.abs(y)) >= Constants.i_DRAG_SPACE;
        
        return xCheck || yCheck;
    }
    
    private void moveAndPress(int iButtonMask, int iX_start, int iY_start, int i_delay) {
        
        naturalMoveTo(iX_start, iY_start);
        delay(i_delay);
        robot.mousePress(iButtonMask);
        delay(i_delay);
    }
    
    private void release(int iButtonMask, int i_delay, boolean stopMotion) {
        
        if (stopMotion) {delay(250);}
        robot.mouseRelease(iButtonMask);
        delay(i_delay);
    }
    
    private void zoom(int steps, boolean out) {
        
        //        helper.delay(200);
        
        int wheelAmt = out ? 1 : -1;
        
        for (int j = 1; j <= steps; j++) {
            
            //            helper.robot.keyPress(KeyEvent.VK_CONTROL);
            
            for (int i = 1; i <= 5; i++) {
    
                robot.mouseWheel(wheelAmt);
                delay(20);
            }
            //            helper.robot.keyRelease(KeyEvent.VK_CONTROL);
        }
    }
}
