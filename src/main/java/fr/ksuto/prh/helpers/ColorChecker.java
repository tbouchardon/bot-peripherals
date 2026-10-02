package fr.ksuto.prh.helpers;

import fr.ksuto.prh.capture.Capture;
import fr.ksuto.prh.capture.Frame;

import java.awt.*;

public class ColorChecker {
    
    int           x     = 1;
    int           y     = 1;
    Frame         capturedScreen;
    int           color;
    
    public ColorChecker() throws AWTException {}
    
    public ColorChecker setCoordinates(int x, int y) {
        
        this.x = x;
        this.y = y;
        
        return this;
    }
    
    public boolean check() {
        
        if (capturedScreen == null) { capturedScreen = Capture.screen(); }
        
        int iCapturedRGB = capturedScreen.rgb(x, y);
        // Debug.sysOut("Peripheral > (" + x + ", " + y + ") Searching : " + color + ", found : " + iCapturedRGB + ".");
        return (color == iCapturedRGB);
    }
    
    public ColorChecker setCapturedScreen(Frame biCapturedScreen) {
        
        this.capturedScreen = biCapturedScreen;
        
        return this;
    }
    
    public ColorChecker setColor(int iColor) {
        
        this.color = iColor;
        
        return this;
    }
}
