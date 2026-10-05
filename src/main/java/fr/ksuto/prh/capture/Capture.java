package fr.ksuto.prh.capture;

import fr.ksuto.prh.peripherals.Screen;

import java.awt.*;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Point d'entrée unique pour capturer l'écran. Le moyen de capture est interchangeable via {@link #setBackend(CaptureBackend)}.
 * <p>
 * Par défaut, choisi à la première capture : sous Windows, DXGI Desktop Duplication (50 à 100 fois plus rapide que Robot
 * sur une petite zone, voir docs/performances-capture.md) avec repli sur Robot ; ailleurs, Robot. La propriété système
 * {@code ksuto.capture} impose un moyen : {@code dxgi}, {@code gdi} ou {@code robot}.
 */
public final class Capture {

    private static final Logger logger = LoggerFactory.getLogger(Capture.class);

    private static volatile CaptureBackend backend;

    private Capture() {}

    public static Frame zone(Rectangle screenZone) {

        return getBackend().capture(screenZone);
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

        CaptureBackend current = backend;
        if (current == null) {
            synchronized (Capture.class) {
                if (backend == null) {backend = defaultBackend();}
                current = backend;
            }
        }
        return current;
    }

    private static CaptureBackend defaultBackend() {

        String choice  = System.getProperty("ksuto.capture", "").toLowerCase(Locale.ROOT);
        boolean windows = System.getProperty("os.name", "").startsWith("Windows");
        RobotCaptureBackend robot = new RobotCaptureBackend();

        if (choice.equals("robot") || (!windows && choice.isEmpty())) {return robot;}
        try {
            CaptureBackend chosen = choice.equals("gdi") ? new GdiCaptureBackend() : new DxgiCaptureBackend();
            logger.info("Capture d'écran : {}, repli sur Robot", chosen.getClass().getSimpleName());
            return new FallbackCaptureBackend(chosen, robot);
        }
        catch (RuntimeException | LinkageError e) {
            logger.warn("Capture rapide indisponible, capture par Robot : {}", e.getMessage());
            return robot;
        }
    }

    public static void setBackend(CaptureBackend captureBackend) {

        backend = captureBackend;
    }
}
