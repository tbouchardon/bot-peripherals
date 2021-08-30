package fr.ksuto.prh.helpers;

import fr.ksuto.prh.peripherals.Screen;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ColorChecker {
    
    int           x     = 1;
    int           y     = 1;
    BufferedImage capturedScreen;
    int           color;
    Robot         robot = new Robot();
    
    public ColorChecker() throws AWTException {}
    
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
