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
import java.sql.Types;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
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
     * Uses simulation-based availability checking for optimal table allocation.
     * 
     * @param request Message containing date and guestCount
     * @return Message with List of available LocalDateTime slots
     */
    public Message getAvailableSlots(Message request) {
        MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
        PooledConnection pConn = null;

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) request.getData();
            LocalDate date = LocalDate.parse((String) data.get("date"));
            int guestCount = (Integer) data.get("guestCount");

            pConn = pool.getConnection();
            if (pConn == null) {
                return Message.fail("GET_AVAILABLE_SLOTS", "Database connection failed");
            }

            Connection conn = pConn.getConnection();

            // Check if any table can fit this guest count
            if (!hasTableForGuestCount(guestCount, conn)) {
                return Message.fail("GET_AVAILABLE_SLOTS", 
                    "No table available for " + guestCount + " guests. Maximum capacity is " + 
                    getMaxTableCapacity(conn) + " guests per table.");
            }

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

            // Get all tables for simulation
            List<Table> allTables = getAllTables(conn);

            // Generate all possible slots (every 30 minutes)
            List<LocalDateTime> availableSlots = new ArrayList<>();
            LocalTime currentTime = openingTime;

            while (!currentTime.isAfter(lastSlot)) {
                LocalDateTime slotDateTime = LocalDateTime.of(date, currentTime);
                
                if (isSlotAvailable(slotDateTime, guestCount, allTables, conn)) {
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
     * Creates a new reservation for a subscriber.
     * 
     * @param request Message containing reservation details
     * @return Message with created Reservation object if successful
     */
    public Message createReservation(Message request) {
        MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
        PooledConnection pConn = null;

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) request.getData();
            
            LocalDate bookingDate = LocalDate.parse((String) data.get("bookingDate"));
            LocalTime bookingTime = LocalTime.parse((String) data.get("bookingTime"));
            int guestCount = (Integer) data.get("guestCount");
            String subscriberNumber = (String) data.get("subscriberNumber");
            String walkInPhone = (String) data.get("walkInPhone");
            String walkInEmail = (String) data.get("walkInEmail");

            pConn = pool.getConnection();
            if (pConn == null) {
                return Message.fail("CREATE_RESERVATION", "Database connection failed");
            }

            Connection conn = pConn.getConnection();

            // Check if any table can fit this guest count
            if (!hasTableForGuestCount(guestCount, conn)) {
                return Message.fail("CREATE_RESERVATION", 
                    "No table available for " + guestCount + " guests");
            }

            // Get all tables for simulation
            List<Table> allTables = getAllTables(conn);

            // Check if slot is still available
            LocalDateTime requestedDateTime = LocalDateTime.of(bookingDate, bookingTime);
            if (!isSlotAvailable(requestedDateTime, guestCount, allTables, conn)) {
                return Message.fail("CREATE_RESERVATION", "Selected time slot is no longer available");
            }

            // Generate unique confirmation code
            String confirmationCode = generateConfirmationCode();

            // Insert reservation (assigned_table_number is NULL until check-in)
            String sql = "INSERT INTO reservations (booking_date, booking_time, guest_count, " +
                        "confirmation_code, reservation_status, assigned_table_number, " +
                        "subscriber_number, walk_in_phone, walk_in_email) " +
                        "VALUES (?, ?, ?, ?, 'ACTIVE', NULL, ?, ?, ?)";
            
            PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setDate(1, Date.valueOf(bookingDate));
            ps.setTime(2, Time.valueOf(bookingTime));
            ps.setInt(3, guestCount);
            ps.setString(4, confirmationCode);
            ps.setString(5, subscriberNumber);
            ps.setString(6, walkInPhone);
            ps.setString(7, walkInEmail);
            
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
            reservation.setAssignedTableNumber(null);
            reservation.setWalkInPhone(walkInPhone);
            reservation.setWalkInEmail(walkInEmail);

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
            @SuppressWarnings("unchecked")
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
            @SuppressWarnings("unchecked")
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
    
    /**
     * Gets a reservation by its confirmation code.
     * Used when a customer (subscriber or walk-in) checks in with their code.
     * 
     * @param request Message containing "confirmationCode"
     * @return Message with Reservation object if found
     */
    public Message getReservationByCode(Message request) {
        MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
        PooledConnection pConn = null;

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) request.getData();
            String confirmationCode = (String) data.get("confirmationCode");

            if (confirmationCode == null || confirmationCode.trim().isEmpty()) {
                return Message.fail("GET_RESERVATION_BY_CODE", "Confirmation code is required");
            }

            pConn = pool.getConnection();
            if (pConn == null) {
                return Message.fail("GET_RESERVATION_BY_CODE", "Database connection failed");
            }

            Connection conn = pConn.getConnection();
            
            String sql = "SELECT * FROM reservations WHERE confirmation_code = ?";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, confirmationCode);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Reservation reservation = extractReservationFromResultSet(rs);
                rs.close();
                ps.close();
                
                return Message.ok("GET_RESERVATION_BY_CODE", reservation);
            }

            rs.close();
            ps.close();

            return Message.fail("GET_RESERVATION_BY_CODE", "Reservation not found");

        } catch (SQLException e) {
            e.printStackTrace();
            return Message.fail("GET_RESERVATION_BY_CODE", "Database error: " + e.getMessage());
        } finally {
            pool.releaseConnection(pConn);
        }
    }
    
    /**
     * Retrieves a lost reservation confirmation code using a user identifier.
     * Searches both subscribers (by phone/email) and walk-in customers.
     * 
     * @param request a Message containing "identifier" (phone or email)
     * @return a Message containing the confirmation code if found
     */
    public Message retrieveLostCode(Message request) {
        MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
        PooledConnection pConn = null;

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) request.getData();
            String identifier = (String) data.get("identifier");

            if (identifier == null || identifier.trim().isEmpty()) {
                return Message.fail("LOST_CODE", "Identifier is required");
            }

            pConn = pool.getConnection();
            if (pConn == null) {
                return Message.fail("LOST_CODE", "Database connection failed");
            }

            Connection conn = pConn.getConnection();

            // First try: subscriber reservations (upcoming)
            String sqlSubscriberUpcoming =
                    "SELECT r.confirmation_code " +
                    "FROM reservations r " +
                    "JOIN subscribers s ON r.subscriber_number = s.subscriber_number " +
                    "JOIN users u ON s.user_id = u.user_id " +
                    "WHERE r.reservation_status = 'ACTIVE' " +
                    "AND (u.phone_number = ? OR u.email_address = ?) " +
                    "AND (r.booking_date > CURDATE() " +
                    "     OR (r.booking_date = CURDATE() AND r.booking_time >= CURTIME())) " +
                    "ORDER BY r.booking_date ASC, r.booking_time ASC " +
                    "LIMIT 1";

            String confirmationCode = null;

            try (PreparedStatement ps = conn.prepareStatement(sqlSubscriberUpcoming)) {
                ps.setString(1, identifier);
                ps.setString(2, identifier);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        confirmationCode = rs.getString("confirmation_code");
                    }
                }
            }

            // Second try: walk-in reservations (upcoming)
            if (confirmationCode == null) {
                String sqlWalkInUpcoming =
                        "SELECT confirmation_code " +
                        "FROM reservations " +
                        "WHERE reservation_status = 'ACTIVE' " +
                        "AND (walk_in_phone = ? OR walk_in_email = ?) " +
                        "AND (booking_date > CURDATE() " +
                        "     OR (booking_date = CURDATE() AND booking_time >= CURTIME())) " +
                        "ORDER BY booking_date ASC, booking_time ASC " +
                        "LIMIT 1";

                try (PreparedStatement ps = conn.prepareStatement(sqlWalkInUpcoming)) {
                    ps.setString(1, identifier);
                    ps.setString(2, identifier);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            confirmationCode = rs.getString("confirmation_code");
                        }
                    }
                }
            }

            // Fallback: most recent ACTIVE subscriber reservation
            if (confirmationCode == null) {
                String sqlSubscriberLatest =
                        "SELECT r.confirmation_code " +
                        "FROM reservations r " +
                        "JOIN subscribers s ON r.subscriber_number = s.subscriber_number " +
                        "JOIN users u ON s.user_id = u.user_id " +
                        "WHERE r.reservation_status = 'ACTIVE' " +
                        "AND (u.phone_number = ? OR u.email_address = ?) " +
                        "ORDER BY r.booking_date DESC, r.booking_time DESC " +
                        "LIMIT 1";

                try (PreparedStatement ps = conn.prepareStatement(sqlSubscriberLatest)) {
                    ps.setString(1, identifier);
                    ps.setString(2, identifier);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            confirmationCode = rs.getString("confirmation_code");
                        }
                    }
                }
            }

            // Fallback: most recent ACTIVE walk-in reservation
            if (confirmationCode == null) {
                String sqlWalkInLatest =
                        "SELECT confirmation_code " +
                        "FROM reservations " +
                        "WHERE reservation_status = 'ACTIVE' " +
                        "AND (walk_in_phone = ? OR walk_in_email = ?) " +
                        "ORDER BY booking_date DESC, booking_time DESC " +
                        "LIMIT 1";

                try (PreparedStatement ps = conn.prepareStatement(sqlWalkInLatest)) {
                    ps.setString(1, identifier);
                    ps.setString(2, identifier);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            confirmationCode = rs.getString("confirmation_code");
                        }
                    }
                }
            }

            if (confirmationCode != null) {
                return Message.ok("LOST_CODE", confirmationCode);
            }

            return Message.fail("LOST_CODE", "No active reservation found for this identifier");

        } catch (SQLException e) {
            e.printStackTrace();
            return Message.fail("LOST_CODE", "Database error: " + e.getMessage());
        } finally {
            pool.releaseConnection(pConn);
        }
    }

    //  Helper methods 

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
     * Checks if any table in the restaurant can fit the guest count.
     */
    private boolean hasTableForGuestCount(int guestCount, Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM tables_info WHERE seat_capacity >= ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, guestCount);
        ResultSet rs = ps.executeQuery();
        
        boolean hasTable = false;
        if (rs.next()) {
            hasTable = rs.getInt("count") > 0;
        }
        
        rs.close();
        ps.close();
        return hasTable;
    }

    /**
     * Gets the maximum table capacity in the restaurant.
     */
    private int getMaxTableCapacity(Connection conn) throws SQLException {
        String sql = "SELECT MAX(seat_capacity) as max_capacity FROM tables_info";
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        
        int maxCapacity = 0;
        if (rs.next()) {
            maxCapacity = rs.getInt("max_capacity");
        }
        
        rs.close();
        ps.close();
        return maxCapacity;
    }

    /**
     * Gets all tables from the database.
     */
    private List<Table> getAllTables(Connection conn) throws SQLException {
        String sql = "SELECT * FROM tables_info ORDER BY seat_capacity";
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        List<Table> tables = new ArrayList<>();
        while (rs.next()) {
            Table table = new Table();
            table.setTableNumber(rs.getInt("table_number"));
            table.setSeatCapacity(rs.getInt("seat_capacity"));
            table.setTableLocation(rs.getString("table_location"));
            table.setTableStatus(Table.TableStatus.valueOf(rs.getString("table_status")));
            tables.add(table);
        }
        
        rs.close();
        ps.close();
        return tables;
    }

    /**
     * Gets all active reservations for a specific time slot (2-hour window).
     */
    private List<Integer> getGuestCountsForTimeSlot(LocalDateTime startTime, Connection conn) throws SQLException {
        LocalDate date = startTime.toLocalDate();
        LocalTime time = startTime.toLocalTime();
        LocalTime slotStart = time.minusHours(2).plusMinutes(1);
        LocalTime slotEnd = time.plusHours(2).minusMinutes(1);

        String sql = "SELECT guest_count FROM reservations " +
                    "WHERE reservation_status = 'ACTIVE' " +
                    "AND booking_date = ? " +
                    "AND booking_time > ? AND booking_time < ?";
        
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setDate(1, Date.valueOf(date));
        ps.setTime(2, Time.valueOf(slotStart));
        ps.setTime(3, Time.valueOf(slotEnd));
        
        ResultSet rs = ps.executeQuery();
        
        List<Integer> guestCounts = new ArrayList<>();
        while (rs.next()) {
            guestCounts.add(rs.getInt("guest_count"));
        }
        
        rs.close();
        ps.close();
        return guestCounts;
    }

    /**
     * Checks if a time slot is available using best-fit simulation.
     * Simulates seating all existing reservations plus the new one.
     */
    private boolean isSlotAvailable(LocalDateTime startTime, int newGuestCount, 
                                    List<Table> allTables, Connection conn) throws SQLException {
        // Get all guest counts for overlapping reservations
        List<Integer> existingGuestCounts = getGuestCountsForTimeSlot(startTime, conn);
        
        // Add the new reservation's guest count
        List<Integer> allGuestCounts = new ArrayList<>(existingGuestCounts);
        allGuestCounts.add(newGuestCount);
        
        // Sort from largest to smallest (seat big groups first)
        allGuestCounts.sort(Comparator.reverseOrder());
        
        // Create a copy of available tables for simulation
        List<Table> availableTables = new ArrayList<>(allTables);
        
        // Try to seat each group
        for (Integer guestCount : allGuestCounts) {
            // Find the smallest table that fits this group
            Table assignedTable = null;
            for (Table table : availableTables) {
                if (table.getSeatCapacity() >= guestCount) {
                    assignedTable = table;
                    break; // Tables are sorted by capacity, so first match is best fit
                }
            }
            
            if (assignedTable == null) {
                // Can't fit this group - no availability
                return false;
            }
            
            // Remove the assigned table from available pool
            availableTables.remove(assignedTable);
        }
        
        // All groups can be seated
        return true;
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
        
        // Handle nullable assigned_table_number
        int assignedTableNumber = rs.getInt("assigned_table_number");
        if (!rs.wasNull()) {
            reservation.setAssignedTableNumber(assignedTableNumber);
        } else {
            reservation.setAssignedTableNumber(null);
        }
        
        // Handle subscriber number
        String subscriberNumber = rs.getString("subscriber_number");
        reservation.setSubscriberNumber(subscriberNumber);
        
        // Handle walk-in contact info
        String walkInPhone = rs.getString("walk_in_phone");
        reservation.setWalkInPhone(walkInPhone);
        
        String walkInEmail = rs.getString("walk_in_email");
        reservation.setWalkInEmail(walkInEmail);
        
        return reservation;
    }
}