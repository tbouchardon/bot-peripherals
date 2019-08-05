package net.ddns.ksuto.prh;

import net.ddns.ksuto.prh.peripherals.Keyboard;
import net.ddns.ksuto.prh.peripherals.Mouse;
import net.ddns.ksuto.prh.peripherals.MousePosition;
import net.ddns.ksuto.prh.peripherals.Screen;

import java.awt.*;

import com.google.inject.Inject;

/**
 * Created by TBO on 15/07/2016.
 */
@SuppressWarnings({"unused", "DefaultFileTemplate"})
public class PeripheralRobotHelper {
    
    public Robot robot = new Robot();
    
    @Inject
    private MousePosition mousePosition;
    @Inject
    private Screen        screen;
    @Inject
    private Mouse         mouse;
    @Inject
    private Keyboard      keyboard;
    
    public PeripheralRobotHelper() throws AWTException {}
    
    public Keyboard getKeyboard() {
        
        return keyboard;
    }
    
    public Mouse getMouse() {
        
        return mouse;
    }
    
    public Screen getScreen() {
        
        return screen;
    }
}
