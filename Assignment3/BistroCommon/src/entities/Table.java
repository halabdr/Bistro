package entities;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * Represents a table in the restaurant.
 */
public class Table implements Serializable {

    private static final long serialVersionUID = 1L;

    private int tableNumber;
    private int seatCapacity;
    private String tableLocation;
    private TableStatus tableStatus;
    private Timestamp reservationStart;
    private Timestamp reservationEnd;

    /**
     * Table status enum.
     */
    public enum TableStatus {
        AVAILABLE,
        OCCUPIED
    }

    /**
     * Default constructor.
     */
    public Table() {
        this.tableStatus = TableStatus.AVAILABLE;
    }

    /**
     * Full constructor.
     * @param tableNumber table number
     * @param seatCapacity number of seats
     * @param tableLocation location of the table
     * @param tableStatus table status
     * @param reservationStart reservation start time
     * @param reservationEnd reservation end time
     */
    public Table(int tableNumber, int seatCapacity, String tableLocation, 
                 TableStatus tableStatus, Timestamp reservationStart, Timestamp reservationEnd) {
        this.tableNumber = tableNumber;
        this.seatCapacity = seatCapacity;
        this.tableLocation = tableLocation;
        this.tableStatus = tableStatus;
        this.reservationStart = reservationStart;
        this.reservationEnd = reservationEnd;
    }

    /**
     * Constructor for creating new table.
     * @param tableNumber table number
     * @param seatCapacity number of seats
     * @param tableLocation location of the table
     */
    public Table(int tableNumber, int seatCapacity, String tableLocation) {
        this.tableNumber = tableNumber;
        this.seatCapacity = seatCapacity;
        this.tableLocation = tableLocation;
        this.tableStatus = TableStatus.AVAILABLE;
    }

    /**
     * Gets the table number.
     * @return table number
     */
    public int getTableNumber() {
        return tableNumber;
    }

    /**
     * Sets the table number.
     * @param tableNumber table number
     */
    public void setTableNumber(int tableNumber) {
        this.tableNumber = tableNumber;
    }

    /**
     * Gets the seat capacity.
     * @return seat capacity
     */
    public int getSeatCapacity() {
        return seatCapacity;
    }

    /**
     * Sets the seat capacity.
     * @param seatCapacity seat capacity
     */
    public void setSeatCapacity(int seatCapacity) {
        this.seatCapacity = seatCapacity;
    }

    /**
     * Gets the table location.
     * @return table location
     */
    public String getTableLocation() {
        return tableLocation;
    }

    /**
     * Sets the table location.
     * @param tableLocation table location
     */
    public void setTableLocation(String tableLocation) {
        this.tableLocation = tableLocation;
    }

    /**
     * Gets the table status.
     * @return table status
     */
    public TableStatus getTableStatus() {
        return tableStatus;
    }

    /**
     * Sets the table status.
     * @param tableStatus table status
     */
    public void setTableStatus(TableStatus tableStatus) {
        this.tableStatus = tableStatus;
    }

    /**
     * Gets the reservation start time.
     * @return reservation start time
     */
    public Timestamp getReservationStart() {
        return reservationStart;
    }

    /**
     * Sets the reservation start time.
     * @param reservationStart reservation start time
     */
    public void setReservationStart(Timestamp reservationStart) {
        this.reservationStart = reservationStart;
    }

    /**
     * Gets the reservation end time.
     * @return reservation end time
     */
    public Timestamp getReservationEnd() {
        return reservationEnd;
    }

    /**
     * Sets the reservation end time.
     * @param reservationEnd reservation end time
     */
    public void setReservationEnd(Timestamp reservationEnd) {
        this.reservationEnd = reservationEnd;
    }

    /**
     * Checks if table is available.
     * @return true if available
     */
    public boolean isAvailable() {
        return tableStatus == TableStatus.AVAILABLE;
    }

    /**
     * Checks if a number of guests can be seated.
     * @param numberOfGuests number of guests
     * @return true if table can accommodate the guests
     */
    public boolean canSeat(int numberOfGuests) {
        return isAvailable() && numberOfGuests > 0 && numberOfGuests <= seatCapacity;
    }

    /**
     * Marks the table as available.
     */
    public void releaseTable() {
        this.tableStatus = TableStatus.AVAILABLE;
        this.reservationStart = null;
        this.reservationEnd = null;
    }

    /**
     * Marks the table as occupied.
     */
    public void occupyTable() {
        this.tableStatus = TableStatus.OCCUPIED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
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
                ", seatCapacity=" + seatCapacity +
                ", tableLocation='" + tableLocation + '\'' +
                ", tableStatus=" + tableStatus +
                ", reservationStart=" + reservationStart +
                ", reservationEnd=" + reservationEnd +
                '}';
    }
}
