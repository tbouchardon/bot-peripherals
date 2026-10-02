package fr.ksuto.prh.helpers;

import fr.ksuto.commons.awt.Painter;
import fr.ksuto.prh.capture.Capture;
import fr.ksuto.prh.capture.Frame;
import fr.ksuto.prh.capture.Rgb;
import fr.ksuto.prh.entities.Position;
import fr.ksuto.prh.peripherals.Screen;
import lombok.NoArgsConstructor;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class ColorHelper {
    
    public void showColorMatches(int r, int g, int b, int precision) throws AWTException {
        
        List<Position> positions = new ArrayList<>();
        
        Frame capturedScreen = Capture.zone(Screen.Zone.ALL);
        
        for (int x = 0; x < capturedScreen.width(); x++) {
            for (int y = 0; y < capturedScreen.height(); y++) {
                
                if (Rgb.isClose(capturedScreen.rgb(x, y), r, g, b, precision)) {positions.add(new Position(x, y));}
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
