package fr.ksuto.prh;

import fr.ksuto.prh.peripherals.Keyboard;
import fr.ksuto.prh.peripherals.Mouse;
import fr.ksuto.prh.peripherals.MousePosition;
import fr.ksuto.prh.peripherals.Peripheral;
import fr.ksuto.prh.peripherals.Screen;

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
    
    public static void delay(long ms) {
        
        Peripheral.delay(ms);
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
}
