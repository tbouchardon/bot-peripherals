package net.ddns.ksuto.prh.helpers;

import net.ddns.ksuto.prh.entities.Picture;
import net.ddns.ksuto.prh.entities.Position;

import java.awt.*;
import java.awt.image.BufferedImage;

public class PictureSearch extends AbstractSeeker<PictureSearch, Picture> {
    
    public PictureSearch() throws AWTException {
        
        super();
    }
    
    @Override
    boolean searchObject(BufferedImage capturedScreen, Position currentPosition, Picture picture) {
        
        if (isPictureFound(capturedScreen, picture, currentPosition)) {
            
            picture.getPositions().add(new Position(currentPosition.getX() + searchZone.getXMin(), currentPosition.getY() + searchZone.getYMin()));
            picture.setPresent(true);
            return true;
        }
        return false;
    }
    
    @Override
    boolean isObjectFound(BufferedImage capturedScreen, Position currentPosition, Picture object) {
        
        return isPictureFound(capturedScreen, object, currentPosition);
    }
    
    public PictureSearch addPictureWithUrl(String url, Object o) {
        
        Picture picture = new Picture(url);
        picture.setObject(o);
        
        objects.add(picture);
        
        return this;
    }
    
    public PictureSearch addPictureWithUrl(String url) {
        
        this.objects.add(new Picture(url));
        
        return this;
    }
    
    public PictureSearch addPicturesWithUrls(String[] urls) {
        
        for (String url : urls) { addPictureWithUrl(url); }
        
        return this;
    }
    
    private boolean isPictureFound(BufferedImage capturedScreen, Picture picture, Position currentPosition) {
        
        if (picture.getReferenceImage().getHeight() + currentPosition.getY() >= capturedScreen.getHeight()) { return false; }
        if (picture.getReferenceImage().getWidth() + currentPosition.getX() >= capturedScreen.getWidth()) { return false; }
        
        int    tempCapturedRGB;
        int    refRGB      = picture.getReferenceImage().getRGB(0, 0);
        int    capturedRGB = capturedScreen.getRGB(currentPosition.getX(), currentPosition.getY());
        double area        = picture.getReferenceImage().getHeight() * picture.getReferenceImage().getWidth();
        double errorNumber = 0;
        
        boolean found = false;
        
        if (isMatch(capturedRGB, refRGB)) {
            
            found = true;

yxLoop:
            for (int yRef = 0; yRef < picture.getReferenceImage().getHeight(); yRef++) {
                
                for (int xRef = 0; xRef < picture.getReferenceImage().getWidth(); xRef++) {
                    
                    tempCapturedRGB = capturedScreen.getRGB(xRef + currentPosition.getX(), yRef + currentPosition.getY());
                    refRGB = picture.getReferenceImage().getRGB(xRef, yRef);
                    
                    boolean match = isMatch(tempCapturedRGB, refRGB);
                    if (!match) { errorNumber++; }
                    
                    double errorRate = errorNumber / area;
                    
                    if (errorRate > allowedErrorRate) {
                        found = false;
                        break yxLoop;
                    }
                }
            }
        }
        
        return found;
    }
}

