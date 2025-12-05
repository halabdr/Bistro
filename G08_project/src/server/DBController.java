package server;

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
	private static final String URL = "jdbc:mysql://localhost:3306/matala2?serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "YAah300825#1sH";
    
    private static Connection conn = null; //added this line
    
    //I changed this function
    public static void connect() throws SQLException {
        try {
        	conn = DriverManager.getConnection(URL, USER, PASS);
        	System.out.println("DB connection succeeded");
        	System.out.println("DB password: " + PASS);
        } catch (SQLException e) {
            System.out.println("connecting to DB is failed");
            e.printStackTrace();
        }
    }
    
    //I added this function
    public  static Connection getConnection() throws SQLException {
    	if (conn == null || conn.isClosed())
    	{
    		connect();
    	}
    	return conn;
    }
    
    //I added this
    public static void close()
    {
    	if (conn != null)
    	{
    		try {
    			conn.close();
    			System.out.println("DB connection closed");
    		} catch (SQLException e) {
    			e.printStackTrace();
    		}
    	}
    }
    
    //I change in this
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
    
    public static void updateOrder(int orderNumber, LocalDate newDate, Integer newGuests) {
        if (newDate == null && newGuests == null) {
            // אין מה לעדכן
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
            System.out.println("Order status: " + rows + " row(s) updated");
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}