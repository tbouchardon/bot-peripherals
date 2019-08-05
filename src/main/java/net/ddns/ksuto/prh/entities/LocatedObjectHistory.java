package net.ddns.ksuto.prh.entities;

import lombok.Data;

import java.util.Date;

@Data
public class LocatedObjectHistory {
    
    private Long    id;
    private String  hash;
    private Integer version;
    
    @Data
    public class Position {
        
        Integer position_x;
        Integer position_y;
        Date    date;
        Integer version;
    }
}
