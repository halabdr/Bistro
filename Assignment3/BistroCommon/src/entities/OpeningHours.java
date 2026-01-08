package entities;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalTime;
import java.util.Objects;

/**
 * OpeningHours entity class represents the regular weekly operating hours of the restaurant.
 * Each record defines the opening and closing times for a specific day of the week.
 */
public class OpeningHours implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private int id;
    private Weekday weekday;
    private LocalTime openingTime;
    private LocalTime closingTime;

    /**
     * Enum represents days of the week.
     */
    public enum Weekday {
        SUNDAY,
        MONDAY,
        TUESDAY,
        WEDNESDAY,
        THURSDAY,
        FRIDAY,
        SATURDAY
    }

    /**
     * Default constructor.
     */
    public OpeningHours() {
    }

    /**
     * Full constructor.
     * 
     * @param id          Unique identifier
     * @param weekday     Day of the week
     * @param openingTime Opening time
     * @param closingTime Closing time
     */
    public OpeningHours(int id, Weekday weekday, LocalTime openingTime, LocalTime closingTime) {
        setId(id);
        setWeekday(weekday);
        setOpeningTime(openingTime);
        setClosingTime(closingTime);
        validate();
    }

    /**
     * Constructor without ID (for creating new entries).
     * 
     * @param weekday     Day of the week
     * @param openingTime Opening time
     * @param closingTime Closing time
     */
    public OpeningHours(Weekday weekday, LocalTime openingTime, LocalTime closingTime) {
        setWeekday(weekday);
        setOpeningTime(openingTime);
        setClosingTime(closingTime);
        validate();
    }

    /**
     * Checks if the restaurant is open at a specific time on this weekday.
     * 
     * @param time the time to check
     * @return true if the restaurant is open at the given time
     */
    public boolean isOpenAt(LocalTime time) {
        if (time == null || openingTime == null || closingTime == null) {
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
     * Validates the opening hours state.
     * 
     * @throws IllegalArgumentException if the data is invalid
     */
    public void validate() {
        if (weekday == null) {
            throw new IllegalArgumentException("Weekday must not be null");
        }
        if (openingTime == null) {
            throw new IllegalArgumentException("Opening time must not be null");
        }
        if (closingTime == null) {
            throw new IllegalArgumentException("Closing time must not be null");
        }
        if (id < 0) {
            throw new IllegalArgumentException("ID must not be negative");
        }
    }

    /**
     * Gets the ID.
     * 
     * @return ID
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the ID.
     * 
     * @param id ID
     * @throws IllegalArgumentException if ID is negative
     */
    public void setId(int id) {
        if (id < 0) {
            throw new IllegalArgumentException("ID must not be negative");
        }
        this.id = id;
    }

    /**
     * Gets the weekday.
     * 
     * @return weekday
     */
    public Weekday getWeekday() {
        return weekday;
    }

    /**
     * Sets the weekday.
     * 
     * @param weekday weekday
     * @throws IllegalArgumentException if weekday is null
     */
    public void setWeekday(Weekday weekday) {
        if (weekday == null) {
            throw new IllegalArgumentException("Weekday must not be null");
        }
        this.weekday = weekday;
    }

    /**
     * Gets the opening time.
     * 
     * @return opening time
     */
    public LocalTime getOpeningTime() {
        return openingTime;
    }

    /**
     * Sets the opening time.
     * 
     * @param openingTime opening time
     * @throws IllegalArgumentException if opening time is null
     */
    public void setOpeningTime(LocalTime openingTime) {
        if (openingTime == null) {
            throw new IllegalArgumentException("Opening time must not be null");
        }
        this.openingTime = openingTime;
    }

    /**
     * Gets the closing time.
     * 
     * @return closing time
     */
    public LocalTime getClosingTime() {
        return closingTime;
    }

    /**
     * Sets the closing time.
     * 
     * @param closingTime closing time
     * @throws IllegalArgumentException if closing time is null
     */
    public void setClosingTime(LocalTime closingTime) {
        if (closingTime == null) {
            throw new IllegalArgumentException("Closing time must not be null");
        }
        this.closingTime = closingTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OpeningHours)) return false;
        OpeningHours that = (OpeningHours) o;
        return id > 0 && id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "OpeningHours{" +
                "id=" + id +
                ", weekday=" + weekday +
                ", openingTime=" + openingTime +
                ", closingTime=" + closingTime +
                '}';
    }
}