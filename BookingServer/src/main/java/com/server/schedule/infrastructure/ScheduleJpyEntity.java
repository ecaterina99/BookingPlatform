
package com.server.schedule.infrastructure;

import com.server.schedule.domain.WorkingDay;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "schedule")
public class ScheduleJpyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, name = "specialist_id")
    private int specialistId;

    @Column(nullable = false, name = "day_of_week")
    private String workingDay;

    @Column(nullable = false, name = "start_time")
    private LocalDateTime startTime;
    @Column(nullable = false, name = "end_time")
    private LocalDateTime endTime;

}
