package services;
import connection.MySQLConnectionPool;
import connection.PooledConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Background scheduler that runs periodic notification checks.
 * Handles reminders, no-shows, table clearing, and waitlist notifications.
 */
public class NotificationScheduler {

    private static final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static ScheduledExecutorService scheduler;

    /**
     * Starts the notification scheduler.
     * Runs checks every 1 minute.
     */
    public static void start() {
        if (scheduler != null && !scheduler.isShutdown()) {
            System.out.println("[NotificationScheduler] Already running");
            return;
        }

        scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(() -> {
            try {
                System.out.println("\n[" + LocalDateTime.now().format(fmt) + "] [NotificationScheduler] Running notification checks...");

                checkReservationReminders();
                checkNoShows();
                checkTableClearing();
                checkWaitlistNotifications();
                checkWaitlistNoShows();

                System.out.println("[" + LocalDateTime.now().format(fmt) + "] [NotificationScheduler] Checks completed.\n");

            } catch (Exception e) {
                System.err.println("[NotificationScheduler] Error: " + e.getMessage());
                e.printStackTrace();
            }
        }, 0, 1, TimeUnit.MINUTES);

        System.out.println("[NotificationScheduler] Started (runs every 1 minute)");
    }

    /**
     * Stops the notification scheduler.
     */
    public static void stop() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
            }
            System.out.println("[NotificationScheduler] Stopped");
        }
    }

    /**
     * Check 1: Send reminders 2 hours before reservation time.
     */
    private static void checkReservationReminders() {
        MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
        PooledConnection pConn = null;

        try {
            pConn = pool.getConnection();
            if (pConn == null) return;

            Connection conn = pConn.getConnection();

            String sql =
                "SELECT r.reservation_id, r.confirmation_code, r.booking_date, r.booking_time, " +
                "       r.guest_count, r.subscriber_number, r.walk_in_phone, r.walk_in_email, " +
                "       u.phone_number AS sub_phone, u.email_address AS sub_email " +
                "FROM reservations r " +
                "LEFT JOIN subscribers s ON r.subscriber_number = s.subscriber_number " +
                "LEFT JOIN users u ON s.user_id = u.user_id " +
                "WHERE r.reservation_status = 'ACTIVE' " +
                "AND TIMESTAMP(r.booking_date, r.booking_time) " +
                "    BETWEEN NOW() + INTERVAL 115 MINUTE AND NOW() + INTERVAL 125 MINUTE";

            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            int count = 0;
            while (rs.next()) {
                String code = rs.getString("confirmation_code");
                String date = rs.getString("booking_date");
                String time = rs.getString("booking_time");
                int guests = rs.getInt("guest_count");

                String phone = rs.getString("sub_phone");
                String email = rs.getString("sub_email");
                if (phone == null) phone = rs.getString("walk_in_phone");
                if (email == null) email = rs.getString("walk_in_email");

                String message = String.format(
                    "Reminder: Your reservation (code: %s) for %d guests is in 2 hours (%s at %s). See you soon!",
                    code, guests, date, time
                );

                NotificationService.sendNotification(phone, email, "Reservation Reminder - Bistro", message);
                count++;
            }

            rs.close();
            ps.close();

            if (count > 0) {
                System.out.println("    [Reminders] Sent " + count + " reminder(s)");
            }

        } catch (SQLException e) {
            System.err.println("    [Reminders] Error: " + e.getMessage());
        } finally {
            pool.releaseConnection(pConn);
        }
    }

    /**
     * Check 2: Mark reservation no-shows (15 minutes after reservation time, not checked in).
     */
    private static void checkNoShows() {
        MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
        PooledConnection pConn = null;

        try {
            pConn = pool.getConnection();
            if (pConn == null) return;

            Connection conn = pConn.getConnection();

            String selectSql =
                "SELECT r.reservation_id, r.confirmation_code, r.subscriber_number, " +
                "       r.walk_in_phone, r.walk_in_email, " +
                "       u.phone_number AS sub_phone, u.email_address AS sub_email " +
                "FROM reservations r " +
                "LEFT JOIN subscribers s ON r.subscriber_number = s.subscriber_number " +
                "LEFT JOIN users u ON s.user_id = u.user_id " +
                "WHERE r.reservation_status = 'ACTIVE' " +
                "AND r.assigned_table_number IS NULL " +
                "AND TIMESTAMP(r.booking_date, r.booking_time) < NOW() - INTERVAL 15 MINUTE";

            PreparedStatement selectPs = conn.prepareStatement(selectSql);
            ResultSet rs = selectPs.executeQuery();

            int count = 0;
            while (rs.next()) {
                int reservationId = rs.getInt("reservation_id");
                String code = rs.getString("confirmation_code");

                String phone = rs.getString("sub_phone");
                String email = rs.getString("sub_email");
                if (phone == null) phone = rs.getString("walk_in_phone");
                if (email == null) email = rs.getString("walk_in_email");

                // Update status to NO_SHOW
                String updateSql = "UPDATE reservations SET reservation_status = 'NO_SHOW' WHERE reservation_id = ?";
                PreparedStatement updatePs = conn.prepareStatement(updateSql);
                updatePs.setInt(1, reservationId);
                updatePs.executeUpdate();
                updatePs.close();

                String message = String.format(
                    "Your reservation (code: %s) has been cancelled because you did not arrive within 15 minutes of the scheduled time.",
                    code
                );

                NotificationService.sendNotification(phone, email, "Reservation Cancelled - No Show", message);
                System.out.println("    [No-Show] Reservation " + code + " marked as NO_SHOW");
                count++;
            }

            rs.close();
            selectPs.close();

            if (count > 0) {
                System.out.println("    [No-Show] Processed " + count + " reservation no-show(s)");
            }

        } catch (SQLException e) {
            System.err.println("    [No-Show] Error: " + e.getMessage());
        } finally {
            pool.releaseConnection(pConn);
        }
    }

    /**
     * Check 3: Send bill notification 2 hours after seating.
     */
    private static void checkTableClearing() {
        MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
        PooledConnection pConn = null;

        try {
            pConn = pool.getConnection();
            if (pConn == null) return;

            Connection conn = pConn.getConnection();

            String sql =
                "SELECT t.table_number, t.reservation_start, " +
                "       r.confirmation_code, r.subscriber_number, r.walk_in_phone, r.walk_in_email, " +
                "       u.phone_number AS sub_phone, u.email_address AS sub_email " +
                "FROM tables_info t " +
                "JOIN reservations r ON t.table_number = r.assigned_table_number " +
                "LEFT JOIN subscribers s ON r.subscriber_number = s.subscriber_number " +
                "LEFT JOIN users u ON s.user_id = u.user_id " +
                "WHERE t.table_status = 'OCCUPIED' " +
                "AND t.reservation_start IS NOT NULL " +
                "AND t.reservation_start <= NOW() - INTERVAL 115 MINUTE " +
                "AND t.reservation_start > NOW() - INTERVAL 125 MINUTE " +
                "AND r.reservation_status = 'ACTIVE'";

            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            int count = 0;
            while (rs.next()) {
                int tableNumber = rs.getInt("table_number");
                String code = rs.getString("confirmation_code");

                String phone = rs.getString("sub_phone");
                String email = rs.getString("sub_email");
                if (phone == null) phone = rs.getString("walk_in_phone");
                if (email == null) email = rs.getString("walk_in_email");

                String message = String.format(
                    "Your 2-hour table time is ending. Please review your bill (reservation: %s, table: %d). Thank you for dining with us!",
                    code, tableNumber
                );

                NotificationService.sendNotification(phone, email, "Bill Ready - Bistro", message);
                count++;
            }

            rs.close();
            ps.close();

            if (count > 0) {
                System.out.println("    [Table Clearing] Sent " + count + " bill notification(s)");
            }

        } catch (SQLException e) {
            System.err.println("    [Table Clearing] Error: " + e.getMessage());
        } finally {
            pool.releaseConnection(pConn);
        }
    }

    /**
     * Check 4: Notify waitlist customers when table becomes available.
     * Sets notified_at timestamp to track the 15-minute timeout.
     */
    private static void checkWaitlistNotifications() {
        MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
        PooledConnection pConn = null;

        try {
            pConn = pool.getConnection();
            if (pConn == null) return;

            Connection conn = pConn.getConnection();

            // Find available tables ordered by capacity
            String tablesSql =
                "SELECT table_number, seat_capacity FROM tables_info " +
                "WHERE table_status = 'AVAILABLE' ORDER BY seat_capacity";

            PreparedStatement tablesPs = conn.prepareStatement(tablesSql);
            ResultSet tablesRs = tablesPs.executeQuery();

            int count = 0;
            while (tablesRs.next()) {
                int tableNumber = tablesRs.getInt("table_number");
                int capacity = tablesRs.getInt("seat_capacity");

                // Find first waitlist entry that fits this table and hasn't been notified yet
                String waitlistSql =
                    "SELECT w.entry_id, w.entry_code, w.number_of_diners, w.subscriber_number, " +
                    "       w.walk_in_phone, w.walk_in_email, " +
                    "       u.phone_number AS sub_phone, u.email_address AS sub_email " +
                    "FROM waiting_list w " +
                    "LEFT JOIN subscribers s ON w.subscriber_number = s.subscriber_number " +
                    "LEFT JOIN users u ON s.user_id = u.user_id " +
                    "WHERE w.number_of_diners <= ? " +
                    "AND w.notified_at IS NULL " +
                    "ORDER BY w.request_time ASC " +
                    "LIMIT 1";

                PreparedStatement waitlistPs = conn.prepareStatement(waitlistSql);
                waitlistPs.setInt(1, capacity);
                ResultSet waitlistRs = waitlistPs.executeQuery();

                if (waitlistRs.next()) {
                    int entryId = waitlistRs.getInt("entry_id");
                    String entryCode = waitlistRs.getString("entry_code");
                    int diners = waitlistRs.getInt("number_of_diners");

                    String phone = waitlistRs.getString("sub_phone");
                    String email = waitlistRs.getString("sub_email");
                    if (phone == null) phone = waitlistRs.getString("walk_in_phone");
                    if (email == null) email = waitlistRs.getString("walk_in_email");

                    // Update notified_at timestamp
                    String updateSql = "UPDATE waiting_list SET notified_at = NOW() WHERE entry_id = ?";
                    PreparedStatement updatePs = conn.prepareStatement(updateSql);
                    updatePs.setInt(1, entryId);
                    updatePs.executeUpdate();
                    updatePs.close();

                    String message = String.format(
                        "Great news! A table is now available for your party of %d. " +
                        "Please check in within 15 minutes using code: %s",
                        diners, entryCode
                    );

                    NotificationService.sendNotification(phone, email, "Table Available! - Bistro", message);
                    System.out.println("    [Waitlist] Notified entry " + entryCode + " - table " + tableNumber + " available");
                    count++;
                }

                waitlistRs.close();
                waitlistPs.close();
            }

            tablesRs.close();
            tablesPs.close();

            if (count > 0) {
                System.out.println("    [Waitlist] Sent " + count + " availability notification(s)");
            }

        } catch (SQLException e) {
            System.err.println("    [Waitlist] Error: " + e.getMessage());
        } finally {
            pool.releaseConnection(pConn);
        }
    }

    /**
     * Check 5: Remove waitlist entries that didn't show up within 15 minutes of notification.
     * Deletes entries where notified_at is more than 15 minutes ago.
     */
    private static void checkWaitlistNoShows() {
        MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
        PooledConnection pConn = null;

        try {
            pConn = pool.getConnection();
            if (pConn == null) return;

            Connection conn = pConn.getConnection();

            // Find waitlist entries that were notified more than 15 minutes ago
            String selectSql =
                "SELECT w.entry_id, w.entry_code, w.number_of_diners, w.subscriber_number, " +
                "       w.walk_in_phone, w.walk_in_email, " +
                "       u.phone_number AS sub_phone, u.email_address AS sub_email " +
                "FROM waiting_list w " +
                "LEFT JOIN subscribers s ON w.subscriber_number = s.subscriber_number " +
                "LEFT JOIN users u ON s.user_id = u.user_id " +
                "WHERE w.notified_at IS NOT NULL " +
                "AND w.notified_at < NOW() - INTERVAL 15 MINUTE";

            PreparedStatement selectPs = conn.prepareStatement(selectSql);
            ResultSet rs = selectPs.executeQuery();

            int count = 0;
            while (rs.next()) {
                int entryId = rs.getInt("entry_id");
                String entryCode = rs.getString("entry_code");

                String phone = rs.getString("sub_phone");
                String email = rs.getString("sub_email");
                if (phone == null) phone = rs.getString("walk_in_phone");
                if (email == null) email = rs.getString("walk_in_email");

                // Delete the entry from waitlist
                String deleteSql = "DELETE FROM waiting_list WHERE entry_id = ?";
                PreparedStatement deletePs = conn.prepareStatement(deleteSql);
                deletePs.setInt(1, entryId);
                deletePs.executeUpdate();
                deletePs.close();

                String message = String.format(
                    "Your waitlist entry (code: %s) has been cancelled because you did not arrive within 15 minutes after being notified.",
                    entryCode
                );

                NotificationService.sendNotification(phone, email, "Waitlist Entry Cancelled - No Show", message);
                System.out.println("    [Waitlist No-Show] Entry " + entryCode + " removed from waitlist");
                count++;
            }

            rs.close();
            selectPs.close();

            if (count > 0) {
                System.out.println("    [Waitlist No-Show] Processed " + count + " waitlist no-show(s)");
            }

        } catch (SQLException e) {
            System.err.println("    [Waitlist No-Show] Error: " + e.getMessage());
        } finally {
            pool.releaseConnection(pConn);
        }
    }
}