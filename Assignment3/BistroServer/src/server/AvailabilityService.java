package server;

import java.time.*;
import java.util.*;

import database.InmemoryStore;
import entities.Reservation;
import entities.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service responsible for availability and table allocation logic.
 * Contains all business rules related to table availability.
 */
public class AvailabilityService {

    private final InmemoryStore store;

    public AvailabilityService(InmemoryStore store) {
        this.store = store;
    }
    /**
     * Returns all available reservation slots for a given date and party size.
     *
     * @param date requested reservation date
     * @param diners number of diners
     * @return list of available start times
     */
    public List<LocalDateTime> getAvailableSlots(LocalDate date, int diners) {
        var hours = store.getHoursFor(date);
        if (hours == null) return List.of();

        LocalTime open = hours.getOpen();
        LocalTime close = hours.getClose();

        LocalTime lastStart = close.minusHours(2);
        if (lastStart.isBefore(open)) return List.of();

        List<LocalDateTime> result = new ArrayList<>();
        for (LocalTime t = open; !t.isAfter(lastStart); t = t.plusMinutes(30)) {
            LocalDateTime start = LocalDateTime.of(date, t);
            LocalDateTime end = start.plusHours(2);

            if (hasFreeTable(start, end, diners)) {
                result.add(start);
            }
        }
        return result;
    }
    /**
     * Allocates a free table for a specific date-time and party size.
     *
     * @param dateTime reservation start time
     * @param diners  number of diners
     * @return allocated table ID, or -1 if none available
     */
    public int allocateTable(LocalDateTime dateTime, int diners) {
        LocalDateTime endTime = dateTime.plusHours(2);

        for (Table table : store.tables) {
            if (table.getNumberOfSeats() < diners) continue;

            boolean taken = store.reservations.stream().anyMatch(r ->
                r.getAssignedTableNumber() == table.getTableNumber() &&
                overlaps(dateTime, endTime, r.getStartDateTime(), r.getEndDateTime())
            );

            if (!taken) {
                return table.getTableNumber();
            }
        }
        return -1;
    }
    /**
     * Checks if at least one suitable table is free in the given time range.
     */
    private boolean hasFreeTable(LocalDateTime start, LocalDateTime end, int diners) {
        for (Table table : store.tables) {
            if (table.getNumberOfSeats() < diners) continue;

            boolean taken = store.reservations.stream().anyMatch(r ->
                r.getAssignedTableNumber() == table.getTableNumber() && overlaps(start, end, r.getStartDateTime(), r.getEndDateTime())
            );

            if (!taken) return true;
        }
        return false;
    }
    /**
     * Checks if two time ranges overlap.
     */
    private boolean overlaps(LocalDateTime aStart, LocalDateTime aEnd, LocalDateTime bStart, LocalDateTime bEnd) {
        return aStart.isBefore(bEnd) && bStart.isBefore(aEnd);
    }
}