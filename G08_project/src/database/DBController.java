package database;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DBController {
	private static final String URL = "jdbc:mysql://localhost:3306/bistrodb?serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "R101016m";
    
    private Connection getConnection() throws Exception {
        return DriverManager.getConnection(URL, USER, PASS);
    }

}
