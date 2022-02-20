package fr.ksuto.prh.helpers;

import fr.ksuto.prh.entities.Position;
import fr.ksuto.prh.peripherals.Screen;
import lombok.NoArgsConstructor;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class ColorHelper {
    
    public void showColorMatches(int r, int g, int b, int precision) throws AWTException {
        
        List<Position> positions = new ArrayList<>();
        
        Robot robot = new Robot();
        
        BufferedImage capturedScreen = robot.createScreenCapture(Screen.Zone.ALL.getRectangle());
        
        for (int x = 0; x < capturedScreen.getWidth(); x++) {
            for (int y = 0; y < capturedScreen.getHeight(); y++) {
                
                int capturedRGB = capturedScreen.getRGB(x, y);
                int capturedB   = (capturedRGB) & 0xFF;
                int capturedG   = (capturedRGB >> 8) & 0xFF;
                int capturedR   = (capturedRGB >> 16) & 0xFF;
                
                boolean match = r > capturedR - precision && r < capturedR + precision &&
                                g > capturedG - precision && g < capturedG + precision &&
                                b > capturedB - precision && b < capturedB + precision;
                
                if (match) {positions.add(new Position(x, y));}
            }
        }
        
        Painter  painter            = new Painter(false);
        Graphics whiteBoardGraphics = painter.getWhiteBoardGraphics();
        whiteBoardGraphics.setColor(Color.getHSBColor(0, 1, 0.75f));
        
        for (Position point : positions) {
            
            whiteBoardGraphics.drawLine((int) point.getX(),
                                        (int) point.getY(),
                                        (int) point.getX(),
                                        (int) point.getY());
            painter.repaintWhiteBoard();
        }
        
        //                painter.dispose();
    }
}
