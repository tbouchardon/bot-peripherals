package net.ddns.ksuto.prh;

import net.ddns.ksuto.prh.peripherals.Keyboard;
import net.ddns.ksuto.prh.peripherals.Mouse;
import net.ddns.ksuto.prh.peripherals.MousePosition;
import net.ddns.ksuto.prh.peripherals.Screen;
import net.ddns.ksuto.prh.properties.Constants;

import java.awt.*;

import javax.swing.*;

/**
 * Created by TBO on 15/07/2016.
 */
@SuppressWarnings({"unused", "DefaultFileTemplate"})
public class TBoPeripheralRobotHelper {
    
    public final MousePosition mousePosition = new MousePosition();
    
    public Robot robot;
    
    private Constants constants;
    private Screen    screen;
    private Mouse     mouse;
    private Keyboard  keyboard;
    private boolean waitIfUserActive = false;
    
    public TBoPeripheralRobotHelper() throws AWTException {
        
        robot = new Robot();
        
        constants = new Constants();
        screen = new Screen(this);
        mouse = new Mouse(this);
        keyboard = new Keyboard(this);
    }
    
    public void waitIfUserActive() {
        
        if (!waitIfUserActive) { return; }
        
        System.out.println("Method : waitIfUserActive()");
        
        while (mousePosition.hasMoved()) {
            mousePosition.updateMousePosition();
            
            JFrame frame = new JFrame("test");
            frame.setAlwaysOnTop(true);
            frame.setUndecorated(true);
            frame.setPreferredSize(new Dimension(150, 90));
            frame.setBackground(new Color(0, 0, 0, 100));
            frame.getContentPane().setLayout(new FlowLayout(FlowLayout.CENTER, 2, 2));
            frame.setLocation(screen.i_SCREEN_WIDTH / 2 - 75, screen.i_SCREEN_HEIGHT / 2 - 45);
            
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
                
                if (mousePosition.hasMoved()) {
                    mousePosition.updateMousePosition();
                    i = 5;
                }
                
                countDown.setText(String.valueOf(i));
                robot.delay(1000);
            }
            
            frame.dispose();
        }
    }
    
    public Constants getConstants() {
        
        return constants;
    }
    
    public Keyboard getKeyboard() {
        
        return keyboard;
    }
    
    public Mouse getMouse() {
        
        return mouse;
    }
    
    public Screen getScreen() {
        
        return screen;
    }
    
    public void setWaitIfUserActive(boolean waitIfUserActive) {
        
        this.waitIfUserActive = waitIfUserActive;
    }
}
