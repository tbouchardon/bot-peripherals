package fr.ksuto.prh;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.google.inject.Guice;
import com.google.inject.Inject;

public class Tests {
    
    final Dimension dim_D         = new Dimension(Toolkit.getDefaultToolkit().getScreenSize());
    final int       SCREEN_WIDTH  = (int) dim_D.getWidth();
    final int       SCREEN_HEIGHT = (int) dim_D.getHeight();
    @Inject
    PeripheralRobotHelper robotHelper;
    
    public static void main(String[] args) throws AWTException {
        
        Tests tests = Guice.createInjector().getInstance(Tests.class);
        tests.robotHelper.getMouse().move(200, 200);
        tests.naturalMove();
    }
    
    private void naturalMove() throws AWTException {
        
        robotHelper.getMouse().naturalMoveTo(500, 500);
    }
    
    private void run() throws InterruptedException {
        
        int             nbProcs         = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(nbProcs);
        
        long                   startTime      = System.currentTimeMillis();
        Set<Callable<Integer>> runnableRobots = new HashSet<>();
        for (int loop = 0; loop < 100; loop++) {
            
            RunnableRobot runnableRobot = new RunnableRobot(loop, startTime);
            runnableRobots.add(runnableRobot);
        }
        List<Future<Integer>> runnableRobotFuture = executorService.invokeAll(runnableRobots);
        
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);
        
        System.out.println("[TRACE] PRH : RunnableRobot : " + (System.currentTimeMillis() - startTime));
    }
    
    private void rgb() throws AWTException {
        
        Robot         robot     = new Robot();
        Rectangle     rectangle = new Rectangle(SCREEN_WIDTH, SCREEN_HEIGHT);
        long          startTime;
        BufferedImage capturedScreen;
        startTime = System.currentTimeMillis();
        for (int loop = 0; loop < 100; loop++) {
            
            capturedScreen = robot.createScreenCapture(rectangle);
            
            for (int x = 0; x < capturedScreen.getWidth(); x++) {
                for (int y = 0; y < capturedScreen.getHeight(); y++) {
                    if (capturedScreen.getRGB(x, y) == 13) { System.out.println("[TRACE] PRH : hello"); }
                }
            }
        }
        System.out.println("[TRACE] PRH : getRGB : " + (System.currentTimeMillis() - startTime));
    }
    
    private void raster() throws AWTException {
        
        Robot         robot     = new Robot();
        Rectangle     rectangle = new Rectangle(SCREEN_WIDTH, SCREEN_HEIGHT);
        long          startTime;
        BufferedImage capturedScreen;
        startTime = System.currentTimeMillis();
        for (int loop = 0; loop < 100; loop++) {
            
            capturedScreen = robot.createScreenCapture(rectangle);
            
            int[] pixels = ((DataBufferInt) capturedScreen.getRaster().getDataBuffer()).getData();
            for (int p : pixels) {
    
                if (p == 13) { System.out.println("[TRACE] PRH : hello"); }
            }
        }
        System.out.println("[TRACE] PRH : getRaster : " + (System.currentTimeMillis() - startTime));
    }
    
    private String getMouseHex(int delay) throws AWTException {
        
        Robot robot = new Robot();
        robot.delay(delay);
        Color pixelColor = robot.getPixelColor(MouseInfo.getPointerInfo().getLocation().x, MouseInfo.getPointerInfo().getLocation().y);
        System.out.print(pixelColor);
        String formatedColor = String.format("#%02x%02x%02x", pixelColor.getRed(), pixelColor.getGreen(), pixelColor.getBlue());
        System.out.println("[TRACE] PRH :  => " + formatedColor);
        
        return formatedColor;
    }
}
