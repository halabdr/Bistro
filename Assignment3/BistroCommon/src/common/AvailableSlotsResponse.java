package common;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public class AvailableSlotsResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private final List<LocalDateTime> slots;

    public AvailableSlotsResponse(List<LocalDateTime> slots) {
        this.slots = slots;
    }

    public List<LocalDateTime> getSlots() {
        return slots;
    }
}