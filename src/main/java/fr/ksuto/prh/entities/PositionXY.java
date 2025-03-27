package fr.ksuto.prh.entities;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class PositionXY {
    
    protected int x = -1;
    protected int y = -1;
    
    public PositionXY() {
    
    }
    
    public PositionXY(int x, int y) {
        
        this.x = x;
        this.y = y;
    }
    
    public void incX() {
        
        x++;
    }
    
    public void incY() {
        
        y++;
    }
}