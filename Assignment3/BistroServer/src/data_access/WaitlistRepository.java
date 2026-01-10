package data_access;
import connection.MySQLConnectionPool;
import connection.PooledConnection;
import common.Message;
import entities.WaitlistEntry;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Repository for managing waitlist entries.
 * Handles joining, leaving, and retrieving waitlist information.
 */
public class WaitlistRepository {

    /**
     * Adds a customer to the waitlist.
     * 
     * @param request Message containing "numberOfDiners" and optional "subscriberNumber"
     * @return Message with created WaitlistEntry object if successful
     */
    public Message joinWaitlist(Message request) {
        MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
        PooledConnection pConn = null;

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) request.getData();
            
            int numberOfDiners = (Integer) data.get("numberOfDiners");
            String subscriberNumber = (String) data.get("subscriberNumber");

            pConn = pool.getConnection();
            if (pConn == null) {
                return Message.fail("JOIN_WAITLIST", "Database connection failed");
            }

            Connection conn = pConn.getConnection();

            // Generate unique entry code
            String entryCode = generateEntryCode();

            // Insert into waitlist
            String sql = "INSERT INTO waiting_list (number_of_diners, entry_code, subscriber_number) " +
                        "VALUES (?, ?, ?)";
            
            PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setInt(1, numberOfDiners);
            ps.setString(2, entryCode);
            ps.setString(3, subscriberNumber);
            
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            int entryId = 0;
            if (rs.next()) {
                entryId = rs.getInt(1);
            }
            rs.close();
            ps.close();

            // Create and return WaitlistEntry object
            WaitlistEntry entry = new WaitlistEntry();
            entry.setEntryId(entryId);
            entry.setNumberOfDiners(numberOfDiners);
            entry.setEntryCode(entryCode);
            entry.setSubscriberNumber(subscriberNumber);
            entry.setRequestTime(LocalDateTime.now());

            return Message.ok("JOIN_WAITLIST", entry);

        } catch (SQLException e) {
            e.printStackTrace();
            return Message.fail("JOIN_WAITLIST", "Database error: " + e.getMessage());
        } finally {
            pool.releaseConnection(pConn);
        }
    }

    /**
     * Removes a customer from the waitlist by entry code.
     * 
     * @param request Message containing "entryCode"
     * @return Message with success or error
     */
    public Message leaveWaitlist(Message request) {
        MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
        PooledConnection pConn = null;

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) request.getData();
            String entryCode = (String) data.get("entryCode");

            pConn = pool.getConnection();
            if (pConn == null) {
                return Message.fail("LEAVE_WAITLIST", "Database connection failed");
            }

            Connection conn = pConn.getConnection();
            
            String sql = "DELETE FROM waiting_list WHERE entry_code = ?";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, entryCode);
            
            int rowsAffected = ps.executeUpdate();
            ps.close();

            if (rowsAffected > 0) {
                return Message.ok("LEAVE_WAITLIST", "Successfully removed from waitlist");
            } else {
                return Message.fail("LEAVE_WAITLIST", "Entry not found in waitlist");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return Message.fail("LEAVE_WAITLIST", "Database error: " + e.getMessage());
        } finally {
            pool.releaseConnection(pConn);
        }
    }

    /**
     * Gets all current waitlist entries ordered by request time.
     * 
     * @param request Message (empty data)
     * @return Message with List of WaitlistEntry objects
     */
    public Message getWaitlist(Message request) {
        MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
        PooledConnection pConn = null;

        try {
            pConn = pool.getConnection();
            if (pConn == null) {
                return Message.fail("GET_WAITLIST", "Database connection failed");
            }

            Connection conn = pConn.getConnection();
            
            String sql = "SELECT * FROM waiting_list ORDER BY request_time ASC";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            List<WaitlistEntry> waitlist = new ArrayList<>();
            while (rs.next()) {
                waitlist.add(extractWaitlistEntryFromResultSet(rs));
            }

            rs.close();
            ps.close();

            return Message.ok("GET_WAITLIST", waitlist);

        } catch (SQLException e) {
            e.printStackTrace();
            return Message.fail("GET_WAITLIST", "Database error: " + e.getMessage());
        } finally {
            pool.releaseConnection(pConn);
        }
    }

    //methods to keep code clean 

    /**
     * Generates a unique entry code for waitlist.
     */
    private String generateEntryCode() {
        return "WAIT" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    /**
     * Extracts a WaitlistEntry object from ResultSet.
     */
    private WaitlistEntry extractWaitlistEntryFromResultSet(ResultSet rs) throws SQLException {
        WaitlistEntry entry = new WaitlistEntry();
        entry.setEntryId(rs.getInt("entry_id"));
        
        // Convert Timestamp from DB to LocalDateTime
        Timestamp timestamp = rs.getTimestamp("request_time");
        if (timestamp != null) {
            entry.setRequestTime(timestamp.toLocalDateTime());
        }
        
        entry.setNumberOfDiners(rs.getInt("number_of_diners"));
        entry.setEntryCode(rs.getString("entry_code"));
        
        String subscriberNumber = rs.getString("subscriber_number");
        if (subscriberNumber != null) {
            entry.setSubscriberNumber(subscriberNumber);
        }
        
        return entry;
    }
}