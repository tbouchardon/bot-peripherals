package fr.ksuto.prh.entities;

import fr.ksuto.prh.peripherals.Mouse;
import lombok.Data;

import java.awt.*;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Data
public abstract class LocatedObject {
    
    public  Integer        precision        = null;
    public  Double         allowedErrorRate = null;
    private boolean        present          = false;
    private List<Position> positions        = new ArrayList<>();
    private int            width            = 0;
    private int            height           = 0;
    
    public void clickFirst() {
        
        clickNth(1);
    }
    
    public void clickNth(int nth) {
    
        if (positions.isEmpty()) {return;}
        if (positions.size() < nth) {return;}
    
        try {
            Mouse mouse = new Mouse();
        
            int xOffest = (int) Math.floor(Math.random() * getWidth());
            int yOffest = (int) Math.floor(Math.random() * getHeight());
        
            mouse.naturalMoveTo(positions.get(nth - 1).getX() + xOffest, positions.get(nth - 1).getY() + yOffest);
            mouse.clickLeft();
        }
        catch (AWTException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
    
    public boolean hasAnyResults() {
        
        return positions.size() > 0;
    }
    
    public Position getFirstPosition() {
        
        if (!hasAnyResults()) {return null;}
        return positions.get(0);
    }
    
    public abstract String getHash();
    
    public int getNumberOfResults() {
        
        return positions.size();
    }
}
