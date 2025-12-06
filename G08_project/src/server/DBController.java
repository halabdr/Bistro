package server;

import java.util.function.Consumer;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.sql.PreparedStatement;
import java.sql.Date;
import java.time.LocalDate;

public class DBController {
	
	//Logger used to print messages 
	private static Consumer<String> logger = System.out::println;
	
	private static final String URL = "jdbc:mysql://localhost:3306/matala2?serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "YAah300825#1sH";
    
    private static Connection conn = null;
    
    //Allows to the server to "choose" it's own logger
    public static void setLogger(Consumer<String> log) {
    	if (log !=null) {
    		logger = log;
    	}
    }
    
    //Helper to log messages via the current logger
    private static void log(String msg) {
    	logger.accept(msg);
    }
    
    //Opens connection to DB 
    public static void connect() throws SQLException {
        try {
        	conn = DriverManager.getConnection(URL, USER, PASS);
        	log("DB connection succeeded");
        	log("DB password: " + PASS);
        } catch (SQLException e) {
            log("connecting to DB is failed");
            e.printStackTrace();
        }
    }
    
    //Returns the shared DB connection  
    public  static Connection getConnection() throws SQLException {
    	if (conn == null || conn.isClosed())
    	{
    		connect();
    	}
    	return conn;
    }
    
    public static void close()
    {
    	if (conn != null)
    	{
    		try {
    			conn.close();
    			log("DB connection closed");
    		} catch (SQLException e) {
    			e.printStackTrace();
    		}
    	}
    }
    
    //Reads all orders from the DB and returns them as a list  
    public static List<String> readOrders() {
        List<String> orders = new ArrayList<>();

        String sql = "SELECT order_number, order_date, number_of_guests, " + "confirmation_code, subscriber_id, date_of_placing_order " + "FROM matala2.`order`";
        
        try {
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                int orderNumber = rs.getInt("order_number");
                String orderDate = rs.getDate("order_date").toString();
                int numberOfGuests = rs.getInt("number_of_guests");
                String confCode = rs.getString("confirmation_code");
                
                //Check in case suscriber_id is NULL
                Object subObj = rs.getObject("subscriber_id");
                String subscriberId = (subObj == null) ? "" : subObj.toString();
                
                String placingDate = rs.getDate("date_of_placing_order").toString();
                
                String row = orderNumber + "," + orderDate + "," + numberOfGuests + "," + confCode + "," + subscriberId + "," + placingDate;
                
                orders.add(row);
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
        	e.printStackTrace();
        }
        return orders;
    }
    
    //Get the order_date and date_of_placing_order according to order number
    public static LocalDate[] getOrderDates(int orderNumber) {
        String sql = "SELECT order_date, date_of_placing_order FROM `order` WHERE order_number = ?";

        try {
            Connection conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, orderNumber);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        LocalDate orderDate   = rs.getDate("order_date").toLocalDate();
                        LocalDate placingDate = rs.getDate("date_of_placing_order").toLocalDate();
                        return new LocalDate[] { orderDate, placingDate };
                    }
                }
            }
        } catch (SQLException e) {
            log("DB error (getOrderDates): " + e.getMessage());
        }

        return null;
    }
    
  //Update an order in the DB 
    public static void updateOrder(int orderNumber, LocalDate newDate, Integer newGuests) {
        //If there is nothing to update just return
    	if (newDate == null && newGuests == null) {
            return;
        }

        StringBuilder sql = new StringBuilder("UPDATE `order` SET ");
        List<Object> params = new ArrayList<>();

        if (newDate != null) {
            sql.append("order_date = ?");
            params.add(Date.valueOf(newDate));
        }

        if (newGuests != null) {
            if (!params.isEmpty()) {
                sql.append(", ");
            }
            sql.append("number_of_guests = ?");
            params.add(newGuests);
        }

        sql.append(" WHERE order_number = ?");
        params.add(orderNumber);

        try {
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql.toString());

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            int rows = ps.executeUpdate();
            log("Order status: " + rows + " row(s) updated");
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}