package net.ddns.ksuto.prh.entities;

import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

@Data
public class SearchHistory {
    
    private Long            id;
    private String          hash;
    private Integer         version          = 1;
    private Area            optimisedSearchArea;
    private int             iterations       = 0;
    private List<Position>  positions        = new ArrayList<>();
    private List<Parameter> searchParameters = new ArrayList<>();
    
    public SearchHistory() {
    
    }
    
    public SearchHistory(String hash) {
    
        this.id = Math.abs(new Random().nextLong());
        this.hash = hash;
    }
    
    @Data
    public static class Position {
        
        private Long    id;
        private Integer position_x;
        private Integer position_y;
        private Date    date    = new Date();
        private Integer version = 1;
        
        public Position(Integer position_x, Integer position_y) {
    
            this.id = Math.abs(new Random().nextLong());
            this.position_x = position_x;
            this.position_y = position_y;
        }
    }
    
    @Data
    public static class Area {
        
        private Long    id;
        private Integer x_1;
        private Integer x_2;
        private Integer y_1;
        private Integer y_2;
        private Integer version = 1;
        
        public Area(Integer x_1, Integer x_2, Integer y_1, Integer y_2) {
    
            this.id = Math.abs(new Random().nextLong());
            this.x_1 = x_1;
            this.x_2 = x_2;
            this.y_1 = y_1;
            this.y_2 = y_2;
        }
    }
    
    @Data
    public static class Parameter {
        
        private final String  object_hash;
        private       Long    id;
        private       int     precision = 0;
        private       double  errorRate = 0.0;
        private       Integer version   = 1;
        
        public Parameter(String hash) {
    
            this.id = Math.abs(new Random().nextLong());
            this.object_hash = hash;
        }
    }
}
