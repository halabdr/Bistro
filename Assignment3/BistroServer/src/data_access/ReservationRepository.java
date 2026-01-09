package data_access;
import connection.MySQLConnectionPool;
import connection.PooledConnection;
import common.Message;
import entities.Reservation;
import entities.Table;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Repository for managing reservations.
 * Handles reservation creation, cancellation, availability checking, and retrieval.
 */
public class ReservationRepository {

    /**
     * Gets available time slots for a given date and number of guests.
     * 
     * @param request Message containing date and guestCount
     * @return Message with List of available LocalDateTime slots
     */
    public Message getAvailableSlots(Message request) {
        MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
        PooledConnection pConn = null;

        try {
            Map<String, Object> data = (Map<String, Object>) request.getData();
            LocalDate date = LocalDate.parse((String) data.get("date"));
            int guestCount = (Integer) data.get("guestCount");

            pConn = pool.getConnection();
            if (pConn == null) {
                return Message.fail("GET_AVAILABLE_SLOTS", "Database connection failed");
            }

            Connection conn = pConn.getConnection();

            // Get opening hours for this date
            OpeningHours hours = getOpeningHoursForDate(date, conn);
            if (hours == null) {
                return Message.ok("GET_AVAILABLE_SLOTS", new ArrayList<LocalDateTime>());
            }

            LocalTime openingTime = hours.getOpeningTime();
            LocalTime closingTime = hours.getClosingTime();
            LocalTime lastSlot = closingTime.minusHours(2);

            if (lastSlot.isBefore(openingTime)) {
                return Message.ok("GET_AVAILABLE_SLOTS", new ArrayList<LocalDateTime>());
            }

            // Generate all possible slots (every 30 minutes)
            List<LocalDateTime> availableSlots = new ArrayList<>();
            LocalTime currentTime = openingTime;

            while (!currentTime.isAfter(lastSlot)) {
                LocalDateTime slotDateTime = LocalDateTime.of(date, currentTime);
                
                if (isSlotAvailable(slotDateTime, guestCount, conn)) {
                    availableSlots.add(slotDateTime);
                }
                
                currentTime = currentTime.plusMinutes(30);
            }

            return Message.ok("GET_AVAILABLE_SLOTS", availableSlots);

        } catch (Exception e) {
            e.printStackTrace();
            return Message.fail("GET_AVAILABLE_SLOTS", "Error: " + e.getMessage());
        } finally {
            pool.releaseConnection(pConn);
        }
    }

    /**
     * Creates a new reservation.
     * 
     * @param request Message containing reservation details
     * @return Message with created Reservation object if successful
     */
    public Message createReservation(Message request) {
        MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
        PooledConnection pConn = null;

        try {
            Map<String, Object> data = (Map<String, Object>) request.getData();
            
            LocalDate bookingDate = LocalDate.parse((String) data.get("bookingDate"));
            LocalTime bookingTime = LocalTime.parse((String) data.get("bookingTime"));
            int guestCount = (Integer) data.get("guestCount");
            String subscriberNumber = (String) data.get("subscriberNumber");

            pConn = pool.getConnection();
            if (pConn == null) {
                return Message.fail("CREATE_RESERVATION", "Database connection failed");
            }

            Connection conn = pConn.getConnection();

            // Check if slot is still available
            LocalDateTime requestedDateTime = LocalDateTime.of(bookingDate, bookingTime);
            if (!isSlotAvailable(requestedDateTime, guestCount, conn)) {
                return Message.fail("CREATE_RESERVATION", "Selected time slot is no longer available");
            }

            // Generate unique confirmation code
            String confirmationCode = generateConfirmationCode();

            // Insert reservation
            String sql = "INSERT INTO reservations (booking_date, booking_time, guest_count, " +
                        "confirmation_code, reservation_status, subscriber_number) " +
                        "VALUES (?, ?, ?, ?, 'ACTIVE', ?)";
            
            PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setDate(1, Date.valueOf(bookingDate));
            ps.setTime(2, Time.valueOf(bookingTime));
            ps.setInt(3, guestCount);
            ps.setString(4, confirmationCode);
            ps.setString(5, subscriberNumber);
            
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            int reservationId = 0;
            if (rs.next()) {
                reservationId = rs.getInt(1);
            }
            rs.close();
            ps.close();

            // Create and return Reservation object
            Reservation reservation = new Reservation();
            reservation.setReservationId(reservationId);
            reservation.setBookingDate(bookingDate);
            reservation.setBookingTime(bookingTime);
            reservation.setGuestCount(guestCount);
            reservation.setConfirmationCode(confirmationCode);
            reservation.setReservationStatus(Reservation.ReservationStatus.ACTIVE);
            reservation.setSubscriberNumber(subscriberNumber);

            return Message.ok("CREATE_RESERVATION", reservation);

        } catch (SQLException e) {
            e.printStackTrace();
            return Message.fail("CREATE_RESERVATION", "Database error: " + e.getMessage());
        } finally {
            pool.releaseConnection(pConn);
        }
    }

    /**
     * Cancels a reservation by confirmation code.
     * 
     * @param request Message containing "confirmationCode"
     * @return Message with success or error
     */
    public Message cancelReservation(Message request) {
        MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
        PooledConnection pConn = null;

        try {
            Map<String, Object> data = (Map<String, Object>) request.getData();
            String confirmationCode = (String) data.get("confirmationCode");

            pConn = pool.getConnection();
            if (pConn == null) {
                return Message.fail("CANCEL_RESERVATION", "Database connection failed");
            }

            Connection conn = pConn.getConnection();
            
            String sql = "UPDATE reservations SET reservation_status = 'CANCELLED' " +
                        "WHERE confirmation_code = ? AND reservation_status = 'ACTIVE'";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, confirmationCode);
            
            int rowsAffected = ps.executeUpdate();
            ps.close();

            if (rowsAffected > 0) {
                return Message.ok("CANCEL_RESERVATION", "Reservation cancelled successfully");
            } else {
                return Message.fail("CANCEL_RESERVATION", "Reservation not found or already cancelled");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return Message.fail("CANCEL_RESERVATION", "Database error: " + e.getMessage());
        } finally {
            pool.releaseConnection(pConn);
        }
    }

    /**
     * Gets all active reservations (for representatives).
     * 
     * @param request Message (empty data)
     * @return Message with List of all Reservation objects
     */
    public Message getAllReservations(Message request) {
        MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
        PooledConnection pConn = null;

        try {
            pConn = pool.getConnection();
            if (pConn == null) {
                return Message.fail("GET_RESERVATIONS", "Database connection failed");
            }

            Connection conn = pConn.getConnection();
            
            String sql = "SELECT * FROM reservations WHERE reservation_status = 'ACTIVE' " +
                        "ORDER BY booking_date, booking_time";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            List<Reservation> reservations = new ArrayList<>();
            while (rs.next()) {
                reservations.add(extractReservationFromResultSet(rs));
            }

            rs.close();
            ps.close();

            return Message.ok("GET_RESERVATIONS", reservations);

        } catch (SQLException e) {
            e.printStackTrace();
            return Message.fail("GET_RESERVATIONS", "Database error: " + e.getMessage());
        } finally {
            pool.releaseConnection(pConn);
        }
    }

    /**
     * Gets reservations for a specific subscriber.
     * 
     * @param request Message containing "subscriberNumber"
     * @return Message with List of Reservation objects for the subscriber
     */
    public Message getUserReservations(Message request) {
        MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
        PooledConnection pConn = null;

        try {
            Map<String, Object> data = (Map<String, Object>) request.getData();
            String subscriberNumber = (String) data.get("subscriberNumber");

            pConn = pool.getConnection();
            if (pConn == null) {
                return Message.fail("GET_USER_RESERVATIONS", "Database connection failed");
            }

            Connection conn = pConn.getConnection();
            
            String sql = "SELECT * FROM reservations WHERE subscriber_number = ? " +
                        "ORDER BY booking_date DESC, booking_time DESC";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, subscriberNumber);
            ResultSet rs = ps.executeQuery();

            List<Reservation> reservations = new ArrayList<>();
            while (rs.next()) {
                reservations.add(extractReservationFromResultSet(rs));
            }

            rs.close();
            ps.close();

            return Message.ok("GET_USER_RESERVATIONS", reservations);

        } catch (SQLException e) {
            e.printStackTrace();
            return Message.fail("GET_USER_RESERVATIONS", "Database error: " + e.getMessage());
        } finally {
            pool.releaseConnection(pConn);
        }
    }

    //Helper methods

    /**
     * Gets opening hours for a specific date.
     * Checks special hours first, then falls back to weekly hours.
     */
    private OpeningHours getOpeningHoursForDate(LocalDate date, Connection conn) throws SQLException {
        // Check for special hours first
        String specialSql = "SELECT * FROM special_hours WHERE special_date = ?";
        PreparedStatement specialPs = conn.prepareStatement(specialSql);
        specialPs.setDate(1, Date.valueOf(date));
        ResultSet specialRs = specialPs.executeQuery();

        if (specialRs.next()) {
            boolean closedFlag = specialRs.getBoolean("closed_flag");
            if (closedFlag) {
                specialRs.close();
                specialPs.close();
                return null; // Restaurant is closed
            }

            Time openingTime = specialRs.getTime("opening_time");
            Time closingTime = specialRs.getTime("closing_time");
            specialRs.close();
            specialPs.close();

            DayOfWeek dayOfWeek = date.getDayOfWeek();
            OpeningHours.Weekday weekday = OpeningHours.Weekday.valueOf(dayOfWeek.toString());
            
            OpeningHours hours = new OpeningHours();
            hours.setWeekday(weekday);
            hours.setOpeningTime(openingTime.toLocalTime());
            hours.setClosingTime(closingTime.toLocalTime());
            return hours;
        }

        specialRs.close();
        specialPs.close();

        // Get regular weekly hours
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        String weekday = dayOfWeek.toString();

        String weeklySql = "SELECT * FROM opening_hours WHERE weekday = ?";
        PreparedStatement weeklyPs = conn.prepareStatement(weeklySql);
        weeklyPs.setString(1, weekday);
        ResultSet weeklyRs = weeklyPs.executeQuery();

        OpeningHours hours = null;
        if (weeklyRs.next()) {
            hours = new OpeningHours();
            hours.setWeekday(OpeningHours.Weekday.valueOf(weekday));
            hours.setOpeningTime(weeklyRs.getTime("opening_time").toLocalTime());
            hours.setClosingTime(weeklyRs.getTime("closing_time").toLocalTime());
        }

        weeklyRs.close();
        weeklyPs.close();
        return hours;
    }

    /**
     * Checks if a time slot is available for the given number of guests.
     */
    private boolean isSlotAvailable(LocalDateTime startTime, int guestCount, Connection conn) throws SQLException {
        LocalDateTime endTime = startTime.plusHours(2);

        // Get all tables that can fit the guest count
        String tableSql = "SELECT * FROM tables_info WHERE seat_capacity >= ? AND table_status = 'AVAILABLE'";
        PreparedStatement tablePs = conn.prepareStatement(tableSql);
        tablePs.setInt(1, guestCount);
        ResultSet tableRs = tablePs.executeQuery();

        List<Table> suitableTables = new ArrayList<>();
        while (tableRs.next()) {
            Table table = new Table();
            table.setTableNumber(tableRs.getInt("table_number"));
            table.setSeatCapacity(tableRs.getInt("seat_capacity"));
            suitableTables.add(table);
        }
        tableRs.close();
        tablePs.close();

        if (suitableTables.isEmpty()) {
            return false;
        }

        // Check for overlapping reservations
        String resSql = "SELECT COUNT(*) as count FROM reservations " +
                       "WHERE reservation_status = 'ACTIVE' " +
                       "AND booking_date = ? " +
                       "AND booking_time BETWEEN ? AND ?";
        
        PreparedStatement resPs = conn.prepareStatement(resSql);
        resPs.setDate(1, Date.valueOf(startTime.toLocalDate()));
        resPs.setTime(2, Time.valueOf(startTime.toLocalTime().minusHours(2)));
        resPs.setTime(3, Time.valueOf(startTime.toLocalTime().plusMinutes(30)));
        
        ResultSet resRs = resPs.executeQuery();
        int overlappingReservations = 0;
        if (resRs.next()) {
            overlappingReservations = resRs.getInt("count");
        }
        resRs.close();
        resPs.close();

        //  check: if overlapping reservations >= available tables, slot is full
        return overlappingReservations < suitableTables.size();
    }

    /**
     * Generates a unique confirmation code.
     */
    private String generateConfirmationCode() {
        return "RES" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Extracts a Reservation object from ResultSet.
     */
    private Reservation extractReservationFromResultSet(ResultSet rs) throws SQLException {
        Reservation reservation = new Reservation();
        reservation.setReservationId(rs.getInt("reservation_id"));
        reservation.setBookingDate(rs.getDate("booking_date").toLocalDate());
        reservation.setBookingTime(rs.getTime("booking_time").toLocalTime());
        reservation.setGuestCount(rs.getInt("guest_count"));
        reservation.setConfirmationCode(rs.getString("confirmation_code"));
        reservation.setReservationStatus(
            Reservation.ReservationStatus.valueOf(rs.getString("reservation_status"))
        );
        
        int tableNumber = rs.getInt("table_number");
        if (!rs.wasNull()) {
            reservation.setTableNumber(tableNumber);
        }
        
        String subscriberNumber = rs.getString("subscriber_number");
        if (subscriberNumber != null) {
            reservation.setSubscriberNumber(subscriberNumber);
        }
        
        return reservation;
    }
}