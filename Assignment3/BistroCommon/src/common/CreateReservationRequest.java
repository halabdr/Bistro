package common;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Request object sent from the client to create a new reservation.
 * Contains all details required for reservation creation.
 */
public class CreateReservationRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private final LocalDateTime dateTime;
    private final int numOfDiners;
    private final String fullName;
    private final String phone;
    private final boolean subscriber;

    /**
     * Constructs a CreateReservationRequest.
     *
     * @param dateTime reservation start date and time
     * @param numOfDiners number of diners
     * @param fullName customer's full name
     * @param phone customer's phone number
     * @param subscriber whether the customer is a subscriber
     */
    public CreateReservationRequest(LocalDateTime dateTime,
                                    int numOfDiners,
                                    String fullName,
                                    String phone,
                                    boolean subscriber) {
        this.dateTime = dateTime;
        this.numOfDiners = numOfDiners;
        this.fullName = fullName;
        this.phone = phone;
        this.subscriber = subscriber;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public int getNumOfDiners() {
        return numOfDiners;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhone() {
        return phone;
    }

    public boolean isSubscriber() {
        return subscriber;
    }
}
