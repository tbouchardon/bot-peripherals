package net.ddns.ksuto.prh.peripherals;

import net.ddns.ksuto.prh.entities.Picture;
import net.ddns.ksuto.prh.entities.Position;
import net.ddns.ksuto.prh.helpers.Arithmetic;
import net.ddns.ksuto.prh.helpers.PictureSearch;
import net.ddns.ksuto.prh.properties.Constants;
import net.ddns.ksuto.prh.tools.Debug;

import java.awt.*;
import java.awt.event.InputEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import com.google.inject.Inject;

/**
 * Created by thomas.bouchardon on 26/11/2015!
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class Mouse extends Peripheral {
    
    public static final int           LEFT                 = InputEvent.BUTTON1_DOWN_MASK;
    public static final int           RIGHT                = InputEvent.BUTTON3_DOWN_MASK;
    public static final double        Y_ADJUSTEMENT_FACTOR = 0.0;
    public static final double        X_ADJUSTEMENT_FACTOR = 0.0;
    public final        Dimension     dim_D                = new Dimension(Toolkit.getDefaultToolkit().getScreenSize());
    public final        int           i_SCREEN_WIDTH       = (int) dim_D.getWidth();
    public final        int           i_SCREEN_HEIGHT      = (int) dim_D.getHeight();
    private final       double        startX               = (i_SCREEN_WIDTH / 2d);
    private final       double        startY               = (i_SCREEN_HEIGHT / 2d);
    @Inject
    private             Screen        screen;
    @Inject
    private             MousePosition mousePosition;
    
    Mouse() throws AWTException {
    
    }
    
    public static void main(String[] args) throws AWTException {
        
        Mouse mouse = new Mouse();
        
        int length = 400;
        
        mouse.naturalMoveTo(500, 500);
        
        //        for (int x = 0; x <= length; x++) {
        //            System.out.println(mouse.getCircleHeight(length, x, 0.1));
        //        }
    }
    
    public void clickRight() {
        
        robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
        delay(Constants.i_DELAY);
    }
    
    public void clickLeft() {
        
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        delay(Constants.i_DELAY);
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
        
        if (xFactor < 0) { distance = (int) (distance * (1 - X_ADJUSTEMENT_FACTOR * (Math.abs(xFactor)))); }
        if (yFactor < 0) { distance = (int) (distance * (1 - Y_ADJUSTEMENT_FACTOR * (Math.abs(yFactor)))); }
        
        double x = startX;
        double y = startY;
        
        double currentDistance = 0d;
        
        moveAndPress(iButtonMask, screen.iX_START, screen.iY_START, Constants.i_DELAY);
        
        while (currentDistance < distance) {
            
            currentDistance = Math.sqrt(Math.pow(x - startX, 2) + Math.pow(y - startY, 2));
            
            robot.mouseMove((int) x, (int) y);
            robot.delay(Constants.i_DRAG_DELAY);
            
            int oldX = (int) x, oldY = (int) y;
            
            while (!minimalDistance(x, y, oldX, oldY)) {
                x -= xFactor;
                y += yFactor;
            }
        }
        
        release(iButtonMask, Constants.i_DELAY, true);
    }
    
    public void dragTop2Bottom(int iDistance, int iButtonMask) {
        
        moveAndPress(iButtonMask, screen.iX_START, screen.iY_START, Constants.i_DELAY);
        for (int iTB = screen.iY_START; iTB < screen.iY_START + iDistance; iTB += Constants.i_DRAG_SPACE) {
            robot.mouseMove(screen.iX_START, iTB);
            robot.delay(Constants.i_DRAG_DELAY);
        }
        release(iButtonMask, Constants.i_DELAY, false);
    }
    
    public void dragBottom2Top(int iDistance, int iButtonMask) {
        
        moveAndPress(iButtonMask, screen.iX_START, screen.iY_START, Constants.i_DELAY);
        for (int iBT = screen.iY_START; iBT > screen.iY_START - iDistance; iBT -= Constants.i_DRAG_SPACE) {
            robot.mouseMove(Screen.SCREEN_WIDTH / 2, iBT);
            robot.delay(Constants.i_DRAG_DELAY);
        }
        release(iButtonMask, Constants.i_DELAY, false);
    }
    
    public void dragLeft2Right(int iDistance, int iButtonMask) {
        
        moveAndPress(iButtonMask, screen.iX_START, screen.iY_START, Constants.i_DELAY);
        for (int iLR = screen.iX_START; iLR < screen.iX_START + iDistance; iLR += Constants.i_DRAG_SPACE) {
            robot.mouseMove(iLR, screen.iY_START);
            robot.delay(Constants.i_DRAG_DELAY);
        }
        release(iButtonMask, Constants.i_DELAY, false);
    }
    
    public void dragRight2Left(int iDistance, int iButtonMask) {
        
        moveAndPress(iButtonMask, screen.iX_START, screen.iY_START, Constants.i_DELAY);
        for (int iLR = screen.iX_START; iLR > screen.iX_START - iDistance; iLR -= Constants.i_DRAG_SPACE) {
            robot.mouseMove(iLR, screen.iY_START);
            robot.delay(Constants.i_DRAG_DELAY);
        }
        release(iButtonMask, Constants.i_DELAY, false);
    }
    
    public void drag(int iX_start, int iX_end, int iY_start, int iY_end, int iButtonMask) {
        
        moveAndPress(iButtonMask, iX_start, iY_start, 1000);
        while ((iX_start != iX_end) || (iY_start != iY_end)) {
            if (iX_start < iX_end) { iX_start++; }
            if (iX_start > iX_end) { iX_start--; }
            if (iY_start < iY_end) { iY_start++; }
            if (iY_start > iY_end) { iY_start--; }
            robot.mouseMove(iX_start, iY_start);
            Debug.sout(iX_start + " " + iY_start);
            robot.delay(Constants.i_DRAG_DELAY);
        }
        release(iButtonMask, 1000, false);
    }
    
    public void clickLeft(int x, int y) {
    
        mousePosition.waitIfUserActive();
        
        Debug.sout(x + ", " + y + ")");
        
        robot.mouseMove(x, y);
        mousePosition.updateMousePosition();
        clickLeft();
    }
    
    public void zoomOut(int steps) {
        
        zoom(steps, true);
    }
    
    public void zoomIn(int steps) {
        
        zoom(steps, false);
    }
    
    public void clickAlongLine(int numberOf, int x1, int y1, int x2, int y2, int iButtonMask) {
        
        mousePosition.waitIfUserActive();
        
        int step = (numberOf == 1 ? 0 : (x2 - x1) / (numberOf - 1));
        int x    = (numberOf == 1 ? ((x2 - x1) / 2 + x1) : x1);
        
        for (int click = 0; click < numberOf; click++) {
            
            int y = (int) ((double) y1 + (((double) y2 - (double) y1) / ((double) x2 - (double) x1)) * ((double) x - (double) x1));
            
            robot.mouseMove(x, y);
            robot.mousePress(iButtonMask);
            robot.mouseRelease(iButtonMask);
            delay(Constants.i_DELAY);
            x += step;
        }
        
        mousePosition.updateMousePosition();
    }
    
    public void clickRight(int x, int y) {
    
        mousePosition.waitIfUserActive();
        
        Debug.sout(x + ", " + y + ")");
        
        robot.mouseMove(x, y);
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
    
    public void move(int x, int y) {
    
        //        robot.mouseMove(x, y);
        naturalMoveTo(x, y);
        robot.delay(Constants.i_DELAY);
    }
    
    public void naturalMoveTo(int xB, int yB) {
    
        //        Painter painter = new Painter();
    
        final int MAXIMUM_OVERRUN = 200;
    
        double xA = MouseInfo.getPointerInfo().getLocation().getX();
        double yA = MouseInfo.getPointerInfo().getLocation().getY();
    
        boolean isLeftToRight      = xB > xA;
        boolean isTopToBottom      = yB > yA;
        boolean overRun            = Math.random() < 0.2d;
        int     numberOfDeviations = (int) Math.floor(0.001 * Math.exp(Math.random() * 8.5)); //Excel = ARRONDI.INF(0,001*EXP(B1*8,5);0) => 0 à 5 avec 80% de probabilité 1
    
        System.out.println("numberOfDeviations = " + numberOfDeviations);
    
        List<Point> pointList = new ArrayList<>();
        pointList.add(new Point((int) xA,
                                (int) yA));
    
        generateDeviationsPoints(xB, yB, xA, yA, isLeftToRight, numberOfDeviations, pointList);
    
        System.out.println("overRun = " + overRun);
        
        if (overRun) {
            pointList.add(new Point((int) (xB + (Math.random() * MAXIMUM_OVERRUN) - MAXIMUM_OVERRUN / 2),
                                    (int) (yB + (Math.random() * MAXIMUM_OVERRUN) - MAXIMUM_OVERRUN / 2)));
        }
    
        pointList.add(new Point(xB, yB));
        
        System.out.println("pointList = {" + pointList.stream().map(point -> "[" + point.getX() + ", " + point.getY() + "]").collect(Collectors.joining(", ")) + "}");
    
        List<Point> mousePositions = generateMousePositions(pointList);
        
        System.out.println("mousePositions.size() = " + mousePositions.size());
        
        mousePositions.forEach(point -> {
    
            //            Graphics g = painter.getWhiteBoardGraphics();
            //            g.drawLine((int) point.getX(),
            //                       (int) point.getY(),
            //                       (int) point.getX(),
            //                       (int) point.getY());
            //            painter.repaintWhiteBoard();
    
            robot.mouseMove((int) point.getX(),
                            (int) point.getY());
            //            try {
            //                Thread.sleep((long) 0.0);
            //            }
            //            catch (InterruptedException e) {
            //                System.out.println("[ERROR] " + e.getMessage());
            //            }
        });
    }
    
    public void shift(int offsetX, int offsetY) {
        
        Point point = MouseInfo.getPointerInfo().getLocation();
        move((int) point.getX() + offsetX, (int) point.getY() + offsetY);
    }
    
    public void scrollUp(int scrollUp) {
        
        for (int ignore = 0; ignore < scrollUp; ignore++) {
            robot.mouseWheel(-1);
            robot.delay(200);
        }
    }
    
    public void scrollDown(int scrollDown) {
        
        for (int ignore = 0; ignore < scrollDown; ignore++) {
            robot.mouseWheel(1);
            robot.delay(200);
        }
    }
    
    //        xO = xC + X cos θ
    //        yO = yC + X sin θ
    //
    //        avec X déplacement positif ou négatif le long de la droite.
    //        avec θ = angle de la droite / à l'axe
    //        avec θ = tan-1(m)
    //        avec m = coefficient directeur de la droite d'équation y = mx + p
    
    //    double teta = Math.atan(mp);
    //
    //    int side = 1;
    //        for (int index = 1; index < pointList.size(); index++) {
    //        Point a = pointList.get(index - 1);
    //        Point b = pointList.get(index);
    //
    //        double dAB     = Math.sqrt(Math.pow(a.getX() - b.getX(), 2) + Math.pow(a.getY() - b.getY(), 2));
    //        double dCOalea = Math.random() * dAB * 0.2;
    //        double dCO     = dAB + (dCOalea - dCOalea / 2);
    //
    //        // soit C le milieu de AB
    //        double dxAxB = Math.abs(a.getX() - b.getX());
    //        double xC    = isLeftToRight ? a.getX() + dxAxB / 2 : a.getX() - dxAxB / 2;
    //        double yC    = (mp * (xC - a.getX())) + a.getY();
    //
    //        // O le futur centre du cercle
    //        double xO = xC + dCO * side * Math.cos(teta);
    //        double yO = yC + dCO * side * Math.sin(teta);
    //
    //        // la double équation cartésienne du cercle (en fait une équation pour chaque demi-cercle délimité par le diamètre horizontal) :
    //        // y = b +- √(r²-(x-a)²)
    //        // avec b = yO
    //        // avec a = xO
    //
    //        double rayon = Math.sqrt(Math.pow(a.getX() - xO, 2) + Math.pow(a.getY() - yO, 2)); // le rayon correspond à la distance AO
    //
    //        for (double x = a.getX(); x < b.getX(); x += 0.01) {
    //            double y = yO + Math.sqrt(Math.pow(rayon, 2) - Math.pow(x - xO, 2)) * -side;
    //            //                System.out.println("[TRACE] PRH : y = " + y);
    //            if (mousePositions.get(mousePositions.size() - 1).getX() != (int) Math.floor(x) ||
    //                mousePositions.get(mousePositions.size() - 1).getY() != (int) Math.floor(y)) {
    //                mousePositions.add(new Point((int) Math.floor(x), (int) Math.floor(y)));
    //            }
    //        }
    //
    //        System.out.println("mousePositions.size() = " + mousePositions.size());
    //
    //        mousePositions.forEach(point -> {
    //            //                System.out.println("point = " + point);
    //            robot.mouseMove((int) point.getX() - DECALAGE,
    //                            (int) point.getY() - DECALAGE);
    //            robot.delay(1);
    //        });
    //
    //        side = -side;
    //    }
    
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
            
            //                    if (Math.random() < 0.4) { y += 2; }
            //                    Point point = new Point((int) Math.floor(x), (int) Math.floor(y));
            mousePositions.add(pointAtDistanceOnLine.toRoundedPoint());
        }
    }
    
    private void zoom(int steps, boolean out) {
        
        //        helper.robot.delay(200);
        
        int wheelAmt = out ? 1 : -1;
        
        for (int j = 1; j <= steps; j++) {
            
            //            helper.robot.keyPress(KeyEvent.VK_CONTROL);
            
            for (int i = 1; i <= 5; i++) {
                
                robot.mouseWheel(wheelAmt);
                robot.delay(20);
            }
            //            helper.robot.keyRelease(KeyEvent.VK_CONTROL);
        }
    }
    
    private void release(int iButtonMask, int i_delay, boolean stopMotion) {
        
        if (stopMotion) {robot.delay(250);}
        robot.mouseRelease(iButtonMask);
        robot.delay(i_delay);
    }
    
    private void moveAndPress(int iButtonMask, int iX_start, int iY_start, int i_delay) {
        
        robot.mouseMove(iX_start, iY_start);
        robot.delay(i_delay);
        robot.mousePress(iButtonMask);
        robot.delay(i_delay);
    }
    
    private boolean minimalDistance(double x, double y, int oldX, int oldY) {
        
        boolean xCheck = Math.abs(Math.abs(oldX) - Math.abs(x)) >= Constants.i_DRAG_SPACE;
        boolean yCheck = Math.abs(Math.abs(oldY) - Math.abs(y)) >= Constants.i_DRAG_SPACE;
        
        return xCheck || yCheck;
    }
}
