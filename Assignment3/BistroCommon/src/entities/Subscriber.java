package entities;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a registered restaurant subscriber (member).
 * This class is a shared entity transferred between Client and Server.
 */
public class Subscriber implements Serializable {

    private static final long serialVersionUID = 1L;

    private int subscriberId;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;

    /**
     * Full constructor.
     */
    public Subscriber(int subscriberId, String firstName, String lastName,
                      String phoneNumber, String email) {
        this.subscriberId = subscriberId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    /**
     * Empty constructor (required for serialization / frameworks).
     */
    public Subscriber() {
    }

    public int getSubscriberId() {
        return subscriberId;
    }

    public void setSubscriberId(int subscriberId) {
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

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Subscribers are identified uniquely by subscriberId.
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
