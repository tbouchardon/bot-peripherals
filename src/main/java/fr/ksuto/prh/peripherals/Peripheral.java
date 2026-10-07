package fr.ksuto.prh.peripherals;

import fr.ksuto.commons.PropertiesLoader;

import java.awt.*;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.LockSupport;

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
    
    /**
     * @return {@code number} à plus ou moins {@code moreOrLess} près, au hasard ({@code number} si l'écart est nul)
     */
    public static int about(int number, int moreOrLess) {
        
        if (moreOrLess <= 0) {return number;}
        return number + ThreadLocalRandom.current().nextInt(moreOrLess * 2) - moreOrLess;
    }
    
    public static int about(int number) {
        
        return about(number, (int) (number * 0.2));
    }
    
    /**
     * Attend {@code ms} millisecondes, précisément mais sans occuper le processeur : le fil est suspendu, puis la dernière
     * milliseconde est attendue activement (la reprise d'un fil suspendu peut avoir une à deux millisecondes de retard).
     */
    public static void delay(double ms) {
        
        long end = System.nanoTime() + (long) (ms * 1_000_000);
        long remaining;
        while ((remaining = end - System.nanoTime()) > SPIN_NANOS) {
            LockSupport.parkNanos(remaining - SPIN_NANOS);
        }
        while (System.nanoTime() < end) {
            Thread.onSpinWait();
        }
    }
    
    /**
     * Fin d'attente active de {@link #delay(double)} : la précision de la reprise d'un fil suspendu.
     */
    static final long SPIN_NANOS = 1_500_000;
    
    public static void delay(int ms) {
        
        delay((double) ms);
    }
}
