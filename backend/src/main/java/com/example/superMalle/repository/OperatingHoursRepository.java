package com.example.superMalle.repository;

import com.example.superMalle.entity.OperatingHours;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

public interface OperatingHoursRepository extends JpaRepository<OperatingHours, Long> {

    Optional<OperatingHours> findByDayOfWeek(DayOfWeek dayOfWeek);

    List<OperatingHours> findByIsActiveTrueAndIsClosedFalseOrderByDayOfWeek();
}
