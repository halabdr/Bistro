package data_access;

import connection.MySQLConnectionPool;
import connection.PooledConnection;
import common.Message;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository for generating monthly reports.
 * Provides time reports and subscriber activity reports.
 */
public class ReportRepository {

    /**
     * Generates the Time Report for a given month.
     * Shows arrival/departure times, delays, and no-shows.
     *
     * @param request Message containing "year" and "month"
     * @return Message with report data
     */
    public Message getTimeReport(Message request) {
        MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
        PooledConnection pConn = null;

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) request.getData();
            int year = (Integer) data.get("year");
            int month = (Integer) data.get("month");

            pConn = pool.getConnection();
            if (pConn == null) {
                return Message.fail("GET_TIME_REPORT", "Database connection failed");
            }

            Connection conn = pConn.getConnection();
            List<Map<String, Object>> reportData = new ArrayList<>();

            // 1. Total reservations in the month
            String totalSql = "SELECT COUNT(*) AS total FROM reservations " +
                             "WHERE YEAR(booking_date) = ? AND MONTH(booking_date) = ?";
            try (PreparedStatement ps = conn.prepareStatement(totalSql)) {
                ps.setInt(1, year);
                ps.setInt(2, month);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("field", "Total Reservations");
                    row.put("value", String.valueOf(rs.getInt("total")));
                    reportData.add(row);
                }
            }

            // 2. Completed reservations (checked in)
            String completedSql = "SELECT COUNT(*) AS completed FROM reservations " +
                                 "WHERE YEAR(booking_date) = ? AND MONTH(booking_date) = ? " +
                                 "AND reservation_status = 'COMPLETED'";
            try (PreparedStatement ps = conn.prepareStatement(completedSql)) {
                ps.setInt(1, year);
                ps.setInt(2, month);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("field", "Completed (Checked In)");
                    row.put("value", String.valueOf(rs.getInt("completed")));
                    reportData.add(row);
                }
            }

            // 3. No-shows
            String noShowSql = "SELECT COUNT(*) AS noshow FROM reservations " +
                              "WHERE YEAR(booking_date) = ? AND MONTH(booking_date) = ? " +
                              "AND reservation_status = 'NO_SHOW'";
            try (PreparedStatement ps = conn.prepareStatement(noShowSql)) {
                ps.setInt(1, year);
                ps.setInt(2, month);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("field", "No-Shows");
                    row.put("value", String.valueOf(rs.getInt("noshow")));
                    reportData.add(row);
                }
            }

            // 4. Cancelled reservations
            String cancelledSql = "SELECT COUNT(*) AS cancelled FROM reservations " +
                                 "WHERE YEAR(booking_date) = ? AND MONTH(booking_date) = ? " +
                                 "AND reservation_status = 'CANCELLED'";
            try (PreparedStatement ps = conn.prepareStatement(cancelledSql)) {
                ps.setInt(1, year);
                ps.setInt(2, month);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("field", "Cancelled");
                    row.put("value", String.valueOf(rs.getInt("cancelled")));
                    reportData.add(row);
                }
            }

            // 5. Active reservations
            String activeSql = "SELECT COUNT(*) AS active FROM reservations " +
                              "WHERE YEAR(booking_date) = ? AND MONTH(booking_date) = ? " +
                              "AND reservation_status = 'ACTIVE'";
            try (PreparedStatement ps = conn.prepareStatement(activeSql)) {
                ps.setInt(1, year);
                ps.setInt(2, month);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("field", "Active (Pending)");
                    row.put("value", String.valueOf(rs.getInt("active")));
                    reportData.add(row);
                }
            }

            // 6. Reservations by time slot
            String timeSlotSql = "SELECT " +
                                "CASE " +
                                "  WHEN HOUR(booking_time) < 12 THEN 'Morning (before 12:00)' " +
                                "  WHEN HOUR(booking_time) < 17 THEN 'Afternoon (12:00-17:00)' " +
                                "  ELSE 'Evening (after 17:00)' " +
                                "END AS time_slot, COUNT(*) AS count " +
                                "FROM reservations " +
                                "WHERE YEAR(booking_date) = ? AND MONTH(booking_date) = ? " +
                                "GROUP BY time_slot ORDER BY time_slot";
            try (PreparedStatement ps = conn.prepareStatement(timeSlotSql)) {
                ps.setInt(1, year);
                ps.setInt(2, month);
                ResultSet rs = ps.executeQuery();
                
                Map<String, Object> headerRow = new HashMap<>();
                headerRow.put("field", "--- By Time Slot ---");
                headerRow.put("value", "");
                reportData.add(headerRow);
                
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("field", rs.getString("time_slot"));
                    row.put("value", String.valueOf(rs.getInt("count")));
                    reportData.add(row);
                }
            }

            // 7. Average guests per reservation
            String avgGuestsSql = "SELECT AVG(guest_count) AS avg_guests FROM reservations " +
                                 "WHERE YEAR(booking_date) = ? AND MONTH(booking_date) = ?";
            try (PreparedStatement ps = conn.prepareStatement(avgGuestsSql)) {
                ps.setInt(1, year);
                ps.setInt(2, month);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("field", "Average Guests per Reservation");
                    double avg = rs.getDouble("avg_guests");
                    row.put("value", String.format("%.1f", avg));
                    reportData.add(row);
                }
            }

            return Message.ok("GET_TIME_REPORT", reportData);

        } catch (SQLException e) {
            e.printStackTrace();
            return Message.fail("GET_TIME_REPORT", "Database error: " + e.getMessage());
        } finally {
            pool.releaseConnection(pConn);
        }
    }

    /**
     * Generates the Subscribers Report for a given month.
     * Shows subscriber activity: reservations, waitlist entries, bills.
     *
     * @param request Message containing "year" and "month"
     * @return Message with report data
     */
    public Message getSubscribersReport(Message request) {
        MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
        PooledConnection pConn = null;

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) request.getData();
            int year = (Integer) data.get("year");
            int month = (Integer) data.get("month");

            pConn = pool.getConnection();
            if (pConn == null) {
                return Message.fail("GET_SUBSCRIBERS_REPORT", "Database connection failed");
            }

            Connection conn = pConn.getConnection();
            List<Map<String, Object>> reportData = new ArrayList<>();

            // 1. Total subscribers
            String totalSubSql = "SELECT COUNT(*) AS total FROM subscribers";
            try (PreparedStatement ps = conn.prepareStatement(totalSubSql)) {
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("field", "Total Subscribers");
                    row.put("value", String.valueOf(rs.getInt("total")));
                    reportData.add(row);
                }
            }

            // 2. Active subscribers this month (made at least one reservation)
            String activeSubSql = "SELECT COUNT(DISTINCT subscriber_number) AS active " +
                                 "FROM reservations " +
                                 "WHERE subscriber_number IS NOT NULL " +
                                 "AND YEAR(booking_date) = ? AND MONTH(booking_date) = ?";
            try (PreparedStatement ps = conn.prepareStatement(activeSubSql)) {
                ps.setInt(1, year);
                ps.setInt(2, month);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("field", "Active Subscribers This Month");
                    row.put("value", String.valueOf(rs.getInt("active")));
                    reportData.add(row);
                }
            }

            // 3. Subscriber vs Walk-in reservations
            String subResSql = "SELECT COUNT(*) AS sub_res FROM reservations " +
                              "WHERE subscriber_number IS NOT NULL " +
                              "AND YEAR(booking_date) = ? AND MONTH(booking_date) = ?";
            String walkInResSql = "SELECT COUNT(*) AS walkin_res FROM reservations " +
                                 "WHERE subscriber_number IS NULL " +
                                 "AND YEAR(booking_date) = ? AND MONTH(booking_date) = ?";
            
            int subRes = 0, walkInRes = 0;
            try (PreparedStatement ps = conn.prepareStatement(subResSql)) {
                ps.setInt(1, year);
                ps.setInt(2, month);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) subRes = rs.getInt("sub_res");
            }
            try (PreparedStatement ps = conn.prepareStatement(walkInResSql)) {
                ps.setInt(1, year);
                ps.setInt(2, month);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) walkInRes = rs.getInt("walkin_res");
            }

            Map<String, Object> subResRow = new HashMap<>();
            subResRow.put("field", "Subscriber Reservations");
            subResRow.put("value", String.valueOf(subRes));
            reportData.add(subResRow);

            Map<String, Object> walkInResRow = new HashMap<>();
            walkInResRow.put("field", "Walk-in Reservations");
            walkInResRow.put("value", String.valueOf(walkInRes));
            reportData.add(walkInResRow);

            // 4. Waitlist usage
            String waitlistSql = "SELECT COUNT(*) AS wl_total, " +
                                "SUM(CASE WHEN subscriber_number IS NOT NULL THEN 1 ELSE 0 END) AS wl_sub " +
                                "FROM waiting_list " +
                                "WHERE YEAR(request_time) = ? AND MONTH(request_time) = ?";
            try (PreparedStatement ps = conn.prepareStatement(waitlistSql)) {
                ps.setInt(1, year);
                ps.setInt(2, month);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    Map<String, Object> headerRow = new HashMap<>();
                    headerRow.put("field", "--- Waitlist ---");
                    headerRow.put("value", "");
                    reportData.add(headerRow);

                    Map<String, Object> row1 = new HashMap<>();
                    row1.put("field", "Total Waitlist Entries");
                    row1.put("value", String.valueOf(rs.getInt("wl_total")));
                    reportData.add(row1);

                    Map<String, Object> row2 = new HashMap<>();
                    row2.put("field", "Subscriber Waitlist Entries");
                    row2.put("value", String.valueOf(rs.getInt("wl_sub")));
                    reportData.add(row2);
                }
            }

            // 5. Revenue from subscribers (bills)
            String revenueSql = "SELECT COUNT(*) AS bill_count, " +
                               "SUM(total_price) AS total_revenue, " +
                               "SUM(discount_value) AS total_discount " +
                               "FROM bills " +
                               "WHERE subscriber_number IS NOT NULL " +
                               "AND YEAR(payment_date) = ? AND MONTH(payment_date) = ?";
            try (PreparedStatement ps = conn.prepareStatement(revenueSql)) {
                ps.setInt(1, year);
                ps.setInt(2, month);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    Map<String, Object> headerRow = new HashMap<>();
                    headerRow.put("field", "--- Subscriber Revenue ---");
                    headerRow.put("value", "");
                    reportData.add(headerRow);

                    Map<String, Object> row1 = new HashMap<>();
                    row1.put("field", "Bills Paid by Subscribers");
                    row1.put("value", String.valueOf(rs.getInt("bill_count")));
                    reportData.add(row1);

                    Map<String, Object> row2 = new HashMap<>();
                    row2.put("field", "Total Revenue");
                    double revenue = rs.getDouble("total_revenue");
                    row2.put("value", String.format("%.2f NIS", revenue));
                    reportData.add(row2);

                    Map<String, Object> row3 = new HashMap<>();
                    row3.put("field", "Total Discounts Given");
                    double discount = rs.getDouble("total_discount");
                    row3.put("value", String.format("%.2f NIS", discount));
                    reportData.add(row3);
                }
            }

            // 6. Top subscribers
            String topSubSql = "SELECT r.subscriber_number, u.name, COUNT(*) AS res_count " +
                              "FROM reservations r " +
                              "JOIN subscribers s ON r.subscriber_number = s.subscriber_number " +
                              "JOIN users u ON s.user_id = u.user_id " +
                              "WHERE r.subscriber_number IS NOT NULL " +
                              "AND YEAR(r.booking_date) = ? AND MONTH(r.booking_date) = ? " +
                              "GROUP BY r.subscriber_number, u.name " +
                              "ORDER BY res_count DESC LIMIT 5";
            try (PreparedStatement ps = conn.prepareStatement(topSubSql)) {
                ps.setInt(1, year);
                ps.setInt(2, month);
                ResultSet rs = ps.executeQuery();

                Map<String, Object> headerRow = new HashMap<>();
                headerRow.put("field", "--- Top 5 Subscribers ---");
                headerRow.put("value", "");
                reportData.add(headerRow);

                int rank = 1;
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("field", rank + ". " + rs.getString("name"));
                    row.put("value", rs.getInt("res_count") + " reservations");
                    reportData.add(row);
                    rank++;
                }
            }

            return Message.ok("GET_SUBSCRIBERS_REPORT", reportData);

        } catch (SQLException e) {
            e.printStackTrace();
            return Message.fail("GET_SUBSCRIBERS_REPORT", "Database error: " + e.getMessage());
        } finally {
            pool.releaseConnection(pConn);
        }
    }
}