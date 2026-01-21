package com.server.schedule.domain;

import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Getter
public class WorkingDay {

    private final DayOfWeek dayOfWeek;
    private final LocalTime start;
    private final LocalTime end;

    public WorkingDay(DayOfWeek dayOfWeek, LocalTime start, LocalTime end) {
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        this.dayOfWeek = dayOfWeek;
        this.start = start;
        this.end = end;
    }

    public boolean allows(TimeSlot slot) {
        if (!slot.getStart().getDayOfWeek().equals(dayOfWeek)) {
            return false;
        }

        return !slot.getStart().toLocalTime().isBefore(start)
                && !slot.getEnd().toLocalTime().isAfter(end);
    }
}