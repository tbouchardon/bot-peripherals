package fr.ksuto.prh.capture;

import java.awt.*;

/**
 * Moyen de capturer l'écran : {@link RobotCaptureBackend} par défaut, une implémentation native (DXGI) plus tard.
 * Une implémentation doit pouvoir être appelée depuis plusieurs threads en parallèle.
 */
public interface CaptureBackend {

    Frame capture(Rectangle screenZone);
}
