package entities;

import java.io.Serial;
import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Reservation entity class represents a table reservation in the Bistro system.
 * It includes date and time, number of diners, and a confirmation code.
 */
public class Reservation implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final Duration DURATION = Duration.ofHours(2);
    
    private int reservationId;             
    private String confirmationCode;
    private LocalDate bookingDate;
    private LocalTime bookingTime;
    private int guestCount;
    private String subscriberNumber;         
    private ReservationStatus status;

    /** Assigned table number after seating. */
    private int tableNumber;

    /**
     * Enum represents the status of a reservation.
     */
    public enum ReservationStatus {
        ACTIVE,
        CANCELLED,
        COMPLETED,
        
        /** Customer didn't arrive within the allowed time. */
        NO_SHOW
    }

    /** Empty constructor */
    public Reservation() {
    }

    /** Full constructor used for loading reservations from database. */
    public Reservation(int reservationId, String confirmationCode, LocalDate bookingDate, LocalTime bookingTime,
            int guestCount, String subscriberNumber, ReservationStatus status, int tableNumber) {

    	setReservationId(reservationId);
    	setConfirmationCode(confirmationCode);
    	setBookingDate(bookingDate);
    	setBookingTime(bookingTime);
    	setGuestCount(guestCount);
    	setSubscriberNumber(subscriberNumber);
    	setReservationStatus(status);
    	setTableNumber(tableNumber);

    	validate();
    }


    /**
     * Creates a reservation for a subscriber.
     *
     * @param bookingDate reservation date
     * @param bookingTime reservation time
     * @param guestCount number of diners
     * @param subscriberNumber subscriber number
     * @return an active reservation for a subscriber
     * 
     * @throws IllegalArgumentException if input data is invalid
     */
    public static Reservation createForSubscriber(LocalDate bookingDate, LocalTime bookingTime, int guestCount, String subscriberNumber) {
        Reservation r = new Reservation();
        
        r.setBookingDate(bookingDate);
        r.setBookingTime(bookingTime);
        r.setGuestCount(guestCount);
        r.setSubscriberNumber(subscriberNumber);
        r.setReservationStatus(ReservationStatus.ACTIVE);

        r.confirmationCode = generateCode();
        r.tableNumber = 0;

        r.validate();
        return r;
    }


    /**
     * Creates a reservation for a guest.
     *
     * @param bookingDate reservation date
     * @param bookingTime reservation time
     * @param guestCount number of diners
     * @return an active reservation for a guest
     * 
     * @throws IllegalArgumentException if input data is invalid
     */
    public static Reservation createForGuest(LocalDate bookingDate, LocalTime bookingTime, int guestCount) {
        Reservation r = new Reservation();
        
        r.setBookingDate(bookingDate);
        r.setBookingTime(bookingTime);
        r.setGuestCount(guestCount);
        r.setSubscriberNumber(null);
        r.setReservationStatus(ReservationStatus.ACTIVE);

        r.confirmationCode = generateCode();
        r.tableNumber = 0;

        r.validate();
        return r;
    }
 

    /**
     * Calculates the reservation end time 
     * 
     * @return reservation end date/time = startDateTime + 2 hours
     */
    public LocalDateTime getEndDateTime() {
        return startDateTime == null ? null : startDateTime.plus(DURATION);
    }

    /** Cancels the reservation.
     *  The reservation status is updates to CANCELLED 
     */
    public void cancel() {
        setReservationStatus(ReservationStatus.CANCELLED);
    }

    /** Marks the reservation as a no-show. */
    public void markNoShow() {
        setReservationStatus(ReservationStatus.NO_SHOW);
    }

    /**
     * Validates the reservation state.
     * 
     * @throws IllegalArgumentException if the reservation isn't valid
     */
    public void validate() {
        if (startDateTime == null) {
            throw new IllegalArgumentException("Start date/time must not be null");
        }
        if (dinersCount <= 0) {
            throw new IllegalArgumentException("Diners count must be greater than zero");
        }
        if (customerType == null) {
            throw new IllegalArgumentException("Customer type must not be null");
        }
        if (status == null) {
            throw new IllegalArgumentException("Reservation status must not be null");
        }
        if (confCode == null || confCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Confirmation code must not be empty");
        }

        if (assignedTableNumber < 0) {
            throw new IllegalArgumentException("Assigned table number must not be negative");
        }
        if (reservationId < 0) {
            throw new IllegalArgumentException("Reservation ID must not be negative");
        }
        
        validateBookingWindow();
        
        if (customerType == CustomerType.SUBSCRIBER) {
            if (subscriberId <= 0) {
                throw new IllegalArgumentException("Subscriber ID must be greater than zero");
            }
            if (guestPhone != null || guestEmail != null) {
                throw new IllegalArgumentException("Guest details must be null for subscriber reservation");
            }
        } else { /** Validate for guest*/ 
            if (guestPhone == null || guestPhone.trim().isEmpty()) {
                throw new IllegalArgumentException("Guest phone must not be empty");
            }
         
            if (guestEmail != null && !guestEmail.trim().isEmpty() && !guestEmail.contains("@")) {
                throw new IllegalArgumentException("Invalid email address");
            }
            if (subscriberId != 0) {
                throw new IllegalArgumentException("Subscriber ID must be 0 for guest reservation");
            }
        }
    }

    /**
     * Validates the time window allowed for bookin,
     * at least 1 hour and not more than 30 days from creation time (createdAt).
     *
     * @throws IllegalArgumentException if booking time window is smash
     */
    private void validateBookingWindow() {
        if (createdAt == null) {
            return;
        }

        Duration diff = Duration.between(createdAt, startDateTime);

        if (diff.compareTo(MIN_TIME_BEFORE) < 0) {
            throw new IllegalArgumentException("Reservation must be at least 1 hour from booking time");
        }
        
        if (diff.compareTo(MAX_TIME_BEFORE) > 0) {
            throw new IllegalArgumentException("Reservation must be no more than 1 month from booking time");
        }
    }
    
    /**
     * Generates a confirmation code for the reservation.
     * 
     * @return confirmation code string
     */
    private static String generateCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

   
    /** Returns the reservation identifier.
     * 
     * @return reservation id 
     */
    public int getReservationId() {
        return reservationId;
    }

    /**
     * Sets the reservation identifier.
     * 
     * @param reservationId reservation identifier
     * @throws IllegalArgumentException if the table number is negative
     */
    public void setReservationId(int reservationId) {
        if (reservationId < 0) {
            throw new IllegalArgumentException("Reservation ID must not be negative");
        }
        this.reservationId = reservationId;
    }

    /** Returns the confirmation code.
     * 
     * @return cofirmation code 
     */
    public String getConfirmationCode() {
        return confCode;
    }

    /**
     * Sets the confirmation code.
     * 
     * @param confirmationCode cofirmation code
     * @throws IllegalArgumentException if the confirmation code is negative
     */
    public void setConfirmationCode(String confirmationCode) {
        if (confirmationCode == null || confirmationCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Confirmation code must not be empty");
        }
        this.confCode = confirmationCode;
    }
    
    /** Returns the start date and time of the reservation.
     * 
     * @return stat date and time 
     */
    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    /**
     * Sets the start date and time of the reservation.
     * 
     * @param startDateTime start date and time of the reservation
     * @throws IllegalArgumentException if the start date and time is null
     */
    public void setStartDateTime(LocalDateTime startDateTime) {
        if (startDateTime == null) {
            throw new IllegalArgumentException("Start date/time must not be null");
        }
        this.startDateTime = startDateTime;
    }

    /** Returns the number of the diners.
     * 
     * @return number of diners 
     */
    public int getDinersCount() {
        return dinersCount;
    }

    /**
     * Sets the number of diners.
     * 
     * @param dinersCount number of the diners
     * @throws IllegalArgumentException if the number of diners isn't greater than zero
     */
    public void setDinersCount(int dinersCount) {
        if (dinersCount <= 0) {
            throw new IllegalArgumentException("Diners count must be greater than zero");
        }
        this.dinersCount = dinersCount;
    }

    /** Returns the type of the customer (subscriber/guest).
     * 
     * @return type of customer
     */
    public CustomerType getCustomerType() {
        return customerType;
    }

    /**
     * Sets the type of the customer.
     * 
     * @param customerType the type of the customer
     * @throws IllegalArgumentException if the customer type is null
     */
    public void setCustomerType(CustomerType customerType) {
        if (customerType == null) {
            throw new IllegalArgumentException("Customer type must not be null");
        }
        this.customerType = customerType;
    }

    /** Returns the subscriber id.
     * 
     * @return suscriber id
     */
    public int getSubscriberId() {
        return subscriberId;
    }

    /**
     * Sets the subscriber id.
     * 
     * @param subscriberId the ID of the subscriber
     */
    public void setSubscriberId(int subscriberId) {
        this.subscriberId = subscriberId;
    }

    /** Returns the phone number of the guest.
     * 
     * @return the guest phone
     */
    public String getGuestPhone() {
        return guestPhone;
    }

    /**
     * Sets guest phone.
     * 
     * @param guestPhone the phone number of the guest
     */
    public void setGuestPhone(String guestPhone) {
        this.guestPhone = guestPhone;
    }

    /** 
     * Returns the email of the guest.
     * 
     * @return the guest email
     */
    public String getGuestEmail() {
        return guestEmail;
    }

    /**
     * Sets the guest email.
     * 
     * @param guestEmail the email of the guest
     */
    public void setGuestEmail(String guestEmail) {
        this.guestEmail = guestEmail;
    }

    /** 
     * Returns the status of the reservation.
     * 
     * @return the status reservation
     */
    public ReservationStatus getStatus() {
        return status;
    }

    /**
     * Sets the status of the reservation.
     * 
     * @param status the status of the reservation
     * @throws IllegalArgumentException if the reservation status is null
     */
    public void setReservationStatus(ReservationStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Reservation status must not be null");
        }
        this.status = status;
    }

    /** 
     * Returns the assigned table number.
     * 
     * @return the number of the table
     */
    public int getAssignedTableNumber() {
        return assignedTableNumber;
    }

    /**
     * Sets the assigned table number.
     * 
     * @param AssignedTableNumber the number of the assigned table
     * @throws IllegalArgumentException if the number of the table isn't positive
     */
    public void setAssignedTableNumber(int assignedTableNumber) {
        if (assignedTableNumber < 0) {
            throw new IllegalArgumentException("Assigned table number must not be negative");
        }
        this.assignedTableNumber = assignedTableNumber;
    }

    /** 
     * Returns the date and time of the reservation creation.
     * 
     * @return timestamp of reservation creation
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the timestamp of a reservation.
     * 
     * @param createdAt the timestamp of a reservation creation
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Compares two reservations.
     * 
     * If both reservations have database identifiers, comparison is based
     * on the ID.
     * Otherwise, comparison is based on the confirmation code.
     * 
     * @param o object to compare
     * @return true if reservations are considired equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reservation)) return false;
        Reservation that = (Reservation) o;

        if (reservationId > 0 && that.reservationId > 0) {
            return reservationId == that.reservationId;
        }
        return Objects.equals(confCode, that.confCode);
    }

    /**
     * Returns a hash code
     * 
     * @return hash code for this reservation
     */
    @Override
    public int hashCode() {
        return reservationId > 0 ? Objects.hash(reservationId) : Objects.hash(confCode);
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "reservationId=" + reservationId +
                ", confirmationCode='" + confCode + '\'' +
                ", startDateTime=" + startDateTime +
                ", dinersCount=" + dinersCount +
                ", customerType=" + customerType +
                ", status=" + status +
                '}';
    }
}