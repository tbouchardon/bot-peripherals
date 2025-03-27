package fr.ksuto.prh.research.paralelism;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class CaptureScheduler {
    
    private final int                            FPS_PER_THREAD       = 10;
    private final int                            THREADS              = 4;
    private       AtomicReference<BufferedImage> imageAtomicReference = new AtomicReference<>();
    private       AtomicReference<Integer>       captureIndex         = new AtomicReference<>(0);
    private       ScheduledExecutorService       scheduledThreadPool  = Executors.newScheduledThreadPool(THREADS);
    
    private static long fractionToNanos(long a, long b) {
        
        return a * 1000_000_000L / b;
    }
    
    public void init(Rectangle captureSize) throws Exception {
        
        for (int i = 0; i < THREADS; i++) {
            scheduledThreadPool.scheduleAtFixedRate(
                    new CaptureWorker(new Robot(), captureSize, imageAtomicReference, captureIndex),
                    fractionToNanos(i, THREADS),
                    fractionToNanos(1, FPS_PER_THREAD),
                    TimeUnit.NANOSECONDS);
        }
    }
    
    public void init() throws Exception {
        
        init(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
    }
    
    public void stop() {
        
        scheduledThreadPool.shutdown();
    }
    
    public Integer getCaptureIndex() {
        
        return captureIndex.get();
    }
    
    public BufferedImage getLastImage() {
        
        return imageAtomicReference.get();
    }
    
    public static class CaptureWorker implements Runnable {
        
        Robot                          robot;
        Rectangle                      captureSize;
        AtomicReference<BufferedImage> image;
        AtomicReference<Integer>       loopIndex;
        
        public CaptureWorker(Robot robot, Rectangle captureSize, AtomicReference<BufferedImage> image, AtomicReference<Integer> loopIndex) {
            
            this.robot = robot;
            this.captureSize = captureSize;
            this.image = image;
            this.loopIndex = loopIndex;
        }
        
        @Override
        public void run() {
            
            image.set(robot.createScreenCapture(captureSize));
            loopIndex.set(loopIndex.get() + 1);
        }
    }
}
