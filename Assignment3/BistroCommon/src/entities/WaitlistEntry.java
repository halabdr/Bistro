package entities;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * WaitlistEntry represents an entry in the restaurant's waitlist.
 * When no tables are immediately available, customers join the waitlist.
 */
public class WaitlistEntry implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private int entryId;
    private LocalDateTime requestTime;
    private int numberOfDiners;
    private String entryCode;
    private String subscriberNumber;

    /** Walk-in customer phone number (used when subscriberNumber is null). */
    private String walkInPhone;

    /** Walk-in customer email address (used when subscriberNumber is null). */
    private String walkInEmail;

    /**
     * Default constructor.
     */
    public WaitlistEntry() {
    }

    /**
     * Full constructor used for loading entries from database.
     *
     * @param entryId          Unique entry identifier
     * @param requestTime      Time when entry was created
     * @param numberOfDiners   Number of people in the party
     * @param entryCode        Unique entry code
     * @param subscriberNumber Subscriber number (null for guests)
     * @param walkInPhone      Walk-in phone (null for subscribers)
     * @param walkInEmail      Walk-in email (null for subscribers)
     */
    public WaitlistEntry(int entryId, LocalDateTime requestTime, int numberOfDiners, 
                         String entryCode, String subscriberNumber,
                         String walkInPhone, String walkInEmail) {
        setEntryId(entryId);
        setRequestTime(requestTime);
        setNumberOfDiners(numberOfDiners);
        setEntryCode(entryCode);
        setSubscriberNumber(subscriberNumber);
        setWalkInPhone(walkInPhone);
        setWalkInEmail(walkInEmail);
        
        validate();
    }

    /**
     * Creates a waitlist entry for a subscriber.
     *
     * @param numberOfDiners   Number of people in the party
     * @param subscriberNumber Subscriber number
     * @return A new waitlist entry for a subscriber
     * @throws IllegalArgumentException if input data is invalid
     */
    public static WaitlistEntry createForSubscriber(int numberOfDiners, String subscriberNumber) {
        WaitlistEntry entry = new WaitlistEntry();
        
        entry.setNumberOfDiners(numberOfDiners);
        entry.setSubscriberNumber(subscriberNumber);
        entry.walkInPhone = null;
        entry.walkInEmail = null;
        entry.requestTime = LocalDateTime.now();
        entry.entryCode = generateCode();
        
        entry.validate();
        return entry;
    }

    /**
     * Creates a waitlist entry for a walk-in guest.
     *
     * @param numberOfDiners Number of people in the party
     * @param walkInPhone    Guest phone number (at least one contact required)
     * @param walkInEmail    Guest email address (at least one contact required)
     * @return A new waitlist entry for a guest
     * @throws IllegalArgumentException if input data is invalid
     */
    public static WaitlistEntry createForGuest(int numberOfDiners, String walkInPhone, String walkInEmail) {
        WaitlistEntry entry = new WaitlistEntry();
        
        entry.setNumberOfDiners(numberOfDiners);
        entry.subscriberNumber = null;
        entry.setWalkInPhone(walkInPhone);
        entry.setWalkInEmail(walkInEmail);
        entry.requestTime = LocalDateTime.now();
        entry.entryCode = generateCode();
        
        entry.validate();
        return entry;
    }

    /**
     * Checks if this entry is for a subscriber.
     * 
     * @return true if subscriberNumber is not null
     */
    public boolean isSubscriber() {
        return subscriberNumber != null && !subscriberNumber.trim().isEmpty();
    }

    /**
     * Checks if this entry is for a guest (non-subscriber).
     * 
     * @return true if subscriberNumber is null
     */
    public boolean isGuest() {
        return !isSubscriber();
    }

    /**
     * Returns the contact phone for this entry.
     * 
     * @return contact phone or null
     */
    public String getContactPhone() {
        return walkInPhone;
    }

    /**
     * Returns the contact email for this entry.
     * 
     * @return contact email or null
     */
    public String getContactEmail() {
        return walkInEmail;
    }

    /**
     * Validates the waitlist entry state.
     * 
     * @throws IllegalArgumentException if the entry isn't valid
     */
    public void validate() {
        if (requestTime == null) {
            throw new IllegalArgumentException("Request time must not be null");
        }
        if (numberOfDiners <= 0) {
            throw new IllegalArgumentException("Number of diners must be greater than zero");
        }
        if (entryCode == null || entryCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Entry code must not be empty");
        }
        if (entryId < 0) {
            throw new IllegalArgumentException("Entry ID must not be negative");
        }
        // Validate walk-in guest has at least one contact method
        if (!isSubscriber() && 
            (walkInPhone == null || walkInPhone.trim().isEmpty()) && 
            (walkInEmail == null || walkInEmail.trim().isEmpty())) {
            throw new IllegalArgumentException("Walk-in guest must provide phone or email");
        }
    }

    /**
     * Generates an entry code for the waitlist entry.
     * 
     * @return entry code string
     */
    private static String generateCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Gets the unique entry identifier.
     *
     * @return The entry ID
     */
    public int getEntryId() {
        return entryId;
    }

    /**
     * Sets the unique entry identifier.
     *
     * @param entryId The entry ID to set
     * @throws IllegalArgumentException if the entry ID is negative
     */
    public void setEntryId(int entryId) {
        if (entryId < 0) {
            throw new IllegalArgumentException("Entry ID must not be negative");
        }
        this.entryId = entryId;
    }

    /**
     * Gets the request time when the entry was created.
     *
     * @return The request time
     */
    public LocalDateTime getRequestTime() {
        return requestTime;
    }

    /**
     * Sets the request time when the entry was created.
     *
     * @param requestTime The request time to set
     * @throws IllegalArgumentException if the request time is null
     */
    public void setRequestTime(LocalDateTime requestTime) {
        if (requestTime == null) {
            throw new IllegalArgumentException("Request time must not be null");
        }
        this.requestTime = requestTime;
    }

    /**
     * Gets the number of diners in the waiting party.
     *
     * @return The number of diners
     */
    public int getNumberOfDiners() {
        return numberOfDiners;
    }

    /**
     * Sets the number of diners in the waiting party.
     *
     * @param numberOfDiners The number of diners to set
     * @throws IllegalArgumentException if the number of diners is not greater than zero
     */
    public void setNumberOfDiners(int numberOfDiners) {
        if (numberOfDiners <= 0) {
            throw new IllegalArgumentException("Number of diners must be greater than zero");
        }
        this.numberOfDiners = numberOfDiners;
    }

    /**
     * Gets the entry code for this waitlist entry.
     *
     * @return The entry code
     */
    public String getEntryCode() {
        return entryCode;
    }

    /**
     * Sets the entry code for this waitlist entry.
     *
     * @param entryCode The entry code to set
     * @throws IllegalArgumentException if the entry code is empty
     */
    public void setEntryCode(String entryCode) {
        if (entryCode == null || entryCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Entry code must not be empty");
        }
        this.entryCode = entryCode;
    }

    /**
     * Gets the subscriber number (null for guest entries).
     *
     * @return The subscriber number or null
     */
    public String getSubscriberNumber() {
        return subscriberNumber;
    }

    /**
     * Sets the subscriber number.
     *
     * @param subscriberNumber The subscriber number to set (can be null for guests)
     */
    public void setSubscriberNumber(String subscriberNumber) {
        this.subscriberNumber = subscriberNumber;
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
     * Compares two waitlist entries.
     * 
     * If both entries have database identifiers, comparison is based on the ID.
     * Otherwise, comparison is based on the entry code.
     * 
     * @param o object to compare
     * @return true if entries are considered equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WaitlistEntry)) return false;
        WaitlistEntry that = (WaitlistEntry) o;

        if (entryId > 0 && that.entryId > 0) {
            return entryId == that.entryId;
        }
        return Objects.equals(entryCode, that.entryCode);
    }

    /**
     * Returns a hash code for this waitlist entry.
     * 
     * @return hash code
     */
    @Override
    public int hashCode() {
        return entryId > 0 ? Objects.hash(entryId) : Objects.hash(entryCode);
    }

    /**
     * Returns a string representation of the waitlist entry.
     *
     * @return A string containing entry details
     */
    @Override
    public String toString() {
        return "WaitlistEntry{" +
                "entryId=" + entryId +
                ", requestTime=" + requestTime +
                ", numberOfDiners=" + numberOfDiners +
                ", entryCode='" + entryCode + '\'' +
                ", subscriberNumber='" + subscriberNumber + '\'' +
                ", walkInPhone='" + walkInPhone + '\'' +
                ", walkInEmail='" + walkInEmail + '\'' +
                '}';
    }
}