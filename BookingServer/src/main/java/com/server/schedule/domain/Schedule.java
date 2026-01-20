package com.server.schedule.domain;

import java.util.List;

public class Schedule {

    private final int id;
    private final int specialistId;
    private final List<WorkingDay> workingDays;

    public Schedule(int id, int specialistId, List<WorkingDay> workingDays) {
        this.id = id;
        this.specialistId = specialistId;
        this.workingDays = List.copyOf(workingDays);
    }

    public boolean isAvailable(TimeSlot slot) {
        return workingDays.stream().anyMatch(day -> day.allows(slot));
    }
}
