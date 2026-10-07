package fr.ksuto.prh.peripherals;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HumanTypingTest {

    @Test
    void gesturesTakeAHumanAndVaryingTime() {

        Random       random = new Random(1);
        Set<Integer> holds  = new HashSet<>();
        for (int i = 0; i < 500; i++) {
            int lead = HumanTiming.modifierLead(random), hold = HumanTiming.hold(random), lag = HumanTiming.modifierLag(random);
            assertTrue(lead >= 40 && lead <= 90 && hold >= 60 && hold <= 120 && lag >= 20 && lag <= 60);
            int combination = lead + hold + 2 * lag; // relâchement du modificateur puis répit
            assertTrue(combination >= 140 && combination <= 330, "combinaison : " + combination + " ms");
            holds.add(hold);
        }
        assertTrue(holds.size() > 40, "jamais deux fois le même temps : " + holds.size() + " durées différentes");
    }

    @Test
    void typingIntervalsVaryAroundTheConfiguredDelay() {

        Random random = new Random(2);
        for (int i = 0; i < 500; i++) {
            int interval = HumanTiming.betweenCharacters(random, 50);
            assertTrue(interval >= 25 && interval <= 75, "50 ms ± 50 % : " + interval);
        }
        assertEquals(0, HumanTiming.betweenCharacters(random, 0));
    }

    @Test
    void decodesTheKeyOfACharacterOnTheActiveLayout() {

        // Résultats de VkKeyScanW : octet faible = touche, octet fort = 1 Maj, 2 Ctrl, 4 Alt
        assertEquals(new WindowsKeys.Stroke(0x41, true, false, false), WindowsKeys.Stroke.decode((short) 0x0141).orElseThrow(), "A : Maj + A");
        assertEquals(new WindowsKeys.Stroke(0x30, false, true, true), WindowsKeys.Stroke.decode((short) 0x0630).orElseThrow(), "@ en AZERTY : AltGr + 0");
        assertEquals(new WindowsKeys.Stroke(0x31, false, false, false), WindowsKeys.Stroke.decode((short) 0x0031).orElseThrow(), "& en AZERTY : touche 1");
        assertTrue(WindowsKeys.Stroke.decode((short) -1).isEmpty(), "aucune touche");
    }
}
