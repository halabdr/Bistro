package data_access;
import connection.MySQLConnectionPool;
import connection.PooledConnection;
import common.Message;
import entities.Bill;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

/**
 * Repository for managing bills.
 * Handles bill creation, retrieval, and payment processing.
 */
public class BillRepository {

    private static final BigDecimal SUBSCRIBER_DISCOUNT = new BigDecimal("0.10"); // 10% discount

    /**
     * Creates a new bill for a table.
     * 
     * @param request Message containing "billNumber", "totalPrice", "tableNumber", and optional "subscriberNumber"
     * @return Message with created Bill object if successful
     */
    public Message createBill(Message request) {
        MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
        PooledConnection pConn = null;

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) request.getData();
            
            int billNumber = (Integer) data.get("billNumber");
            BigDecimal totalPrice = new BigDecimal(data.get("totalPrice").toString());
            int tableNumber = (Integer) data.get("tableNumber");
            String subscriberNumber = (String) data.get("subscriberNumber");

            pConn = pool.getConnection();
            if (pConn == null) {
                return Message.fail("CREATE_BILL", "Database connection failed");
            }

            Connection conn = pConn.getConnection();

            // Check if bill number already exists
            if (billExists(conn, billNumber)) {
                return Message.fail("CREATE_BILL", "Bill number already exists");
            }

            // Calculate discount for subscribers (10%)
            BigDecimal discountValue = BigDecimal.ZERO;
            if (subscriberNumber != null && !subscriberNumber.trim().isEmpty()) {
                discountValue = totalPrice.multiply(SUBSCRIBER_DISCOUNT);
            }

            LocalDate paymentDate = LocalDate.now();

            String sql = "INSERT INTO bills (bill_number, total_price, discount_value, payment_date, " +
                        "table_number, subscriber_number) VALUES (?, ?, ?, ?, ?, ?)";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, billNumber);
            ps.setBigDecimal(2, totalPrice);
            ps.setBigDecimal(3, discountValue);
            ps.setDate(4, Date.valueOf(paymentDate));
            ps.setInt(5, tableNumber);
            ps.setString(6, subscriberNumber);
            ps.executeUpdate();
            ps.close();

            // Create and return Bill object
            Bill bill = new Bill();
            bill.setBillNumber(billNumber);
            bill.setTotalPrice(totalPrice);
            bill.setDiscountValue(discountValue);
            bill.setPaymentDate(paymentDate);
            bill.setTableNumber(tableNumber);
            bill.setSubscriberNumber(subscriberNumber);

            return Message.ok("CREATE_BILL", bill);

        } catch (SQLException e) {
            e.printStackTrace();
            return Message.fail("CREATE_BILL", "Database error: " + e.getMessage());
        } finally {
            pool.releaseConnection(pConn);
        }
    }

    /**
     * Gets bill details by confirmation code.
     * Validates that the customer is currently seated before allowing bill access.
     * 
     * @param request Message containing "confirmationCode"
     * @return Message with bill data if found and customer is seated
     */
    public Message getBill(Message request) {
        MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
        PooledConnection pConn = null;

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) request.getData();
            String confirmationCode = (String) data.get("confirmationCode");

            if (confirmationCode == null || confirmationCode.trim().isEmpty()) {
                return Message.fail("GET_BILL", "Please enter a confirmation code.");
            }

            pConn = pool.getConnection();
            if (pConn == null) {
                return Message.fail("GET_BILL", "Database connection failed");
            }

            Connection conn = pConn.getConnection();

            // First, check reservations table
            String resSql = "SELECT reservation_id, booking_date, booking_time, reservation_status, table_number, subscriber_number " +
                           "FROM reservations WHERE confirmation_code = ?";
            PreparedStatement resPs = conn.prepareStatement(resSql);
            resPs.setString(1, confirmationCode);
            ResultSet resRs = resPs.executeQuery();

            if (resRs.next()) {
                String status = resRs.getString("reservation_status");
                LocalDate bookingDate = resRs.getDate("booking_date").toLocalDate();
                String bookingTime = resRs.getTime("booking_time").toString().substring(0, 5);
                Integer tableNumber = resRs.getObject("table_number") != null ? resRs.getInt("table_number") : null;
                String subscriberNumber = resRs.getString("subscriber_number");
                resRs.close();
                resPs.close();

                // Check reservation status
                if ("ACTIVE".equals(status)) {
                    // Future reservation - not seated yet
                    return Message.fail("GET_BILL", 
                        "Payment is only available after you've been seated.\n" +
                        "Your reservation is scheduled for " + bookingDate + " at " + bookingTime + ".");
                } else if ("CANCELLED".equals(status)) {
                    return Message.fail("GET_BILL", "This reservation has been cancelled.");
                } else if ("NO_SHOW".equals(status)) {
                    return Message.fail("GET_BILL", "This reservation was marked as no-show.");
                } else if ("COMPLETED".equals(status)) {
                    // Customer was seated - find bill
                    return findBillForTable(conn, tableNumber, subscriberNumber);
                }
            }
            resRs.close();
            resPs.close();

            // Check waitlist table
            String waitSql = "SELECT entry_id, subscriber_number FROM waiting_list WHERE entry_code = ?";
            PreparedStatement waitPs = conn.prepareStatement(waitSql);
            waitPs.setString(1, confirmationCode);
            ResultSet waitRs = waitPs.executeQuery();

            if (waitRs.next()) {
                // Found in waitlist - customer is waiting, not seated
                waitRs.close();
                waitPs.close();
                return Message.fail("GET_BILL", 
                    "You are currently on the waitlist.\n" +
                    "Payment is only available after you've been seated at a table.");
            }
            waitRs.close();
            waitPs.close();

            // Code not found anywhere
            return Message.fail("GET_BILL", 
                "Confirmation code not found.\n" +
                "Please check your code and try again.");

        } catch (SQLException e) {
            e.printStackTrace();
            return Message.fail("GET_BILL", "Database error: " + e.getMessage());
        } finally {
            pool.releaseConnection(pConn);
        }
    }

    /**
     * Finds the bill for a specific table.
     */
    private Message findBillForTable(Connection conn, Integer tableNumber, String subscriberNumber) throws SQLException {
        if (tableNumber == null) {
            return Message.fail("GET_BILL", "No table assigned to this reservation.");
        }

        // Find the most recent bill for this table
        String billSql = "SELECT * FROM bills WHERE table_number = ? ORDER BY payment_date DESC LIMIT 1";
        PreparedStatement billPs = conn.prepareStatement(billSql);
        billPs.setInt(1, tableNumber);
        ResultSet billRs = billPs.executeQuery();

        if (billRs.next()) {
            Map<String, Object> billData = new HashMap<>();
            billData.put("billNumber", billRs.getInt("bill_number"));
            billData.put("tableNumber", tableNumber);
            billData.put("totalPrice", billRs.getBigDecimal("total_price"));
            billData.put("discountValue", billRs.getBigDecimal("discount_value"));
            
            BigDecimal total = billRs.getBigDecimal("total_price");
            BigDecimal discount = billRs.getBigDecimal("discount_value");
            BigDecimal finalAmount = total.subtract(discount);
            billData.put("finalAmount", finalAmount);

            billRs.close();
            billPs.close();
            return Message.ok("GET_BILL", billData);
        }

        billRs.close();
        billPs.close();
        return Message.fail("GET_BILL", 
            "No bill found for your table.\n" +
            "Please contact a staff member for assistance.");
    }

    /**
     * Processes bill payment and releases the table.
     * 
     * @param request Message containing confirmationCode
     * @return Message with payment details
     */
    public Message payBill(Message request) {
        MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
        PooledConnection pConn = null;

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) request.getData();
            String confirmationCode = (String) data.get("confirmationCode");

            if (confirmationCode == null || confirmationCode.trim().isEmpty()) {
                return Message.fail("PAY_BILL", "Please enter a confirmation code.");
            }

            pConn = pool.getConnection();
            if (pConn == null) {
                return Message.fail("PAY_BILL", "Database connection failed");
            }

            Connection conn = pConn.getConnection();
            
            // Find reservation by confirmation code
            String resSql = "SELECT table_number, reservation_status FROM reservations WHERE confirmation_code = ?";
            PreparedStatement resPs = conn.prepareStatement(resSql);
            resPs.setString(1, confirmationCode);
            ResultSet resRs = resPs.executeQuery();

            if (!resRs.next()) {
                resRs.close();
                resPs.close();
                return Message.fail("PAY_BILL", "Confirmation code not found.");
            }

            String status = resRs.getString("reservation_status");
            Integer tableNumber = resRs.getObject("table_number") != null ? resRs.getInt("table_number") : null;
            resRs.close();
            resPs.close();

            if (!"COMPLETED".equals(status)) {
                return Message.fail("PAY_BILL", "Payment is only available after you've been seated.");
            }

            if (tableNumber == null) {
                return Message.fail("PAY_BILL", "No table assigned. Please contact staff.");
            }

            // Find bill for this table
            String billSql = "SELECT * FROM bills WHERE table_number = ? ORDER BY payment_date DESC LIMIT 1";
            PreparedStatement billPs = conn.prepareStatement(billSql);
            billPs.setInt(1, tableNumber);
            ResultSet billRs = billPs.executeQuery();

            if (!billRs.next()) {
                billRs.close();
                billPs.close();
                return Message.fail("PAY_BILL", "No bill found. Please contact staff.");
            }

            Bill bill = extractBillFromResultSet(billRs);
            billRs.close();
            billPs.close();
            
            // Release the table (mark as AVAILABLE)
            String releaseSql = "UPDATE tables_info SET table_status = 'AVAILABLE', " +
                               "reservation_start = NULL, reservation_end = NULL " +
                               "WHERE table_number = ?";
            PreparedStatement releasePs = conn.prepareStatement(releaseSql);
            releasePs.setInt(1, tableNumber);
            releasePs.executeUpdate();
            releasePs.close();
            
            // Calculate final amount after discount
            BigDecimal finalAmount = bill.getTotalPrice().subtract(bill.getDiscountValue());
            
            Map<String, Object> response = new HashMap<>();
            response.put("billNumber", bill.getBillNumber());
            response.put("totalPrice", bill.getTotalPrice());
            response.put("discountValue", bill.getDiscountValue());
            response.put("finalAmount", finalAmount);
            response.put("tableNumber", tableNumber);
            response.put("message", "Payment processed. Table " + tableNumber + " is now available.");
            
            return Message.ok("PAY_BILL", response);

        } catch (SQLException e) {
            e.printStackTrace();
            return Message.fail("PAY_BILL", "Database error: " + e.getMessage());
        } finally {
            pool.releaseConnection(pConn);
        }
    }

    // Helper Method
    /**
     * Checks if a bill number already exists in the database.
     */
    private boolean billExists(Connection conn, int billNumber) throws SQLException {
        String sql = "SELECT COUNT(*) FROM bills WHERE bill_number = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, billNumber);
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
     * Extracts a Bill object from ResultSet.
     */
    private Bill extractBillFromResultSet(ResultSet rs) throws SQLException {
        Bill bill = new Bill();
        bill.setBillNumber(rs.getInt("bill_number"));
        bill.setTotalPrice(rs.getBigDecimal("total_price"));
        bill.setDiscountValue(rs.getBigDecimal("discount_value"));
        bill.setPaymentDate(rs.getDate("payment_date").toLocalDate());
        bill.setTableNumber(rs.getInt("table_number"));
        
        String subscriberNumber = rs.getString("subscriber_number");
        if (subscriberNumber != null) {
            bill.setSubscriberNumber(subscriberNumber);
        }
        
        return bill;
    }
}