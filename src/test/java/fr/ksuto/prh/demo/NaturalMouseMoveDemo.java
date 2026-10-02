package fr.ksuto.prh.demo;

import fr.ksuto.prh.peripherals.Mouse;

import java.util.Random;

/**
 * Démo manuelle (à lancer sous Windows) : 100 déplacements « naturels » de la souris vers le point (500, 500).
 */
public class NaturalMouseMoveDemo {

    public static void main(String[] args) throws Exception {

        Mouse  mouse  = new Mouse();
        Random random = new Random();

        for (int i = 0; i < 100; i++) {mouse.naturalMoveTo(500 + random.nextInt(5), 500 + random.nextInt(5));}
    }
}
