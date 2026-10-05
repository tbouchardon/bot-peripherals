package fr.ksuto.prh.peripherals;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MouseMotionTest {

    @Test
    void durationGrowsWithDistanceLikeAHumanGesture() {

        Random random = new Random(1);
        for (int i = 0; i < 100; i++) {
            long short30 = Mouse.plannedDuration(30, random), mid100 = Mouse.plannedDuration(100, random), long900 = Mouse.plannedDuration(900, random);
            assertTrue(short30 >= 150 && short30 <= 300, "30 px : " + short30);
            assertTrue(mid100 >= 250 && mid100 <= 400, "100 px : " + mid100);
            assertTrue(long900 >= 450 && long900 <= 650, "900 px : " + long900 + " (1,4 à 2,3 s avant)");
        }
    }

    @Test
    void minimumJerkStartsAndEndsSlowly() {

        assertEquals(0, Mouse.minimumJerk(0), 1e-9);
        assertEquals(1, Mouse.minimumJerk(1), 1e-9);
        assertEquals(0.5, Mouse.minimumJerk(0.5), 1e-9);
        assertTrue(Mouse.minimumJerk(0.1) < 0.01, "départ lent");
        assertTrue(Mouse.minimumJerk(0.9) > 0.99, "arrivée lente");
        assertEquals(1, Mouse.minimumJerk(1.5), 1e-9, "borné");
    }

    @Test
    void positionAlongThePathFollowsItsLength() {

        List<Point> path = new ArrayList<>();
        for (int x = 0; x <= 100; x++) {path.add(new Point(x, 0));}
        for (int y = 1; y <= 100; y++) {path.add(new Point(100, y));} // coude : 200 px en tout
        double[] lengths = Mouse.cumulativeLengths(path);

        assertEquals(200, lengths[lengths.length - 1], 1e-9);
        assertEquals(new Point(0, 0), Mouse.positionAt(path, lengths, 0));
        assertEquals(new Point(100, 0), Mouse.positionAt(path, lengths, 0.5));
        assertEquals(new Point(100, 50), Mouse.positionAt(path, lengths, 0.75));
        assertEquals(new Point(100, 100), Mouse.positionAt(path, lengths, 1));
    }
}
