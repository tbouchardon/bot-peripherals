package fr.ksuto.prh.tools;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

public class RandomUtils {
    
    Random rand;
    
    public RandomUtils() {
        
        try {
            this.rand = SecureRandom.getInstanceStrong();
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
    
    public long getPositiveLong() {
        
        long l;
        
        do {
            l = rand.nextLong();
        } while (l == Long.MIN_VALUE);
        
        return Math.abs(l);
    }
}
