package database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBTest {

	public static void main(String[] args) {
		try {
			DBController.connect();
			Connection conn = DBController.getConnection();
			
			String sql = "SELECT * FROM `order` LIMIT 5";
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next())
			{
				int orderNumber = rs.getInt("order_number");
				int guests = rs.getInt("number_of_guests");
				System.out.println("Order #" + orderNumber + "guests=" + guests);
			}

			rs.close();
			stmt.close();
			DBController.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}