package com.server.schedule.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleJpaRepository extends JpaRepository<ScheduleJpaEntity, Integer> {
    List<ScheduleJpaEntity> findBySpecialistId(int specialistId);
    void deleteBySpecialistId(int specialistId);
}

