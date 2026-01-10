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
import java.util.Map;

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
     * Gets a bill by bill number.
     * 
     * @param request Message containing "billNumber"
     * @return Message with Bill object if found
     */
    public Message getBill(Message request) {
        MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
        PooledConnection pConn = null;

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) request.getData();
            int billNumber = (Integer) data.get("billNumber");

            pConn = pool.getConnection();
            if (pConn == null) {
                return Message.fail("GET_BILL", "Database connection failed");
            }

            Connection conn = pConn.getConnection();
            
            String sql = "SELECT * FROM bills WHERE bill_number = ?";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, billNumber);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Bill bill = extractBillFromResultSet(rs);
                rs.close();
                ps.close();
                return Message.ok("GET_BILL", bill);
            } else {
                rs.close();
                ps.close();
                return Message.fail("GET_BILL", "Bill not found");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return Message.fail("GET_BILL", "Database error: " + e.getMessage());
        } finally {
            pool.releaseConnection(pConn);
        }
    }

    /**
     * Processes a bill payment (marks bill as paid).
     * In this implementation, the bill is already created when paid,
     * but this method can be used to verify or update payment status.
     * 
     * @param request Message containing "billNumber"
     * @return Message with success or error
     */
    public Message payBill(Message request) {
        MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
        PooledConnection pConn = null;

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) request.getData();
            int billNumber = (Integer) data.get("billNumber");

            pConn = pool.getConnection();
            if (pConn == null) {
                return Message.fail("PAY_BILL", "Database connection failed");
            }

            Connection conn = pConn.getConnection();
            
            // Check if bill exists
            String checkSql = "SELECT * FROM bills WHERE bill_number = ?";
            PreparedStatement checkPs = conn.prepareStatement(checkSql);
            checkPs.setInt(1, billNumber);
            ResultSet rs = checkPs.executeQuery();

            if (rs.next()) {
                Bill bill = extractBillFromResultSet(rs);
                rs.close();
                checkPs.close();
                
                // Calculate final amount after discount
                BigDecimal finalAmount = bill.getTotalPrice().subtract(bill.getDiscountValue());
                
                Map<String, Object> response = Map.of(
                    "billNumber", bill.getBillNumber(),
                    "totalPrice", bill.getTotalPrice(),
                    "discountValue", bill.getDiscountValue(),
                    "finalAmount", finalAmount,
                    "message", "Payment processed successfully"
                );
                
                return Message.ok("PAY_BILL", response);
            } else {
                rs.close();
                checkPs.close();
                return Message.fail("PAY_BILL", "Bill not found");
            }

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