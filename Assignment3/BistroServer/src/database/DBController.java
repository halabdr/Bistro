<<<<<<< HEAD
package database;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DBController handles all database interactions.
 * This class is the ONLY class that communicates directly with MySQL.
 */
public class DBController {

    private static final String DB_URL =
            "jdbc:mysql://localhost:3306/bistrorestaurant?serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "YOUR_PASSWORD";

    private Connection connection;

    /**
     * Establishes a connection to the database.
     *
     * @throws SQLException if connection fails
     */
    public void connect() throws SQLException {
        connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    /**
     * Closes the database connection.
     */
    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException ignored) {}
    }
    /**
     * Retrieves all tables from the restaurant.
     *
     * @return list of table numbers
     * @throws SQLException if query fails
     */
    public List<Integer> getAllTables() throws SQLException {
        List<Integer> tables = new ArrayList<>();

        String query = "SELECT table_number FROM tables_info";

        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                tables.add(rs.getInt("table_number"));
            }
        }
        return tables;
    }

=======
package database;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DBController handles all database interactions.
 * This class is the ONLY class that communicates directly with MySQL.
 */
public class DBController {

    private static final String DB_URL =
            "jdbc:mysql://localhost:3306/bistrorestaurant?serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "YOUR_PASSWORD";

    private Connection connection;

    /**
     * Establishes a connection to the database.
     *
     * @throws SQLException if connection fails
     */
    public void connect() throws SQLException {
        connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    /**
     * Closes the database connection.
     */
    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException ignored) {}
    }
    /**
     * Retrieves all tables from the restaurant.
     *
     * @return list of table numbers
     * @throws SQLException if query fails
     */
    public List<Integer> getAllTables() throws SQLException {
        List<Integer> tables = new ArrayList<>();

        String query = "SELECT table_number FROM tables_info";

        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                tables.add(rs.getInt("table_number"));
            }
        }
        return tables;
    }
    /**
     * Retrieves tables that can seat at least the given number of diners.
     *
     * @param diners number of diners
     * @return list of matching table numbers
     * @throws SQLException if query fails
     */
    public List<Integer> getTablesByCapacity(int diners) throws SQLException {
        List<Integer> tables = new ArrayList<>();

        String query =
                "SELECT table_number FROM tables_info WHERE seat_capacity >= ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, diners);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tables.add(rs.getInt("table_number"));
            }
        }
        return tables;
    }


>>>>>>> branch 'main' of https://github.com/halabdr/Bistro.git
}