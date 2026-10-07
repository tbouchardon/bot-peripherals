package fr.ksuto.prh.helpers;

import fr.ksuto.prh.capture.Frame;

import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ColorCheckerTest {

    @Test
    void comparesColorsWithoutAlpha() throws Exception {

        BufferedImage image = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
        image.setRGB(2, 1, 0x123456);
        ColorChecker checker = new ColorChecker().setCapturedScreen(Frame.of(image)).setCoordinates(2, 1);

        assertTrue(checker.setColor(0x123456).check(), "couleur sans alpha face à une capture opaque");
        assertTrue(checker.setColor(0xFF123456).check());
        assertFalse(checker.setColor(0x123457).check());
    }
}
