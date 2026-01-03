package common;

import java.io.Serializable;
import java.time.LocalTime;

public class OpeningHours implements Serializable {
    private static final long serialVersionUID = 1L;

    private final LocalTime open;
    private final LocalTime close;

    public OpeningHours(LocalTime open, LocalTime close) {
        this.open = open;
        this.close = close;
    }

    public LocalTime getOpen() { return open; }
    public LocalTime getClose() { return close; }
}