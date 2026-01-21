package com.server.schedule.domain;

import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ScheduleRepository {

    Optional<Schedule> findBySpecialistId(int specialistId);

    Schedule save(Schedule schedule);

    void deleteBySpecialistId(int specialistId);
}
