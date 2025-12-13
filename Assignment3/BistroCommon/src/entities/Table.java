package entities;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a table in the restaurant.
 * Each table has a fixed number of seats and an availability status.
 * This class is shared between client and server.
 */
public class Table implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Unique table number */
    private int tableNumber;

    /** Number of seats at the table */
    private int numberOfSeats;

    /** Indicates whether the table is available */
    private boolean available;

    /**
     * Creates a new table.
     *
     * @param tableNumber unique table identifier
     * @param numberOfSeats number of seats at the table
     * @param available availability status
     */
    public Table(int tableNumber, int numberOfSeats, boolean available) {
        this.tableNumber = tableNumber;
        this.numberOfSeats = numberOfSeats;
        this.available = available;
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

    public void setNumberOfSeats(int numberOfSeats) {
        this.numberOfSeats = numberOfSeats;
    }

    /**
     * Returns whether the table is available.
     *
     * @return true if available, false otherwise
     */
    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
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
}
