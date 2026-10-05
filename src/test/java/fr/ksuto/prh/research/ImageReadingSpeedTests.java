package fr.ksuto.prh.research;

import fr.ksuto.prh.capture.Capture;
import fr.ksuto.prh.capture.RobotCaptureBackend;
import fr.ksuto.prh.capture.GdiCaptureBackend;
import fr.ksuto.prh.capture.DxgiCaptureBackend;
import fr.ksuto.prh.capture.CaptureBackend;
import fr.ksuto.prh.capture.Frame;
import fr.ksuto.prh.capture.Rgb;
import fr.ksuto.prh.research.paralelism.CaptureScheduler;

import java.util.List;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
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

        // Une seule duplication DXGI par écran et par processus : la même instance sert à toutes les mesures
        RobotCaptureBackend robot = new RobotCaptureBackend();
        GdiCaptureBackend   gdi   = new GdiCaptureBackend();
        DxgiCaptureBackend  dxgi  = new DxgiCaptureBackend();

        for (CaptureBackend backend : List.of(robot, gdi, dxgi)) {
            Capture.setBackend(backend);
            System.out.println("## Capture (Capture.zone) : " + backend.getClass().getSimpleName());
            captureBench("plein écran", new Rectangle(SCREEN), loops);
            captureBench("1/4 de surface", new Rectangle(SCREEN.width / 2, SCREEN.height / 2), loops);
            captureBench("1/16 de surface", new Rectangle(SCREEN.width / 4, SCREEN.height / 4), loops);
            captureBench("barre 300x30", new Rectangle(300, 30), loops);
            captureBench("QR code 32x32", new Rectangle(0, 23, 32, 32), loops);
            System.out.println();
        }
        Capture.setBackend(new RobotCaptureBackend());

        System.out.println("## Écran animé (fenêtre redessinée en continu, comme un jeu) : chaque capture DXGI copie une nouvelle image");
        animatedBench(loops, robot, dxgi);
        System.out.println();

        System.out.println("## Même image ? Robot, GDI et DXGI sur 400x300 (écran immobile)");
        compareBackends(new Rectangle(0, 0, 400, 300), robot, gdi, dxgi);

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

    /**
     * Une fenêtre qui change de couleur toutes les 5 ms force le bureau à produire de nouvelles images : c'est le cas d'un
     * jeu, où DXGI recopie l'image à chaque capture au lieu de relire la précédente. On capture dans la fenêtre.
     */
    private static void animatedBench(int loops, CaptureBackend... backends) throws Exception {

        int[]  tick  = {0};
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics graphics) {

                graphics.setColor(new Color(tick[0] % 256, (tick[0] * 7) % 256, (tick[0] * 13) % 256));
                graphics.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        JWindow window = new JWindow();
        window.setContentPane(panel);
        window.setBounds(100, 100, 200, 200);
        window.setAlwaysOnTop(true);
        javax.swing.Timer timer = new javax.swing.Timer(5, event -> {
            tick[0]++;
            panel.paintImmediately(0, 0, panel.getWidth(), panel.getHeight());
        });
        SwingUtilities.invokeAndWait(() -> window.setVisible(true));
        timer.start();
        Thread.sleep(500);

        try {
            for (CaptureBackend backend : backends) {
                Capture.setBackend(backend);
                String name = backend.getClass().getSimpleName();
                captureBench(name + " 32x32", new Rectangle(150, 150, 32, 32), loops);
                captureBench(name + " plein écran", new Rectangle(SCREEN), Math.max(20, loops / 4));

                // Fraîcheur : combien de captures successives voient une couleur différente (nouvelle image)
                int changes  = 0;
                int previous = Capture.zone(new Rectangle(150, 150, 1, 1)).rgb(0, 0);
                long start   = System.nanoTime();
                while (System.nanoTime() - start < 1_000_000_000L) {
                    int current = Capture.zone(new Rectangle(150, 150, 1, 1)).rgb(0, 0);
                    if (current != previous) {changes++;}
                    previous = current;
                }
                System.out.printf("  %-30s %d image(s) différente(s) vue(s) en 1 s (dessinées : %d)%n", name + " fraîcheur", changes, tick[0]);
            }
        }
        finally {
            timer.stop();
            SwingUtilities.invokeAndWait(window::dispose);
        }
    }

    private static void compareBackends(Rectangle zone, CaptureBackend robotBackend, CaptureBackend gdiBackend, CaptureBackend dxgiBackend) {

        Frame robot = robotBackend.capture(zone);
        Frame gdi   = gdiBackend.capture(zone);
        Frame dxgi  = dxgiBackend.capture(zone);
        int   sameDxgi = 0;
        for (int y = 0; y < zone.height; y++) {
            for (int x = 0; x < zone.width; x++) {
                if (robot.rgb(x, y) == dxgi.rgb(x, y)) {sameDxgi++;}
            }
        }
        System.out.printf("%-30s %d / %d pixels identiques à Robot (%.1f %%)%n", "DXGI", sameDxgi, zone.width * zone.height,
                          100.0 * sameDxgi / (zone.width * zone.height));
        int   same  = 0;
        for (int y = 0; y < zone.height; y++) {
            for (int x = 0; x < zone.width; x++) {
                if (robot.rgb(x, y) == gdi.rgb(x, y)) {same++;}
            }
        }
        System.out.printf("%-30s %d / %d pixels identiques (%.1f %%), taille GDI %dx%d%n", "comparaison", same, zone.width * zone.height,
                          100.0 * same / (zone.width * zone.height), gdi.width(), gdi.height());
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
