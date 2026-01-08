package entities;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Representative entity class represents a restaurant representative (employee).
 * A representative is a type of user with special privileges for managing restaurant operations.
 */
public class Representative implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private int userId;
    private String representativeNumber;

    /**
     * Default constructor.
     */
    public Representative() {
    }

    /**
     * Full constructor.
     * 
     * @param userId              The user ID (foreign key to users table)
     * @param representativeNumber The unique representative number
     */
    public Representative(int userId, String representativeNumber) {
        setUserId(userId);
        setRepresentativeNumber(representativeNumber);
        validate();
    }

    /**
     * Validates the representative state.
     * 
     * @throws IllegalArgumentException if the representative data is invalid
     */
    public void validate() {
        if (userId <= 0) {
            throw new IllegalArgumentException("User ID must be greater than zero");
        }
        if (representativeNumber == null || representativeNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Representative number must not be empty");
        }
    }

    /**
     * Gets the user ID.
     * 
     * @return user ID
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Sets the user ID.
     * 
     * @param userId user ID
     * @throws IllegalArgumentException if user ID is not greater than zero
     */
    public void setUserId(int userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("User ID must be greater than zero");
        }
        this.userId = userId;
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
     * @throws IllegalArgumentException if representative number is empty
     */
    public void setRepresentativeNumber(String representativeNumber) {
        if (representativeNumber == null || representativeNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Representative number must not be empty");
        }
        this.representativeNumber = representativeNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Representative)) return false;
        Representative that = (Representative) o;
        return userId == that.userId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Override
    public String toString() {
        return "Representative{" +
                "userId=" + userId +
                ", representativeNumber='" + representativeNumber + '\'' +
                '}';
    }
}