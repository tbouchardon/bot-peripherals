package fr.ksuto.prh.tools;

import fr.ksuto.logger.ConsoleLogger;

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

public abstract class AbstractSQLiteDatabase {
    ConsoleLogger logger = new ConsoleLogger();
    
    String database;
    String url;
    
    public AbstractSQLiteDatabase(String database) {
        
        this.database = database;
        
        this.url = "jdbc:sqlite:/" + database;
        
        try (Connection conn = DriverManager.getConnection(this.url)) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                logger.sysOutInfo("the driver name is " + meta.getDriverName());
            }
        }
        catch (SQLException e) {
            logger.sysOutError("" + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        
        Integer monInt = 5;
        System.out.println(monInt);
    }
    
    public <T> T queryOneRecordWithRunner(@Language(value = "PostgreSQL") String query, Class<T> type) {
        
        List<T> list = queryRecordListWithRunner(query, type);
        
        if (list.isEmpty()) { return null; }
        
        return list.get(0);
    }
    
    public <T> T queryOneFieldWithRunner(@Language(value = "PostgreSQL") String query, Class<T> type) {
        
        try (Connection connection = DriverManager.getConnection(this.url)) {
            
            QueryRunner queryRunner = new QueryRunner();
            return queryRunner.query(connection, query, new ScalarHandler<T>());
        }
        catch (SQLException e) {
            logger.sysOutError("Query failure : " + e.getMessage());
        }
        
        return null;
    }
    
    public <T> List<T> queryRecordListWithRunner(@Language(value = "PostgreSQL") String query, Class<T> type) {
        
        try (Connection connection = DriverManager.getConnection(this.url)) {
            
            QueryRunner               queryRunner     = new QueryRunner();
            ResultSetHandler<List<T>> beanListHandler = new BeanListHandler<>(type);
            
            return queryRunner.query(connection, query, beanListHandler);
        }
        catch (SQLException e) {
            logger.sysOutError("Query failure : " + e.getMessage());
        }
        
        return null;
    }
    
    public ResultSet queryWithStatement(String query) {
    
        try (Connection connection = DriverManager.getConnection(this.url);
             Statement statement = connection.createStatement()) {
        
            if (query.toLowerCase().startsWith("select")) {return statement.executeQuery(query);}
            else {statement.executeUpdate(query);}
        }
        catch (SQLException e) {
            logger.sysOutError("Query failure : " + e.getMessage());
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
