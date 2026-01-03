package server;

import java.time.*;
import java.util.*;

import database.InmemoryStore;
import entities.Reservation;
import entities.Table;

public class AvailabilityService {

    private final InmemoryStore store;

    public AvailabilityService(InmemoryStore store) {
        this.store = store;
    }

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

    private boolean overlaps(LocalDateTime aStart, LocalDateTime aEnd, LocalDateTime bStart, LocalDateTime bEnd) {
        return aStart.isBefore(bEnd) && bStart.isBefore(aEnd);
    }
}