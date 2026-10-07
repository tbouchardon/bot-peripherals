package fr.ksuto.prh.peripherals;

import java.util.Random;

/**
 * Durées d'une frappe humaine, tirées au hasard à chaque geste : un doigt ne tient pas une touche zéro milliseconde, et
 * jamais deux fois exactement le même temps.
 * <ul>
 *   <li>modificateur (Maj, Ctrl, Alt) enfoncé {@value #LEAD_MIN} à {@value #LEAD_MAX} ms avant la touche ;</li>
 *   <li>touche tenue {@value #HOLD_MIN} à {@value #HOLD_MAX} ms ;</li>
 *   <li>modificateur relâché {@value #LAG_MIN} à {@value #LAG_MAX} ms après la touche, et autant de répit après le
 *   geste.</li>
 * </ul>
 * Une combinaison prend ainsi 120 à 270 ms, une touche seule 80 à 180 ms : l'ordre de grandeur d'un joueur rapide.
 */
final class HumanTiming {

    static final int LEAD_MIN = 40, LEAD_MAX = 90;
    static final int HOLD_MIN = 60, HOLD_MAX = 120;
    static final int LAG_MIN  = 20, LAG_MAX = 60;

    private HumanTiming() {}

    private static int between(Random random, int min, int max) {

        return min + random.nextInt(max - min + 1);
    }

    /**
     * Avance du modificateur sur la touche.
     */
    static int modifierLead(Random random) {

        return between(random, LEAD_MIN, LEAD_MAX);
    }

    /**
     * Durée d'appui d'une touche.
     */
    static int hold(Random random) {

        return between(random, HOLD_MIN, HOLD_MAX);
    }

    /**
     * Retard du relâchement du modificateur, et répit après un geste.
     */
    static int modifierLag(Random random) {

        return between(random, LAG_MIN, LAG_MAX);
    }

    /**
     * Intervalle entre deux caractères tapés : {@code typingDelay} à ±50 %.
     */
    static int betweenCharacters(Random random, int typingDelay) {

        return Math.max(0, typingDelay / 2 + random.nextInt(Math.max(1, typingDelay + 1)));
    }
}
