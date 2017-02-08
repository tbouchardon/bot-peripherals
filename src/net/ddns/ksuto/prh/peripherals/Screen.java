package net.ddns.ksuto.prh.peripherals;

import net.ddns.ksuto.prh.TBoPeripheralRobotHelper;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.imageio.ImageIO;

/**
 * Created by TBO on 15/07/2016.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class Screen extends Peripheral {
    
    public final int i_SCREEN_WIDTH  = (int) dim_D.getWidth();
    public final int i_SCREEN_HEIGHT = (int) dim_D.getHeight();
    public final int iX_START        = i_SCREEN_WIDTH / 2, iY_START = i_SCREEN_HEIGHT / 2 - 20;
    private final Dimension dim_D = new Dimension(Toolkit.getDefaultToolkit().getScreenSize());
    private TBoPeripheralRobotHelper helper;
    
    public Screen(TBoPeripheralRobotHelper TBoPeripheralRobotHelper) {
        
        this.helper = TBoPeripheralRobotHelper;
        this.constants = helper.getConstants();
        this.robot = helper.robot;
    }
    
    public ArrayList<int[]> scanFor(String[] strImages) {
        
        helper.waitIfUserActive();
        return scanFor(strImages, 0, 1, 0, 1);
    }
    
    public ArrayList<int[]> scanFor(String[] strImages, double xMin, double xMax, double yMin, double yMax) {
        
        helper.waitIfUserActive();
        
        System.out.println("Method : scanFor(String[] strImages, " + xMin + ", " + xMax + ", " + yMin + ", " + yMax + ")");
        
        //		logger.log("test");
        
        ArrayList<int[]> alCoords = null;
        
        try {
    
            if (robot == null) { robot = new Robot(); }
            
            //			System.out.print(".");
            
            alCoords = new ArrayList<>();
            
            for (String strImage : strImages) {
                
                double time = System.currentTimeMillis();
                
                System.out.print("         " + strImage);
                
                URL url = this.getClass().getResource(strImage);// + ".png");
                
                BufferedImage biReferenceImage = ImageIO.read(url);
                
                BufferedImage biCapturedScreen = robot.createScreenCapture(new Rectangle(0, 0, i_SCREEN_WIDTH, i_SCREEN_HEIGHT));
                
                //				ImageIO.write(biCapturedScreen, "PNG", new File("D:\\tempShot.png"));
                
                int     iRefRGB = biReferenceImage.getRGB(0, 0);
                int     iCapturedRGB;
                int     iTempCapturedRGB;
                boolean result  = false;
                
                System.out.print(" - > " + (System.currentTimeMillis() - time) + "ms");
                time = System.currentTimeMillis();
                
                for (int iXScreen = (int) (biCapturedScreen.getWidth() * xMin);
                     iXScreen < (biCapturedScreen.getWidth() * xMax) - biReferenceImage.getWidth();
                     iXScreen++) {
                    
                    for (int iYScreen = (int) (biCapturedScreen.getHeight() * yMin);
                         iYScreen < (biCapturedScreen.getHeight() * yMax) - biReferenceImage.getHeight();
                         iYScreen++) {
                        
                        iCapturedRGB = biCapturedScreen.getRGB(iXScreen, iYScreen);
                        
                        if (iCapturedRGB == iRefRGB) {
                            
                            result = true;
                            
                            for (int iXRef = 0; iXRef < biReferenceImage.getWidth(); iXRef++) {
                                for (int iYRef = 0; iYRef < biReferenceImage.getHeight(); iYRef++) {
                                    
                                    iTempCapturedRGB = biCapturedScreen.getRGB(iXRef + iXScreen, iYRef + iYScreen);
                                    iRefRGB = biReferenceImage.getRGB(iXRef, iYRef);
                                    if (iTempCapturedRGB != iRefRGB) {
                                        result = false;
                                        break;
                                    }
                                }
                                
                                if (!result) {
                                    break;
                                }
                            }
                        }
                        
                        if (result) {
                            int[] iTemp = new int[]{iXScreen, iYScreen};
                            alCoords.add(iTemp);
                        }
                        
                        // reinitialize values
                        iRefRGB = biReferenceImage.getRGB(0, 0);
                        result = false;
                    }
                }
                
                System.out.println(" - > " + (System.currentTimeMillis() - time) + "ms");
            }
    
            if (!alCoords.isEmpty()) { return alCoords; }
        }
        catch (IOException | AWTException e) {
            e.printStackTrace();
        }
        
        //		if (strImage.equals("/ressources.pictures/cpasbien/CPasBienDowloadFound.png")) {
        //			logger.log("test");
        //		}
        
        return alCoords;
    }
    
    public boolean waitFor(String[] strImages, int seconds) {
        
        helper.waitIfUserActive();
        
        System.out.println("Method : waitFor(strImages[], " + seconds + ")");
        
        return waitFor(strImages, seconds, 0, 1, 0, 1);
    }
    
    public boolean waitFor(String[] strImages, int seconds, double xMin, double xMax, double yMin, double yMax) {
        
        helper.waitIfUserActive();
        
        System.out.println("Method : waitFor(String[] strImages, " + seconds + ", " + xMin + ", " + xMax + ", " + yMin + ", " + yMax + ")");
        
        double startTime = System.currentTimeMillis();
        
        System.out.println("         Waiting for strImages[]");
        
        while (scanFor(strImages, xMin, xMax, yMin, yMax).isEmpty()) {
            if (seconds != -1 && (System.currentTimeMillis() - startTime) > (seconds * 1000)) {
                System.out.println("         /!\\ Not Found /!\\");
                return false;
            }
            
            if (System.currentTimeMillis() - startTime > 10000) {
                System.out.println("         Time > 10s, looking for everywhere");
                xMin = 0;
                xMax = 1;
                yMin = 0;
                yMax = 1;
            }
            
            //			System.out.print(".");
            delay(constants.i_SCAN_DELAY);
        }
        
        System.out.println("         Found !");
        
        return true;
    }
    
    public boolean waitFor(String[] strImages) {
        
        helper.waitIfUserActive();
        
        System.out.println("Method : waitFor(strImages[])");
        
        return waitFor(strImages, -1, 0, 1, 0, 1);
    }
    
    public boolean waitFor(String[] strImages, double xMin, double xMax, double yMin, double yMax) {
        
        helper.waitIfUserActive();
        
        System.out.println("Method : waitFor(String[] strImages, " + xMin + ", " + xMax + ", " + yMin + ", " + yMax + ")");
        
        return waitFor(strImages, -1, xMin, xMax, yMin, yMax);
    }
}
