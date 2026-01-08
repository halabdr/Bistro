package entities;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

/**
 * SpecialHours entity class represents special operating hours for specific dates.
 * Used for holidays, special events, or days when the restaurant has non-standard hours.
 */
public class SpecialHours implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private int specialId;
    private LocalDate specialDate;
    private LocalTime openingTime;
    private LocalTime closingTime;
    private boolean closedFlag;

    /**
     * Default constructor.
     */
    public SpecialHours() {
    }

    /**
     * Full constructor.
     * 
     * @param specialId   Unique identifier
     * @param specialDate The specific date for these special hours
     * @param openingTime Opening time (null if closed)
     * @param closingTime Closing time (null if closed)
     * @param closedFlag  True if restaurant is closed on this date
     */
    public SpecialHours(int specialId, LocalDate specialDate, LocalTime openingTime, 
                        LocalTime closingTime, boolean closedFlag) {
        setSpecialId(specialId);
        setSpecialDate(specialDate);
        setOpeningTime(openingTime);
        setClosingTime(closingTime);
        setClosedFlag(closedFlag);
        validate();
    }

    /**
     * Constructor without ID (for creating new entries).
     * 
     * @param specialDate The specific date for these special hours
     * @param openingTime Opening time (null if closed)
     * @param closingTime Closing time (null if closed)
     * @param closedFlag  True if restaurant is closed on this date
     */
    public SpecialHours(LocalDate specialDate, LocalTime openingTime, 
                        LocalTime closingTime, boolean closedFlag) {
        setSpecialDate(specialDate);
        setOpeningTime(openingTime);
        setClosingTime(closingTime);
        setClosedFlag(closedFlag);
        validate();
    }

    /**
     * Creates a special hours entry for a closed day.
     * 
     * @param specialDate The date when restaurant is closed
     * @return SpecialHours instance representing a closed day
     */
    public static SpecialHours createClosedDay(LocalDate specialDate) {
        return new SpecialHours(specialDate, null, null, true);
    }

    /**
     * Creates a special hours entry for a day with custom hours.
     * 
     * @param specialDate The specific date
     * @param openingTime Opening time
     * @param closingTime Closing time
     * @return SpecialHours instance with custom hours
     */
    public static SpecialHours createCustomHours(LocalDate specialDate, LocalTime openingTime, LocalTime closingTime) {
        return new SpecialHours(specialDate, openingTime, closingTime, false);
    }

    /**
     * Checks if the restaurant is closed on this special date.
     * 
     * @return true if closed
     */
    public boolean isClosed() {
        return closedFlag;
    }

    /**
     * Checks if the restaurant is open at a specific time on this special date.
     * 
     * @param time the time to check
     * @return true if the restaurant is open at the given time
     */
    public boolean isOpenAt(LocalTime time) {
        if (closedFlag || time == null || openingTime == null || closingTime == null) {
            return false;
        }
        
        // Handle case where closing time is after midnight
        if (closingTime.isBefore(openingTime)) {
            return time.isAfter(openingTime) || time.equals(openingTime) || 
                   time.isBefore(closingTime) || time.equals(closingTime);
        }
        
        return (time.isAfter(openingTime) || time.equals(openingTime)) && 
               (time.isBefore(closingTime) || time.equals(closingTime));
    }

    /**
     * Validates the special hours state.
     * 
     * @throws IllegalArgumentException if the data is invalid
     */
    public void validate() {
        if (specialDate == null) {
            throw new IllegalArgumentException("Special date must not be null");
        }
        if (specialId < 0) {
            throw new IllegalArgumentException("Special ID must not be negative");
        }
        
        // If not closed, must have opening and closing times
        if (!closedFlag) {
            if (openingTime == null) {
                throw new IllegalArgumentException("Opening time must not be null when restaurant is open");
            }
            if (closingTime == null) {
                throw new IllegalArgumentException("Closing time must not be null when restaurant is open");
            }
        }
    }

    /**
     * Gets the special ID.
     * 
     * @return special ID
     */
    public int getSpecialId() {
        return specialId;
    }

    /**
     * Sets the special ID.
     * 
     * @param specialId special ID
     * @throws IllegalArgumentException if special ID is negative
     */
    public void setSpecialId(int specialId) {
        if (specialId < 0) {
            throw new IllegalArgumentException("Special ID must not be negative");
        }
        this.specialId = specialId;
    }

    /**
     * Gets the special date.
     * 
     * @return special date
     */
    public LocalDate getSpecialDate() {
        return specialDate;
    }

    /**
     * Sets the special date.
     * 
     * @param specialDate special date
     * @throws IllegalArgumentException if special date is null
     */
    public void setSpecialDate(LocalDate specialDate) {
        if (specialDate == null) {
            throw new IllegalArgumentException("Special date must not be null");
        }
        this.specialDate = specialDate;
    }

    /**
     * Gets the opening time.
     * 
     * @return opening time (null if closed)
     */
    public LocalTime getOpeningTime() {
        return openingTime;
    }

    /**
     * Sets the opening time.
     * 
     * @param openingTime opening time (can be null if closed)
     */
    public void setOpeningTime(LocalTime openingTime) {
        this.openingTime = openingTime;
    }

    /**
     * Gets the closing time.
     * 
     * @return closing time (null if closed)
     */
    public LocalTime getClosingTime() {
        return closingTime;
    }

    /**
     * Sets the closing time.
     * 
     * @param closingTime closing time (can be null if closed)
     */
    public void setClosingTime(LocalTime closingTime) {
        this.closingTime = closingTime;
    }

    /**
     * Gets the closed flag.
     * 
     * @return true if restaurant is closed on this date
     */
    public boolean getClosedFlag() {
        return closedFlag;
    }

    /**
     * Sets the closed flag.
     * 
     * @param closedFlag true if restaurant is closed on this date
     */
    public void setClosedFlag(boolean closedFlag) {
        this.closedFlag = closedFlag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SpecialHours)) return false;
        SpecialHours that = (SpecialHours) o;
        return specialId > 0 && specialId == that.specialId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(specialId);
    }

    @Override
    public String toString() {
        return "SpecialHours{" +
                "specialId=" + specialId +
                ", specialDate=" + specialDate +
                ", openingTime=" + openingTime +
                ", closingTime=" + closingTime +
                ", closedFlag=" + closedFlag +
                '}';
    }
}