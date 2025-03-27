package fr.ksuto.prh.helpers;

import fr.ksuto.prh.entities.ColorBlock;
import fr.ksuto.prh.entities.Position;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ColorSearch extends AbstractSeeker<ColorSearch, ColorBlock> {
    
    public ColorSearch() throws AWTException {
        
        super();
    }
    
    public static ColorSearch getDefault(int red, int green, int blue, int minBlockSize, int maxBlockSize) throws AWTException {
        
        ColorSearch colorSearch = new ColorSearch();
        
        colorSearch.addColorBlock(red, green, blue, minBlockSize, maxBlockSize);
        
        colorSearch.setPrecision(20)
                .setAllowedErrorRate(0.05);
        
        return colorSearch;
    }
    
    public static ColorSearch getDefault(int red, int green, int blue) throws AWTException {
        
        ColorSearch colorSearch = new ColorSearch();
        
        colorSearch.addColorBlock(red, green, blue);
        
        colorSearch.setPrecision(20)
                .setAllowedErrorRate(0.05);
        
        return colorSearch;
    }
    
    @Override
    public AbstractSeeker<ColorSearch, ColorBlock> findWorkingParameters(int numberOfMatches, int numberOfNoChangeLoops) {
        
        logger.sysOutError("Method not implemented !");
        
        return null;
    }
    
    @Override
    public AbstractSeeker<ColorSearch, ColorBlock> findWorkingParameters(int numberOfMatches) {
        
        return findWorkingParameters(numberOfMatches, 1);
    }
    
    @Override
    boolean searchObject(BufferedImage capturedScreen, Position currentPosition, ColorBlock colorBlock) {
        
        if (isBlockFound(capturedScreen, currentPosition, colorBlock)) {
            
            Position position = new Position(currentPosition.getX() + searchZone.getXMin(), currentPosition.getY() + searchZone.getYMin());
            position.setObject(currentPosition.getObject());
            colorBlock.getPositions().add(position);
            int cote = (int) Math.sqrt(colorBlock.getSize());
            colorBlock.setHeight(colorBlock.getHeight() > cote ? colorBlock.getHeight() : cote);
            colorBlock.setWidth(colorBlock.getWidth() > cote ? colorBlock.getWidth() : cote);
            colorBlock.setPresent(true);
            return true;
        }
        return false;
    }
    
    @Override
    boolean isObjectFound(BufferedImage capturedScreen, Position currentPosition, ColorBlock object) {
        
        return isBlockFound(capturedScreen, currentPosition, object);
    }
    
    public ColorSearch addColorBlock(int red, int green, int blue) {
        
        objects.add(new ColorBlock(red, green, blue));
        
        return this;
    }
    
    public ColorSearch addColorBlock(int red, int green, int blue, int minBlockSize, int maxBlockSize) {
        
        objects.add(new ColorBlock(red, green, blue, minBlockSize, maxBlockSize));
        
        return this;
    }
    
    private boolean isBlockFound(BufferedImage capturedScreen, Position currentPosition, ColorBlock colorBlock) {
        
        if (currentPosition.getY() >= capturedScreen.getHeight()) {return false;}
        if (currentPosition.getX() >= capturedScreen.getWidth()) {return false;}
        
        int r, g, b;
        int xDelta    = 0, yDelta = 0;
        int blockSize = 0;
        
        while (true) {
            
            int capturedRGB = capturedScreen.getRGB(currentPosition.getX() + xDelta, currentPosition.getY() + yDelta);
            b = (capturedRGB) & 0xFF;
            g = (capturedRGB >> 8) & 0xFF;
            r = (capturedRGB >> 16) & 0xFF;
            
            boolean match;
            if (precision != null) {
                
                match = r > colorBlock.getRed() - precision && r < colorBlock.getRed() + precision &&
                        g > colorBlock.getGreen() - precision && g < colorBlock.getGreen() + precision &&
                        b > colorBlock.getBlue() - precision && b < colorBlock.getBlue() + precision;
            }
            else {
                
                match = r == colorBlock.getRed() && g == colorBlock.getGreen() && b == colorBlock.getBlue();
            }
            
            if (match) {
                
                blockSize += 1;
                xDelta++;
                if (currentPosition.getX() + xDelta >= capturedScreen.getWidth()) {
                    xDelta = 0;
                    yDelta++;
                }
            }
            else {
                
                if (xDelta == 0) {
                    break;
                }
                
                xDelta = 0;
                yDelta++;
                if (currentPosition.getY() >= capturedScreen.getHeight()) {break;}
            }
        }
        if (blockSize > colorBlock.getSize()) {colorBlock.setSize(blockSize);}
        
        currentPosition.setObject(blockSize);
        
        return blockSize >= colorBlock.getMinBlockSize() && blockSize <= colorBlock.getMaxBlockSize();
    }
    
    public static void main(String[] args) throws AWTException {
    
        ColorSearch colorSearch = ColorSearch.getDefault(0, 255, 0, 256, 256);
        colorSearch.search();
        colorSearch.getAllResults();
    }
}
