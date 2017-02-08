package net.ddns.ksuto.prh.peripherals;

import net.ddns.ksuto.prh.properties.Constants;

import java.awt.*;

/**
 * Created by TBO!!! on 15/07/2016.
 */
class Peripheral {
    
    Constants constants;
    Robot     robot;
    
    void delay(int ms) {
        
        robot.delay(ms);
    }
}
