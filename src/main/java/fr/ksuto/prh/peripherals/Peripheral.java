package fr.ksuto.prh.peripherals;

import fr.ksuto.commons.PropertiesLoader;

import java.awt.*;
import java.util.Properties;
import java.util.Random;

/**
 * Created by TBO!!! on 15/07/2016.
 */
public class Peripheral {
    
    protected int i_DELAY;
    
    protected Properties properties;
    Robot robot = new Robot();
    
    Peripheral() throws AWTException {
        
        this.properties = PropertiesLoader.load("prh");
        this.i_DELAY = Integer.parseInt(properties.getProperty("ksuto.prh.peripherals.delay", "100"));
    }
    
    public static int about(int number, int moreOrLess) {
        
        return number + new Random().nextInt(moreOrLess * 2) - moreOrLess;
    }
    
    public static int about(int number) {
        
        return about(number, (int) (number * 0.2));
    }
    
    public static void delay(double ms) {
        
        long first  = System.nanoTime();
        long second = System.nanoTime();
        
        while (second - first < ms * 1000000) {
            second = System.nanoTime();
        }
    }
    
    public static void delay(int ms) {
        
        delay((double) ms);
    }
}
