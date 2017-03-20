package net.ddns.ksuto.prh.peripherals;

import net.ddns.ksuto.prh.TBoPeripheralRobotHelper;
import net.ddns.ksuto.prh.properties.Constants;
import net.ddns.ksuto.prh.tools.Debug;

import java.awt.*;
import java.awt.event.InputEvent;
import java.util.ArrayList;

/**
 * Created by thomas.bouchardon on 26/11/2015!
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class Mouse extends Peripheral {
    
    private TBoPeripheralRobotHelper helper;
    
    public Mouse(TBoPeripheralRobotHelper TBoPeripheralRobotHelper) {
        
        this.helper = TBoPeripheralRobotHelper;
        this.constants = helper.getConstants();
        this.robot = helper.robot;
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
    
    public void DragTop2Bottom(int iDistance, int iButtonMask) throws AWTException {
        
        Robot robot = new Robot();
        robot.mouseMove(helper.getScreen().iX_START, helper.getScreen().iY_START);
        robot.delay(Constants.i_DELAY);
        robot.mousePress(iButtonMask);
        robot.delay(Constants.i_DELAY);
        for (int iTB = helper.getScreen().iY_START; iTB < helper.getScreen().iY_START + iDistance; iTB += Constants.i_DRAG_SPACE) {
            robot.mouseMove(helper.getScreen().iX_START, iTB);
            robot.delay(Constants.i_DRAG_DELAY);
        }
        robot.mouseRelease(iButtonMask);
        robot.delay(Constants.i_DELAY);
    }
    
    public void DragBottom2Top(int iDistance, int iButtonMask) throws AWTException {
        
        Robot robot = new Robot();
        robot.mouseMove(helper.getScreen().iX_START, helper.getScreen().iY_START);
        robot.delay(Constants.i_DELAY);
        robot.mousePress(iButtonMask);
        robot.delay(Constants.i_DELAY);
        for (int iBT = helper.getScreen().iY_START; iBT > helper.getScreen().iY_START - iDistance; iBT -= Constants.i_DRAG_SPACE) {
            robot.mouseMove(helper.getScreen().i_SCREEN_WIDTH / 2, iBT);
            robot.delay(Constants.i_DRAG_DELAY);
        }
        robot.mouseRelease(iButtonMask);
        robot.delay(Constants.i_DELAY);
    }
    
    public void DragLeft2Right(int iDistance, int iButtonMask) throws AWTException {
        
        Robot robot = new Robot();
        robot.mouseMove(helper.getScreen().iX_START, helper.getScreen().iY_START);
        robot.delay(Constants.i_DELAY);
        robot.mousePress(iButtonMask);
        robot.delay(Constants.i_DELAY);
        for (int iLR = helper.getScreen().iX_START; iLR < helper.getScreen().iX_START + iDistance; iLR += Constants.i_DRAG_SPACE) {
            robot.mouseMove(iLR, helper.getScreen().iY_START);
            robot.delay(Constants.i_DRAG_DELAY);
        }
        robot.mouseRelease(iButtonMask);
        robot.delay(Constants.i_DELAY);
    }
    
    public void DragRight2Left(int iDistance, int iButtonMask) throws AWTException {
        
        Robot robot = new Robot();
        robot.mouseMove(helper.getScreen().iX_START, helper.getScreen().iY_START);
        robot.delay(Constants.i_DELAY);
        robot.mousePress(iButtonMask);
        robot.delay(Constants.i_DELAY);
        for (int iLR = helper.getScreen().iX_START; iLR > helper.getScreen().iX_START - iDistance; iLR -= Constants.i_DRAG_SPACE) {
            robot.mouseMove(iLR, helper.getScreen().iY_START);
            robot.delay(Constants.i_DRAG_DELAY);
        }
        robot.mouseRelease(iButtonMask);
        robot.delay(Constants.i_DELAY);
    }
    
    public void Drag(int iX_start, int iX_end, int iY_start, int iY_end, int iButtonMask) throws AWTException {
        
        Robot robot = new Robot();
        robot.mouseMove(iX_start, iY_start);
        robot.delay(1000);
        robot.mousePress(iButtonMask);
        robot.delay(1000);
        while ((iX_start != iX_end) || (iY_start != iY_end)) {
            if (iX_start < iX_end) { iX_start++; }
            if (iX_start > iX_end) { iX_start--; }
            if (iY_start < iY_end) { iY_start++; }
            if (iY_start > iY_end) { iY_start--; }
            robot.mouseMove(iX_start, iY_start);
            Debug.sout(iX_start + " " + iY_start);
            robot.delay(Constants.i_DRAG_DELAY);
        }
        robot.mouseRelease(iButtonMask);
        robot.delay(1000);
    }
    
    public void clickLeft(int x, int y) {
        
        helper.waitIfUserActive();
    
        Debug.sout(x + ", " + y + ")");
        
        robot.mouseMove(x, y);
        helper.mousePosition.updateMousePosition();
        clickLeft();
    }
    
    public void clickRight(int x, int y) {
        
        helper.waitIfUserActive();
    
        Debug.sout(x + ", " + y + ")");
        
        robot.mouseMove(x, y);
        helper.mousePosition.updateMousePosition();
        clickRight();
    }
    
    public boolean clickThing(String[] sImage) {
        
        helper.waitIfUserActive();
    
        Debug.sout("String[] sImage)");
        
        return clickThing(sImage, 0, 0);
    }
    
    public boolean clickThing(String[] sImage, int xOffset, int yOffset) {
        
        helper.waitIfUserActive();
    
        Debug.sout("sImage, " + xOffset + ", " + yOffset + ")");
    
        Debug.sout("Trying to click '" + sImage[0] + "', Offsets : x=" + xOffset + ", y=" + yOffset);
        
        ArrayList<int[]> alFound;
        
        alFound = helper.getScreen().scanFor(sImage);
        if (!alFound.isEmpty()) {
            Debug.sout(sImage[0] + " Found");
            for (int[] aiCoords : alFound) {
                clickLeft(aiCoords[0] + 3 + xOffset, aiCoords[1] + 3 + yOffset);
            }
            return true;
        }
        return false;
    }
}
