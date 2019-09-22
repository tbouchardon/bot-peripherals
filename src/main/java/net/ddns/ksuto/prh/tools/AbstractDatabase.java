package net.ddns.ksuto.prh.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;

public abstract class AbstractDatabase {
    
    public <T> T queryOneWithRunner(String query, Class<T> type) {
        
        List<T> list = queryListWithRunner(query, type);
        
        if (list.isEmpty()) { return null; }
        
        return list.get(0);
    }
    
    public <T> List<T> queryListWithRunner(String query, Class<T> type) {
    
        //        System.out.println("Java JDBC PostgreSQL Connexion Test");
        
        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/prh", "postgres", "postgres")) {
    
            //            System.out.println("Connected to PostgreSQL database!");
            QueryRunner               queryRunner     = new QueryRunner();
            ResultSetHandler<List<T>> beanListHandler = new BeanListHandler<>(type);
    
            return queryRunner.query(connection, query, beanListHandler);
        }
        catch (SQLException e) {
            System.out.println("Query failure.");
            System.out.println("Query failure.");
            e.printStackTrace();
        }
        
        return null;
    }
    
    public ResultSet queryWithStatement(String query) {
    
        //        System.out.println("Java JDBC PostgreSQL Connexion Test");
        
        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/prh", "postgres", "postgres")) {
    
            //            System.out.println("Connected to PostgreSQL database!");
            
            Statement statement = connection.createStatement();
            //            System.out.println("Reading objects records...");
            //            System.out.printf("%-30.30s  %-30.30s%n", "Id", "X Position");
    
            if (query.toLowerCase().startsWith("select")) { return statement.executeQuery(query); }
            else { statement.executeUpdate(query); }
            
            //            while (resultSet.next()) {
            //                System.out.printf("%-30.30s  %-30.30s%n", resultSet.getString("id"), resultSet.getString("hash"));
            //            }
        }
        catch (SQLException e) {
            System.out.println("Query failure.");
            e.printStackTrace();
        }
        
        return null;
    }
    
    public String formatDate(Date date) {
        
        return getDatabaseDateFormat().format(date);
    }
    
    public SimpleDateFormat getDatabaseDateFormat() {
        
        return new SimpleDateFormat("dd-MM-yyyy");
    }
}
