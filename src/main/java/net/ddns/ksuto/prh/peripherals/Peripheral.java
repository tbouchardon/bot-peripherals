package net.ddns.ksuto.prh.peripherals;

import java.awt.*;

/**
 * Created by TBO!!! on 15/07/2016.
 */
public class Peripheral {
    
    Robot robot = new Robot();
    
    Peripheral() throws AWTException {}
    
    void delay(int ms) {
        
        robot.delay(ms);
    }
}
