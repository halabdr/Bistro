package entities;
import java.io.Serial;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Representative entity represents a restaurant representative or manager.
 * Extends User to include representative-specific information.
 */
public class Representative extends User {

    @Serial
    private static final long serialVersionUID = 1L;
    private String representativeNumber;

    /**
     * Default constructor.
     */
    public Representative() {
        super();
    }

    /**
     * Full constructor for representative.
     * 
     * @param userId user ID
     * @param name user name
     * @param emailAddress email address
     * @param phoneNumber phone number
     * @param userPassword password
     * @param userRole user role (REPRESENTATIVE or MANAGER)
     * @param accountStatus account status
     * @param representativeNumber representative number
     */
    public Representative(int userId, String name, String emailAddress, String phoneNumber,
                         String userPassword, UserRole userRole, boolean accountStatus,
                         String representativeNumber) {
        super();
        
        setUserId(userId);
        setName(name);
        setEmailAddress(emailAddress);
        setPhoneNumber(phoneNumber);
        setUserPassword(userPassword);
        setUserRole(userRole);
        setAccountStatus(accountStatus);
        setRegistrationDate(Timestamp.valueOf(LocalDateTime.now()));
        
        this.representativeNumber = representativeNumber;
    }

    /**
     * Gets the representative number.
     * 
     * @return representative number
     */
    public String getRepresentativeNumber() {
        return representativeNumber;
    }

    /**
     * Sets the representative number.
     * 
     * @param representativeNumber representative number
     */
    public void setRepresentativeNumber(String representativeNumber) {
        this.representativeNumber = representativeNumber;
    }

    @Override
    public String toString() {
        return "Representative{" +
                "userId=" + getUserId() +
                ", name='" + getName() + '\'' +
                ", email='" + getEmailAddress() + '\'' +
                ", role=" + getUserRole() +
                ", representativeNumber='" + representativeNumber + '\'' +
                ", accountStatus=" + isAccountStatus() +
                '}';
    }
}