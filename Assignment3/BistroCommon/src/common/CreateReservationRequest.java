package common;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Request object sent from the client to create a new reservation.
 * Shared between client and server.
 */
public class CreateReservationRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private final LocalDateTime dateTime;
    private final int numOfDiners;

    /** True if the reservation is made by a subscriber, false for a guest. */
    private final boolean subscriber;

    /** Required when subscriber == true. */
    private final int subscriberId;

    /** Required when subscriber == false. */
    private final String guestPhone;

    /** Optional when subscriber == false. */
    private final String guestEmail;

    public CreateReservationRequest(LocalDateTime dateTime,
                                    int numOfDiners,
                                    boolean subscriber,
                                    int subscriberId,
                                    String guestPhone,
                                    String guestEmail) {
        this.dateTime = dateTime;
        this.numOfDiners = numOfDiners;
        this.subscriber = subscriber;
        this.subscriberId = subscriberId;
        this.guestPhone = guestPhone;
        this.guestEmail = guestEmail;
    }

    // Convenience constructors
    public static CreateReservationRequest forSubscriber(LocalDateTime dateTime,
                                                         int numOfDiners,
                                                         int subscriberId) {
        return new CreateReservationRequest(dateTime, numOfDiners, true, subscriberId, null, null);
    }

    public static CreateReservationRequest forGuest(LocalDateTime dateTime,
                                                    int numOfDiners,
                                                    String guestPhone,
                                                    String guestEmail) {
        return new CreateReservationRequest(dateTime, numOfDiners, false, 0, guestPhone, guestEmail);
    }

    public LocalDateTime getDateTime() { return dateTime; }
    public int getNumOfDiners() { return numOfDiners; }
    public boolean isSubscriber() { return subscriber; }
    public int getSubscriberId() { return subscriberId; }
    public String getGuestPhone() { return guestPhone; }
    public String getGuestEmail() { return guestEmail; }
}
