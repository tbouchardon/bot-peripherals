package net.ddns.ksuto.prh.entities;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Parameter {
    
    private int    precision = 0;
    private double errorRate = 0.0;
    
    public Parameter(int precision, double errorRate) {
        
        this.precision = precision;
        this.errorRate = errorRate;
    }
    
    public static List<Parameter> fromDTOs(List<SearchHistory.Parameter> searchParameters) {
        
        List<Parameter> parameters = new ArrayList<>();
        
        for (SearchHistory.Parameter searchParameter : searchParameters) {
            parameters.add(fromDTO(searchParameter));
        }
        
        return parameters;
    }
    
    public static Parameter fromDTO(SearchHistory.Parameter searchParameter) {
        
        return new Parameter(searchParameter.getPrecision(), searchParameter.getErrorRate());
    }
}
