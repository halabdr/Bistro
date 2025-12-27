package entities;

import java.io.Serializable;
import java.util.Objects;

/**
 * User entity class represents a user in the Bistro restaurant system.
 * Users can be subscribers, casual customers, restaurant representatives, or managers.
 * This class is shared between client and server as user information is sent after login.
 */
public class User implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private int userId;
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
    private UserRole role;

    /**
     * Enum representing the role of a user in the system.
     */
    public enum UserRole {
        /** Regular customer (not a subscriber) */
        CUSTOMER,
        
        /** Subscriber with special benefits */
        SUBSCRIBER,
        
        /** Restaurant staff member */
        STAFF,
        
        /** Restaurant manager with access to reports */
        MANAGER
    }

    /**
     * Default constructor.
     */
    public User() {
    }

    /**
     * Full constructor for creating a user.
     *
     * @param userId user identifier
     * @param username unique username for login
     * @param password user password
     * @param firstName user's first name
     * @param lastName user's last name
     * @param phoneNumber user's phone number
     * @param email user's email address
     * @param role user's role in the system
     */
    public User(int userId, String username, String password, String firstName, 
                String lastName, String phoneNumber, String email, UserRole role) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.role = role;
    }

    /**
     * Returns the user identifier.
     * 
     * @return user ID
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Sets the user identifier.
     * 
     * @param userId user identifier
     */
    public void setUserId(int userId) {
        this.userId = userId;
    }

    /**
     * Returns the username (for login).
     * 
     * @return username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username.
     * 
     * @param username unique username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns the user's password.
     * 
     * @return password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the user's password.
     * 
     * @param password user password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns the user's first name.
     * 
     * @return first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the user's first name.
     * 
     * @param firstName first name
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Returns the user's last name.
     * 
     * @return last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the user's last name.
     * 
     * @param lastName last name
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Returns the user's full name.
     * 
     * @return full name (first name + last name)
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Returns the user's phone number.
     * 
     * @return phone number
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Sets the user's phone number.
     * 
     * @param phoneNumber phone number
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * Returns the user's email address.
     * 
     * @return email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user's email address.
     * 
     * @param email email address
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the user's role in the system.
     * 
     * @return user role
     */
    public UserRole getRole() {
        return role;
    }

    /**
     * Sets the user's role in the system.
     * 
     * @param role user role
     */
    public void setRole(UserRole role) {
        this.role = role;
    }

    /**
     * Checks if this user is a subscriber.
     * 
     * @return true if user role is SUBSCRIBER
     */
    public boolean isSubscriber() {
        return role == UserRole.SUBSCRIBER;
    }

    /**
     * Checks if this user is a manager.
     * 
     * @return true if user role is MANAGER
     */
    public boolean isManager() {
        return role == UserRole.MANAGER;
    }

    /**
     * Checks if this user is staff.
     * 
     * @return true if user role is STAFF
     */
    public boolean isStaff() {
        return role == UserRole.STAFF;
    }

    /**
     * Checks if this user is a regular customer.
     * 
     * @return true if user role is CUSTOMER
     */
    public boolean isCustomer() {
        return role == UserRole.CUSTOMER;
    }

    /**
     * Compares two users based on their ID.
     * 
     * @param o object to compare
     * @return true if users have the same ID
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return userId == user.userId;
    }

    /**
     * Returns hash code based on user ID.
     * 
     * @return hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    /**
     * Returns string representation of the user.
     * 
     * @return user details as string
     */
    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", name='" + getFullName() + '\'' +
                ", phone='" + phoneNumber + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                '}';
    }
}
