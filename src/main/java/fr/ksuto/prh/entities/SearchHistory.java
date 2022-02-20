package fr.ksuto.prh.entities;

import fr.ksuto.prh.tools.RandomUtils;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    
        this.id = new RandomUtils().getPositiveLong();
        this.hash = hash;
    }
    
    @Data
    public static class Position {
        
        private Long    id;
        private Integer position_x;
        private Integer position_y;
        private Date    date    = new Date();
        private Integer version = 1;
        
        public Position() {
        
        }
        
        public Position(Integer position_x, Integer position_y) {
    
            this.id = new RandomUtils().getPositiveLong();
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
        
        public Area() { }
        
        public Area(Integer x_1, Integer x_2, Integer y_1, Integer y_2) {
    
            this.id = new RandomUtils().getPositiveLong();
            this.x_1 = x_1;
            this.x_2 = x_2;
            this.y_1 = y_1;
            this.y_2 = y_2;
        }
    }
    
    @Data
    public static class Parameter {
        
        private String  object_hash;
        private Long    id;
        private int     precision  = 0;
        private double  error_rate = 0.0d;
        private Integer version    = 1;
        
        public Parameter() { }
        
        public Parameter(String hash) {
    
            this.id = new RandomUtils().getPositiveLong();
            this.object_hash = hash;
        }
    }
}
