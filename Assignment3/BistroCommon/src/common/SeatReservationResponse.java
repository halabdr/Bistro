package common;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Response returned after successfully seating a reservation.
 */
public class SeatReservationResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String confirmationCode;
    private final int tableNumber;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;

    public SeatReservationResponse(String confirmationCode, int tableNumber,
                                   LocalDateTime startTime, LocalDateTime endTime) {
        this.confirmationCode = confirmationCode;
        this.tableNumber = tableNumber;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getConfirmationCode() { return confirmationCode; }
    public int getTableNumber() { return tableNumber; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
}
