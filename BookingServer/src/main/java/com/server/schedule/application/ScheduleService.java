package com.server.schedule.application;

import com.server.schedule.domain.Schedule;
import com.server.schedule.domain.TimeSlot;
import org.springframework.stereotype.Service;

@Service
public class ScheduleService {

    public boolean canBook(Schedule schedule, TimeSlot slot) {
        return schedule.getWorkingDays().stream()
                .anyMatch(day -> day.allows(slot));
    }
}
