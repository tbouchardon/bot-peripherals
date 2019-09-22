package net.ddns.ksuto.prh.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ColorBlock extends LocatedObject {
    
    private int size         = 0;
    private int red          = 255;
    private int green        = 255;
    private int blue         = 255;
    private int minBlockSize = 1;
    private int maxBlockSize = Integer.MAX_VALUE;
    
    public ColorBlock() {
    
    }
    
    public ColorBlock(int red, int green, int blue, int minBlockSize, int maxBlockSize) {
        
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.minBlockSize = minBlockSize;
        this.maxBlockSize = maxBlockSize;
    }
    
    public ColorBlock(int red, int green, int blue) {
        
        this.red = red;
        this.green = green;
        this.blue = blue;
    }
    
    @Override
    public String getHash() {
        
        return String.valueOf(hashCode());
    }
}