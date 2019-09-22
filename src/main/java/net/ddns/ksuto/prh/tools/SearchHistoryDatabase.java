package net.ddns.ksuto.prh.tools;

import net.ddns.ksuto.prh.entities.Parameter;
import net.ddns.ksuto.prh.entities.SearchHistory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class SearchHistoryDatabase extends AbstractDatabase {
    
    public static void main(String[] args) {
        
        SearchHistoryDatabase searchHistoryDatabase = new SearchHistoryDatabase();
        
        SearchHistory searchHistory = searchHistoryDatabase.selectSearchHistory("stage_clear.png");
        
        System.out.println(searchHistory);
    }
    
    public SearchHistory createSearchHistory(String hash) {
        
        SearchHistory searchHistory = new SearchHistory(hash);
        
        queryWithStatement("INSERT INTO prh.object " +
                           " VALUES (" + searchHistory.getId() + ", '" + searchHistory.getHash() + "', " + searchHistory.getIterations() + ", " + searchHistory.getVersion() + ")");
        
        return searchHistory;
    }
    
    public void updateOptimisedSearchArea(SearchHistory.Area area, String objectHash) {
        
        queryWithStatement("DELETE FROM prh.area WHERE object_hash = '" + objectHash + "'");
        
        queryWithStatement("INSERT INTO prh.area " +
                           " VALUES (" + area.getId() + ", " + objectHash + ", " + area.getX_1() + ", " + area.getX_2() + ", " + area.getY_1() + ", " + area.getY_2() + ", 1)");
    }
    
    public void addPosition(SearchHistory.Position position, String objectHash) {
        
        queryWithStatement("INSERT INTO prh.position " +
                           " VALUES (" + position.getId() + ", '" + objectHash + "', " + position.getPosition_x() + ", " + position.getPosition_y() + ", " + new Date() + ", 1)");
    }
    
    public SearchHistory selectSearchHistory(String objectHash, boolean chargeArea, boolean chargePositions, boolean chargeSearchParameters, boolean createIfNotExists) {
        
        SearchHistory searchHistory = selectSearchHistory(objectHash);
        
        if (searchHistory == null && createIfNotExists) {
            searchHistory = createSearchHistory(objectHash);
        }
        
        if (searchHistory == null) { return null; }
        
        if (chargeArea) {
            SearchHistory.Area area = selectSearchArea(objectHash);
            searchHistory.setOptimisedSearchArea(area);
        }
        
        if (chargeSearchParameters) {
            List<SearchHistory.Parameter> parameters = selectSearchParameters(objectHash);
            searchHistory.setSearchParameters(parameters);
        }
        
        if (chargePositions) {
            List<SearchHistory.Position> positions = selectPositions(objectHash);
            searchHistory.setPositions(positions);
        }
        
        return searchHistory;
    }
    
    public SearchHistory selectSearchHistory(String objectHash) {
        
        return queryOneWithRunner("SELECT * FROM prh.object WHERE hash = '" + objectHash + "'", SearchHistory.class);
    }
    
    public SearchHistory.Area selectSearchArea(String objectHash) {
        
        return queryOneWithRunner("SELECT * FROM prh.area WHERE object_hash = '" + objectHash + "'", SearchHistory.Area.class);
    }
    
    public List<SearchHistory.Position> selectPositions(String objectHash) {
        
        return queryListWithRunner("SELECT * FROM prh.position WHERE object_hash = '" + objectHash + "'", SearchHistory.Position.class);
    }
    
    public List<SearchHistory.Parameter> selectSearchParameters(String objectHash) {
        
        return queryListWithRunner("SELECT * FROM prh.search_parameter WHERE object_hash = '" + objectHash + "'", SearchHistory.Parameter.class);
    }
    
    public Integer increaseIterations(String objectHash) {
        
        ResultSet resultSet = queryWithStatement("SELECT iterations FROM prh.object WHERE hash = '" + objectHash + "'");
        
        try {
            if (resultSet.next()) {
                
                int iterations = resultSet.getInt("iterations");
                
                iterations++;
                
                queryWithStatement("UPDATE prh.object SET iterations = " + iterations + " WHERE hash = '" + objectHash + "'");
                
                return iterations;
            }
        }
        catch (SQLException e) {
            // TODO : Catcher cette exception correctement !
            e.printStackTrace();
        }
        
        return null;
    }
    
    public void resetIterations(String objectHash) {
        
        queryWithStatement("UPDATE prh.object SET iterations = 0 WHERE hash = '" + objectHash + "'");
    }
    
    public void updateSearchParameters(String objectHash, List<Parameter> parameters) {
        
        queryWithStatement("DELETE FROM prh.search_parameter WHERE object_hash = '" + objectHash + "'");
        
        for (Parameter parameter : parameters) {
            
            SearchHistory.Parameter searchParameter = new SearchHistory.Parameter(objectHash);
            searchParameter.setErrorRate(parameter.getErrorRate());
            searchParameter.setPrecision(parameter.getPrecision());
            
            queryWithStatement("INSERT INTO prh.search_parameter " +
                               " VALUES (" + searchParameter.getId() + ", '" + searchParameter.getObject_hash() + "', " + searchParameter.getPrecision() + ", " + searchParameter.getErrorRate() + "," +
                               " 1)");
        }
    }
}
