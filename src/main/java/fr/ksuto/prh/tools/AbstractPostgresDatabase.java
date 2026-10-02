package fr.ksuto.prh.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fr.ksuto.commons.PropertiesLoader;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.intellij.lang.annotations.Language;

public abstract class AbstractPostgresDatabase {
    
    private final Properties properties;
    final Logger logger = LoggerFactory.getLogger(getClass());
    String        database;
    String        url;
    
    public AbstractPostgresDatabase(String database) {
        
        this.properties = PropertiesLoader.load("prh");
        
        this.database = database;
        this.url = "jdbc:postgresql://127.0.0.1:5432/" + database;
        
        testDatabaseConnexion();
    }
    
    public AbstractPostgresDatabase(String url, String database) {
        
        this.properties = PropertiesLoader.load("prh");
        
        this.database = database;
        this.url = "jdbc:postgresql://" + url + "/" + database;
        
        testDatabaseConnexion();
    }
    
    public String formatDate(Date date) {
        
        return getDatabaseDateFormat().format(date);
    }
    
    public <T> T queryOneFieldWithRunner(@Language(value = "PostgreSQL") String query, Class<T> type) {
        
        //        System.out.println("[TRACE] PRH : Java JDBC PostgreSQL Connexion Test");
        
        try (Connection connection = DriverManager.getConnection(this.url,
                                                                 properties.getProperty("ksuto.prh.database.login"),
                                                                 properties.getProperty("ksuto.prh.database.password"))) {
            
            QueryRunner queryRunner = new QueryRunner();
            return queryRunner.query(connection, query, new ScalarHandler<T>());
        }
        catch (SQLException e) {
            logger.error("Query failure : " + e.getMessage());
        }
        
        return null;
    }
    
    public <T> T queryOneRecordWithRunner(@Language(value = "PostgreSQL") String query, Class<T> type) {
        
        List<T> list = queryRecordListWithRunner(query, type);
        
        if (list.isEmpty()) {return null;}
        
        return list.get(0);
    }
    
    public <T> List<T> queryRecordListWithRunner(@Language(value = "PostgreSQL") String query, Class<T> type) {
        
        //        System.out.println("[TRACE] PRH : Java JDBC PostgreSQL Connexion Test");
        
        try (Connection connection = DriverManager.getConnection(this.url,
                                                                 properties.getProperty("ksuto.prh.database.login"),
                                                                 properties.getProperty("ksuto.prh.database.password"))) {
            
            //            System.out.println("[TRACE] PRH : Connected to PostgreSQL database!");
            QueryRunner               queryRunner     = new QueryRunner();
            ResultSetHandler<List<T>> beanListHandler = new BeanListHandler<>(type);
            
            return queryRunner.query(connection, query, beanListHandler);
        }
        catch (SQLException e) {
            logger.error("Query failure : " + e.getMessage());
        }
        
        return null;
    }
    
    public ResultSet queryWithStatement(String query) {
        
        //        System.out.println("[TRACE] PRH : Java JDBC PostgreSQL Connexion Test");
        
        try (Connection connection = DriverManager.getConnection(this.url,
                                                                 properties.getProperty("ksuto.prh.database.login"),
                                                                 properties.getProperty("ksuto.prh.database.password"));
             Statement statement = connection.createStatement()) {
            
            //            System.out.println("[TRACE] PRH : Connected to PostgreSQL database!");
            
            //            System.out.println("[TRACE] PRH : Reading objects records...");
            //            System.out.printf("%-30.30s  %-30.30s%n", "Id", "X Position");
            
            if (query.toLowerCase().startsWith("select")) {return statement.executeQuery(query);}
            else {statement.executeUpdate(query);}
            
            //            while (resultSet.next()) {
            //                System.out.printf("%-30.30s  %-30.30s%n", resultSet.getString("id"), resultSet.getString("hash"));
            //            }
        }
        catch (SQLException e) {
            logger.error("Query failure : " + e.getMessage());
        }
        
        return null;
    }
    
    private void testDatabaseConnexion() {
        
        try (Connection connection = DriverManager.getConnection(this.url,
                                                                 properties.getProperty("ksuto.prh.database.login"),
                                                                 properties.getProperty("ksuto.prh.database.password"))) {
            if (connection != null) {
                DatabaseMetaData meta = connection.getMetaData();
                //                System.out.println("[INFO] the driver name is " + meta.getDriverName());
            }
        }
        catch (SQLException e) {
            logger.error("" + e.getMessage());
        }
    }
    
    public SimpleDateFormat getDatabaseDateFormat() {
        
        return new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    }
}
