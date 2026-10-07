package fr.ksuto.prh.capture;

import fr.ksuto.prh.capture.Capture;
import fr.ksuto.prh.capture.Frame;

import java.awt.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Capture l'écran en continu sur plusieurs threads et garde la dernière image.
 * Le débit augmente avec le nombre de threads, mais chaque image a toujours le délai d'une capture.
 */
public class CaptureScheduler {

    private final int                      FPS_PER_THREAD      = 10;
    private final int                      THREADS             = 4;
    private final AtomicReference<Frame>   lastFrame           = new AtomicReference<>();
    private final AtomicInteger            captureIndex        = new AtomicInteger(0);
    private final ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(THREADS);

    private static long fractionToNanos(long a, long b) {

        return a * 1000_000_000L / b;
    }

    public void init(Rectangle captureSize) {

        for (int i = 0; i < THREADS; i++) {
            scheduledThreadPool.scheduleAtFixedRate(
                    new CaptureWorker(captureSize, lastFrame, captureIndex),
                    fractionToNanos(i, THREADS),
                    fractionToNanos(1, FPS_PER_THREAD),
                    TimeUnit.NANOSECONDS);
        }
    }

    public void init() {

        init(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
    }

    public void stop() {

        scheduledThreadPool.shutdown();
    }

    public Integer getCaptureIndex() {

        return captureIndex.get();
    }

    public Frame getLastFrame() {

        return lastFrame.get();
    }

    public static class CaptureWorker implements Runnable {

        private final Rectangle              captureSize;
        private final AtomicReference<Frame> frame;
        private final AtomicInteger          loopIndex;

        public CaptureWorker(Rectangle captureSize, AtomicReference<Frame> frame, AtomicInteger loopIndex) {

            this.captureSize = captureSize;
            this.frame = frame;
            this.loopIndex = loopIndex;
        }

        @Override
        public void run() {

            frame.set(Capture.zone(captureSize));
            loopIndex.incrementAndGet();
        }
    }
}
