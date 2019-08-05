package net.ddns.ksuto.prh.tools;

import java.lang.reflect.ParameterizedType;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;

public abstract class Database<T> {
    
    final Class<T> type;
    
    public Database() {
        
        //noinspection unchecked
        this.type = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }
    
    public static void main(String[] args) {
    
        Database<ShowObjects> database = new Database<ShowObjects>() {};
        
        database.queryWithStatement("SELECT * FROM prh.prh.object");
        database.queryWithRunner("SELECT * FROM prh.prh.object");
    }
    
    private void queryWithRunner(String query) {
        
        System.out.println("Java JDBC PostgreSQL Connexion Test");
        
        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/prh", "postgres", "postgres")) {
            
            System.out.println("Connected to PostgreSQL database!");
            QueryRunner               queryRunner            = new QueryRunner();
            ResultSetHandler<List<T>> showObjectsBeanHandler = new BeanListHandler<T>(type);
            List<T>                   objects                = queryRunner.query(connection, query, showObjectsBeanHandler);
            System.out.println(objects);
        }
        catch (SQLException e) {
            System.out.println("Connection failure.");
            e.printStackTrace();
        }
    }
    
    private void queryWithStatement(String query) {
        
        System.out.println("Java JDBC PostgreSQL Connexion Test");
        
        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/prh", "postgres", "postgres")) {
            
            System.out.println("Connected to PostgreSQL database!");
            
            Statement statement = connection.createStatement();
            System.out.println("Reading objects records...");
            System.out.printf("%-30.30s  %-30.30s%n", "Id", "X Position");
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                System.out.printf("%-30.30s  %-30.30s%n", resultSet.getString("id"), resultSet.getString("position_x"));
            }
        }
        catch (SQLException e) {
            System.out.println("Connection failure.");
            e.printStackTrace();
        }
    }
}
