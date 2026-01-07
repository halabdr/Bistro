package entities;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a subscriber in the Bistro restaurant system
 */
public class Subscriber implements Serializable {

	@Serial
    private static final long serialVersionUID = 1L;

    private int userId;
    private String subscriberNumber;
    private String membershipCard;

    /**
     * Default constructor.
     */
    public Subscriber() {
    }

    /**
     * Full constructor.
     * @param userId user ID
     * @param subscriberNumber unique subscriber number
     * @param membershipCard membership card identifier
     */
    public Subscriber(int userId, String subscriberNumber, String membershipCard) {
        this.userId = userId;
        this.subscriberNumber = subscriberNumber;
        this.membershipCard = membershipCard;
    }

    /**
     * Constructor for creating new subscriber.
     * @param subscriberNumber unique subscriber number
     * @param membershipCard membership card identifier
     */
    public Subscriber(String subscriberNumber, String membershipCard) {
        this.subscriberNumber = subscriberNumber;
        this.membershipCard = membershipCard;
    }

    /**
     * Gets the user ID.
     * @return user ID
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Sets the user ID.
     * @param userId user ID
     */
    public void setUserId(int userId) {
        this.userId = userId;
    }

    /**
     * Gets the subscriber number.
     * @return subscriber number
     */
    public String getSubscriberNumber() {
        return subscriberNumber;
    }

    /**
     * Sets the subscriber number.
     * @param subscriberNumber subscriber number
     */
    public void setSubscriberNumber(String subscriberNumber) {
        this.subscriberNumber = subscriberNumber;
    }

    /**
     * Gets the membership card identifier.
     * @return membership card
     */
    public String getMembershipCard() {
        return membershipCard;
    }

    /**
     * Sets the membership card identifier.
     * @param membershipCard membership card
     */
    public void setMembershipCard(String membershipCard) {
        this.membershipCard = membershipCard;
    }

    /**
     * Subscribers are uniquely identified by subscriberId.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Subscriber)) return false;
        Subscriber that = (Subscriber) o;
        return subscriberId == that.subscriberId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(subscriberId);
    }

    @Override
    public String toString() {
        return "Subscriber{" +
                "subscriberId=" + subscriberId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
