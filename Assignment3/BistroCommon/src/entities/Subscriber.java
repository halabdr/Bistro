package entities;

import java.io.Serial;
import java.time.LocalDateTime;
import java.sql.Timestamp;

/**
 * Subscriber entity represents a restaurant subscriber (member).
 * Extends User to include subscriber-specific information.
 */
public class Subscriber extends User {

    @Serial
    private static final long serialVersionUID = 1L;

    private String subscriberNumber;
    private String membershipCard;

    /**
     * Default constructor.
     */
    public Subscriber() {
        super();
        setUserRole(UserRole.SUBSCRIBER);
    }

    /**
     * Full constructor.
     * 
     * @param userId user ID
     * @param name user name
     * @param emailAddress email address
     * @param phoneNumber phone number
     * @param userPassword password
     * @param accountStatus account status
     * @param subscriberNumber subscriber number
     * @param membershipCard membership card
     */
    public Subscriber(int userId, String name, String emailAddress, String phoneNumber,
                     String userPassword, boolean accountStatus, String subscriberNumber, 
                     String membershipCard) {
        super();
        
        setUserId(userId);
        setName(name);
        setEmailAddress(emailAddress);
        setPhoneNumber(phoneNumber);
        setUserPassword(userPassword);
        setUserRole(UserRole.SUBSCRIBER);
        setAccountStatus(accountStatus);
        setRegistrationDate(Timestamp.valueOf(LocalDateTime.now()));
        
        this.subscriberNumber = subscriberNumber;
        this.membershipCard = membershipCard;
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
     */
    public void setSubscriberNumber(String subscriberNumber) {
        this.subscriberNumber = subscriberNumber;
    }

    /**
     * Gets the membership card.
     * 
     * @return membership card
     */
    public String getMembershipCard() {
        return membershipCard;
    }

    /**
     * Sets the membership card.
     * 
     * @param membershipCard membership card
     */
    public void setMembershipCard(String membershipCard) {
        this.membershipCard = membershipCard;
    }

    @Override
    public String toString() {
        return "Subscriber{" +
                "userId=" + getUserId() +
                ", name='" + getName() + '\'' +
                ", email='" + getEmailAddress() + '\'' +
                ", subscriberNumber='" + subscriberNumber + '\'' +
                ", membershipCard='" + membershipCard + '\'' +
                ", accountStatus=" + isAccountStatus() +
                '}';
    }
}