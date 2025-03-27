package fr.ksuto.prh.peripherals;

import fr.ksuto.commons.PropertiesLoader;
import fr.ksuto.commons.helpers.InOut;
import fr.ksuto.logger.ConsoleLogger;
import fr.ksuto.prh.entities.Picture;
import fr.ksuto.prh.helpers.ColorChecker;
import fr.ksuto.prh.helpers.PictureSearch;
import fr.ksuto.prh.research.paralelism.CaptureScheduler;
import fr.ksuto.prh.tools.ShowObjects;
import lombok.Data;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;

/**
 * Created by TBO on 15/07/2016.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class Screen extends Peripheral {
    
    ConsoleLogger logger = new ConsoleLogger();
    
    public static final Dimension        dim_D         = new Dimension(Toolkit.getDefaultToolkit().getScreenSize());
    public static final int              SCREEN_HEIGHT = (int) dim_D.getHeight();
    public static final int              Y_START       = Screen.SCREEN_HEIGHT / 2 - 20;
    public static final int              SCREEN_WIDTH  = (int) dim_D.getWidth();
    public static final int              X_START       = Screen.SCREEN_WIDTH / 2;
    private             ColorChecker     colorChecker;
    private             CaptureScheduler captureScheduler;
    
    Screen() throws AWTException {
        
        super();
    }
    
    public static void main(String[] args) throws AWTException {
        
        ShowObjects<Picture> showObjects   = new ShowObjects<>();
        PictureSearch        pictureSearch = new PictureSearch();
        int                  precision     = 0;
        pictureSearch.addPictureWithUrl("/test.png")
                .setPrecision(precision)
                .setExclusiveZone(5);
        
        while (pictureSearch.getObjects().get(0).getPositions().size() < 10) {
            pictureSearch.search();
            delay(1000);
            showObjects.setLocatedObjects(pictureSearch.getObjects());
            precision++;
        }
    }
    
    public int numberOfChangedZones(BufferedImage image1, BufferedImage image2, List<Zone> zones) {
        
        int numberOfChangedZones = 0;
        
        for (Zone zone : zones) {
            if (zoneHasChanged(image1, image2, zone)) {numberOfChangedZones++;}
        }
        
        return numberOfChangedZones;
    }
    
    public void startCapture() throws Exception {
        
        captureScheduler = new CaptureScheduler();
        captureScheduler.init(new Rectangle(SCREEN_WIDTH, SCREEN_HEIGHT));
    }
    
    public void stopCapture() throws Exception {
        
        captureScheduler.stop();
    }
    
    public void waitUntilAnyHasChanged(List<Zone> zones) {
        
        waitUntilHasChanged(zones, 250, ZoneEnum.ANY, null);
    }
    
    public void waitUntilHasChanged(List<Zone> zones, Integer msDelay, ZoneEnum zoneEnum, Integer maxWaitingMilliseconds) {
        
        waitUntilHasChanged(zones, msDelay, zoneEnum, maxWaitingMilliseconds, false);
    }
    
    public void waitUntilHasChanged(List<Zone> zones, Integer msDelay, ZoneEnum zoneEnum, Integer maxWaitingMilliseconds, boolean debug) {
        
        BufferedImage image1 = robot.createScreenCapture(Zone.ALL.getRectangle());
        BufferedImage image2;
        
        if (debug) {InOut.writeImage(image1, System.currentTimeMillis() + "_base");}
        
        int numberOfChangedZones;
        
        long startTime = System.currentTimeMillis();
        
        do {
            delay(msDelay == null ? 250 : msDelay);
            image2 = robot.createScreenCapture(Zone.ALL.getRectangle());
            
            if (debug) {InOut.writeImage(image2, System.currentTimeMillis() + "_comparingTo");}
            
            numberOfChangedZones = numberOfChangedZones(image1, image2, zones);
            if (System.currentTimeMillis() > startTime + (maxWaitingMilliseconds == null ? 60 * 1000 : maxWaitingMilliseconds)) {
                logger.sysOutDebug("Things should have changed but nothing happened");
                break;
            }
        }
        while (!(zoneEnum == ZoneEnum.ALL && numberOfChangedZones == zones.size()) &&
               !(zoneEnum == ZoneEnum.ANY && numberOfChangedZones > 0));
    }
    
    public void waitUntilHasChanged(Zone zone) {
        
        waitUntilHasChanged(Collections.singletonList(zone), 250, ZoneEnum.ALL, null);
    }
    
    public void waitUntilHasChanged(Zone zone, Integer maxWaitingMilliseconds) {
        
        waitUntilHasChanged(Collections.singletonList(zone), 250, ZoneEnum.ALL, maxWaitingMilliseconds);
    }
    
    public void waitUntilHasChanged(List<Zone> zones) {
        
        waitUntilHasChanged(zones, 250, ZoneEnum.ALL, null);
    }
    
    public void waitUntilStopsMoving() {
        
        while (isMoving()) {delay(200);}
    }
    
    public boolean zoneHasChanged(BufferedImage image1, BufferedImage image2, Zone zone) {
        
        for (int x = zone.xMin; x < zone.xMax; x++) {
            for (int y = zone.yMin; y < zone.yMax; y++) {
                if (image1.getRGB(x, y) != image2.getRGB(x, y)) {
                    logger.sysOutTrace("Change : x = " + x + ", y = " + y);
                    return true;
                }
            }
        }
        return false;
    }
    
    public CaptureScheduler getCaptureScheduler() {
        
        return captureScheduler;
    }
    
    public BufferedImage getLastCapture() {
        
        return captureScheduler.getLastImage();
    }
    
    public boolean isMoving() {
        
        BufferedImage screenCapture = robot.createScreenCapture(new Rectangle(0, 0, Screen.SCREEN_WIDTH, Screen.SCREEN_HEIGHT));
        
        int initialPixelColor1 = screenCapture.getRGB(screenCapture.getWidth() / 2 - 100, screenCapture.getHeight() / 2 - 100);
        int initialPixelColor2 = screenCapture.getRGB(screenCapture.getWidth() / 2, screenCapture.getHeight() / 2);
        int initialPixelColor3 = screenCapture.getRGB(screenCapture.getWidth() / 2 + 100, screenCapture.getHeight() / 2 + 100);
        
        delay(200);
        
        screenCapture = robot.createScreenCapture(new Rectangle(0, 0, Screen.SCREEN_WIDTH, Screen.SCREEN_HEIGHT));
        
        int pixelColor1 = screenCapture.getRGB(screenCapture.getWidth() / 2 - 100, screenCapture.getHeight() / 2 - 100);
        int pixelColor2 = screenCapture.getRGB(screenCapture.getWidth() / 2, screenCapture.getHeight() / 2);
        int pixelColor3 = screenCapture.getRGB(screenCapture.getWidth() / 2 + 100, screenCapture.getHeight() / 2 + 100);
        
        return pixelColor1 != initialPixelColor1 &&
               pixelColor2 != initialPixelColor2;
    }
    
    public enum ZoneEnum {
        ANY, ALL
    }
    
    @Data
    public static class Zone implements Serializable {
        
        public static final Zone NONE          = new Zone(0, 0, 0, 0);
        public static final Zone ALL          = new Zone(0, Screen.SCREEN_WIDTH, 0, Screen.SCREEN_HEIGHT);
        public static final Zone BOTTOM       = new Zone(0, Screen.SCREEN_WIDTH, Screen.SCREEN_HEIGHT / 2, Screen.SCREEN_HEIGHT);
        public static final Zone BOTTOM_LEFT  = new Zone(0, Screen.SCREEN_WIDTH / 2, Screen.SCREEN_HEIGHT / 2, Screen.SCREEN_HEIGHT);
        public static final Zone BOTTOM_RIGHT = new Zone(Screen.SCREEN_WIDTH / 2, Screen.SCREEN_WIDTH, Screen.SCREEN_HEIGHT / 2, Screen.SCREEN_HEIGHT);
        public static final Zone LEFT         = new Zone(0, Screen.SCREEN_WIDTH / 2, 0, Screen.SCREEN_HEIGHT);
        public static final Zone MIDDLE       = new Zone(Screen.SCREEN_WIDTH / 4, Screen.SCREEN_WIDTH - Screen.SCREEN_WIDTH / 4, Screen.SCREEN_HEIGHT / 4,
                                                         Screen.SCREEN_HEIGHT - Screen.SCREEN_HEIGHT / 4);
        public static final Zone RIGHT        = new Zone(Screen.SCREEN_WIDTH / 2, Screen.SCREEN_WIDTH, 0, Screen.SCREEN_HEIGHT);
        public static final Zone TOP          = new Zone(0, Screen.SCREEN_WIDTH, 0, Screen.SCREEN_HEIGHT / 2);
        public static final Zone TOP_LEFT     = new Zone(0, Screen.SCREEN_WIDTH / 2, 0, Screen.SCREEN_HEIGHT / 2);
        public static final Zone TOP_RIGHT    = new Zone(Screen.SCREEN_WIDTH / 2, Screen.SCREEN_WIDTH, 0, Screen.SCREEN_HEIGHT / 2);
        private             int  xMin         = 0;
        private             int  xMax         = Screen.SCREEN_WIDTH;
        private             int  yMin         = 0;
        private             int  yMax         = Screen.SCREEN_HEIGHT;
        private             int  width        = Screen.SCREEN_WIDTH;
        private             int  height       = Screen.SCREEN_HEIGHT;
        
        public Zone(int xMin, int xMax, int yMin, int yMax) {
            
            this.xMin = xMin;
            this.xMax = xMax;
            this.yMin = yMin;
            this.yMax = yMax;
            this.width = xMax - xMin;
            this.height = yMax - yMin;
        }
        
        public Zone() {
            
        }
        
        public static Zone fraction(int xFraction, int xIndex, int yFraction, int yIndex) {
            
            xFraction = Screen.SCREEN_WIDTH / xFraction;
            yFraction = Screen.SCREEN_HEIGHT / yFraction;
            
            return new Zone((xIndex - 1) * xFraction,
                            xIndex * xFraction,
                            (yIndex - 1) * yFraction,
                            yIndex * yFraction);
        }
        
        public Rectangle getRectangle() {
            
            return new Rectangle(xMin, yMin, width, height);
        }
    }
}
