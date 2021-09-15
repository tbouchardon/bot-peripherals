package fr.ksuto.prh.peripherals;

import java.awt.*;

/**
 * Created by TBO!!! on 15/07/2016.
 */
public class Peripheral {
    
    Robot robot = new Robot();
    
    Peripheral() throws AWTException {}
    
    public static void delay(int ms) {
        
        delay((long) ms);
    }
    
    public static void delay(long ms) {
        
        long first  = System.nanoTime();
        long second = System.nanoTime();
        
        while (second - first < ms * 1000000) {
            second = System.nanoTime();
        }
    }
}
