package entities;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * Represents a user in the Bistro restaurant system.
 */
public class User implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private int userId;
    private String name;
    private String emailAddress;
    private String phoneNumber;
    private String userPassword;
    private UserRole userRole;
    
    /** Indicates if the account is active (true) or inactive (false) */
    private boolean accountStatus;
    
    private Timestamp registrationDate;

    /**
     * User roles in the system.
     */
    public enum UserRole {
        SUBSCRIBER,
        REPRESENTATIVE,
        MANAGER
    }

    /**
     * Default constructor.
     */
    public User() {
        this.accountStatus = true;
    }

    /**
     * Full constructor.
     * @param userId user ID
     * @param name user's full name
     * @param emailAddress user's email
     * @param phoneNumber user's phone
     * @param userPassword user's password
     * @param userRole user's role
     * @param accountStatus account status
     * @param registrationDate registration timestamp
     */
    public User(int userId, String name, String emailAddress, String phoneNumber,
                String userPassword, UserRole userRole, boolean accountStatus, 
                Timestamp registrationDate) {
        this.userId = userId;
        this.name = name;
        this.emailAddress = emailAddress;
        this.phoneNumber = phoneNumber;
        this.userPassword = userPassword;
        this.userRole = userRole;
        this.accountStatus = accountStatus;
        this.registrationDate = registrationDate;
    }

    /**
     * Constructor for creating new user.
     * @param name user's full name
     * @param emailAddress user's email
     * @param phoneNumber user's phone
     * @param userPassword user's password
     * @param userRole user's role
     */
    public User(String name, String emailAddress, String phoneNumber,
                String userPassword, UserRole userRole) {
        this.name = name;
        this.emailAddress = emailAddress;
        this.phoneNumber = phoneNumber;
        this.userPassword = userPassword;
        this.userRole = userRole;
        this.accountStatus = true;
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
     * Gets the user's name.
     * @return user's name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the user's name.
     * @param name user's name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the user's email address.
     * @return email address
     */
    public String getEmailAddress() {
        return emailAddress;
    }

    /**
     * Sets the user's email address.
     * @param emailAddress email address
     */
    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    /**
     * Gets the user's phone number.
     * @return phone number
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Sets the user's phone number.
     * @param phoneNumber phone number
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * Gets the user's password.
     * @return password
     */
    public String getUserPassword() {
        return userPassword;
    }

    /**
     * Sets the user's password.
     * @param userPassword password
     */
    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    /**
     * Gets the user's role.
     * @return user role
     */
    public UserRole getUserRole() {
        return userRole;
    }

    /**
     * Sets the user's role.
     * @param userRole user role
     */
    public void setUserRole(UserRole userRole) {
        this.userRole = userRole;
    }

    /**
     * Gets the account status.
     * @return true if active, false otherwise
     */
    public boolean isAccountStatus() {
        return accountStatus;
    }

    /**
     * Sets the account status.
     * @param accountStatus account status
     */
    public void setAccountStatus(boolean accountStatus) {
        this.accountStatus = accountStatus;
    }

    /**
     * Gets the registration date.
     * @return registration timestamp
     */
    public Timestamp getRegistrationDate() {
        return registrationDate;
    }

    /**
     * Sets the registration date.
     * @param registrationDate registration timestamp
     */
    public void setRegistrationDate(Timestamp registrationDate) {
        this.registrationDate = registrationDate;
    }

    /**
     * Checks if user is a subscriber.
     * @return true if subscriber
     */
    public boolean isSubscriber() {
        return userRole == UserRole.SUBSCRIBER;
    }

    /**
     * Checks if user is a representative.
     * @return true if representative
     */
    public boolean isRepresentative() {
        return userRole == UserRole.REPRESENTATIVE;
    }

    /**
     * Checks if user is a manager.
     * @return true if manager
     */
    public boolean isManager() {
        return userRole == UserRole.MANAGER;
    }

    /**
     * Checks if account is active.
     * @return true if active
     */
    public boolean isActive() {
        return accountStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return userId == user.userId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", name='" + name + '\'' +
                ", emailAddress='" + emailAddress + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", userRole=" + userRole +
                ", accountStatus=" + accountStatus +
                ", registrationDate=" + registrationDate +
                '}';
    }
}