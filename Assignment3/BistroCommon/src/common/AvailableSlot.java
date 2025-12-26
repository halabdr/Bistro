package common;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Represents a single available reservation time slot.
 * This object is sent from the server to the client to indicate
 * a possible starting time for a reservation.
 */
public class AvailableSlot implements Serializable {
    private static final long serialVersionUID = 1L;

    /** The start time of the available slot */
    private final LocalDateTime startTime;

    /**
     * Creates a new available time slot.
     *
     * @param startTime the start time of the reservation slot
     */
    public AvailableSlot(LocalDateTime startTime) 
    {
        this.startTime = startTime;
    }

    /**
     * @return the start time of the slot
     */
    public LocalDateTime getStartTime() 
    { 
    	    return startTime; 
    }

    /**
     * Returns the slot as a readable string.
     *
     * @return start time as string
     */
    @Override
    public String toString() 
    {
        return startTime.toString();
    }
}