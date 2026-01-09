package connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton connection pool for managing MySQL database connections.
 * Maintains a pool of reusable connections and automatically cleans up idle connections.
 */
public class MySQLConnectionPool {

    private static MySQLConnectionPool instance;
    
    // Database Configuration
    private static final String DB_URL = 
        "jdbc:mysql://localhost:3306/bistrorestaurant?serverTimezone=Asia/Jerusalem";
    private static final String USER = "root";
    private static final String PASS = "Aa123456";

    // Pool Configuration
    private static final int MAX_POOL_SIZE = 10;
    private static final long MAX_IDLE_TIME = 300000; // 5 minutes in milliseconds
    private static final long CHECK_INTERVAL = 60;    // Check every 60 seconds

    private BlockingQueue<PooledConnection> pool;
    private ScheduledExecutorService cleanerService;

    /**
     * Private constructor for Singleton pattern.
     * Initializes the connection pool and starts the cleanup timer.
     */
    private MySQLConnectionPool() {
        pool = new LinkedBlockingQueue<>(MAX_POOL_SIZE);
        startCleanupTimer();
        System.out.println("[Pool] Initialized. Max Size: " + MAX_POOL_SIZE);
    }

    /**
     * Gets the singleton instance of the connection pool.
     * 
     * @return the connection pool instance
     */
    public static synchronized MySQLConnectionPool getInstance() {
        if (instance == null) {
            instance = new MySQLConnectionPool();
        }
        return instance;
    }

    /**
     * Gets a connection from the pool.
     * If the pool is empty, creates a new connection.
     * 
     * @return a pooled connection, or null if creation fails
     */
    public PooledConnection getConnection() {
        PooledConnection pConn = pool.poll();// Try to get from queue
        
        if (pConn == null) {
            System.out.println("[Pool] Queue empty. Creating NEW physical connection.");
            return createNewConnection();
        }
        
        pConn.touch(); // Reset timer
        System.out.println("[Pool] Reusing existing connection.");
        return pConn;
    }

    /**
     * Returns a connection back to the pool.
     * If the pool is full, closes the physical connection.
     * 
     * @param pConn the pooled connection to release
     */
    public void releaseConnection(PooledConnection pConn) {
        if (pConn != null) {
            pConn.touch();
            boolean added = pool.offer(pConn); // Return to queue
            if (added) {
                System.out.println("[Pool] Connection returned. Current pool size: " + pool.size());
            } else {
                System.out.println("[Pool] Pool is full. Closing connection.");
                try {
                    pConn.closePhysicalConnection();
                } catch (SQLException e) {
                    System.err.println("[Pool] Error closing connection: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Creates a new physical database connection.
     * 
     * @return a new pooled connection, or null if creation fails
     */
    private PooledConnection createNewConnection() {
        try {
            return new PooledConnection(DriverManager.getConnection(DB_URL, USER, PASS));
        } catch (SQLException e) {
            System.err.println("[Pool] CONNECTION ERROR:");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Starts the scheduled cleanup timer.
     * Runs every CHECK_INTERVAL seconds to remove idle connections.
     */
    private void startCleanupTimer() {
        cleanerService = Executors.newSingleThreadScheduledExecutor();
        cleanerService.scheduleAtFixedRate(
            this::checkIdleConnections, 
            CHECK_INTERVAL, 
            CHECK_INTERVAL, 
            TimeUnit.SECONDS
        );
    }

    /**
     * Checks for idle connections and closes them.
     * Called periodically by the cleanup timer.
     */
    private void checkIdleConnections() {
        if (pool.isEmpty()) {
            return;
        }

        List<PooledConnection> activeConnections = new ArrayList<>();
        pool.drainTo(activeConnections);

        long now = System.currentTimeMillis();
        int closedCount = 0;

        for (PooledConnection pConn : activeConnections) {
            if (now - pConn.getLastUsed() > MAX_IDLE_TIME) {
                try {
                    pConn.closePhysicalConnection();
                    closedCount++;
                } catch (SQLException e) {
                    System.err.println("[Timer] Error closing connection: " + e.getMessage());
                }
            } else {
                pool.offer(pConn); // Put back
            }
        }
        
        if (closedCount > 0) {
            System.out.println("[Timer] Evicted " + closedCount + 
                             " idle connections. Pool size: " + pool.size());
        }
    }

    /**
     * Shuts down the connection pool and closes all connections.
     * Should be called when the server stops.
     */
    public void shutdown() {
        System.out.println("[Pool] Shutting down...");
        
        if (cleanerService != null) {
            cleanerService.shutdown();
        }
        
        List<PooledConnection> connections = new ArrayList<>();
        pool.drainTo(connections);
        
        for (PooledConnection pConn : connections) {
            try {
                pConn.closePhysicalConnection();
            } catch (SQLException e) {
                System.err.println("[Pool] Error closing connection during shutdown: " + e.getMessage());
            }
        }
        
        System.out.println("[Pool] Shutdown complete.");
    }
}