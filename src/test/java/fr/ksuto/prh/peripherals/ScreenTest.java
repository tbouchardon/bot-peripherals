package fr.ksuto.prh.peripherals;

import fr.ksuto.prh.capture.Frame;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScreenTest {

    @Test
    void zonesAreComparedOnPartialCaptures() throws Exception {

        // Captures de la seule zone surveillée (100, 50, 20x20) : les coordonnées de la zone sont celles de l'écran
        BufferedImage before = new BufferedImage(20, 20, BufferedImage.TYPE_INT_RGB);
        BufferedImage after  = new BufferedImage(20, 20, BufferedImage.TYPE_INT_RGB);
        after.setRGB(15, 5, 0xFFFFFF);
        Rectangle area = new Rectangle(100, 50, 20, 20);
        Screen    screen = new Screen();

        assertTrue(screen.zoneHasChanged(Frame.of(before, area), Frame.of(after, area), new Screen.Zone(110, 120, 50, 60)));
        assertFalse(screen.zoneHasChanged(Frame.of(before, area), Frame.of(after, area), new Screen.Zone(100, 110, 50, 70)));
    }
}
