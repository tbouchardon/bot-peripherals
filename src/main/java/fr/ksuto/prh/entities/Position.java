package fr.ksuto.prh.entities;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Position extends PositionXY {
    
    private Object  object;
    private boolean explored = false;
    private boolean hasMoved = false;
    
    public Position() {
        
        super();
    }
    
    public Position(int x, int y) {
        
        super(x, y);
    }
}