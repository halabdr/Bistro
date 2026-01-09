package data_access;

import connection.MySQLConnectionPool;
import connection.PooledConnection;
import common.Message;
import entities.User;
import entities.Subscriber;
import entities.Representative;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * Repository for managing user data.
 * Handles user authentication, subscriber registration, and user information updates.
 */
public class UserRepository {

    /**
     * Authenticates a user with email and password.
     * 
     * @param request Message containing email and password
     * @return Message with Subscriber/Representative/User object if successful
     */
    public Message login(Message request) {
        MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
        PooledConnection pConn = null;

        try {
            
            Map<String, Object> data = (Map<String, Object>) request.getData();
            String email = (String) data.get("email");
            String password = (String) data.get("password");

            pConn = pool.getConnection();
            if (pConn == null) {
                return Message.fail("LOGIN", "Database connection failed");
            }

            Connection conn = pConn.getConnection();
            String sql = "SELECT * FROM users WHERE email_address = ? AND user_password = ? AND account_status = 1";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, email);
            ps.setString(2, password);
            
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                User.UserRole role = User.UserRole.valueOf(rs.getString("user_role"));
                Object userObject;

                if (role == User.UserRole.SUBSCRIBER) {
                    userObject = loadSubscriberWithDetails(rs, conn);
                } else if (role == User.UserRole.REPRESENTATIVE || role == User.UserRole.MANAGER) {
                    userObject = loadRepresentativeWithDetails(rs, conn);
                } else {
                    userObject = extractUserFromResultSet(rs);
                }

                rs.close();
                ps.close();
                return Message.ok("LOGIN", userObject);
            } else {
                rs.close();
                ps.close();
                return Message.fail("LOGIN", "Invalid email or password");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return Message.fail("LOGIN", "Database error: " + e.getMessage());
        } finally {
            pool.releaseConnection(pConn);
        }
    }

    /**
     * Registers a new subscriber through a representative.
     * 
     * @param request Message containing subscriber and user details
     * @return Message with Subscriber object if successful
     */
    public Message registerSubscriber(Message request) {
        MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
        PooledConnection pConn = null;

        try {
            Map<String, Object> data = (Map<String, Object>) request.getData();
            
            String name = (String) data.get("name");
            String email = (String) data.get("email");
            String phone = (String) data.get("phone");
            String password = (String) data.get("password");
            String subscriberNumber = (String) data.get("subscriberNumber");
            String membershipCard = (String) data.get("membershipCard");

            pConn = pool.getConnection();
            if (pConn == null) {
                return Message.fail("REGISTER_SUBSCRIBER", "Database connection failed");
            }

            Connection conn = pConn.getConnection();
            
            if (emailExists(conn, email)) {
                return Message.fail("REGISTER_SUBSCRIBER", "Email already registered");
            }

            if (subscriberNumberExists(conn, subscriberNumber)) {
                return Message.fail("REGISTER_SUBSCRIBER", "Subscriber number already exists");
            }

            conn.setAutoCommit(false);

            try {
                String userSql = "INSERT INTO users (name, email_address, phone_number, user_password, user_role) " +
                                "VALUES (?, ?, ?, ?, 'SUBSCRIBER')";
                PreparedStatement userPs = conn.prepareStatement(userSql, PreparedStatement.RETURN_GENERATED_KEYS);
                userPs.setString(1, name);
                userPs.setString(2, email);
                userPs.setString(3, phone);
                userPs.setString(4, password);
                userPs.executeUpdate();

                ResultSet rs = userPs.getGeneratedKeys();
                int userId = 0;
                if (rs.next()) {
                    userId = rs.getInt(1);
                }
                rs.close();
                userPs.close();

                String subscriberSql = "INSERT INTO subscribers (user_id, subscriber_number, membership_card) " +
                                      "VALUES (?, ?, ?)";
                PreparedStatement subPs = conn.prepareStatement(subscriberSql);
                subPs.setInt(1, userId);
                subPs.setString(2, subscriberNumber);
                subPs.setString(3, membershipCard);
                subPs.executeUpdate();
                subPs.close();

                conn.commit();
                conn.setAutoCommit(true);

                Subscriber subscriber = new Subscriber();
                subscriber.setUserId(userId);
                subscriber.setName(name);
                subscriber.setEmailAddress(email);
                subscriber.setPhoneNumber(phone);
                subscriber.setUserPassword(password);
                subscriber.setUserRole(User.UserRole.SUBSCRIBER);
                subscriber.setAccountStatus(true);
                subscriber.setSubscriberNumber(subscriberNumber);
                subscriber.setMembershipCard(membershipCard);

                return Message.ok("REGISTER_SUBSCRIBER", subscriber);

            } catch (SQLException e) {
                conn.rollback();
                conn.setAutoCommit(true);
                throw e;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return Message.fail("REGISTER_SUBSCRIBER", "Database error: " + e.getMessage());
        } finally {
            pool.releaseConnection(pConn);
        }
    }

    /**
     * Gets user information by user ID.
     * 
     * @param request Message containing "userId"
     * @return Message with User/Subscriber/Representative object if found
     */
    public Message getUser(Message request) {
        MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
        PooledConnection pConn = null;

        try {
            Map<String, Object> data = (Map<String, Object>) request.getData();
            int userId = (Integer) data.get("userId");

            pConn = pool.getConnection();
            if (pConn == null) {
                return Message.fail("GET_USER", "Database connection failed");
            }

            Connection conn = pConn.getConnection();
            String sql = "SELECT * FROM users WHERE user_id = ?";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                User.UserRole role = User.UserRole.valueOf(rs.getString("user_role"));
                Object userObject;

                if (role == User.UserRole.SUBSCRIBER) {
                    userObject = loadSubscriberWithDetails(rs, conn);
                } else if (role == User.UserRole.REPRESENTATIVE || role == User.UserRole.MANAGER) {
                    userObject = loadRepresentativeWithDetails(rs, conn);
                } else {
                    userObject = extractUserFromResultSet(rs);
                }

                rs.close();
                ps.close();
                return Message.ok("GET_USER", userObject);
            } else {
                rs.close();
                ps.close();
                return Message.fail("GET_USER", "User not found");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return Message.fail("GET_USER", "Database error: " + e.getMessage());
        } finally {
            pool.releaseConnection(pConn);
        }
    }

    /**
     * Updates user information.
     * 
     * @param request Message containing User/Subscriber/Representative object
     * @return Message with success or error
     */
    public Message updateUser(Message request) {
        MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
        PooledConnection pConn = null;

        try {
            User user = (User) request.getData();

            pConn = pool.getConnection();
            if (pConn == null) {
                return Message.fail("UPDATE_USER", "Database connection failed");
            }

            Connection conn = pConn.getConnection();
            String sql = "UPDATE users SET name = ?, phone_number = ?, email_address = ? WHERE user_id = ?";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, user.getName());
            ps.setString(2, user.getPhoneNumber());
            ps.setString(3, user.getEmailAddress());
            ps.setInt(4, user.getUserId());
            
            int rowsAffected = ps.executeUpdate();
            ps.close();

            if (rowsAffected > 0) {
                return Message.ok("UPDATE_USER", "User updated successfully");
            } else {
                return Message.fail("UPDATE_USER", "User not found");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return Message.fail("UPDATE_USER", "Database error: " + e.getMessage());
        } finally {
            pool.releaseConnection(pConn);
        }
    }

    // Helper methods to make code more clean

    /**
     * Extracts a basic User object from ResultSet.
     */
    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setName(rs.getString("name"));
        user.setEmailAddress(rs.getString("email_address"));
        user.setPhoneNumber(rs.getString("phone_number"));
        user.setUserPassword(rs.getString("user_password"));
        user.setUserRole(User.UserRole.valueOf(rs.getString("user_role")));
        user.setAccountStatus(rs.getBoolean("account_status"));
        user.setRegistrationDate(rs.getTimestamp("registration_date"));
        return user;
    }

    /**
     * Loads a complete Subscriber object with all details.
     */
    private Subscriber loadSubscriberWithDetails(ResultSet userRs, Connection conn) throws SQLException {
        Subscriber subscriber = new Subscriber();
        
        subscriber.setUserId(userRs.getInt("user_id"));
        subscriber.setName(userRs.getString("name"));
        subscriber.setEmailAddress(userRs.getString("email_address"));
        subscriber.setPhoneNumber(userRs.getString("phone_number"));
        subscriber.setUserPassword(userRs.getString("user_password"));
        subscriber.setUserRole(User.UserRole.SUBSCRIBER);
        subscriber.setAccountStatus(userRs.getBoolean("account_status"));
        subscriber.setRegistrationDate(userRs.getTimestamp("registration_date"));

        String sql = "SELECT * FROM subscribers WHERE user_id = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, subscriber.getUserId());
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            subscriber.setSubscriberNumber(rs.getString("subscriber_number"));
            subscriber.setMembershipCard(rs.getString("membership_card"));
        }

        rs.close();
        ps.close();
        return subscriber;
    }

    /**
     * Loads a complete Representative object with all details.
     */
    private Representative loadRepresentativeWithDetails(ResultSet userRs, Connection conn) throws SQLException {
        Representative representative = new Representative();
        
        representative.setUserId(userRs.getInt("user_id"));
        representative.setName(userRs.getString("name"));
        representative.setEmailAddress(userRs.getString("email_address"));
        representative.setPhoneNumber(userRs.getString("phone_number"));
        representative.setUserPassword(userRs.getString("user_password"));
        representative.setUserRole(User.UserRole.valueOf(userRs.getString("user_role")));
        representative.setAccountStatus(userRs.getBoolean("account_status"));
        representative.setRegistrationDate(userRs.getTimestamp("registration_date"));

        String sql = "SELECT * FROM representatives WHERE user_id = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, representative.getUserId());
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            representative.setRepresentativeNumber(rs.getString("representative_number"));
        }

        rs.close();
        ps.close();
        return representative;
    }

    /**
     * Checks if an email already exists in the database.
     */
    private boolean emailExists(Connection conn, String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE email_address = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, email);
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
     * Checks if a subscriber number already exists in the database.
     */
    private boolean subscriberNumberExists(Connection conn, String subscriberNumber) throws SQLException {
        String sql = "SELECT COUNT(*) FROM subscribers WHERE subscriber_number = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, subscriberNumber);
        ResultSet rs = ps.executeQuery();
        
        boolean exists = false;
        if (rs.next()) {
            exists = rs.getInt(1) > 0;
        }
        
        rs.close();
        ps.close();
        return exists;
    }
}