package connection;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Wrapper class for a database connection with timestamp tracking.
 * Used by MySQLConnectionPool to manage connection lifecycle.
 */
public class PooledConnection {
    
    private Connection connection;
    private long lastUsed;

    /**
     * Creates a new pooled connection.
     * 
     * @param connection the physical database connection
     */
    public PooledConnection(Connection connection) {
        this.connection = connection;
        this.lastUsed = System.currentTimeMillis();
    }

    /**
     * Gets the underlying database connection.
     * 
     * @return the connection
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Updates the last used timestamp to current time.
     * Called when the connection is retrieved from or returned to the pool.
     */
    public void touch() {
        this.lastUsed = System.currentTimeMillis();
    }

    /**
     * Gets the timestamp of when this connection was last used.
     * 
     * @return timestamp in milliseconds
     */
    public long getLastUsed() {
        return lastUsed;
    }
    
    /**
     * Closes the physical database connection.
     * 
     * @throws SQLException if closing fails
     */
    public void closePhysicalConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}