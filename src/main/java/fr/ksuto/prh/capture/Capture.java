package fr.ksuto.prh.capture;

import fr.ksuto.prh.peripherals.Screen;

import java.awt.*;

/**
 * Point d'entrée unique pour capturer l'écran. Le moyen de capture est interchangeable via {@link #setBackend(CaptureBackend)}.
 */
public final class Capture {

    private static volatile CaptureBackend backend = new RobotCaptureBackend();

    private Capture() {}

    public static Frame zone(Rectangle screenZone) {

        return backend.capture(screenZone);
    }

    public static Frame zone(int x, int y, int width, int height) {

        return zone(new Rectangle(x, y, width, height));
    }

    public static Frame zone(Screen.Zone zone) {

        return zone(zone.getRectangle());
    }

    public static Frame screen() {

        return zone(Screen.Zone.ALL);
    }

    public static CaptureBackend getBackend() {

        return backend;
    }

    public static void setBackend(CaptureBackend captureBackend) {

        backend = captureBackend;
    }
}
