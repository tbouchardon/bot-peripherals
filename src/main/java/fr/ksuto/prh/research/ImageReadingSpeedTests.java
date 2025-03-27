package fr.ksuto.prh.research;

import fr.ksuto.prh.research.paralelism.CaptureScheduler;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageReadingSpeedTests {
    
    private static final int       DIVIDED_BY    = 1;
    private static final int       LOOPS         = 1000;
    private final        Dimension dim_D         = new Dimension(Toolkit.getDefaultToolkit().getScreenSize());
    private final        int       SCREEN_HEIGHT = (int) dim_D.getHeight();
    private final        int       SCREEN_WIDTH  = (int) dim_D.getWidth();
    
    public static void main(String[] args) throws Exception {
        
        ImageReadingSpeedTests imageReadingSpeedTests = new ImageReadingSpeedTests();
        long                   start                  = System.currentTimeMillis();
        imageReadingSpeedTests.schedulerRobotWay();
        System.out.println("schedulerRobotWay (" + LOOPS + " loops) took " + (System.currentTimeMillis() - start) / 1000 + " seconds");
    }
    
    private void robotWay() throws AWTException {
        
        Robot robot = new Robot();
        
        for (int loop = 1; loop <= LOOPS; loop++) {
            BufferedImage screenCapture = robot.createScreenCapture(new Rectangle(SCREEN_WIDTH / DIVIDED_BY, SCREEN_HEIGHT / DIVIDED_BY));
            
            for (int y = 0; y < SCREEN_HEIGHT / DIVIDED_BY; y++) {
                for (int x = 0; x < SCREEN_WIDTH / DIVIDED_BY; x++) {
                    int rgb  = screenCapture.getRGB(x, y);
                    int imgB = (rgb) & 0xFF;
                    int imgG = (rgb >> 8) & 0xFF;
                    int imgR = (rgb >> 16) & 0xFF;
                }
            }
        }
    }
    
    private void schedulerRobotWay() throws Exception {
        
        CaptureScheduler captureScheduler = new CaptureScheduler();
        captureScheduler.init(new Rectangle(SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2));
        
        while (captureScheduler.getCaptureIndex() < LOOPS) {
            
            if (captureScheduler.getLastImage() == null) {continue;}
            
            //            if (captureScheduler.store.size() > 2) captureScheduler.store.removeFirst();
            
            BufferedImage screenCapture = captureScheduler.getLastImage();
            
            for (int y = 0; y < SCREEN_HEIGHT / 2; y++) {
                for (int x = 0; x < SCREEN_WIDTH / 2; x++) {
                    int rgb  = screenCapture.getRGB(x, y);
                    int imgB = (rgb) & 0xFF;
                    int imgG = (rgb >> 8) & 0xFF;
                    int imgR = (rgb >> 16) & 0xFF;
                }
            }
        }
        
        captureScheduler.stop();
    }
}

