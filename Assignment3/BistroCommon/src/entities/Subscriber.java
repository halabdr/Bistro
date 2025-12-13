package entities;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a registered restaurant subscriber (member).
 * This class holds subscriber personal details and basic validations.
 */
public class Subscriber implements Serializable {

    private static final long serialVersionUID = 1L;

    private int subscriberId;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;

    /**
     * Creates a new subscriber.
     *
     * @param subscriberId unique subscriber identifier (must be positive)
     * @param firstName subscriber first name
     * @param lastName subscriber last name
     * @param phoneNumber subscriber phone number
     * @param email subscriber email address
     */
    public Subscriber(int subscriberId, String firstName, String lastName,
                      String phoneNumber, String email) {

        if (subscriberId <= 0) {
            throw new IllegalArgumentException("Subscriber ID must be greater than zero");
        }

        this.subscriberId = subscriberId;
        this.firstName = firstName;
        this.lastName = lastName;
        setPhoneNumber(phoneNumber);
        setEmail(email);
    }

    /** Empty constructor */
    public Subscriber() {
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
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
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
        this.email = email;
    }

    /**
     * Checks whether the subscriber has valid contact information.
     *
     * @return true if both phone number and email are valid
     */
    public boolean hasValidContactInfo() {
        return phoneNumber != null && !phoneNumber.trim().isEmpty()
                && email != null && !email.trim().isEmpty();
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
package entities;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a registered restaurant subscriber (member).
 * This class holds subscriber personal details and basic validations.
 */
public class Subscriber implements Serializable {

    private static final long serialVersionUID = 1L;

    private int subscriberId;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;

    /**
     * Creates a new subscriber.
     *
     * @param subscriberId unique subscriber identifier (must be positive)
     * @param firstName subscriber first name
     * @param lastName subscriber last name
     * @param phoneNumber subscriber phone number
     * @param email subscriber email address
     */
    public Subscriber(int subscriberId, String firstName, String lastName,
                      String phoneNumber, String email) {

        if (subscriberId <= 0) {
            throw new IllegalArgumentException("Subscriber ID must be greater than zero");
        }

        this.subscriberId = subscriberId;
        this.firstName = firstName;
        this.lastName = lastName;
        setPhoneNumber(phoneNumber);
        setEmail(email);
    }

    /** Empty constructor */
    public Subscriber() {
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
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
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
        this.email = email;
    }

    /**
     * Checks whether the subscriber has valid contact information.
     *
     * @return true if both phone number and email are valid
     */
    public boolean hasValidContactInfo() {
        return phoneNumber != null && !phoneNumber.trim().isEmpty()
                && email != null && !email.trim().isEmpty();
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
