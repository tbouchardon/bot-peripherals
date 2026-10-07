package fr.ksuto.prh.helpers;

import fr.ksuto.prh.capture.Frame;
import fr.ksuto.prh.entities.Parameter;
import fr.ksuto.prh.entities.Picture;
import fr.ksuto.prh.entities.Position;
import fr.ksuto.prh.peripherals.Screen;
import fr.ksuto.prh.tools.SearchMemory;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Auto-apprentissage de la recherche d'image sur des écrans synthétiques : tolérances apprises ({@code learn}) et zone de
 * recherche réduite ({@code optimize}), avec une capture et une mémoire de test.
 */
class SeekerLearningTest {

    private static final int WIDTH = 240, HEIGHT = 160, SIZE = 12;

    private final SearchMemory    memory   = SearchMemory.inMemory();
    private final BufferedImage   pattern  = pattern();
    private final List<Rectangle> captured = new ArrayList<>();
    private       BufferedImage   screen;

    @AfterEach
    void close() {

        memory.close();
    }

    /**
     * Motif de 12x12 : dégradé et pixels pseudo-aléatoires, pour qu'il ne ressemble à rien d'autre sur l'écran.
     */
    private static BufferedImage pattern() {

        BufferedImage image  = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_RGB);
        Random        random = new Random(7);
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                image.setRGB(x, y, (40 + x * 15) << 16 | (200 - y * 12) << 8 | (60 + random.nextInt(120)));
            }
        }
        return image;
    }

    /**
     * Écran de fond bruité, sombre (loin des couleurs du motif).
     */
    private static BufferedImage background() {

        BufferedImage image  = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Random        random = new Random(11);
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                image.setRGB(x, y, random.nextInt(30) << 16 | random.nextInt(30) << 8 | random.nextInt(30));
            }
        }
        return image;
    }

    /**
     * Copie du motif, couleurs décalées de {@code shift} et une part des pixels remplacés par du bruit (anticrénelage,
     * éclairage...).
     */
    private void draw(BufferedImage target, int left, int top, int shift, double noise, long seed) {

        Random random = new Random(seed);
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                int rgb = pattern.getRGB(x, y);
                int r = Math.min(255, (rgb >> 16 & 0xFF) + shift), g = Math.min(255, (rgb >> 8 & 0xFF) + shift), b = Math.min(255, (rgb & 0xFF) + shift);
                if (random.nextDouble() < noise) {r = random.nextInt(256); g = random.nextInt(256); b = random.nextInt(256);}
                target.setRGB(left + x, top + y, r << 16 | g << 8 | b);
            }
        }
    }

    private PictureSearch seeker() throws Exception {

        PictureSearch seeker = new PictureSearch();
        seeker.addPicture(new Picture("motif", pattern));
        seeker.setSearchZone(new Screen.Zone(0, WIDTH, 0, HEIGHT));
        seeker.setMemory(memory);
        seeker.setCapture(zone -> {
            captured.add(new Rectangle(zone));
            Rectangle clipped = zone.intersection(new Rectangle(0, 0, WIDTH, HEIGHT));
            return Frame.of(screen.getSubimage(clipped.x, clipped.y, clipped.width, clipped.height), clipped);
        });
        return seeker;
    }

    private static List<String> positions(PictureSearch seeker) {

        return seeker.getAllPositions().stream().map(p -> p.getX() + "," + p.getY()).sorted().toList();
    }

    @Test
    void learnsTolerancesThatFindExactlyTheExpectedObjects() throws Exception {

        // Un motif exact et un autre décalé de 12 par canal, 4 % de pixels altérés : ni la recherche stricte (0, 0) ni la
        // plus permissive (50, 30 %) ne trouvent exactement les deux
        screen = background();
        draw(screen, 30, 40, 0, 0, 1);
        draw(screen, 150, 90, 12, 0.04, 2);

        PictureSearch seeker = seeker();
        seeker.learn(2);
        for (int i = 0; i < 12; i++) {seeker.search();}

        assertEquals(List.of("150,90", "30,40"), positions(seeker), "les deux motifs, et eux seuls");
        assertTrue(seeker.getPrecision() >= 12 && seeker.getAllowedErrorRate() >= 0.04, "tolérances apprises : " + seeker.getPrecision() + ", " + seeker.getAllowedErrorRate());

        var stats = memory.parameterStats(seeker.getSearchKey());
        assertEquals(ParameterLearning.CANDIDATES.size(), stats.size(), "toutes les combinaisons essayées en 12 recherches");
        assertEquals(0, stats.get(new Parameter(0, 0.0)).successes(), "la recherche stricte rate le motif altéré");

        // Une nouvelle recherche sans apprentissage reprend les tolérances apprises
        PictureSearch fresh = seeker();
        fresh.search();
        assertEquals(List.of("150,90", "30,40"), positions(fresh));
    }

    @Test
    void eachTrialIsJudgedOnItsOwnResults() throws Exception {

        // Régression : les positions trouvées par une combinaison permissive s'ajoutaient à celles de la suivante
        screen = background();
        draw(screen, 30, 40, 0, 0, 1);
        draw(screen, 150, 90, 20, 0, 2);

        PictureSearch seeker = seeker();
        seeker.learn(1).setTrialsPerSearch(ParameterLearning.CANDIDATES.size());
        seeker.search();

        var stats = memory.parameterStats(seeker.getSearchKey());
        assertEquals(1, stats.get(new Parameter(0, 0.0)).successes(), "stricte : le seul motif exact");
        assertEquals(0, stats.get(new Parameter(25, 0.0)).successes(), "les deux motifs (écart de 20 toléré) : 2 ≠ 1");
    }

    @Test
    void searchShrinksToWhereTheObjectWasFoundThenFallsBack() throws Exception {

        screen = background();
        draw(screen, 100, 50, 0, 0, 1);

        PictureSearch seeker = seeker();
        seeker.setPrecision(20).setAllowedErrorRate(0.05);
        seeker.optimize(3, 10);
        for (int i = 0; i < 3; i++) {seeker.search();}

        Rectangle area = memory.area(seeker.getSearchKey()).orElseThrow();
        assertTrue(area.contains(100, 50) && area.contains(100 + SIZE, 50 + SIZE), "zone autour du motif : " + area);
        assertTrue(area.width < WIDTH && area.height < HEIGHT);

        captured.clear();
        seeker.search();
        assertEquals(List.of(area), captured, "recherche limitée à la zone apprise");

        // Le motif change de place : rien dans la zone réduite, la même recherche reprend sur tout l'écran
        screen = background();
        draw(screen, 10, 120, 0, 0, 1);
        captured.clear();
        seeker.search();
        assertEquals(List.of("10,120"), positions(seeker));
        assertEquals(2, captured.size(), "zone réduite, puis écran entier");
        assertEquals(new Rectangle(0, 0, WIDTH, HEIGHT), captured.get(1));
    }

    @Test
    void reducedAreaIsForgottenAfterRepeatedMisses() throws Exception {

        screen = background();
        draw(screen, 100, 50, 0, 0, 1);

        PictureSearch seeker = seeker();
        seeker.setPrecision(20).setAllowedErrorRate(0.05);
        seeker.optimize(3, 10);
        for (int i = 0; i < 3; i++) {seeker.search();}
        assertTrue(memory.area(seeker.getSearchKey()).isPresent());

        screen = background(); // l'objet a disparu
        for (int i = 0; i < AbstractSeeker.MISSES_BEFORE_RESET; i++) {seeker.search();}

        assertTrue(memory.area(seeker.getSearchKey()).isEmpty(), "zone oubliée après " + AbstractSeeker.MISSES_BEFORE_RESET + " échecs");
        assertFalse(seeker.hasAnyResults());
    }

    @Test
    void searchReplacesPreviousResults() throws Exception {

        screen = background();
        draw(screen, 100, 50, 0, 0, 1);
        PictureSearch seeker = seeker();
        seeker.setPrecision(20).setAllowedErrorRate(0.05);
        seeker.search();
        assertTrue(seeker.hasAnyResults());

        screen = background();
        seeker.search();
        assertFalse(seeker.hasAnyResults(), "un objet disparu n'est plus signalé");
        assertTrue(seeker.getAllPositions().stream().map(Position::getX).toList().isEmpty());
    }
}
