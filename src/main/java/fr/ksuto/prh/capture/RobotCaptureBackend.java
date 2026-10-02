package fr.ksuto.prh.capture;

import java.awt.*;

/**
 * Capture via {@link Robot}. Un Robot par thread : {@link Robot#createScreenCapture} est synchronisé,
 * un Robot partagé sérialiserait les captures parallèles (voir CaptureScheduler).
 */
public class RobotCaptureBackend implements CaptureBackend {

    private final ThreadLocal<Robot> robots = ThreadLocal.withInitial(RobotCaptureBackend::newRobot);

    private static Robot newRobot() {

        try {
            return new Robot();
        }
        catch (AWTException e) {
            throw new IllegalStateException("Impossible de créer un Robot (environnement sans écran ?)", e);
        }
    }

    @Override
    public Frame capture(Rectangle screenZone) {

        return Frame.of(robots.get().createScreenCapture(screenZone), screenZone);
    }
}
