package entities;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Tag entity class represents an activity log entry in the Bistro system.
 * Each tag records a specific action or event performed by a subscriber.
 */
public class Tag implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private int logId;
    private String subscriberNumber;
    private String activityDetails;
    private LocalDateTime createdAt;

    /**
     * Default constructor.
     */
    public Tag() {
    }

    /**
     * Full constructor.
     * 
     * @param logId           Unique log identifier
     * @param subscriberNumber Subscriber who performed the activity
     * @param activityDetails Description of the activity
     * @param createdAt       Timestamp when activity was logged
     */
    public Tag(int logId, String subscriberNumber, String activityDetails, LocalDateTime createdAt) {
        setLogId(logId);
        setSubscriberNumber(subscriberNumber);
        setActivityDetails(activityDetails);
        setCreatedAt(createdAt);
        validate();
    }

    /**
     * Constructor without ID (for creating new tags).
     * 
     * @param subscriberNumber Subscriber who performed the activity
     * @param activityDetails Description of the activity
     * @param createdAt       Timestamp when activity was logged
     */
    public Tag(String subscriberNumber, String activityDetails, LocalDateTime createdAt) {
        setSubscriberNumber(subscriberNumber);
        setActivityDetails(activityDetails);
        setCreatedAt(createdAt);
        validate();
    }

    /**
     * Creates a new tag for a subscriber activity.
     * The createdAt timestamp is set to the current time.
     * 
     * @param subscriberNumber Subscriber who performed the activity
     * @param activityDetails Description of the activity
     * @return new Tag instance
     */
    public static Tag create(String subscriberNumber, String activityDetails) {
        return new Tag(subscriberNumber, activityDetails, LocalDateTime.now());
    }

    /**
     * Validates the tag state.
     * 
     * @throws IllegalArgumentException if the data is invalid
     */
    public void validate() {
        if (subscriberNumber == null || subscriberNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Subscriber number must not be empty");
        }
        if (activityDetails == null || activityDetails.trim().isEmpty()) {
            throw new IllegalArgumentException("Activity details must not be empty");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("Created at must not be null");
        }
        if (logId < 0) {
            throw new IllegalArgumentException("Log ID must not be negative");
        }
    }

    /**
     * Gets the log ID.
     * 
     * @return log ID
     */
    public int getLogId() {
        return logId;
    }

    /**
     * Sets the log ID.
     * 
     * @param logId log ID
     * @throws IllegalArgumentException if log ID is negative
     */
    public void setLogId(int logId) {
        if (logId < 0) {
            throw new IllegalArgumentException("Log ID must not be negative");
        }
        this.logId = logId;
    }

    /**
     * Gets the subscriber number.
     * 
     * @return subscriber number
     */
    public String getSubscriberNumber() {
        return subscriberNumber;
    }

    /**
     * Sets the subscriber number.
     * 
     * @param subscriberNumber subscriber number
     * @throws IllegalArgumentException if subscriber number is empty
     */
    public void setSubscriberNumber(String subscriberNumber) {
        if (subscriberNumber == null || subscriberNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Subscriber number must not be empty");
        }
        this.subscriberNumber = subscriberNumber;
    }

    /**
     * Gets the activity details.
     * 
     * @return activity details
     */
    public String getActivityDetails() {
        return activityDetails;
    }

    /**
     * Sets the activity details.
     * 
     * @param activityDetails activity details
     * @throws IllegalArgumentException if activity details is empty
     */
    public void setActivityDetails(String activityDetails) {
        if (activityDetails == null || activityDetails.trim().isEmpty()) {
            throw new IllegalArgumentException("Activity details must not be empty");
        }
        this.activityDetails = activityDetails;
    }

    /**
     * Gets the creation timestamp.
     * 
     * @return creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the creation timestamp.
     * 
     * @param createdAt creation timestamp
     * @throws IllegalArgumentException if created at is null
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        if (createdAt == null) {
            throw new IllegalArgumentException("Created at must not be null");
        }
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tag)) return false;
        Tag tag = (Tag) o;
        return logId > 0 && logId == tag.logId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(logId);
    }

    @Override
    public String toString() {
        return "Tag{" +
                "logId=" + logId +
                ", subscriberNumber='" + subscriberNumber + '\'' +
                ", activityDetails='" + activityDetails + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}