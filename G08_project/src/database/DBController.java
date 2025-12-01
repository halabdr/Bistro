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

    public List<String> readOrders() throws Exception {
        List<String> list = new ArrayList<>();

        String sql = "SELECT order_number, number_of_guests FROM `Order`";

        Connection conn = getConnection();
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            int num = rs.getInt("order_number");
            int guests = rs.getInt("number_of_guests");
            list.add("Order #" + num + " guests = " + guests);
        }

        rs.close();
        st.close();
        conn.close();

        return list;
    }
}
}
