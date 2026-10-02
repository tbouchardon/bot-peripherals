package fr.ksuto.prh.capture;

import java.awt.*;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RgbTest {

    @Test
    void decodesComponents() {

        int rgb = new Color(12, 200, 255).getRGB();

        assertEquals(12, Rgb.red(rgb));
        assertEquals(200, Rgb.green(rgb));
        assertEquals(255, Rgb.blue(rgb));
    }

    @Test
    void encodesLikeAwtColor() {

        assertEquals(new Color(12, 200, 255).getRGB(), Rgb.of(12, 200, 255));
        assertEquals(Color.WHITE.getRGB(), Rgb.ARGB_WHITE);
        assertEquals(Color.BLACK.getRGB(), Rgb.ARGB_BLACK);
        assertEquals(Color.RED.getRGB(), Rgb.ARGB_RED);
        assertEquals(Color.GREEN.getRGB(), Rgb.ARGB_GREEN);
        assertEquals(Color.BLUE.getRGB(), Rgb.ARGB_BLUE);
    }

    @Test
    void isCloseUsesStrictTolerancePerComponent() {

        int reference = Rgb.of(100, 100, 100);

        assertTrue(Rgb.isClose(Rgb.of(119, 81, 100), reference, 20));
        assertFalse(Rgb.isClose(Rgb.of(120, 100, 100), reference, 20), "écart égal à la précision : rejeté (comportement historique)");
        assertFalse(Rgb.isClose(Rgb.of(100, 100, 80), reference, 20));
        assertTrue(Rgb.isClose(Rgb.of(100, 100, 100), 100, 100, 100, 1));
    }
}
