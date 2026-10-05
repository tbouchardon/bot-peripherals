package fr.ksuto.prh.capture;

import java.awt.Rectangle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Capture par un moyen rapide (DXGI), avec repli sur un moyen sûr (Robot) quand il échoue : session verrouillée,
 * changement de mode d'affichage, pilote... Le moyen rapide est réessayé après {@value #RETRY_DELAY} ms.
 */
public class FallbackCaptureBackend implements CaptureBackend {

    private static final Logger logger = LoggerFactory.getLogger(FallbackCaptureBackend.class);

    static final long RETRY_DELAY = 5000;

    private final CaptureBackend primary;
    private final CaptureBackend fallback;

    private volatile long failedUntil = 0;

    public FallbackCaptureBackend(CaptureBackend primary, CaptureBackend fallback) {

        this.primary = primary;
        this.fallback = fallback;
    }

    @Override
    public Frame capture(Rectangle screenZone) {

        if (System.currentTimeMillis() >= failedUntil) {
            try {
                Frame frame = primary.capture(screenZone);
                if (failedUntil != 0) {
                    failedUntil = 0;
                    logger.info("Capture {} rétablie", name(primary));
                }
                return frame;
            }
            catch (RuntimeException e) {
                if (failedUntil == 0) {logger.warn("Capture {} en échec, repli sur {} : {}", name(primary), name(fallback), e.getMessage());}
                failedUntil = System.currentTimeMillis() + RETRY_DELAY;
            }
        }
        return fallback.capture(screenZone);
    }

    private static String name(CaptureBackend backend) {

        return backend.getClass().getSimpleName();
    }
}
