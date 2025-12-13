package entities;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a table in the restaurant.
 * The table has a fixed number of seats and an availability status.
 */
public class Table implements Serializable {

    private static final long serialVersionUID = 1L;

    private int tableNumber;
    private int numberOfSeats;
    private boolean availabilityStatus;

    /**
     * Creates a new table.
     *
     * @param tableNumber unique table identifier
     * @param numberOfSeats number of seats at the table (must be positive)
     * @param availabilityStatus availability status of the table
     */
    public Table(int tableNumber, int numberOfSeats, boolean availabilityStatus) {
        if (numberOfSeats <= 0) {
            throw new IllegalArgumentException("Number of seats must be greater than zero");
        }
        this.tableNumber = tableNumber;
        this.numberOfSeats = numberOfSeats;
        this.availabilityStatus = availabilityStatus;
    }

    /** Empty constructor */
    public Table() {
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(int tableNumber) {
        this.tableNumber = tableNumber;
    }

    public int getNumberOfSeats() {
        return numberOfSeats;
    }

    /**
     * Updates the number of seats at the table.
     *
     * @param numberOfSeats new number of seats (must be positive)
     */
    public void setNumberOfSeats(int numberOfSeats) {
        if (numberOfSeats <= 0) {
            throw new IllegalArgumentException("Number of seats must be greater than zero");
        }
        this.numberOfSeats = numberOfSeats;
    }

    /**
     * Returns the availability status of the table.
     *
     * @return true if the table is available
     */
    public boolean isAvailabilityStatus() {
        return availabilityStatus;
    }

    /**
     * Sets the availability status of the table.
     *
     * @param availabilityStatus new availability status
     */
    public void setAvailabilityStatus(boolean availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }

    /**
     * Checks whether a given number of guests can be seated at the table.
     *
     * @param numberOfGuests number of guests
     * @return true if the table is available and has enough seats
     */
    public boolean canSeat(int numberOfGuests) {
        return availabilityStatus && numberOfGuests > 0 && numberOfGuests <= numberOfSeats;
    }

    /**
     * Releases the table and marks it as available.
     */
    public void releaseTable() {
        this.availabilityStatus = true;
    }

    /**
     * Marks the table as occupied.
     */
    public void occupyTable() {
        this.availabilityStatus = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Table)) return false;
        Table table = (Table) o;
        return tableNumber == table.tableNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tableNumber);
    }

    @Override
    public String toString() {
        return "Table{" +
                "tableNumber=" + tableNumber +
                ", numberOfSeats=" + numberOfSeats +
                ", availabilityStatus=" + availabilityStatus +
                '}';
    }
}