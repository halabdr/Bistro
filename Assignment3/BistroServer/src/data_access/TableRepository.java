package data_access;
import connection.MySQLConnectionPool;
import connection.PooledConnection;
import common.Message;
import entities.Table;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Table Repository for managing restaurant tables.
 * Handles table retrieval, addition, updates, and status management.
 */
public class TableRepository {

    /**
     * Gets all tables in the restaurant.
     * 
     * @param request Message (empty data)
     * @return Message with List of all Table objects
     */
    public Message getAllTables(Message request) {
        MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
        PooledConnection pConn = null;

        try {
            pConn = pool.getConnection();
            if (pConn == null) {
                return Message.fail("GET_TABLES", "Database connection failed");
            }

            Connection conn = pConn.getConnection();
            
            String sql = "SELECT * FROM tables_info ORDER BY table_number";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            List<Table> tables = new ArrayList<>();
            while (rs.next()) {
                tables.add(extractTableFromResultSet(rs));
            }

            rs.close();
            ps.close();

            return Message.ok("GET_TABLES", tables);

        } catch (SQLException e) {
            e.printStackTrace();
            return Message.fail("GET_TABLES", "Database error: " + e.getMessage());
        } finally {
            pool.releaseConnection(pConn);
        }
    }

    /**
     * Gets all available tables.
     * 
     * @param request Message (empty data)
     * @return Message with List of available Table objects
     */
    public Message getAvailableTables(Message request) {
        MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
        PooledConnection pConn = null;

        try {
            pConn = pool.getConnection();
            if (pConn == null) {
                return Message.fail("GET_AVAILABLE_TABLES", "Database connection failed");
            }

            Connection conn = pConn.getConnection();
            
            String sql = "SELECT * FROM tables_info WHERE table_status = 'AVAILABLE' ORDER BY table_number";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            List<Table> tables = new ArrayList<>();
            while (rs.next()) {
                tables.add(extractTableFromResultSet(rs));
            }

            rs.close();
            ps.close();

            return Message.ok("GET_AVAILABLE_TABLES", tables);

        } catch (SQLException e) {
            e.printStackTrace();
            return Message.fail("GET_AVAILABLE_TABLES", "Database error: " + e.getMessage());
        } finally {
            pool.releaseConnection(pConn);
        }
    }

    /**
     * Adds a new table to the restaurant.
     * 
     * @param request Message containing "tableNumber", "seatCapacity", "tableLocation"
     * @return Message with created Table object if successful
     */
    public Message addTable(Message request) {
        MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
        PooledConnection pConn = null;

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) request.getData();
            
            int tableNumber = (Integer) data.get("tableNumber");
            int seatCapacity = (Integer) data.get("seatCapacity");
            String tableLocation = (String) data.get("tableLocation");

            pConn = pool.getConnection();
            if (pConn == null) {
                return Message.fail("ADD_TABLE", "Database connection failed");
            }

            Connection conn = pConn.getConnection();

            // Check if table number already exists
            if (tableExists(conn, tableNumber)) {
                return Message.fail("ADD_TABLE", "Table number already exists");
            }

            String sql = "INSERT INTO tables_info (table_number, seat_capacity, table_location, table_status) " +
                        "VALUES (?, ?, ?, 'AVAILABLE')";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, tableNumber);
            ps.setInt(2, seatCapacity);
            ps.setString(3, tableLocation);
            ps.executeUpdate();
            ps.close();

            // Create and return Table object
            Table table = new Table(tableNumber, seatCapacity, tableLocation);

            return Message.ok("ADD_TABLE", table);

        } catch (SQLException e) {
            e.printStackTrace();
            return Message.fail("ADD_TABLE", "Database error: " + e.getMessage());
        } finally {
            pool.releaseConnection(pConn);
        }
    }

    /**
     * Updates table information (capacity and location).
     * 
     * @param request Message containing Table object with updated data
     * @return Message with success or error
     */
    public Message updateTable(Message request) {
        MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
        PooledConnection pConn = null;

        try {
            Table table = (Table) request.getData();

            pConn = pool.getConnection();
            if (pConn == null) {
                return Message.fail("UPDATE_TABLE", "Database connection failed");
            }

            Connection conn = pConn.getConnection();
            
            String sql = "UPDATE tables_info SET seat_capacity = ?, table_location = ? WHERE table_number = ?";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, table.getSeatCapacity());
            ps.setString(2, table.getTableLocation());
            ps.setInt(3, table.getTableNumber());
            
            int rowsAffected = ps.executeUpdate();
            ps.close();

            if (rowsAffected > 0) {
                return Message.ok("UPDATE_TABLE", "Table updated successfully");
            } else {
                return Message.fail("UPDATE_TABLE", "Table not found");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return Message.fail("UPDATE_TABLE", "Database error: " + e.getMessage());
        } finally {
            pool.releaseConnection(pConn);
        }
    }

    /**
     * Deletes a table from the restaurant.
     * 
     * @param request Message containing "tableNumber"
     * @return Message with success or error
     */
    public Message deleteTable(Message request) {
        MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
        PooledConnection pConn = null;

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) request.getData();
            int tableNumber = (Integer) data.get("tableNumber");

            pConn = pool.getConnection();
            if (pConn == null) {
                return Message.fail("DELETE_TABLE", "Database connection failed");
            }

            Connection conn = pConn.getConnection();
            
            String sql = "DELETE FROM tables_info WHERE table_number = ?";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, tableNumber);
            
            int rowsAffected = ps.executeUpdate();
            ps.close();

            if (rowsAffected > 0) {
                return Message.ok("DELETE_TABLE", "Table deleted successfully");
            } else {
                return Message.fail("DELETE_TABLE", "Table not found");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return Message.fail("DELETE_TABLE", "Database error: " + e.getMessage());
        } finally {
            pool.releaseConnection(pConn);
        }
    }

    /**
     * Marks a table as occupied.
     * 
     * @param request Message containing "tableNumber"
     * @return Message with success or error
     */
    public Message occupyTable(Message request) {
        MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
        PooledConnection pConn = null;

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) request.getData();
            int tableNumber = (Integer) data.get("tableNumber");

            pConn = pool.getConnection();
            if (pConn == null) {
                return Message.fail("OCCUPY_TABLE", "Database connection failed");
            }

            Connection conn = pConn.getConnection();
            
            String sql = "UPDATE tables_info SET table_status = 'OCCUPIED', " +
                        "reservation_start = ? WHERE table_number = ?";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            ps.setInt(2, tableNumber);
            
            int rowsAffected = ps.executeUpdate();
            ps.close();

            if (rowsAffected > 0) {
                return Message.ok("OCCUPY_TABLE", "Table marked as occupied");
            } else {
                return Message.fail("OCCUPY_TABLE", "Table not found");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return Message.fail("OCCUPY_TABLE", "Database error: " + e.getMessage());
        } finally {
            pool.releaseConnection(pConn);
        }
    }

    /**
     * Marks a table as available (releases the table).
     * 
     * @param request Message containing "tableNumber"
     * @return Message with success or error
     */
    public Message releaseTable(Message request) {
        MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
        PooledConnection pConn = null;

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) request.getData();
            int tableNumber = (Integer) data.get("tableNumber");

            pConn = pool.getConnection();
            if (pConn == null) {
                return Message.fail("RELEASE_TABLE", "Database connection failed");
            }

            Connection conn = pConn.getConnection();
            
            String sql = "UPDATE tables_info SET table_status = 'AVAILABLE', " +
                        "reservation_start = NULL, reservation_end = NULL WHERE table_number = ?";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, tableNumber);
            
            int rowsAffected = ps.executeUpdate();
            ps.close();

            if (rowsAffected > 0) {
                return Message.ok("RELEASE_TABLE", "Table marked as available");
            } else {
                return Message.fail("RELEASE_TABLE", "Table not found");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return Message.fail("RELEASE_TABLE", "Database error: " + e.getMessage());
        } finally {
            pool.releaseConnection(pConn);
        }
    }

    // Helper methods

    /**
     * Checks if a table number already exists in the database.
     */
    private boolean tableExists(Connection conn, int tableNumber) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tables_info WHERE table_number = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, tableNumber);
        ResultSet rs = ps.executeQuery();
        
        boolean exists = false;
        if (rs.next()) {
            exists = rs.getInt(1) > 0;
        }
        
        rs.close();
        ps.close();
        return exists;
    }

    /**
     * Extracts a Table object from ResultSet.
     */
    private Table extractTableFromResultSet(ResultSet rs) throws SQLException {
        Table table = new Table();
        table.setTableNumber(rs.getInt("table_number"));
        table.setSeatCapacity(rs.getInt("seat_capacity"));
        table.setTableLocation(rs.getString("table_location"));
        table.setTableStatus(Table.TableStatus.valueOf(rs.getString("table_status")));
        
        Timestamp reservationStart = rs.getTimestamp("reservation_start");
        if (reservationStart != null) {
            table.setReservationStart(reservationStart);
        }
        
        Timestamp reservationEnd = rs.getTimestamp("reservation_end");
        if (reservationEnd != null) {
            table.setReservationEnd(reservationEnd);
        }
        
        return table;
    }
}