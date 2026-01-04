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

            if (canAccommodate(start, diners)) {
                result.add(start);
            }
        }
        return result;
    }
    
    public boolean canAccommodate(LocalDateTime start, int diners) {
        LocalDateTime end = start.plusHours(2);

        // Tables sorted by seats (ascending)
        List<Table> tables = new ArrayList<>(store.tables);
        tables.sort(Comparator.comparingInt(Table::getNumberOfSeats));

        // Collect overlapping reservations (not cancelled etc. if you later add statuses)
        List<Reservation> overlapping = store.reservations.stream()
                .filter(r -> overlaps(start, end, r.getStartDateTime(), r.getEndDateTime()))
                .toList();

        // Fixed tables (already seated / assignedTableNumber > 0) occupy tables
        Set<Integer> takenTableNumbers = new HashSet<>();
        for (Reservation r : overlapping) {
            if (r.getAssignedTableNumber() > 0) {
                takenTableNumbers.add(r.getAssignedTableNumber());
            }
        }

        // Build list of "unfixed" party sizes (reservations with no assigned table yet)
        List<Integer> parties = new ArrayList<>();
        for (Reservation r : overlapping) {
            if (r.getAssignedTableNumber() <= 0) {
                parties.add(r.getDinersCount());
            }
        }
        // include the new requested party
        parties.add(diners);

        // Sort parties descending (greedy best-fit)
        parties.sort(Comparator.reverseOrder());

        // Available tables after removing fixed ones
        List<Integer> freeSeats = new ArrayList<>();
        for (Table t : tables) {
            if (!takenTableNumbers.contains(t.getTableNumber())) {
                freeSeats.add(t.getNumberOfSeats());
            }
        }
        freeSeats.sort(Integer::compareTo);

        // Assign each party to the smallest available table that fits
        for (int p : parties) {
            int idx = -1;
            for (int i = 0; i < freeSeats.size(); i++) {
                if (freeSeats.get(i) >= p) {
                    idx = i;
                    break;
                }
            }
            if (idx == -1) return false; // no table fits this party
            freeSeats.remove(idx);
        }

        return true;
    }
    
    public int allocateTableForSeating(Reservation toSeat) {
        LocalDateTime start = toSeat.getStartDateTime();
        LocalDateTime end = toSeat.getEndDateTime();

        // Get overlapping reservations in that time window (including those not yet seated)
        List<Reservation> overlapping = store.reservations.stream()
                .filter(r -> overlaps(start, end, r.getStartDateTime(), r.getEndDateTime()))
                .toList();

        // Tables sorted by seats ascending (try smallest that fits)
        List<Table> tables = new ArrayList<>(store.tables);
        tables.sort(Comparator.comparingInt(Table::getNumberOfSeats));

        for (Table t : tables) {
            if (t.getNumberOfSeats() < toSeat.getDinersCount()) continue;

            // is this table already occupied by an already-seated reservation in the same time window?
            boolean occupiedByFixed = overlapping.stream().anyMatch(r ->
                    r.getAssignedTableNumber() == t.getTableNumber()
            );
            if (occupiedByFixed) continue;

            // Create a local list where we "fix" this reservation to this table (without changing the real object)
            List<Reservation> simulated = new ArrayList<>();

            for (Reservation r : overlapping) {
                if (r == toSeat) continue; // skip original (we will add a simulated copy)
                simulated.add(r);
            }

            // add a simulated fixed reservation entry for toSeat
            Reservation fixed = new Reservation();
            fixed.setStartDateTime(toSeat.getStartDateTime());
            fixed.setDinersCount(toSeat.getDinersCount());
            fixed.setCustomerType(toSeat.getCustomerType());
            fixed.setReservationStatus(toSeat.getStatus());
            fixed.setConfirmationCode(toSeat.getConfirmationCode());
            fixed.setAssignedTableNumber(t.getTableNumber());
            fixed.setCreatedAt(toSeat.getCreatedAt());
            fixed.setSubscriberId(toSeat.getSubscriberId());
            fixed.setGuestPhone(toSeat.getGuestPhone());
            fixed.setGuestEmail(toSeat.getGuestEmail());

            simulated.add(fixed);

            // If with this fixed choice the whole set is still feasible -> choose this table
            if (canAccommodateWithFixed(simulated, tables)) {
                return t.getTableNumber();
            }
        }

        return -1;
    }
    
      //======================== Help functions ===========================
    
    /**
     * Checks if two time ranges overlap.
     */
    private boolean overlaps(LocalDateTime startA, LocalDateTime endA, LocalDateTime startB, LocalDateTime endB) {
            return startA.isBefore(endB) && startB.isBefore(endA);
    }
    
    private boolean canAccommodateWithFixed(List<Reservation> overlapping, List<Table> tables) {
		// tables sorted by seats ascending
		List<Table> sortedTables = new ArrayList<>(tables);
		sortedTables.sort(Comparator.comparingInt(Table::getNumberOfSeats));
		
		// mark fixed tables taken (already assigned reservations)
		Set<Integer> taken = new HashSet<>();
		for (Reservation r : overlapping) {
			if (r.getAssignedTableNumber() > 0) {
				taken.add(r.getAssignedTableNumber());
			}
		}
		
		// party sizes that still need tables
		List<Integer> parties = new ArrayList<>();
		for (Reservation r : overlapping) {
			if (r.getAssignedTableNumber() <= 0) {
				parties.add(r.getDinersCount());
			}
		}
		
		// sort parties descending (place biggest first)
		parties.sort(Comparator.reverseOrder());
		
		// free tables seats
		List<Integer> freeSeats = new ArrayList<>();
		for (Table t : sortedTables) {
			if (!taken.contains(t.getTableNumber())) {
				freeSeats.add(t.getNumberOfSeats());
			}
		}
		freeSeats.sort(Integer::compareTo);
		
		for (int p : parties) {
			int idx = -1;
			for (int i = 0; i < freeSeats.size(); i++) {
				if (freeSeats.get(i) >= p) { idx = i; break; }
			}
			if (idx == -1) return false;
			freeSeats.remove(idx);
		}
		return true;
	}
		
}