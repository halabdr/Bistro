package data_access;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;

/**
 * Repository for logging subscriber activities to the tags table.
 * Used for generating reports and tracking subscriber history.
 * 
 * Activity types:
 * - RESERVATION: New reservation created
 * - CANCEL: Reservation cancelled
 * - CHECK_IN: Customer seated at table
 * - PAYMENT: Bill paid
 */
public class TagRepository {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Logs a new reservation for a subscriber.
     * 
     * @param conn database connection
     * @param subscriberNumber subscriber number
     * @param confirmationCode reservation confirmation code
     * @param bookingDate reservation date
     * @param bookingTime reservation time
     * @param guestCount number of guests
     */
    public static void logReservation(Connection conn, String subscriberNumber, 
            String confirmationCode, LocalDate bookingDate, LocalTime bookingTime, int guestCount) {
        
        if (subscriberNumber == null || subscriberNumber.trim().isEmpty()) {
            return; // Only log for subscribers
        }

        String details = String.format(
            "{\"type\":\"RESERVATION\",\"confirmationCode\":\"%s\",\"date\":\"%s\",\"time\":\"%s\",\"guests\":%d}",
            confirmationCode,
            bookingDate.format(DATE_FMT),
            bookingTime.format(TIME_FMT),
            guestCount
        );

        insertTag(conn, subscriberNumber, details);
    }

    /**
     * Logs a reservation cancellation for a subscriber.
     * 
     * @param conn database connection
     * @param subscriberNumber subscriber number
     * @param confirmationCode reservation confirmation code
     */
    public static void logCancellation(Connection conn, String subscriberNumber, String confirmationCode) {
        
        if (subscriberNumber == null || subscriberNumber.trim().isEmpty()) {
            return; // Only log for subscribers
        }

        String details = String.format(
            "{\"type\":\"CANCEL\",\"confirmationCode\":\"%s\"}",
            confirmationCode
        );

        insertTag(conn, subscriberNumber, details);
    }

    /**
     * Logs a check-in (seating) for a subscriber.
     * Records the booking time vs actual arrival time to track delays.
     * 
     * @param conn database connection
     * @param subscriberNumber subscriber number
     * @param confirmationCode reservation confirmation code
     * @param tableNumber assigned table number
     * @param bookingTime original booking time
     * @param actualTime actual arrival time
     */
    public static void logCheckIn(Connection conn, String subscriberNumber, 
            String confirmationCode, int tableNumber, LocalTime bookingTime, LocalTime actualTime) {
        
        if (subscriberNumber == null || subscriberNumber.trim().isEmpty()) {
            return; // Only log for subscribers
        }

        String details = String.format(
            "{\"type\":\"CHECK_IN\",\"confirmationCode\":\"%s\",\"tableNumber\":%d,\"bookingTime\":\"%s\",\"actualTime\":\"%s\"}",
            confirmationCode,
            tableNumber,
            bookingTime.format(TIME_FMT),
            actualTime.format(TIME_FMT)
        );

        insertTag(conn, subscriberNumber, details);
    }

    /**
     * Logs a payment for a subscriber.
     * 
     * @param conn database connection
     * @param subscriberNumber subscriber number
     * @param confirmationCode reservation confirmation code
     * @param tableNumber table number
     * @param totalAmount total bill amount
     * @param discountAmount discount given
     */
    public static void logPayment(Connection conn, String subscriberNumber, 
            String confirmationCode, int tableNumber, BigDecimal totalAmount, BigDecimal discountAmount) {
        
        if (subscriberNumber == null || subscriberNumber.trim().isEmpty()) {
            return; // Only log for subscribers
        }

        String details = String.format(
            "{\"type\":\"PAYMENT\",\"confirmationCode\":\"%s\",\"tableNumber\":%d,\"amount\":%.2f,\"discount\":%.2f}",
            confirmationCode,
            tableNumber,
            totalAmount.doubleValue(),
            discountAmount.doubleValue()
        );

        insertTag(conn, subscriberNumber, details);
    }
    
    /**
     * Logs a waitlist join for a subscriber.
     * 
     * @param conn database connection
     * @param subscriberNumber subscriber number
     * @param entryCode waitlist entry code
     * @param numberOfDiners number of diners
     */
    public static void logWaitlistJoin(Connection conn, String subscriberNumber, 
            String entryCode, int numberOfDiners) {
        
        if (subscriberNumber == null || subscriberNumber.trim().isEmpty()) {
            return; // Only log for subscribers
        }

        String details = String.format(
            "{\"type\":\"WAITLIST_JOIN\",\"entryCode\":\"%s\",\"diners\":%d}",
            entryCode,
            numberOfDiners
        );

        insertTag(conn, subscriberNumber, details);
    }

    /**
     * Inserts a tag record into the database.
     * 
     * @param conn database connection
     * @param subscriberNumber subscriber number
     * @param activityDetails JSON string with activity details
     */
    private static void insertTag(Connection conn, String subscriberNumber, String activityDetails) {
        String sql = "INSERT INTO tags (subscriber_number, activity_details) VALUES (?, ?)";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, subscriberNumber);
            ps.setString(2, activityDetails);
            ps.executeUpdate();
        } catch (SQLException e) {
            // Log error but don't fail the main operation
            System.err.println("[TagRepository] Failed to log activity: " + e.getMessage());
            e.printStackTrace();
        }
    }
}