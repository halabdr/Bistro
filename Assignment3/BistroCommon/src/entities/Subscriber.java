package entities;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a subscriber in the Bistro restaurant system.
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

    public int getSubscriberId() {
        return subscriberId;
    }

    public void setSubscriberId(int subscriberId) {
        if (subscriberId <= 0) {
            throw new IllegalArgumentException("Subscriber ID must be greater than zero");
        }
        this.subscriberId = subscriberId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
    	if (firstName == null || firstName.trim().isEmpty())
    		throw new IllegalArgumentException("First name must not be empty");
    	this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        if (lastName == null || lastName.trim().isEmpty())
        	throw new IllegalArgumentException("Last name must not be empty");
    	this.lastName = lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Updates subscriber phone number.
     *
     * @param phoneNumber phone number (must not be null or empty)
     */
    public void setPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number must not be empty");
        }
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    /**
     * Updates subscriber email address.
     *
     * @param email email address (must not be null or empty)
     */
    public void setEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email must not be empty");
        }
        
        if (!email.contains("@")) {
        	throw new IllegalArgumentException("Invalid email address");
        }
        this.email = email;
    }

    /**
     * Checks whether the subscriber has valid contact information.
     *
     * @return true if both phone number and email are valid
     */
    public boolean hasValidContactInfo() {
        return phoneNumber != null && !phoneNumber.trim().isEmpty() && 
        	   email != null && !email.trim().isEmpty() && email.contains("@");
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
