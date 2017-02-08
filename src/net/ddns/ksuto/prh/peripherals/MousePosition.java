package net.ddns.ksuto.prh.peripherals;

import java.awt.*;

/**
 * Created by TBO on 15/07/2016.
 */
@SuppressWarnings("unused")
public class MousePosition {
    
    private int xPos;
    private int yPos;
    
    public MousePosition() {
        
        updateMousePosition();
    }
    
    MousePosition(int xPos, int yPos) {
        
        this.xPos = xPos;
        this.yPos = yPos;
    }
    
    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object other) {
    
        if (other == null) { return false; }
        if (other == this) { return true; }
        if (!(other instanceof MousePosition)) { return false; }
        
        //noinspection UnnecessaryLocalVariable
        boolean hasMoved = (xPos != ((MousePosition) other).xPos) || (yPos != ((MousePosition) other).yPos);
        
        return hasMoved;
    }
    
    public void updateMousePosition() {
        
        Point point = MouseInfo.getPointerInfo().getLocation();
        xPos = point.x;
        yPos = point.y;
    }
    
    public boolean hasMoved() {
        
        MousePosition currentMousePosition = new MousePosition();
        
        return this.equals(currentMousePosition);
    }
}
