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

    /** Assigned table number after check-in. NULL until customer arrives. */
    private Integer assignedTableNumber;

    /** Walk-in customer phone number (used when subscriberNumber is null). */
    private String walkInPhone;

    /** Walk-in customer email address (used when subscriberNumber is null). */
    private String walkInEmail;

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
            int guestCount, String subscriberNumber, ReservationStatus status, Integer assignedTableNumber,
            String walkInPhone, String walkInEmail) {

        setReservationId(reservationId);
        setConfirmationCode(confirmationCode);
        setBookingDate(bookingDate);
        setBookingTime(bookingTime);
        setGuestCount(guestCount);
        setSubscriberNumber(subscriberNumber);
        setReservationStatus(status);
        setAssignedTableNumber(assignedTableNumber);
        setWalkInPhone(walkInPhone);
        setWalkInEmail(walkInEmail);

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
    public static Reservation createForSubscriber(LocalDate bookingDate, LocalTime bookingTime, 
            int guestCount, String subscriberNumber) {
        Reservation r = new Reservation();
        
        r.setBookingDate(bookingDate);
        r.setBookingTime(bookingTime);
        r.setGuestCount(guestCount);
        r.setSubscriberNumber(subscriberNumber);
        r.setReservationStatus(ReservationStatus.ACTIVE);

        r.confirmationCode = generateCode();
        r.assignedTableNumber = null;
        r.walkInPhone = null;
        r.walkInEmail = null;

        r.validate();
        return r;
    }


    /**
     * Creates a reservation for a walk-in guest.
     *
     * @param bookingDate reservation date
     * @param bookingTime reservation time
     * @param guestCount number of diners
     * @param walkInPhone guest phone number (at least one contact required)
     * @param walkInEmail guest email address (at least one contact required)
     * @return an active reservation for a guest
     * 
     * @throws IllegalArgumentException if input data is invalid
     */
    public static Reservation createForGuest(LocalDate bookingDate, LocalTime bookingTime, 
            int guestCount, String walkInPhone, String walkInEmail) {
        Reservation r = new Reservation();
        
        r.setBookingDate(bookingDate);
        r.setBookingTime(bookingTime);
        r.setGuestCount(guestCount);
        r.setSubscriberNumber(null);
        r.setWalkInPhone(walkInPhone);
        r.setWalkInEmail(walkInEmail);
        r.setReservationStatus(ReservationStatus.ACTIVE);

        r.confirmationCode = generateCode();
        r.assignedTableNumber = null;

        r.validate();
        return r;
    }
 

    /**
     * Returns the start date and time as LocalDateTime.
     * 
     * @return LocalDateTime combining bookingDate and bookingTime
     */
    public LocalDateTime getStartDateTime() {
        if (bookingDate == null || bookingTime == null) {
            return null;
        }
        return LocalDateTime.of(bookingDate, bookingTime);
    }

    /**
     * Calculates the reservation end time 
     * 
     * @return reservation end date/time = startDateTime + 2 hours
     */
    public LocalDateTime getEndDateTime() {
        LocalDateTime start = getStartDateTime();
        return start == null ? null : start.plus(DURATION);
    }

    /**
     * Checks if this reservation is for a subscriber.
     * 
     * @return true if subscriberNumber is not null
     */
    public boolean isSubscriber() {
        return subscriberNumber != null && !subscriberNumber.trim().isEmpty();
    }

    /**
     * Checks if this reservation is for a guest (non-subscriber).
     * 
     * @return true if subscriberNumber is null
     */
    public boolean isGuest() {
        return !isSubscriber();
    }

    /**
     * Checks if the reservation has a table assigned.
     * 
     * @return true if a table has been assigned (after check-in)
     */
    public boolean hasTableAssigned() {
        return assignedTableNumber != null && assignedTableNumber > 0;
    }

    /** Cancels the reservation.
     *  The reservation status is updated to CANCELLED 
     */
    public void cancel() {
        setReservationStatus(ReservationStatus.CANCELLED);
    }

    /** Marks the reservation as a no-show. */
    public void markNoShow() {
        setReservationStatus(ReservationStatus.NO_SHOW);
    }

    /** Marks the reservation as completed. */
    public void complete() {
        setReservationStatus(ReservationStatus.COMPLETED);
    }

    /**
     * Returns the contact phone for this reservation.
     * For subscribers, returns null (phone is in User entity).
     * For walk-in guests, returns the walkInPhone.
     * 
     * @return contact phone or null
     */
    public String getContactPhone() {
        return walkInPhone;
    }

    /**
     * Returns the contact email for this reservation.
     * For subscribers, returns null (email is in User entity).
     * For walk-in guests, returns the walkInEmail.
     * 
     * @return contact email or null
     */
    public String getContactEmail() {
        return walkInEmail;
    }

    /**
     * Validates the reservation state.
     * 
     * @throws IllegalArgumentException if the reservation isn't valid
     */
    public void validate() {
        if (bookingDate == null) {
            throw new IllegalArgumentException("Booking date must not be null");
        }
        if (bookingTime == null) {
            throw new IllegalArgumentException("Booking time must not be null");
        }
        if (guestCount <= 0) {
            throw new IllegalArgumentException("Guest count must be greater than zero");
        }
        if (status == null) {
            throw new IllegalArgumentException("Reservation status must not be null");
        }
        if (confirmationCode == null || confirmationCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Confirmation code must not be empty");
        }
        if (assignedTableNumber != null && assignedTableNumber < 0) {
            throw new IllegalArgumentException("Assigned table number must not be negative");
        }
        if (reservationId < 0) {
            throw new IllegalArgumentException("Reservation ID must not be negative");
        }
        // Validate walk-in guest has at least one contact method
        if (!isSubscriber() && 
            (walkInPhone == null || walkInPhone.trim().isEmpty()) && 
            (walkInEmail == null || walkInEmail.trim().isEmpty())) {
            throw new IllegalArgumentException("Walk-in guest must provide phone or email");
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
     * @return reservation identifier 
     */
    public int getReservationId() {
        return reservationId;
    }

    /**
     * Sets the reservation identifier.
     * 
     * @param reservationId reservation identifier
     * @throws IllegalArgumentException if the reservation ID is negative
     */
    public void setReservationId(int reservationId) {
        if (reservationId < 0) {
            throw new IllegalArgumentException("Reservation ID must not be negative");
        }
        this.reservationId = reservationId;
    }

    /** Returns the confirmation code.
     * 
     * @return confirmation code 
     */
    public String getConfirmationCode() {
        return confirmationCode;
    }

    /**
     * Sets the confirmation code.
     * 
     * @param confirmationCode confirmation code
     * @throws IllegalArgumentException if the confirmation code is empty
     */
    public void setConfirmationCode(String confirmationCode) {
        if (confirmationCode == null || confirmationCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Confirmation code must not be empty");
        }
        this.confirmationCode = confirmationCode;
    }
    
    /** Returns the booking date.
     * 
     * @return booking date 
     */
    public LocalDate getBookingDate() {
        return bookingDate;
    }

    /**
     * Sets the booking date.
     * 
     * @param bookingDate booking date
     * @throws IllegalArgumentException if the booking date is null
     */
    public void setBookingDate(LocalDate bookingDate) {
        if (bookingDate == null) {
            throw new IllegalArgumentException("Booking date must not be null");
        }
        this.bookingDate = bookingDate;
    }

    /** Returns the booking time.
     * 
     * @return booking time 
     */
    public LocalTime getBookingTime() {
        return bookingTime;
    }

    /**
     * Sets the booking time.
     * 
     * @param bookingTime booking time
     * @throws IllegalArgumentException if the booking time is null
     */
    public void setBookingTime(LocalTime bookingTime) {
        if (bookingTime == null) {
            throw new IllegalArgumentException("Booking time must not be null");
        }
        this.bookingTime = bookingTime;
    }

    /** Returns the number of guests.
     * 
     * @return number of guests 
     */
    public int getGuestCount() {
        return guestCount;
    }

    /**
     * Sets the number of guests.
     * 
     * @param guestCount number of guests
     * @throws IllegalArgumentException if the number of guests isn't greater than zero
     */
    public void setGuestCount(int guestCount) {
        if (guestCount <= 0) {
            throw new IllegalArgumentException("Guest count must be greater than zero");
        }
        this.guestCount = guestCount;
    }

    /** Returns the subscriber number (null for guest reservations).
     * 
     * @return subscriber number or null
     */
    public String getSubscriberNumber() {
        return subscriberNumber;
    }

    /**
     * Sets the subscriber number.
     * 
     * @param subscriberNumber the subscriber number (can be null for guests)
     */
    public void setSubscriberNumber(String subscriberNumber) {
        this.subscriberNumber = subscriberNumber;
    }

    /** 
     * Returns the status of the reservation.
     * 
     * @return the reservation status
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
     * Returns the assigned table number (null until check-in).
     * 
     * @return the assigned table number or null
     */
    public Integer getAssignedTableNumber() {
        return assignedTableNumber;
    }

    /**
     * Sets the assigned table number.
     * 
     * @param assignedTableNumber the table number (can be null before check-in)
     * @throws IllegalArgumentException if the table number is negative
     */
    public void setAssignedTableNumber(Integer assignedTableNumber) {
        if (assignedTableNumber != null && assignedTableNumber < 0) {
            throw new IllegalArgumentException("Assigned table number must not be negative");
        }
        this.assignedTableNumber = assignedTableNumber;
    }

    /** 
     * Returns the walk-in customer phone number.
     * 
     * @return walk-in phone or null
     */
    public String getWalkInPhone() {
        return walkInPhone;
    }

    /**
     * Sets the walk-in customer phone number.
     * 
     * @param walkInPhone the phone number (can be null for subscribers)
     */
    public void setWalkInPhone(String walkInPhone) {
        this.walkInPhone = walkInPhone;
    }

    /** 
     * Returns the walk-in customer email address.
     * 
     * @return walk-in email or null
     */
    public String getWalkInEmail() {
        return walkInEmail;
    }

    /**
     * Sets the walk-in customer email address.
     * 
     * @param walkInEmail the email address (can be null for subscribers)
     */
    public void setWalkInEmail(String walkInEmail) {
        this.walkInEmail = walkInEmail;
    }

    /**
     * Compares two reservations.
     * 
     * If both reservations have database identifiers, comparison is based
     * on the ID.
     * Otherwise, comparison is based on the confirmation code.
     * 
     * @param o object to compare
     * @return true if reservations are considered equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reservation)) return false;
        Reservation that = (Reservation) o;

        if (reservationId > 0 && that.reservationId > 0) {
            return reservationId == that.reservationId;
        }
        return Objects.equals(confirmationCode, that.confirmationCode);
    }

    /**
     * Returns a hash code
     * 
     * @return hash code for this reservation
     */
    @Override
    public int hashCode() {
        return reservationId > 0 ? Objects.hash(reservationId) : Objects.hash(confirmationCode);
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "reservationId=" + reservationId +
                ", confirmationCode='" + confirmationCode + '\'' +
                ", bookingDate=" + bookingDate +
                ", bookingTime=" + bookingTime +
                ", guestCount=" + guestCount +
                ", subscriberNumber='" + subscriberNumber + '\'' +
                ", status=" + status +
                ", assignedTableNumber=" + assignedTableNumber +
                ", walkInPhone='" + walkInPhone + '\'' +
                ", walkInEmail='" + walkInEmail + '\'' +
                '}';
    }
    
    /**
     * Validates that booking date/time is at least 1 hour and at most 1 month from now.
     * 
     * @return true if booking time is within valid range
     */
    public boolean isBookingTimeValid() {
        if (bookingDate == null || bookingTime == null) {
            return false;
        }
        
        LocalDateTime bookingDateTime = LocalDateTime.of(bookingDate, bookingTime);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime minTime = now.plusHours(1);
        LocalDateTime maxTime = now.plusMonths(1);
        
        return !bookingDateTime.isBefore(minTime) && !bookingDateTime.isAfter(maxTime);
    }
}