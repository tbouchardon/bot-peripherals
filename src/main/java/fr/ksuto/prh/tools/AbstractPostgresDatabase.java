package fr.ksuto.prh.tools;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
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
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.intellij.lang.annotations.Language;

public abstract class AbstractPostgresDatabase {
    
    String database;
    String url;
    
    public AbstractPostgresDatabase(String database) {
        
        this.database = database;
        this.url = "jdbc:postgresql://127.0.0.1:5432/" + database;
        
        try (Connection connection = DriverManager.getConnection(this.url, "postgres", "postgres")) {
            if (connection != null) {
                DatabaseMetaData meta = connection.getMetaData();
                //                System.out.println("[INFO] the driver name is " + meta.getDriverName());
            }
        }
        catch (SQLException e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        
        Integer monInt = new Integer(5);
        System.out.println(monInt);
    }
    
    public <T> T queryOneRecordWithRunner(@Language(value = "PostgreSQL") String query, Class<T> type) {
        
        List<T> list = queryRecordListWithRunner(query, type);
        
        if (list.isEmpty()) { return null; }
        
        return list.get(0);
    }
    
    public <T> T queryOneFieldWithRunner(@Language(value = "PostgreSQL") String query, Class<T> type) {
        
        //        System.out.println("[TRACE] PRH : Java JDBC PostgreSQL Connexion Test");
        
        try (Connection connection = DriverManager.getConnection(this.url, "postgres", "postgres")) {
            
            QueryRunner queryRunner = new QueryRunner();
            return queryRunner.query(connection, query, new ScalarHandler<T>());
        }
        catch (SQLException e) {
            System.out.println("[ERROR] PRH : Query failure : " + e.getMessage());
        }
        
        return null;
    }
    
    public <T> List<T> queryRecordListWithRunner(@Language(value = "PostgreSQL") String query, Class<T> type) {
        
        //        System.out.println("[TRACE] PRH : Java JDBC PostgreSQL Connexion Test");
        
        try (Connection connection = DriverManager.getConnection(this.url, "postgres", "postgres")) {
            
            //            System.out.println("[TRACE] PRH : Connected to PostgreSQL database!");
            QueryRunner               queryRunner     = new QueryRunner();
            ResultSetHandler<List<T>> beanListHandler = new BeanListHandler<>(type);
            
            return queryRunner.query(connection, query, beanListHandler);
        }
        catch (SQLException e) {
            System.out.println("[ERROR] PRH : Query failure : " + e.getMessage());
        }
        
        return null;
    }
    
    public ResultSet queryWithStatement(String query) {
    
        //        System.out.println("[TRACE] PRH : Java JDBC PostgreSQL Connexion Test");
    
        try (Connection connection = DriverManager.getConnection(this.url, "postgres", "postgres")) {
        
            //            System.out.println("[TRACE] PRH : Connected to PostgreSQL database!");
        
            Statement statement = connection.createStatement();
            //            System.out.println("[TRACE] PRH : Reading objects records...");
            //            System.out.printf("%-30.30s  %-30.30s%n", "Id", "X Position");
        
            if (query.toLowerCase().startsWith("select")) { return statement.executeQuery(query); }
            else { statement.executeUpdate(query); }
        
            //            while (resultSet.next()) {
            //                System.out.printf("%-30.30s  %-30.30s%n", resultSet.getString("id"), resultSet.getString("hash"));
            //            }
        }
        catch (SQLException e) {
            System.out.println("[ERROR] PRH : Query failure : " + e.getMessage());
        }
        
        return null;
    }
    
    public String formatDate(Date date) {
        
        return getDatabaseDateFormat().format(date);
    }
    
    public SimpleDateFormat getDatabaseDateFormat() {
        
        return new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    }
}
