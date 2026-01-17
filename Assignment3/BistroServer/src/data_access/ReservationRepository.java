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
 * Uses database transactions to prevent race conditions in concurrent access.
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
            @SuppressWarnings("unchecked")
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
     * Gets alternative time slots when the requested date is fully booked.
     * Searches within 7 days before and after the requested date.
     * 
     * @param request Message containing date and guestCount
     * @return Message with List of alternative LocalDateTime slots
     */
    public Message getAlternativeSlots(Message request) {
        MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
        PooledConnection pConn = null;

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) request.getData();
            LocalDate requestedDate = LocalDate.parse((String) data.get("date"));
            int guestCount = (Integer) data.get("guestCount");

            pConn = pool.getConnection();
            if (pConn == null) {
                return Message.fail("GET_ALTERNATIVE_SLOTS", "Database connection failed");
            }

            Connection conn = pConn.getConnection();

            List<LocalDateTime> alternatives = new ArrayList<>();
            List<Table> suitableTables = getSuitableTablesForGuests(guestCount, conn);
            
            if (suitableTables.isEmpty()) {
                return Message.ok("GET_ALTERNATIVE_SLOTS", alternatives);
            }

            // Search 7 days before and after
            for (int dayOffset = -7; dayOffset <= 7; dayOffset++) {
                if (dayOffset == 0) continue; // Skip the original date
                
                LocalDate checkDate = requestedDate.plusDays(dayOffset);
                
                // Don't suggest dates in the past
                if (checkDate.isBefore(LocalDate.now())) continue;
                
                // Don't suggest dates more than 1 month ahead
                if (checkDate.isAfter(LocalDate.now().plusMonths(1))) continue;

                OpeningHours hours = getOpeningHoursForDate(checkDate, conn);
                if (hours == null) continue;

                LocalTime openingTime = hours.getOpeningTime();
                LocalTime closingTime = hours.getClosingTime();
                LocalTime lastSlot = closingTime.minusHours(2);

                if (lastSlot.isBefore(openingTime)) continue;

                int slotsFoundForDay = 0;
                LocalTime currentTime = openingTime;

                while (!currentTime.isAfter(lastSlot) && slotsFoundForDay < 3) {
                    LocalDateTime slotDateTime = LocalDateTime.of(checkDate, currentTime);
                    
                    if (isSlotAvailable(slotDateTime, guestCount, suitableTables, conn)) {
                        alternatives.add(slotDateTime);
                        slotsFoundForDay++;
                    }
                    
                    currentTime = currentTime.plusMinutes(30);
                }

                if (alternatives.size() >= 15) break;
            }

            return Message.ok("GET_ALTERNATIVE_SLOTS", alternatives);

        } catch (Exception e) {
            e.printStackTrace();
            return Message.fail("GET_ALTERNATIVE_SLOTS", "Error: " + e.getMessage());
        } finally {
            pool.releaseConnection(pConn);
        }
    }

    /**
     * Creates a new reservation with transaction support to prevent race conditions.
     * Uses database transaction to ensure atomicity between availability check and insert.
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
            String guestPhone = (String) data.get("guestPhone");
            String guestEmail = (String) data.get("guestEmail");

            pConn = pool.getConnection();
            if (pConn == null) {
                return Message.fail("CREATE_RESERVATION", "Database connection failed");
            }

            Connection conn = pConn.getConnection();
            
            // Start transaction to prevent race conditions
            conn.setAutoCommit(false);
            
            try {
                // Check if slot is still available (inside transaction)
                LocalDateTime requestedDateTime = LocalDateTime.of(bookingDate, bookingTime);
                if (!isSlotAvailable(requestedDateTime, guestCount, conn)) {
                    conn.rollback();
                    return Message.fail("CREATE_RESERVATION", "Selected time slot is no longer available");
                }

                // Generate unique confirmation code
                String confirmationCode = generateConfirmationCode();

                // Insert reservation (without table_number - will be assigned at check-in)
                String sql = "INSERT INTO reservations (booking_date, booking_time, guest_count, " +
                            "confirmation_code, reservation_status, subscriber_number, " +
                            "walk_in_phone, walk_in_email) " +
                            "VALUES (?, ?, ?, ?, 'ACTIVE', ?, ?, ?)";
                
                PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
                ps.setDate(1, Date.valueOf(bookingDate));
                ps.setTime(2, Time.valueOf(bookingTime));
                ps.setInt(3, guestCount);
                ps.setString(4, confirmationCode);
                ps.setString(5, subscriberNumber);
                ps.setString(6, guestPhone);
                ps.setString(7, guestEmail);
                
                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                int reservationId = 0;
                if (rs.next()) {
                    reservationId = rs.getInt(1);
                }
                rs.close();
                ps.close();
                
                // Commit transaction - both check and insert succeeded
                conn.commit();

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
                // Rollback on any error
                conn.rollback();
                throw e;
            } finally {
                // Restore auto-commit mode
                conn.setAutoCommit(true);
            }

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
     * Seats a customer by their confirmation code (Check-In process).
     * This method implements the full check-in flow:
     * 1. Validates the reservation exists and is ACTIVE
     * 2. Validates the reservation is for today and within valid time window
     * 3. Finds the best-fit available table (smallest table that fits guest count)
     * 4. If table found: assigns it, marks as OCCUPIED, returns success
     * 5. If no table available: returns special "WAIT" status
     * 
     * @param request Message containing "confirmationCode"
     * @return Message with Reservation object (table assigned) or error/wait status
     */
    public Message seatByCode(Message request) {
        MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
        PooledConnection pConn = null;

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) request.getData();
            String confirmationCode = (String) data.get("confirmationCode");

            if (confirmationCode == null || confirmationCode.trim().isEmpty()) {
                return Message.fail("SEAT_BY_CODE", "Confirmation code is required");
            }

            pConn = pool.getConnection();
            if (pConn == null) {
                return Message.fail("SEAT_BY_CODE", "Database connection failed");
            }

            Connection conn = pConn.getConnection();
            
            // Start transaction for atomic table assignment
            conn.setAutoCommit(false);
            
            try {
                // 1. Get the reservation (with lock to prevent concurrent modifications)
                String resSql = "SELECT * FROM reservations WHERE confirmation_code = ? FOR UPDATE";
                PreparedStatement resPs = conn.prepareStatement(resSql);
                resPs.setString(1, confirmationCode);
                ResultSet resRs = resPs.executeQuery();

                if (!resRs.next()) {
                    resRs.close();
                    resPs.close();
                    conn.rollback();
                    return Message.fail("SEAT_BY_CODE", "Reservation not found");
                }

                Reservation reservation = extractReservationFromResultSet(resRs);
                resRs.close();
                resPs.close();

                // 2. Validate reservation status
                if (reservation.getStatus() != Reservation.ReservationStatus.ACTIVE) {
                    conn.rollback();
                    return Message.fail("SEAT_BY_CODE", 
                        "Reservation is not active (status: " + reservation.getStatus() + ")");
                }

                // 3. Check if already seated (table already assigned)
                if (reservation.hasTableAssigned()) {
                    conn.rollback();
                    // Already seated, return current state
                    return Message.ok("SEAT_BY_CODE", reservation);
                }

                // 4. Validate reservation is for today
                LocalDate today = LocalDate.now();
                if (!reservation.getBookingDate().equals(today)) {
                    conn.rollback();
                    String dateStr = reservation.getBookingDate().toString();
                    if (reservation.getBookingDate().isBefore(today)) {
                        return Message.fail("SEAT_BY_CODE", 
                            "Reservation date has passed (" + dateStr + ")");
                    } else {
                        return Message.fail("SEAT_BY_CODE", 
                            "Reservation is for a future date (" + dateStr + ")");
                    }
                }

                // 5. Validate time window (allow check-in from 15 min before booking time)
                LocalTime now = LocalTime.now();
                LocalTime bookingTime = reservation.getBookingTime();
                LocalTime earliestCheckIn = bookingTime.minusMinutes(15);
                
                if (now.isBefore(earliestCheckIn)) {
                    conn.rollback();
                    String timeStr = earliestCheckIn.toString();
                    if (timeStr.length() > 5) timeStr = timeStr.substring(0, 5);
                    return Message.fail("SEAT_BY_CODE", 
                        "Too early for check-in. Please return at " + timeStr);
                }
                
                // Note: After 15 minutes late, reservation should be marked NO_SHOW by scheduler
                // But we still allow check-in here for flexibility

                // 6. Find best-fit available table (smallest table that fits guest count)
                int guestCount = reservation.getGuestCount();
                
                String tableSql = "SELECT * FROM tables_info " +
                                  "WHERE seat_capacity >= ? AND table_status = 'AVAILABLE' " +
                                  "ORDER BY seat_capacity ASC " +
                                  "LIMIT 1 FOR UPDATE";
                
                PreparedStatement tablePs = conn.prepareStatement(tableSql);
                tablePs.setInt(1, guestCount);
                ResultSet tableRs = tablePs.executeQuery();

                if (!tableRs.next()) {
                    // No available table - customer needs to wait
                    tableRs.close();
                    tablePs.close();
                    conn.rollback();
                    
                    // Return special "WAIT" response (error message starts with "WAIT:")
                    return Message.fail("SEAT_BY_CODE", 
                        "WAIT:No table available right now. Please wait, you will be notified when a table is ready.");
                }

                // 7. Found a table - get its number
                int tableNumber = tableRs.getInt("table_number");
                tableRs.close();
                tablePs.close();

                // 8. Update table status to OCCUPIED
                String updateTableSql = "UPDATE tables_info SET table_status = 'OCCUPIED', " +
                                        "reservation_start = NOW(), " +
                                        "reservation_end = DATE_ADD(NOW(), INTERVAL 2 HOUR) " +
                                        "WHERE table_number = ?";
                PreparedStatement updateTablePs = conn.prepareStatement(updateTableSql);
                updateTablePs.setInt(1, tableNumber);
                updateTablePs.executeUpdate();
                updateTablePs.close();

             // 9. Update reservation with assigned table
                String updateResSql = "UPDATE reservations SET assigned_table_number = ? WHERE reservation_id = ?";
                PreparedStatement updateResPs = conn.prepareStatement(updateResSql);
                updateResPs.setInt(1, tableNumber);
                updateResPs.setInt(2, reservation.getReservationId());
                updateResPs.executeUpdate();
                updateResPs.close();

                // 10. Commit transaction
                conn.commit();

                // 11. Return updated reservation with table number
                reservation.setAssignedTableNumber(tableNumber);
                return Message.ok("SEAT_BY_CODE", reservation);

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return Message.fail("SEAT_BY_CODE", "Database error: " + e.getMessage());
        } finally {
            pool.releaseConnection(pConn);
        }
    }
    
    /**
     * Retrieves a lost reservation confirmation code using a user identifier.
     * The identifier can be either a phone number or an email address.
     * 
     * @param request Message containing "identifier" (phone or email)
     * @return Message with confirmation code if found
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

            // Nearest upcoming ACTIVE reservation for subscriber
            String sqlUpcoming =
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

            // Fallback: most recent ACTIVE reservation for subscriber
            String sqlLatest =
                    "SELECT r.confirmation_code " +
                    "FROM reservations r " +
                    "JOIN subscribers s ON r.subscriber_number = s.subscriber_number " +
                    "JOIN users u ON s.user_id = u.user_id " +
                    "WHERE r.reservation_status = 'ACTIVE' " +
                    "AND (u.phone_number = ? OR u.email_address = ?) " +
                    "ORDER BY r.booking_date DESC, r.booking_time DESC " +
                    "LIMIT 1";

            // Also check walk-in reservations
            String sqlWalkIn =
                    "SELECT confirmation_code " +
                    "FROM reservations " +
                    "WHERE reservation_status = 'ACTIVE' " +
                    "AND (walk_in_phone = ? OR walk_in_email = ?) " +
                    "AND (booking_date > CURDATE() " +
                    "     OR (booking_date = CURDATE() AND booking_time >= CURTIME())) " +
                    "ORDER BY booking_date ASC, booking_time ASC " +
                    "LIMIT 1";

            String confirmationCode = null;

            // Try upcoming subscriber reservation
            try (PreparedStatement ps = conn.prepareStatement(sqlUpcoming)) {
                ps.setString(1, identifier);
                ps.setString(2, identifier);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        confirmationCode = rs.getString("confirmation_code");
                    }
                }
            }

            // Try latest subscriber reservation
            if (confirmationCode == null) {
                try (PreparedStatement ps = conn.prepareStatement(sqlLatest)) {
                    ps.setString(1, identifier);
                    ps.setString(2, identifier);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            confirmationCode = rs.getString("confirmation_code");
                        }
                    }
                }
            }

            // Try walk-in reservation
            if (confirmationCode == null) {
                try (PreparedStatement ps = conn.prepareStatement(sqlWalkIn)) {
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

    //  Helper Methods 

    /**
     * Gets opening hours for a specific date.
     * Checks special hours first, then falls back to weekly hours.
     * 
     * @param date the date to check
     * @param conn database connection
     * @return OpeningHours or null if closed
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
     * 
     * @param startTime the requested start time
     * @param guestCount number of guests
     * @param conn database connection
     * @return true if slot is available
     */
    private boolean isSlotAvailable(LocalDateTime startTime, int guestCount, Connection conn) throws SQLException {
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

        // Check: if overlapping reservations >= available tables, slot is full
        return overlappingReservations < suitableTables.size();
    }

    /**
     * Checks if a time slot is available with pre-fetched table list.
     * Used by getAlternativeSlots for efficiency.
     * 
     * @param startTime the requested start time
     * @param guestCount number of guests
     * @param suitableTables pre-fetched list of suitable tables
     * @param conn database connection
     * @return true if slot is available
     */
    private boolean isSlotAvailable(LocalDateTime startTime, int guestCount, 
                                    List<Table> suitableTables, Connection conn) throws SQLException {
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

        return overlappingReservations < suitableTables.size();
    }

    /**
     * Gets all tables suitable for the given guest count.
     * 
     * @param guestCount number of guests
     * @param conn database connection
     * @return list of suitable tables
     */
    private List<Table> getSuitableTablesForGuests(int guestCount, Connection conn) throws SQLException {
        String sql = "SELECT * FROM tables_info WHERE seat_capacity >= ? AND table_status = 'AVAILABLE'";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, guestCount);
        ResultSet rs = ps.executeQuery();

        List<Table> tables = new ArrayList<>();
        while (rs.next()) {
            Table table = new Table();
            table.setTableNumber(rs.getInt("table_number"));
            table.setSeatCapacity(rs.getInt("seat_capacity"));
            tables.add(table);
        }
        rs.close();
        ps.close();

        return tables;
    }

    /**
     * Generates a unique confirmation code.
     * 
     * @return unique confirmation code string
     */
    private String generateConfirmationCode() {
        return "RES" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Extracts a Reservation object from ResultSet.
     * 
     * @param rs result set positioned at a reservation row
     * @return Reservation object
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
        
        int tableNumber = rs.getInt("assigned_table_number");
        if (!rs.wasNull()) {
            reservation.setAssignedTableNumber(tableNumber);
        }
        
        String subscriberNumber = rs.getString("subscriber_number");
        if (subscriberNumber != null) {
            reservation.setSubscriberNumber(subscriberNumber);
        }
        
        return reservation;
    }
}