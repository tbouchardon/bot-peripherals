package fr.ksuto.prh.peripherals;

import fr.ksuto.prh.tools.Debug;

import java.awt.*;

import javax.swing.*;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Created by TBO on 15/07/2016.
 */
@SuppressWarnings("unused")
@Singleton
public class MousePosition {
    
    public  boolean waitIfUserActive = false;
    @Inject
    private Screen  screen;
    private int     xPos;
    private int     yPos;
    private Robot   robot            = new Robot();
    
    public MousePosition() throws AWTException {
        
        updateMousePosition();
    }
    
    //    MousePosition(int xPos, int yPos) throws AWTException {
    //
    //        this.xPos = xPos;
    //        this.yPos = yPos;
    //    }
    
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
    
        Point point = MouseInfo.getPointerInfo().getLocation();
    
        return point.x == this.xPos && point.y == this.yPos;
    }
    
    public void waitIfUserActive() {
        
        if (!waitIfUserActive) { return; }
        
        Debug.sout();
        
        while (hasMoved()) {
            updateMousePosition();
            
            JFrame frame = new JFrame("test");
            frame.setAlwaysOnTop(true);
            frame.setUndecorated(true);
            frame.setPreferredSize(new Dimension(150, 90));
            frame.setBackground(new Color(0, 0, 0, 100));
            frame.getContentPane().setLayout(new FlowLayout(FlowLayout.CENTER, 2, 2));
            frame.setLocation(screen.SCREEN_WIDTH / 2 - 75, screen.SCREEN_HEIGHT / 2 - 45);
            
            JLabel waiting = new JLabel("<html><body>Activity detected :<br>waiting...</body></html>", SwingConstants.CENTER);
            waiting.setPreferredSize(new Dimension(148, 40));
            waiting.setFont(waiting.getFont().deriveFont(15f));
            waiting.setForeground(new Color(255, 225, 225));
            
            JLabel countDown = new JLabel("5", SwingConstants.CENTER);
            countDown.setPreferredSize(new Dimension(148, 40));
            countDown.setFont(countDown.getFont().deriveFont(50f));
            countDown.setForeground(new Color(255, 225, 225));
            
            frame.add(waiting);
            frame.add(countDown);
            frame.pack();
            frame.setVisible(true);
            
            for (int i = 5; i != 0; i--) {
                
                if (hasMoved()) {
                    updateMousePosition();
                    i = 5;
                }
                
                countDown.setText(String.valueOf(i));
                robot.delay(1000);
            }
            
            frame.dispose();
        }
    }
}
