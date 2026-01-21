package com.server.schedule.infrastructure;

import com.server.schedule.domain.Schedule;
import com.server.schedule.domain.ScheduleRepository;

import java.util.Optional;

public class JpaScheduleRepository implements ScheduleRepository {
    @Override
    public Optional<Schedule> findBySpecialistId(int specialistId) {
        return Optional.empty();
    }

    @Override
    public Schedule save(Schedule schedule) {
        return null;
    }

    @Override
    public void deleteBySpecialistId(int specialistId) {

    }
}
