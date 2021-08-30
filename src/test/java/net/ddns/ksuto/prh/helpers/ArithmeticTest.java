package fr.ksuto.prh.helpers;

import fr.ksuto.prh.peripherals.Screen;

import java.awt.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ArithmeticTest {
    
    @Test
    void getOrdonnee() {
        
        Point a = new Point(50, 50);
        
        Assertions.assertEquals(Arithmetic.getOrdonnee(a, 1), -0.0);
        
        Assertions.assertEquals(Arithmetic.getOrdonnee(a, 2), -50.0);
        
        Assertions.assertEquals(Arithmetic.getOrdonnee(a, 0.5), 25.0);
        
        Assertions.assertEquals(Arithmetic.getOrdonnee(a, -2), 150.0);
    }
    
    @Test
    void getPente() {
        
        Point a = new Point(0, 0);
        Point b = new Point(500, 500);
        
        Assertions.assertEquals(Arithmetic.getPente(a, b), 1.0);
        
        a = new Point(500, 500);
        b = new Point(0, 0);
        
        Assertions.assertEquals(Arithmetic.getPente(a, b), 1.0);
        
        a = new Point(0, 0);
        b = new Point(250, 500);
        
        Assertions.assertEquals(Arithmetic.getPente(a, b), 2.0);
        
        a = new Point(0, 0);
        b = new Point(500, 250);
        
        Assertions.assertEquals(Arithmetic.getPente(a, b), 0.5);
        
        a = new Point(0, 500);
        b = new Point(500, 0);
        
        Assertions.assertEquals(Arithmetic.getPente(a, b), -1.0);
    }
    
    @Test
    void getPointAtDistanceOnLine() {
        
        Painter painter = new Painter(Screen.Zone.ALL, "Test getPointAtDistanceOnLine");
        
        Graphics g = painter.getWhiteBoardGraphics();
        
        g.setColor(Color.RED);
        g.drawString("o", 200, 200);
        
        double m, p;
        
        g.setColor(Color.ORANGE);
        m = 1;
        Arithmetic.PrecisePoint pointAtDistanceOnLine = Arithmetic.getPointAtDistanceOnLine(200, 200, m, 100, true);
        //        System.out.println(pointAtDistanceOnLine);
        Assertions.assertEquals(pointAtDistanceOnLine.getX(), 129.28932188134524);
        Assertions.assertEquals(pointAtDistanceOnLine.getY(), 129.28932188134524);
        p = Arithmetic.getOrdonnee(new Point(200, 200), m);
        g.drawLine(0, (int) (m * 0 + p), 3000, (int) (m * 3000 + p));
        g.drawString("X", (int) pointAtDistanceOnLine.getX(), (int) pointAtDistanceOnLine.getY());
        
        g.setColor(Color.BLUE);
        m = 0.5;
        pointAtDistanceOnLine = Arithmetic.getPointAtDistanceOnLine(200, 200, m, 100, true);
        //        System.out.println(pointAtDistanceOnLine);
        Assertions.assertEquals(pointAtDistanceOnLine.getX(), 110.55728090000841);
        Assertions.assertEquals(pointAtDistanceOnLine.getY(), 155.27864045000422);
        p = Arithmetic.getOrdonnee(new Point(200, 200), m);
        g.drawLine(0, (int) (m * 0 + p), 3000, (int) (m * 3000 + p));
        g.drawString("X", (int) pointAtDistanceOnLine.getX(), (int) pointAtDistanceOnLine.getY());
        
        g.setColor(Color.GREEN);
        m = 2;
        pointAtDistanceOnLine = Arithmetic.getPointAtDistanceOnLine(200, 200, m, 100, false);
        //        System.out.println(pointAtDistanceOnLine);
        Assertions.assertEquals(pointAtDistanceOnLine.getX(), 244.7213595499958);
        Assertions.assertEquals(pointAtDistanceOnLine.getY(), 289.44271909999156);
        p = Arithmetic.getOrdonnee(new Point(200, 200), m);
        g.drawLine(0, (int) (m * 0 + p), 3000, (int) (m * 3000 + p));
        g.drawString("X", (int) pointAtDistanceOnLine.getX(), (int) pointAtDistanceOnLine.getY());
        
        g.setColor(Color.CYAN);
        m = -1;
        pointAtDistanceOnLine = Arithmetic.getPointAtDistanceOnLine(200, 200, m, 100, true);
        //        System.out.println(pointAtDistanceOnLine);
        Assertions.assertEquals(pointAtDistanceOnLine.getX(), 270.71067811865476);
        Assertions.assertEquals(pointAtDistanceOnLine.getY(), 129.28932188134524);
        p = Arithmetic.getOrdonnee(new Point(200, 200), m);
        g.drawLine(0, (int) (m * 0 + p), 3000, (int) (m * 3000 + p));
        g.drawString("X", (int) pointAtDistanceOnLine.getX(), (int) pointAtDistanceOnLine.getY());
        
        g.setColor(Color.MAGENTA);
        m = -0.75;
        pointAtDistanceOnLine = Arithmetic.getPointAtDistanceOnLine(200, 200, m, 100, false);
        //        System.out.println(pointAtDistanceOnLine);
        Assertions.assertEquals(pointAtDistanceOnLine.getX(), 120.0);
        Assertions.assertEquals(pointAtDistanceOnLine.getY(), 260.0);
        p = Arithmetic.getOrdonnee(new Point(200, 200), m);
        g.drawLine(0, (int) (m * 0 + p), 3000, (int) (m * 3000 + p));
        g.drawString("X", (int) pointAtDistanceOnLine.getX(), (int) pointAtDistanceOnLine.getY());
        
        g.dispose();
        
        painter.repaintWhiteBoard();
    }
    
    @Test
    void getDistance() {
        
        Point a = new Point(0, 100);
        Point b = new Point(500, 100);
        
        Assertions.assertEquals(Arithmetic.getDistance(a.getX(), a.getY(), b.getX(), b.getY()), 500.0);
        
        a = new Point(100, 0);
        b = new Point(100, 500);
        
        Assertions.assertEquals(Arithmetic.getDistance(a.getX(), a.getY(), b.getX(), b.getY()), 500.0);
        
        a = new Point(0, 0);
        b = new Point(100, 100);
        
        Assertions.assertEquals(Arithmetic.getDistance(a.getX(), a.getY(), b.getX(), b.getY()), 141.4213562373095);
        
        a = new Point(100, 100);
        b = new Point(0, 0);
        
        Assertions.assertEquals(Arithmetic.getDistance(a.getX(), a.getY(), b.getX(), b.getY()), 141.4213562373095);
        
        a = new Point(0, 100);
        b = new Point(100, 0);
        
        Assertions.assertEquals(Arithmetic.getDistance(a.getX(), a.getY(), b.getX(), b.getY()), 141.4213562373095);
    }
    
    @Test
    void getCircleHeight() {
        
        Assertions.assertEquals(Arithmetic.getCircleHeight(2, 0, 1), 0.0);
        
        Assertions.assertEquals(Arithmetic.getCircleHeight(2, 1, 1), 2.0);
        
        Assertions.assertEquals(Arithmetic.getCircleHeight(2, 2, 1), 0.0);
        
        Assertions.assertEquals(Arithmetic.getCircleHeight(2, 1, 0.1), 0.2);
        
        Assertions.assertEquals(Arithmetic.getCircleHeight(30, 10, 0.5), 14.142135623730951);
    }
}