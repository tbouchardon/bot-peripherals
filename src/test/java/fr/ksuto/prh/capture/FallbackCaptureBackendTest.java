package fr.ksuto.prh.capture;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FallbackCaptureBackendTest {

    private static final Rectangle ZONE = new Rectangle(0, 0, 1, 1);

    /**
     * Moyen de capture factice : une image d'un pixel de la couleur donnée, ou une erreur tant qu'il est en panne.
     */
    private static final class FakeBackend implements CaptureBackend {

        final int           color;
        final AtomicInteger calls  = new AtomicInteger();
        volatile boolean    broken = false;

        FakeBackend(int color) {

            this.color = color;
        }

        @Override
        public Frame capture(Rectangle screenZone) {

            calls.incrementAndGet();
            if (broken) {throw new IllegalStateException("AcquireNextFrame a échoué");}
            BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
            image.setRGB(0, 0, color);
            return Frame.of(image, screenZone);
        }
    }

    @Test
    void usesThePrimaryBackendWhileItWorks() {

        FakeBackend dxgi  = new FakeBackend(0x112233);
        FakeBackend robot = new FakeBackend(0x445566);

        assertEquals(0x112233, new FallbackCaptureBackend(dxgi, robot).capture(ZONE).rgb(0, 0) & 0xFFFFFF);
        assertEquals(0, robot.calls.get());
    }

    @Test
    void fallsBackThenWaitsBeforeRetrying() {

        FakeBackend            dxgi    = new FakeBackend(0x112233);
        FakeBackend            robot   = new FakeBackend(0x445566);
        FallbackCaptureBackend capture = new FallbackCaptureBackend(dxgi, robot);
        dxgi.broken = true;

        assertEquals(0x445566, capture.capture(ZONE).rgb(0, 0) & 0xFFFFFF, "repli sur Robot");
        assertEquals(0x445566, capture.capture(ZONE).rgb(0, 0) & 0xFFFFFF);
        assertEquals(1, dxgi.calls.get(), "pas de nouvel essai avant le délai : pas de coût d'échec à chaque capture");
    }

    @Test
    void defaultBackendOutsideWindowsIsRobot() {

        if (System.getProperty("os.name", "").startsWith("Windows")) {return;}
        assertInstanceOf(RobotCaptureBackend.class, Capture.getBackend());
    }
}
