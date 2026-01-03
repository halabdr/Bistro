package database;

import java.time.*;
import java.util.*;
import entities.Table;
import entities.Reservation;
import entities.WaitlistEntry;
import common.OpeningHours;

//זה קובץ זמני עד שנעבוד על MYSQL
public class InmemoryStore {

    public final List<Table> tables = new ArrayList<>();
    public final List<Reservation> reservations = new ArrayList<>();
    public final List<WaitlistEntry> waitlist = new ArrayList<>();

    public final Map<DayOfWeek, OpeningHours> weeklyHours = new EnumMap<>(DayOfWeek.class);
    public final Map<LocalDate, OpeningHours> specialHours = new HashMap<>();

    public InmemoryStore() {
    	//Basic opening hours (WILL change via the representative screen later)
        weeklyHours.put(DayOfWeek.MONDAY,    new OpeningHours(LocalTime.of(12,0), LocalTime.of(22,0)));
        weeklyHours.put(DayOfWeek.TUESDAY,   new OpeningHours(LocalTime.of(12,0), LocalTime.of(22,0)));
        weeklyHours.put(DayOfWeek.WEDNESDAY, new OpeningHours(LocalTime.of(12,0), LocalTime.of(22,0)));
        weeklyHours.put(DayOfWeek.THURSDAY,  new OpeningHours(LocalTime.of(12,0), LocalTime.of(23,0)));
        weeklyHours.put(DayOfWeek.FRIDAY,    new OpeningHours(LocalTime.of(12,0), LocalTime.of(23,0)));
        weeklyHours.put(DayOfWeek.SATURDAY,  new OpeningHours(LocalTime.of(12,0), LocalTime.of(23,0)));

        //WILL CHANGE TO DB
        tables.add(new Table(1, 2, false));
        tables.add(new Table(2, 2, false));
        tables.add(new Table(3, 4, false));
        tables.add(new Table(4, 4, false));
        tables.add(new Table(5, 6, false));
    }

    public OpeningHours getHoursFor(LocalDate date) {
        return specialHours.getOrDefault(date, weeklyHours.get(date.getDayOfWeek()));
    }
    public void addReservation(Reservation r) {
        reservations.add(r);
    }

    public List<Reservation> getReservations() {
        return reservations;
    }

}