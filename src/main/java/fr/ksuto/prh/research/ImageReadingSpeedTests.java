package fr.ksuto.prh.research;

import fr.ksuto.prh.capture.Capture;
import fr.ksuto.prh.capture.Frame;
import fr.ksuto.prh.capture.Rgb;
import fr.ksuto.prh.research.paralelism.CaptureScheduler;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Locale;

/**
 * Banc d'essai de la capture et de la lecture d'écran (à lancer sous Windows, écran actif).
 * <p>
 * Chaque mesure accumule une somme de contrôle affichée à la fin : sans elle, la JVM peut supprimer les boucles dont le résultat
 * n'est pas utilisé et fausser les temps.
 */
public class ImageReadingSpeedTests {

    private static final int       WARMUP_LOOPS = 20;
    private static final Dimension SCREEN       = Toolkit.getDefaultToolkit().getScreenSize();

    private static long checksum = 0;

    public static void main(String[] args) throws Exception {

        int loops = args.length > 0 ? Integer.parseInt(args[0]) : 200;

        System.out.printf("Écran %dx%d, %d boucles par mesure%n%n", SCREEN.width, SCREEN.height, loops);

        System.out.println("## Capture (Capture.zone)");
        captureBench("plein écran", new Rectangle(SCREEN), loops);
        captureBench("1/4 de surface", new Rectangle(SCREEN.width / 2, SCREEN.height / 2), loops);
        captureBench("1/16 de surface", new Rectangle(SCREEN.width / 4, SCREEN.height / 4), loops);
        captureBench("barre 300x30", new Rectangle(300, 30), loops);
        captureBench("QR code 16x16", new Rectangle(16, 16), loops);

        System.out.println();
        System.out.println("## Lecture de tous les pixels d'une capture plein écran");
        Frame frame = Capture.screen();
        readBench("BufferedImage.getRGB(x, y)", loops, () -> readWithGetRgb(frame.image()));
        readBench("Frame.rgb(x, y)", loops, () -> readWithFrame(frame));

        System.out.println();
        System.out.println("## CaptureScheduler (4 threads x 10 i/s, plein écran)");
        schedulerBench(loops);

        System.out.println();
        System.out.println("(somme de contrôle : " + checksum + ")");
    }

    private static void captureBench(String label, Rectangle zone, int loops) {

        for (int i = 0; i < WARMUP_LOOPS; i++) {checksum += Capture.zone(zone).rgb(0, 0);}

        long start = System.nanoTime();
        for (int i = 0; i < loops; i++) {checksum += Capture.zone(zone).rgb(0, 0);}

        print(label, System.nanoTime() - start, loops);
    }

    private static void readBench(String label, int loops, Runnable read) {

        for (int i = 0; i < WARMUP_LOOPS; i++) {read.run();}

        long start = System.nanoTime();
        for (int i = 0; i < loops; i++) {read.run();}

        print(label, System.nanoTime() - start, loops);
    }

    private static void readWithGetRgb(BufferedImage image) {

        long sum = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                sum += Rgb.red(rgb) + Rgb.green(rgb) + Rgb.blue(rgb);
            }
        }
        checksum += sum;
    }

    private static void readWithFrame(Frame frame) {

        long sum = 0;
        for (int y = 0; y < frame.height(); y++) {
            for (int x = 0; x < frame.width(); x++) {
                int rgb = frame.rgb(x, y);
                sum += Rgb.red(rgb) + Rgb.green(rgb) + Rgb.blue(rgb);
            }
        }
        checksum += sum;
    }

    private static void schedulerBench(int loops) throws InterruptedException {

        CaptureScheduler captureScheduler = new CaptureScheduler();
        captureScheduler.init(new Rectangle(SCREEN));

        long start = System.nanoTime();
        while (captureScheduler.getCaptureIndex() < loops) {Thread.sleep(1);}
        long elapsed = System.nanoTime() - start;

        captureScheduler.stop();

        System.out.printf(Locale.ROOT, "  %-28s %6.1f images/s%n", "débit", loops / (elapsed / 1e9));
    }

    private static void print(String label, long nanos, int loops) {

        System.out.printf(Locale.ROOT, "  %-28s %8.2f ms/boucle%n", label, nanos / 1e6 / loops);
    }
}
