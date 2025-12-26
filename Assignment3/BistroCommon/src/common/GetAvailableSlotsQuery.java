package common;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * This class represents a request sent from the client to the server
 * in order to check which reservation time slots are available.
 * The request includes the desired date and the number of diners.
 */
public class GetAvailableSlotsQuery implements Serializable {
    private static final long serialVersionUID = 1L;

    private final LocalDate date;
    private final int NumOfDiners;

    /**
     * Creates a request to check available time slots.
     *
     * @param date the requested reservation date
     * @param NumOfDiners the number of diners
     */
    public GetAvailableSlotsQuery(LocalDate date, int NumOfDiners) 
    {
        this.date = date;
        this.NumOfDiners = NumOfDiners;
    }

    /**
     * @return the requested date
     */
    public LocalDate getDate() 
    { 
    	   return date; 
    	}
    
    /**
     * @return the number of diners
     */
    public int getNumOfDiners() 
    { 
    	   return NumOfDiners; 
    }
}