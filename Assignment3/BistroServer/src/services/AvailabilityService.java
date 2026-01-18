package services;

import connection.MySQLConnectionPool;
import connection.PooledConnection;
import entities.OpeningHours;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.sql.Time;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing reservation availability when restaurant configuration changes.
 * Handles automatic cancellation of reservations that become invalid due to:
 * - Changes in opening hours
 * - Changes in table configuration
 * - Restaurant closures on specific dates
 */
public class AvailabilityService {

    /**
     * Data class to hold reservation info for cancellation notifications.
     */
    private static class ReservationInfo {
        int reservationId;
        String confirmationCode;
        LocalDate bookingDate;
        LocalTime bookingTime;
        int guestCount;
        String subscriberNumber;
        String walkInPhone;
        String walkInEmail;
        // Subscriber contact info (fetched from users table)
        String subscriberPhone;
        String subscriberEmail;
        String subscriberName;
    }

    /**
     * Checks and cancels reservations affected by opening hours change for a weekday.
     * Called after updating regular weekly opening hours.
     * 
     * @param weekday the weekday that was updated
     * @param newOpeningTime new opening time
     * @param newClosingTime new closing time
     */
    public static void handleOpeningHoursChange(OpeningHours.Weekday weekday, 
                                                 LocalTime newOpeningTime, 
                                                 LocalTime newClosingTime) {
        MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
        PooledConnection pConn = null;

        try {
            pConn = pool.getConnection();
            if (pConn == null) {
                System.err.println("[AvailabilityService] Failed to get database connection");
                return;
            }

            Connection conn = pConn.getConnection();
            
            // Find all future dates that match this weekday (next 30 days)
            LocalDate today = LocalDate.now();
            List<LocalDate> affectedDates = new ArrayList<>();
            
            for (int i = 0; i <= 30; i++) {
                LocalDate checkDate = today.plusDays(i);
                if (checkDate.getDayOfWeek().toString().equals(weekday.name())) {
                    // Check if there's no special hours override for this date
                    if (!hasSpecialHours(conn, checkDate)) {
                        affectedDates.add(checkDate);
                    }
                }
            }

            // For each affected date, check and cancel invalid reservations
            for (LocalDate date : affectedDates) {
                cancelInvalidReservationsForDate(conn, date, newOpeningTime, newClosingTime);
            }

            System.out.println("[AvailabilityService] Processed opening hours change for " + weekday);

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            pool.releaseConnection(pConn);
        }
    }

    /**
     * Checks and cancels reservations affected by special hours change for a specific date.
     * Called after adding or updating special hours.
     * 
     * @param specialDate the date with special hours
     * @param openingTime opening time (null if closed)
     * @param closingTime closing time (null if closed)
     * @param isClosed true if restaurant is closed on this date
     */
    public static void handleSpecialHoursChange(LocalDate specialDate, 
                                                 LocalTime openingTime, 
                                                 LocalTime closingTime, 
                                                 boolean isClosed) {
        MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
        PooledConnection pConn = null;

        try {
            pConn = pool.getConnection();
            if (pConn == null) {
                System.err.println("[AvailabilityService] Failed to get database connection");
                return;
            }

            Connection conn = pConn.getConnection();

            if (isClosed) {
                // Cancel ALL reservations for this date
                cancelAllReservationsForDate(conn, specialDate);
            } else {
                // Cancel reservations outside the new hours
                cancelInvalidReservationsForDate(conn, specialDate, openingTime, closingTime);
            }

            System.out.println("[AvailabilityService] Processed special hours change for " + specialDate);

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            pool.releaseConnection(pConn);
        }
    }

    /**
     * Checks and cancels reservations affected by table deletion.
     * Called after deleting a table.
     * 
     * @param deletedTableNumber the table number that was deleted
     * @param deletedCapacity the capacity of the deleted table
     */
    public static void handleTableDeletion(int deletedTableNumber, int deletedCapacity) {
        MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
        PooledConnection pConn = null;

        try {
            pConn = pool.getConnection();
            if (pConn == null) {
                System.err.println("[AvailabilityService] Failed to get database connection");
                return;
            }

            Connection conn = pConn.getConnection();
            
            // Check all future active reservations
            cancelReservationsWithInsufficientTables(conn);

            System.out.println("[AvailabilityService] Processed table deletion: Table " + deletedTableNumber);

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            pool.releaseConnection(pConn);
        }
    }

    /**
     * Checks and cancels reservations affected by table capacity reduction.
     * Called after updating a table's seat capacity.
     * 
     * @param tableNumber the table that was updated
     * @param oldCapacity the old capacity
     * @param newCapacity the new capacity
     */
    public static void handleTableCapacityChange(int tableNumber, int oldCapacity, int newCapacity) {
        if (newCapacity >= oldCapacity) {
            // Capacity increased or stayed the same - no reservations affected
            return;
        }

        MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
        PooledConnection pConn = null;

        try {
            pConn = pool.getConnection();
            if (pConn == null) {
                System.err.println("[AvailabilityService] Failed to get database connection");
                return;
            }

            Connection conn = pConn.getConnection();
            
            // Check all future active reservations
            cancelReservationsWithInsufficientTables(conn);

            System.out.println("[AvailabilityService] Processed table capacity change: Table " + tableNumber);

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            pool.releaseConnection(pConn);
        }
    }

    // ==================== Private Helper Methods ====================

    /**
     * Checks if a specific date has special hours defined.
     */
    private static boolean hasSpecialHours(Connection conn, LocalDate date) throws SQLException {
        String sql = "SELECT COUNT(*) FROM special_hours WHERE special_date = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setDate(1, Date.valueOf(date));
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
     * Cancels all reservations for a specific date (used when restaurant is closed).
     */
    private static void cancelAllReservationsForDate(Connection conn, LocalDate date) throws SQLException {
        // Get all active reservations for this date
        List<ReservationInfo> reservations = getActiveReservationsForDate(conn, date);
        
        for (ReservationInfo res : reservations) {
            cancelReservationAndNotify(conn, res, "Restaurant is closed on " + date);
        }
    }

    /**
     * Cancels reservations that fall outside the valid operating hours for a date.
     */
    private static void cancelInvalidReservationsForDate(Connection conn, LocalDate date, 
                                                          LocalTime openingTime, 
                                                          LocalTime closingTime) throws SQLException {
        // Get all active reservations for this date
        List<ReservationInfo> reservations = getActiveReservationsForDate(conn, date);
        
        // Last valid booking time is 2 hours before closing
        LocalTime lastValidBookingTime = closingTime.minusHours(2);
        
        for (ReservationInfo res : reservations) {
            // Check if reservation time is invalid
            boolean beforeOpening = res.bookingTime.isBefore(openingTime);
            boolean afterLastSlot = res.bookingTime.isAfter(lastValidBookingTime);
            
            if (beforeOpening || afterLastSlot) {
                String reason = beforeOpening 
                    ? "Restaurant opens at " + openingTime + " on " + date
                    : "Restaurant closes at " + closingTime + " on " + date;
                cancelReservationAndNotify(conn, res, reason);
            }
        }
    }

    /**
     * Cancels reservations where there aren't enough suitable tables available.
     */
    private static void cancelReservationsWithInsufficientTables(Connection conn) throws SQLException {
        // Get all future active reservations
        String sql = "SELECT r.*, " +
                    "u.phone_number AS subscriber_phone, u.email_address AS subscriber_email, u.name AS subscriber_name " +
                    "FROM reservations r " +
                    "LEFT JOIN subscribers s ON r.subscriber_number = s.subscriber_number " +
                    "LEFT JOIN users u ON s.user_id = u.user_id " +
                    "WHERE r.reservation_status = 'ACTIVE' " +
                    "AND (r.booking_date > CURDATE() OR (r.booking_date = CURDATE() AND r.booking_time > CURTIME())) " +
                    "ORDER BY r.booking_date, r.booking_time";

        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        List<ReservationInfo> allReservations = new ArrayList<>();
        while (rs.next()) {
            allReservations.add(extractReservationInfo(rs));
        }
        rs.close();
        ps.close();

        // For each reservation, check if there's still capacity
        for (ReservationInfo res : allReservations) {
            if (!canAccommodateReservation(conn, res)) {
                cancelReservationAndNotify(conn, res, "No suitable table available for your party size");
            }
        }
    }

    /**
     * Checks if a reservation can be accommodated given current table configuration.
     */
    private static boolean canAccommodateReservation(Connection conn, ReservationInfo res) throws SQLException {
        // Count tables that can fit this guest count
        String tableSql = "SELECT COUNT(*) FROM tables_info WHERE seat_capacity >= ?";
        PreparedStatement tablePs = conn.prepareStatement(tableSql);
        tablePs.setInt(1, res.guestCount);
        ResultSet tableRs = tablePs.executeQuery();
        
        int suitableTableCount = 0;
        if (tableRs.next()) {
            suitableTableCount = tableRs.getInt(1);
        }
        tableRs.close();
        tablePs.close();

        if (suitableTableCount == 0) {
            return false;
        }

        // Count overlapping reservations (within 2-hour window)
        String resSql = "SELECT COUNT(*) FROM reservations " +
                       "WHERE reservation_status = 'ACTIVE' " +
                       "AND reservation_id != ? " +
                       "AND booking_date = ? " +
                       "AND booking_time BETWEEN ? AND ?";

        PreparedStatement resPs = conn.prepareStatement(resSql);
        resPs.setInt(1, res.reservationId);
        resPs.setDate(2, Date.valueOf(res.bookingDate));
        resPs.setTime(3, Time.valueOf(res.bookingTime.minusHours(2)));
        resPs.setTime(4, Time.valueOf(res.bookingTime.plusMinutes(119))); // Just under 2 hours

        ResultSet resRs = resPs.executeQuery();
        int overlappingCount = 0;
        if (resRs.next()) {
            overlappingCount = resRs.getInt(1);
        }
        resRs.close();
        resPs.close();

        // If overlapping reservations >= suitable tables, can't accommodate
        return overlappingCount < suitableTableCount;
    }

    /**
     * Gets all active reservations for a specific date.
     */
    private static List<ReservationInfo> getActiveReservationsForDate(Connection conn, LocalDate date) throws SQLException {
        String sql = "SELECT r.*, " +
                    "u.phone_number AS subscriber_phone, u.email_address AS subscriber_email, u.name AS subscriber_name " +
                    "FROM reservations r " +
                    "LEFT JOIN subscribers s ON r.subscriber_number = s.subscriber_number " +
                    "LEFT JOIN users u ON s.user_id = u.user_id " +
                    "WHERE r.reservation_status = 'ACTIVE' AND r.booking_date = ?";

        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setDate(1, Date.valueOf(date));
        ResultSet rs = ps.executeQuery();

        List<ReservationInfo> reservations = new ArrayList<>();
        while (rs.next()) {
            reservations.add(extractReservationInfo(rs));
        }
        
        rs.close();
        ps.close();
        return reservations;
    }

    /**
     * Extracts reservation info from ResultSet.
     */
    private static ReservationInfo extractReservationInfo(ResultSet rs) throws SQLException {
        ReservationInfo info = new ReservationInfo();
        info.reservationId = rs.getInt("reservation_id");
        info.confirmationCode = rs.getString("confirmation_code");
        info.bookingDate = rs.getDate("booking_date").toLocalDate();
        info.bookingTime = rs.getTime("booking_time").toLocalTime();
        info.guestCount = rs.getInt("guest_count");
        info.subscriberNumber = rs.getString("subscriber_number");
        info.walkInPhone = rs.getString("walk_in_phone");
        info.walkInEmail = rs.getString("walk_in_email");
        info.subscriberPhone = rs.getString("subscriber_phone");
        info.subscriberEmail = rs.getString("subscriber_email");
        info.subscriberName = rs.getString("subscriber_name");
        return info;
    }

    /**
     * Cancels a reservation and sends notification to the customer.
     */
    private static void cancelReservationAndNotify(Connection conn, ReservationInfo res, String reason) throws SQLException {
        // Update reservation status to CANCELLED
        String sql = "UPDATE reservations SET reservation_status = 'CANCELLED' WHERE reservation_id = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, res.reservationId);
        ps.executeUpdate();
        ps.close();

        // Determine contact info
        String phone = res.subscriberNumber != null ? res.subscriberPhone : res.walkInPhone;
        String email = res.subscriberNumber != null ? res.subscriberEmail : res.walkInEmail;
        String customerName = res.subscriberNumber != null ? res.subscriberName : "Valued Customer";

        // Build notification message
        String subject = "Bistro Reservation Cancelled - " + res.confirmationCode;
        String message = String.format(
            "Dear %s,\n\n" +
            "We regret to inform you that your reservation has been cancelled.\n\n" +
            "Reservation Details:\n" +
            "- Confirmation Code: %s\n" +
            "- Date: %s\n" +
            "- Time: %s\n" +
            "- Guests: %d\n\n" +
            "Reason: %s\n\n" +
            "We apologize for any inconvenience. Please contact us to reschedule.\n\n" +
            "Best regards,\n" +
            "Bistro Restaurant",
            customerName,
            res.confirmationCode,
            res.bookingDate,
            res.bookingTime,
            res.guestCount,
            reason
        );

        // Send notification
        NotificationService.sendNotification(phone, email, subject, message);

        System.out.println("[AvailabilityService] Cancelled reservation " + res.confirmationCode + 
                          " and notified customer. Reason: " + reason);
    }
}