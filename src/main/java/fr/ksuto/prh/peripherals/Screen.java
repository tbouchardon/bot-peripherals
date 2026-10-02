package fr.ksuto.prh.peripherals;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fr.ksuto.commons.helpers.InOut;
import fr.ksuto.prh.capture.Capture;
import fr.ksuto.prh.capture.Frame;
import fr.ksuto.prh.helpers.ColorChecker;
import fr.ksuto.prh.research.paralelism.CaptureScheduler;
import lombok.Data;

import java.awt.*;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Created by TBO on 15/07/2016.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class Screen extends Peripheral {
    
    private static final Logger logger = LoggerFactory.getLogger(Screen.class);
    
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
    
    public int numberOfChangedZones(Frame image1, Frame image2, List<Zone> zones) {
        
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
        
        Frame image1 = Capture.screen();
        Frame image2;
        
        if (debug) {InOut.writeImage(image1.image(), System.currentTimeMillis() + "_base");}
        
        int numberOfChangedZones;
        
        long startTime = System.currentTimeMillis();
        
        do {
            delay(msDelay == null ? 250 : msDelay);
            image2 = Capture.screen();
            
            if (debug) {InOut.writeImage(image2.image(), System.currentTimeMillis() + "_comparingTo");}
            
            numberOfChangedZones = numberOfChangedZones(image1, image2, zones);
            if (System.currentTimeMillis() > startTime + (maxWaitingMilliseconds == null ? 60 * 1000 : maxWaitingMilliseconds)) {
                logger.debug("Things should have changed but nothing happened");
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
    
    public boolean zoneHasChanged(Frame image1, Frame image2, Zone zone) {
        
        for (int x = zone.xMin; x < zone.xMax; x++) {
            for (int y = zone.yMin; y < zone.yMax; y++) {
                if (image1.rgb(x, y) != image2.rgb(x, y)) {
                    logger.trace("Change : x = " + x + ", y = " + y);
                    return true;
                }
            }
        }
        return false;
    }
    
    public CaptureScheduler getCaptureScheduler() {
        
        return captureScheduler;
    }
    
    public Frame getLastCapture() {
        
        return captureScheduler.getLastFrame();
    }
    
    public boolean isMoving() {
        
        Frame screenCapture = Capture.screen();
        
        int initialPixelColor1 = screenCapture.rgb(screenCapture.width() / 2 - 100, screenCapture.height() / 2 - 100);
        int initialPixelColor2 = screenCapture.rgb(screenCapture.width() / 2, screenCapture.height() / 2);
        int initialPixelColor3 = screenCapture.rgb(screenCapture.width() / 2 + 100, screenCapture.height() / 2 + 100);
        
        delay(200);
        
        screenCapture = Capture.screen();
        
        int pixelColor1 = screenCapture.rgb(screenCapture.width() / 2 - 100, screenCapture.height() / 2 - 100);
        int pixelColor2 = screenCapture.rgb(screenCapture.width() / 2, screenCapture.height() / 2);
        int pixelColor3 = screenCapture.rgb(screenCapture.width() / 2 + 100, screenCapture.height() / 2 + 100);
        
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
