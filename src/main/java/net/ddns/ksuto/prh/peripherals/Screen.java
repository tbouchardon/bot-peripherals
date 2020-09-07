package net.ddns.ksuto.prh.peripherals;

import lombok.Data;
import net.ddns.ksuto.prh.entities.Picture;
import net.ddns.ksuto.prh.helpers.ColorChecker;
import net.ddns.ksuto.prh.helpers.PictureSearch;
import net.ddns.ksuto.prh.tools.ShowObjects;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by TBO on 15/07/2016.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class Screen extends Peripheral {
    
    public static final Dimension dim_D         = new Dimension(Toolkit.getDefaultToolkit().getScreenSize());
    public static final int       SCREEN_WIDTH  = (int) dim_D.getWidth();
    public static final int       SCREEN_HEIGHT = (int) dim_D.getHeight();
    public final        int       iX_START      = Screen.SCREEN_WIDTH / 2, iY_START = Screen.SCREEN_HEIGHT / 2 - 20;
    private ColorChecker colorChecker;
    
    Screen() throws AWTException {
    
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
            pictureSearch.robot.delay(1000);
            showObjects.setLocatedObjects(pictureSearch.getObjects());
            precision++;
        }
    }
    
    public void waitUntilStopsMoving() {
        
        while (isMoving()) {robot.delay(200);}
    }
    
    public boolean hasChanged(BufferedImage image1, BufferedImage image2) {
        
        if (image1.getWidth() != image2.getWidth()) { return true; }
        if (image1.getHeight() != image2.getHeight()) { return true; }
        
        for (int x = 0; x < image1.getWidth(); x++) {
            for (int y = 0; y < image1.getHeight(); y++) {
                if (image1.getRGB(x, y) != image2.getRGB(x, y)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    public void waitUntilHasChanged(Zone zone) {
        
        BufferedImage image1 = robot.createScreenCapture(zone.getRectangle());
        BufferedImage image2;
        
        do {
            robot.delay(250);
            image2 = robot.createScreenCapture(zone.getRectangle());
        }
        while (!hasChanged(image1, image2));
    }
    
    public boolean isMoving() {
        
        BufferedImage screenCapture = robot.createScreenCapture(new Rectangle(0, 0, Screen.SCREEN_WIDTH, Screen.SCREEN_HEIGHT));
        
        //        try {
        //            BufferedWriter writer     = null;
        //            File           outputfile = new File("moving1.jpg");
        //            ImageIO.write(screenCapture, "png", outputfile);
        //        }
        //        catch (IOException e) {
        //        }
        
        int initialPixelColor1 = screenCapture.getRGB(screenCapture.getWidth() / 2 - 100, screenCapture.getHeight() / 2 - 100);
        int initialPixelColor2 = screenCapture.getRGB(screenCapture.getWidth() / 2, screenCapture.getHeight() / 2);
        int initialPixelColor3 = screenCapture.getRGB(screenCapture.getWidth() / 2 + 100, screenCapture.getHeight() / 2 + 100);
        
        robot.delay(200);
        
        screenCapture = robot.createScreenCapture(new Rectangle(0, 0, Screen.SCREEN_WIDTH, Screen.SCREEN_HEIGHT));
        
        //        try {
        //            BufferedWriter writer     = null;
        //            File           outputfile = new File("moving2.jpg");
        //            ImageIO.write(screenCapture, "png", outputfile);
        //        }
        //        catch (IOException e) {
        //        }
        
        int pixelColor1 = screenCapture.getRGB(screenCapture.getWidth() / 2 - 100, screenCapture.getHeight() / 2 - 100);
        int pixelColor2 = screenCapture.getRGB(screenCapture.getWidth() / 2, screenCapture.getHeight() / 2);
        int pixelColor3 = screenCapture.getRGB(screenCapture.getWidth() / 2 + 100, screenCapture.getHeight() / 2 + 100);
        
        //        System.out.println("px1 " + pixelColor1 + " : " + initialPixelColor1);
        //        System.out.println("px2 " + pixelColor2 + " : " + initialPixelColor2);
        //        System.out.println(" ");
        //        System.out.println("px3 " + pixelColor3 + " : " + initialPixelColor3);
        
        return pixelColor1 != initialPixelColor1 &&
               pixelColor2 != initialPixelColor2; //|| pixelColor3 == initialPixelColor3;
    }
    
    @Data
    public static class Zone {
        
        //        public static final Dimension dim_D         = new Dimension(Toolkit.getDefaultToolkit().getScreenSize());
        //        public static final int       Screen.SCREEN_WIDTH  = (int) dim_D.getWidth();
        //        public static final int       Screen.SCREEN_HEIGHT = (int) dim_D.getHeight();
        
        public static final Zone TOP          = new Zone(0, Screen.SCREEN_WIDTH, 0, Screen.SCREEN_HEIGHT / 2);
        public static final Zone TOP_LEFT     = new Zone(0, Screen.SCREEN_WIDTH / 2, 0, Screen.SCREEN_HEIGHT / 2);
        public static final Zone TOP_RIGHT    = new Zone(Screen.SCREEN_WIDTH / 2, Screen.SCREEN_WIDTH, 0, Screen.SCREEN_HEIGHT / 2);
        public static final Zone BOTTOM       = new Zone(0, Screen.SCREEN_WIDTH, Screen.SCREEN_HEIGHT / 2, Screen.SCREEN_HEIGHT);
        public static final Zone BOTTOM_LEFT  = new Zone(0, Screen.SCREEN_WIDTH / 2, Screen.SCREEN_HEIGHT / 2, Screen.SCREEN_HEIGHT);
        public static final Zone BOTTOM_RIGHT = new Zone(Screen.SCREEN_WIDTH / 2, Screen.SCREEN_WIDTH, Screen.SCREEN_HEIGHT / 2, Screen.SCREEN_HEIGHT);
        public static final Zone LEFT         = new Zone(0, Screen.SCREEN_WIDTH / 2, 0, Screen.SCREEN_HEIGHT);
        public static final Zone RIGHT        = new Zone(Screen.SCREEN_WIDTH / 2, Screen.SCREEN_WIDTH, 0, Screen.SCREEN_HEIGHT);
        public static final Zone MIDDLE       = new Zone(Screen.SCREEN_WIDTH / 4, Screen.SCREEN_WIDTH - Screen.SCREEN_WIDTH / 4, Screen.SCREEN_HEIGHT / 4,
                                                         Screen.SCREEN_HEIGHT - Screen.SCREEN_HEIGHT / 4);
        public static final Zone ALL          = new Zone(0, Screen.SCREEN_WIDTH, 0, Screen.SCREEN_HEIGHT);
        
        private int xMin   = 0;
        private int xMax   = Screen.SCREEN_WIDTH;
        private int yMin   = 0;
        private int yMax   = Screen.SCREEN_HEIGHT;
        private int width  = Screen.SCREEN_WIDTH;
        private int height = Screen.SCREEN_HEIGHT;
        
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
        
        public Rectangle getRectangle() {
            
            return new Rectangle(xMin, yMin, width, height);
        }
    }
}
