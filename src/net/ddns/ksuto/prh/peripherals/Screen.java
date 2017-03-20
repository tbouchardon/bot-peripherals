package net.ddns.ksuto.prh.peripherals;

import net.ddns.ksuto.prh.TBoPeripheralRobotHelper;
import net.ddns.ksuto.prh.entities.ColorBlock;
import net.ddns.ksuto.prh.properties.Constants;
import net.ddns.ksuto.prh.tools.Debug;

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
    
    private final Dimension dim_D           = new Dimension(Toolkit.getDefaultToolkit().getScreenSize());
    public final  int       i_SCREEN_WIDTH  = (int) dim_D.getWidth();
    public final  int       i_SCREEN_HEIGHT = (int) dim_D.getHeight();
    public final  int       iX_START        = i_SCREEN_WIDTH / 2, iY_START = i_SCREEN_HEIGHT / 2 - 20;
    private TBoPeripheralRobotHelper helper;
    
    public Screen(TBoPeripheralRobotHelper TBoPeripheralRobotHelper) {
        
        this.helper = TBoPeripheralRobotHelper;
        this.constants = helper.getConstants();
        this.robot = helper.robot;
    }
    
    public ArrayList<int[]> scanFor(String strImage) {
        
        String[] strImages = new String[]{strImage};
        
        return scanFor(strImages);
    }
    
    public ArrayList<int[]> scanFor(String[] strImages) {
        
        helper.waitIfUserActive();
        return scanFor(strImages, 0, 1, 0, 1);
    }
    
    public ArrayList<int[]> scanFor(String strImage, double xMin, double xMax, double yMin, double yMax) {
        
        String[] strImages = new String[]{strImage};
        
        return scanFor(strImages, xMin, xMax, yMin, yMax);
    }
    
    public ArrayList<int[]> scanFor(String[] strImages, double xMin, double xMax, double yMin, double yMax) {
        
        helper.waitIfUserActive();
    
        Debug.sout("String[] strImages, " + xMin + ", " + xMax + ", " + yMin + ", " + yMax + ")");
        
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
    
                Debug.sout((System.currentTimeMillis() - time) + "ms");
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
    
    public boolean waitFor(String strImage, int seconds) {
    
        Debug.sout("String[] strImage, " + seconds + ")");
        
        String[] strImages = new String[]{strImage};
        
        return waitFor(strImages, seconds);
    }
    
    public boolean waitFor(String[] strImages, int seconds) {
        
        helper.waitIfUserActive();
    
        Debug.sout("String[] strImages, " + seconds + ")");
        
        return waitFor(strImages, seconds, 0, 1, 0, 1);
    }
    
    public boolean waitFor(String strImage, int seconds, double xMin, double xMax, double yMin, double yMax) {
    
        Debug.sout("String strImage, " + seconds + ", " + xMin + ", " + xMax + ", " + yMin + ", " + yMax + ")");
        
        String[] strImages = new String[]{strImage};
        
        return waitFor(strImages, seconds, xMin, xMax, yMax, yMax);
    }
    
    public boolean waitFor(String[] strImages, int seconds, double xMin, double xMax, double yMin, double yMax) {
        
        helper.waitIfUserActive();
    
        Debug.sout("String[] strImages, " + seconds + ", " + xMin + ", " + xMax + ", " + yMin + ", " + yMax + ")");
        
        double startTime = System.currentTimeMillis();
    
        Debug.sout("Waiting for strImages[]");
        
        while (scanFor(strImages, xMin, xMax, yMin, yMax).isEmpty()) {
            if (seconds != -1 && (System.currentTimeMillis() - startTime) > (seconds * 1000)) {
                Debug.sout("/!\\ Not Found /!\\");
                return false;
            }
            
            if (System.currentTimeMillis() - startTime > 10000) {
                Debug.sout("Time > 10s, looking for everywhere");
                xMin = 0;
                xMax = 1;
                yMin = 0;
                yMax = 1;
            }
            
            //			System.out.print(".");
            delay(Constants.i_SCAN_DELAY);
        }
    
        Debug.sout("Found !");
        
        return true;
    }
    
    public boolean waitFor(String strImage) {
        
        String[] strImages = new String[]{strImage};
    
        Debug.sout();
        
        return waitFor(strImages);
    }
    
    public boolean waitFor(String[] strImages) {
        
        helper.waitIfUserActive();
    
        Debug.sout();
        
        return waitFor(strImages, -1, 0, 1, 0, 1);
    }
    
    public boolean waitFor(String strImage, double xMin, double xMax, double yMin, double yMax) {
        
        String[] strImages = new String[]{strImage};
    
        Debug.sout("strImage, " + xMin + ", " + xMax + ", " + yMin + ", " + yMax + ")");
        
        return waitFor(strImages, xMin, xMax, yMin, yMax);
    }
    
    public boolean waitFor(String[] strImages, double xMin, double xMax, double yMin, double yMax) {
        
        helper.waitIfUserActive();
    
        Debug.sout("String[] strImages, " + xMin + ", " + xMax + ", " + yMin + ", " + yMax + ")");
        
        return waitFor(strImages, -1, xMin, xMax, yMin, yMax);
    }
    
    public ArrayList<ColorBlock> searchColorBlocks(int iRed, int iGreen, int iBlue, int iMinSize, int iMaxSize) throws AWTException {
        
        Robot         robot            = new Robot();
        BufferedImage biCapturedScreen = robot.createScreenCapture(new Rectangle(0, 0, i_SCREEN_WIDTH, i_SCREEN_HEIGHT));
        
        ArrayList<ColorBlock> colorBlocks = new ArrayList<>();
        int                   iCapturedRGB;
        int                   r, g, b;
        boolean               bFound;
        Debug.sout(iRed + " " + iGreen + " " + iBlue);
        
        for (int iX = 0; iX < i_SCREEN_WIDTH; iX++) {
            for (int iY = 0; iY < i_SCREEN_HEIGHT; iY++) {
                
                if (!colorBlocks.isEmpty()) {
                    for (ColorBlock colorBlock : colorBlocks) {
                        if (iY >= colorBlock.yPosition && iY <= colorBlock.yPosition + colorBlock.ySize && iX >= colorBlock.xPosition && iX <= colorBlock.xPosition + colorBlock.xSize) {
                            iY = colorBlock.yPosition + colorBlock.ySize;
                        }
                    }
                }
                
                
                ColorBlock colorBlock = new ColorBlock();
                
                iCapturedRGB = biCapturedScreen.getRGB(iX, iY);
                b = (iCapturedRGB) & 0xFF;
                g = (iCapturedRGB >> 8) & 0xFF;
                r = (iCapturedRGB >> 16) & 0xFF;
                
                int xDelta = 0;
                int yDelta = 0;
                
                if (r == iRed && g == iGreen && b == iBlue) {
                    bFound = true;
                    while (bFound) {
                        iCapturedRGB = biCapturedScreen.getRGB(iX + ++xDelta, iY + yDelta);
                        b = (iCapturedRGB) & 0xFF;
                        g = (iCapturedRGB >> 8) & 0xFF;
                        r = (iCapturedRGB >> 16) & 0xFF;
                        if (!(r == iRed && g == iGreen && b == iBlue)) {
                            colorBlock.ySize = yDelta;
                            yDelta++;
                            if (xDelta != 1) { colorBlock.xSize = xDelta; }
                            else { bFound = false; }
                            xDelta = 0;
                        }
                    }
                    
                    colorBlock.xPosition = iX;
                    colorBlock.yPosition = iY;
                    colorBlocks.add(colorBlock);
                }
            }
        }
        
        if (!colorBlocks.isEmpty()) {
            int iSize = colorBlocks.size();
            for (int i = 0; i < iSize; i++) {
                ColorBlock colorBlock = colorBlocks.get(i);
                if (((colorBlock.xSize < iMinSize || colorBlock.ySize < iMinSize) && iMinSize != 0) || ((colorBlock.xSize > iMaxSize || colorBlock.ySize > iMaxSize) && iMaxSize != 0)) {
                    colorBlocks.remove(colorBlock);
                    i--;
                    iSize--;
                }
            }
        }
        
        return colorBlocks;
    }
    
    private boolean checkColor(int iX, int iY, BufferedImage biCapturedScreen, int iColor) {
        
        int iCapturedRGB = biCapturedScreen.getRGB(iX, iY);
        // Debug.sout("Peripheral > (" + iX + ", " + iY + ") Searching : " + iColor + ", found : " + iCapturedRGB + ".");
        return (iColor == iCapturedRGB);
    }
    
    private boolean checkColor(int iX, int iY, int iColor) {
        
        BufferedImage biCapturedScreen = robot.createScreenCapture(new Rectangle(0, 0, i_SCREEN_WIDTH, i_SCREEN_HEIGHT));
        return checkColor(iX, iY, biCapturedScreen, iColor);
    }
}
