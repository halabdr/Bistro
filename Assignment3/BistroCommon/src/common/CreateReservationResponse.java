package common;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Response object returned after a successful reservation creation.
 * Contains reservation confirmation details.
 */
public class CreateReservationResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String confirmationCode;
    // private final int tableId;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    /**
     * Constructs a CreateReservationResponse.
     *
     * @param confirmationCode unique reservation confirmation code
     * @param tableId allocated table ID
     *  @param startTime reservation start time
     * @param endTime reservation end time
     */
    public CreateReservationResponse(String confirmationCode,
                                    
                                     LocalDateTime startTime,
                                     LocalDateTime endTime) {
        this.confirmationCode = confirmationCode;
       // this.tableId = tableId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getConfirmationCode() {
        return confirmationCode;
    }

  //  public int getTableId() {
  //      return tableId;
  //  }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }
}
