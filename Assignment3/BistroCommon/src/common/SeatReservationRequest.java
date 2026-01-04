package common;

import java.io.Serializable;

/**
 * Request to seat a customer (assign a table) using confirmation code.
 */
public class SeatReservationRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String confirmationCode;

    public SeatReservationRequest(String confirmationCode) {
        this.confirmationCode = confirmationCode;
    }

    public String getConfirmationCode() {
        return confirmationCode;
    }
}
