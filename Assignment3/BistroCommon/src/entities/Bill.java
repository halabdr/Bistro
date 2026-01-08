package entities;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Bill entity class represents a payment bill in the Bistro system.
 * Each bill is associated with a table and optionally a subscriber (for discounts).
 */
public class Bill implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private int billNumber;
    private BigDecimal totalPrice;
    private BigDecimal discountValue;
    private LocalDate paymentDate;
    private int tableNumber;
    private String subscriberNumber;

    /**
     * Default constructor.
     */
    public Bill() {
        this.discountValue = BigDecimal.ZERO;
    }

    /**
     * Full constructor.
     * 
     * @param billNumber       Unique bill number
     * @param totalPrice       Total price before discount
     * @param discountValue    Discount amount
     * @param paymentDate      Date of payment
     * @param tableNumber      Table number
     * @param subscriberNumber Subscriber number (null for non-subscribers)
     */
    public Bill(int billNumber, BigDecimal totalPrice, BigDecimal discountValue,
                LocalDate paymentDate, int tableNumber, String subscriberNumber) {
        setBillNumber(billNumber);
        setTotalPrice(totalPrice);
        setDiscountValue(discountValue);
        setPaymentDate(paymentDate);
        setTableNumber(tableNumber);
        setSubscriberNumber(subscriberNumber);
        validate();
    }

    /**
     * Constructor without bill number (for creating new bills).
     * 
     * @param totalPrice       Total price before discount
     * @param discountValue    Discount amount
     * @param paymentDate      Date of payment
     * @param tableNumber      Table number
     * @param subscriberNumber Subscriber number (null for non-subscribers)
     */
    public Bill(BigDecimal totalPrice, BigDecimal discountValue, LocalDate paymentDate,
                int tableNumber, String subscriberNumber) {
        setTotalPrice(totalPrice);
        setDiscountValue(discountValue);
        setPaymentDate(paymentDate);
        setTableNumber(tableNumber);
        setSubscriberNumber(subscriberNumber);
        validate();
    }

    /**
     * Creates a bill for a subscriber (with discount).
     * 
     * @param totalPrice       Total price before discount
     * @param discountValue    Discount amount
     * @param tableNumber      Table number
     * @param subscriberNumber Subscriber number
     * @return new Bill instance
     */
    public static Bill createForSubscriber(BigDecimal totalPrice, BigDecimal discountValue,
                                           int tableNumber, String subscriberNumber) {
        return new Bill(totalPrice, discountValue, LocalDate.now(), tableNumber, subscriberNumber);
    }

    /**
     * Creates a bill for a guest (no discount).
     * 
     * @param totalPrice  Total price
     * @param tableNumber Table number
     * @return new Bill instance
     */
    public static Bill createForGuest(BigDecimal totalPrice, int tableNumber) {
        return new Bill(totalPrice, BigDecimal.ZERO, LocalDate.now(), tableNumber, null);
    }

    /**
     * Calculates the final amount after applying discount.
     * 
     * @return final amount to be paid
     */
    public BigDecimal getFinalAmount() {
        if (totalPrice == null || discountValue == null) {
            return BigDecimal.ZERO;
        }
        return totalPrice.subtract(discountValue).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculates the discount percentage.
     * 
     * @return discount percentage (0-100)
     */
    public BigDecimal getDiscountPercentage() {
        if (totalPrice == null || discountValue == null || 
            totalPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return discountValue.divide(totalPrice, 4, RoundingMode.HALF_UP)
                           .multiply(new BigDecimal("100"))
                           .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Checks if this bill has a discount.
     * 
     * @return true if discount value is greater than zero
     */
    public boolean hasDiscount() {
        return discountValue != null && discountValue.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Checks if this bill is for a subscriber.
     * 
     * @return true if subscriber number is not null
     */
    public boolean isSubscriber() {
        return subscriberNumber != null && !subscriberNumber.trim().isEmpty();
    }

    /**
     * Validates the bill state.
     * 
     * @throws IllegalArgumentException if the data is invalid
     */
    public void validate() {
        if (totalPrice == null || totalPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Total price must not be negative");
        }
        if (discountValue == null || discountValue.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Discount value must not be negative");
        }
        if (discountValue.compareTo(totalPrice) > 0) {
            throw new IllegalArgumentException("Discount cannot exceed total price");
        }
        if (paymentDate == null) {
            throw new IllegalArgumentException("Payment date must not be null");
        }
        if (tableNumber <= 0) {
            throw new IllegalArgumentException("Table number must be greater than zero");
        }
        if (billNumber < 0) {
            throw new IllegalArgumentException("Bill number must not be negative");
        }
    }

    /**
     * Gets the bill number.
     * 
     * @return bill number
     */
    public int getBillNumber() {
        return billNumber;
    }

    /**
     * Sets the bill number.
     * 
     * @param billNumber bill number
     * @throws IllegalArgumentException if bill number is negative
     */
    public void setBillNumber(int billNumber) {
        if (billNumber < 0) {
            throw new IllegalArgumentException("Bill number must not be negative");
        }
        this.billNumber = billNumber;
    }

    /**
     * Gets the total price.
     * 
     * @return total price
     */
    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    /**
     * Sets the total price.
     * 
     * @param totalPrice total price
     * @throws IllegalArgumentException if total price is negative
     */
    public void setTotalPrice(BigDecimal totalPrice) {
        if (totalPrice == null || totalPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Total price must not be negative");
        }
        this.totalPrice = totalPrice.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Gets the discount value.
     * 
     * @return discount value
     */
    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    /**
     * Sets the discount value.
     * 
     * @param discountValue discount value
     * @throws IllegalArgumentException if discount is negative or exceeds total price
     */
    public void setDiscountValue(BigDecimal discountValue) {
        if (discountValue == null || discountValue.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Discount value must not be negative");
        }
        if (totalPrice != null && discountValue.compareTo(totalPrice) > 0) {
            throw new IllegalArgumentException("Discount cannot exceed total price");
        }
        this.discountValue = discountValue.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Gets the payment date.
     * 
     * @return payment date
     */
    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    /**
     * Sets the payment date.
     * 
     * @param paymentDate payment date
     * @throws IllegalArgumentException if payment date is null
     */
    public void setPaymentDate(LocalDate paymentDate) {
        if (paymentDate == null) {
            throw new IllegalArgumentException("Payment date must not be null");
        }
        this.paymentDate = paymentDate;
    }

    /**
     * Gets the table number.
     * 
     * @return table number
     */
    public int getTableNumber() {
        return tableNumber;
    }

    /**
     * Sets the table number.
     * 
     * @param tableNumber table number
     * @throws IllegalArgumentException if table number is not greater than zero
     */
    public void setTableNumber(int tableNumber) {
        if (tableNumber <= 0) {
            throw new IllegalArgumentException("Table number must be greater than zero");
        }
        this.tableNumber = tableNumber;
    }

    /**
     * Gets the subscriber number.
     * 
     * @return subscriber number (null for non-subscribers)
     */
    public String getSubscriberNumber() {
        return subscriberNumber;
    }

    /**
     * Sets the subscriber number.
     * 
     * @param subscriberNumber subscriber number (can be null for guests)
     */
    public void setSubscriberNumber(String subscriberNumber) {
        this.subscriberNumber = subscriberNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Bill)) return false;
        Bill bill = (Bill) o;
        return billNumber > 0 && billNumber == bill.billNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(billNumber);
    }

    @Override
    public String toString() {
        return "Bill{" +
                "billNumber=" + billNumber +
                ", totalPrice=" + totalPrice +
                ", discountValue=" + discountValue +
                ", finalAmount=" + getFinalAmount() +
                ", paymentDate=" + paymentDate +
                ", tableNumber=" + tableNumber +
                ", subscriberNumber='" + subscriberNumber + '\'' +
                '}';
    }
}