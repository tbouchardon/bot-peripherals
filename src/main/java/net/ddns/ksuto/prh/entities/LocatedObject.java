package net.ddns.ksuto.prh.entities;

import lombok.Data;

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
    
    public abstract String getHash();
}
