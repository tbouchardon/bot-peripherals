package fr.ksuto.prh.capture;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class FrameTest {

    private static BufferedImage randomImage(int type, int width, int height) {

        BufferedImage image  = new BufferedImage(width, height, type);
        Random        random = new Random(42);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image.setRGB(x, y, random.nextInt());
            }
        }
        return image;
    }

    private static void assertSamePixels(BufferedImage expected, Frame frame) {

        assertEquals(expected.getWidth(), frame.width());
        assertEquals(expected.getHeight(), frame.height());
        for (int y = 0; y < expected.getHeight(); y++) {
            for (int x = 0; x < expected.getWidth(); x++) {
                int rgb = expected.getRGB(x, y);
                assertEquals(rgb, frame.rgb(x, y), "rgb(" + x + ", " + y + ")");
                assertEquals(Rgb.red(rgb), frame.red(x, y));
                assertEquals(Rgb.green(rgb), frame.green(x, y));
                assertEquals(Rgb.blue(rgb), frame.blue(x, y));
            }
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {BufferedImage.TYPE_INT_RGB, BufferedImage.TYPE_INT_ARGB, BufferedImage.TYPE_3BYTE_BGR, BufferedImage.TYPE_4BYTE_ABGR,
                         BufferedImage.TYPE_INT_BGR})
    void rgbMatchesGetRgbForEveryImageType(int type) {

        BufferedImage image = randomImage(type, 37, 23);

        assertSamePixels(image, Frame.of(image));
    }

    @Test
    void readsIntRgbImagesWithoutCopy() {

        BufferedImage image = randomImage(BufferedImage.TYPE_INT_RGB, 10, 10);

        assertSame(image, Frame.of(image).image());
    }

    @Test
    void copiesSubImagesSharingTheirParentRaster() {

        BufferedImage parent = randomImage(BufferedImage.TYPE_INT_RGB, 50, 40);
        BufferedImage sub    = parent.getSubimage(7, 5, 20, 12);

        Frame frame = Frame.of(sub);

        assertNotSame(sub, frame.image());
        assertSame(sub, frame.source());
        assertSamePixels(sub, frame);
    }

    @Test
    void keepsScreenOrigin() {

        Frame frame = Frame.of(randomImage(BufferedImage.TYPE_INT_RGB, 4, 4), new Rectangle(120, 80, 4, 4));

        assertEquals(120, frame.x());
        assertEquals(80, frame.y());
        assertTrue(frame.contains(3, 3));
        assertFalse(frame.contains(4, 0));
        assertFalse(frame.contains(0, -1));
    }
}
