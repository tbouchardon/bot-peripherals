package net.ddns.ksuto.prh.peripherals;

import net.ddns.ksuto.prh.entities.Picture;
import net.ddns.ksuto.prh.entities.Position;
import net.ddns.ksuto.prh.properties.Constants;
import net.ddns.ksuto.prh.tools.Debug;

import java.awt.*;
import java.awt.event.InputEvent;
import java.util.ArrayList;

import com.google.inject.Inject;

/**
 * Created by thomas.bouchardon on 26/11/2015!
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class Mouse extends Peripheral {
    
    public static final int LEFT  = InputEvent.BUTTON1_DOWN_MASK;
    public static final int RIGHT = InputEvent.BUTTON3_DOWN_MASK;
    
    public static final double Y_ADJUSTEMENT_FACTOR = 0.0;
    public static final double X_ADJUSTEMENT_FACTOR = 0.0;
    
    public final Dimension     dim_D           = new Dimension(Toolkit.getDefaultToolkit().getScreenSize());
    public final int           i_SCREEN_WIDTH  = (int) dim_D.getWidth();
    public final int           i_SCREEN_HEIGHT = (int) dim_D.getHeight();
    @Inject
    private      Screen        screen;
    @Inject
    private      MousePosition mousePosition;
    private      double        startX          = (i_SCREEN_WIDTH / 2d);
    private      double        startY          = (i_SCREEN_HEIGHT / 2d);
    
    Mouse() throws AWTException {
        
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
        
        //        System.out.println("angle = " + angle);
        double toRadians = Math.toRadians(angle);
        //        System.out.println("toRadians = " + toRadians);
        
        double xMod = toRadians % (2 * Math.PI) - Math.PI / 2;
        //        System.out.println("xMod = " + xMod);
        double yMod = toRadians % (2 * Math.PI) - Math.PI;
        //        System.out.println("yMod = " + yMod);
        
        double xFactor = -(2 / Math.PI * Math.abs(xMod) - 1);
        //        System.out.println("xFactor = " + xFactor);
        double yFactor = 2 / Math.PI * Math.abs(yMod) - 1;
        //        System.out.println("yFactor = " + yFactor);
        
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
            robot.mouseMove(screen.SCREEN_WIDTH / 2, iBT);
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
            Screen.PictureSearch pictureSearch = new Screen.PictureSearch()
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
        
        robot.mouseMove(x, y);
        robot.delay(Constants.i_DELAY);
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
