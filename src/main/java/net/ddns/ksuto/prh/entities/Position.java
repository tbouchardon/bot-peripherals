package net.ddns.ksuto.prh.entities;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Position {
    
    private Object  object;
    private boolean explored = false;
    private boolean hasMoved = false;
    private int     x        = -1;
    private int     y        = -1;
    
    public Position() {
    
    }
    
    public Position(int x, int y) {
        
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