package com.example.superMalle.controller.admin;

import com.example.superMalle.dto.admin.OperatingHoursRequest;
import com.example.superMalle.dto.admin.OperatingHoursResponse;
import com.example.superMalle.entity.OperatingHours;
import com.example.superMalle.exception.BadRequestException;
import com.example.superMalle.exception.ResourceNotFoundException;
import com.example.superMalle.repository.OperatingHoursRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/admin/operating-hours")
@RequiredArgsConstructor
public class AdminOperatingHoursController {

    private final OperatingHoursRepository operatingHoursRepository;

    @GetMapping
    public ResponseEntity<List<OperatingHoursResponse>> getAll() {
        List<OperatingHours> hours = operatingHoursRepository.findAll();
        return ResponseEntity.ok(hours.stream().map(this::toResponse).toList());
    }

    @GetMapping("/{dayOfWeek}")
    public ResponseEntity<OperatingHoursResponse> getByDay(@PathVariable String dayOfWeek) {
        OperatingHours hours = operatingHoursRepository.findByDayOfWeek(
                java.time.DayOfWeek.valueOf(dayOfWeek.toUpperCase()))
                .orElseThrow(() -> new ResourceNotFoundException("OperatingHours", "dayOfWeek", dayOfWeek));
        return ResponseEntity.ok(toResponse(hours));
    }

    @PutMapping("/{dayOfWeek}")
    public ResponseEntity<OperatingHoursResponse> update(@PathVariable String dayOfWeek,
                                                          @Valid @RequestBody OperatingHoursRequest request) {
        java.time.DayOfWeek day = java.time.DayOfWeek.valueOf(dayOfWeek.toUpperCase());
        OperatingHours hours = operatingHoursRepository.findByDayOfWeek(day)
                .orElse(OperatingHours.builder().dayOfWeek(day).build());

        hours.setOpenTime(LocalTime.parse(request.getOpenTime(), DateTimeFormatter.ISO_LOCAL_TIME));
        hours.setCloseTime(LocalTime.parse(request.getCloseTime(), DateTimeFormatter.ISO_LOCAL_TIME));
        if (request.getIsClosed() != null) {
            hours.setIsClosed(request.getIsClosed());
        }
        hours = operatingHoursRepository.save(hours);
        return ResponseEntity.ok(toResponse(hours));
    }

    private OperatingHoursResponse toResponse(OperatingHours hours) {
        return OperatingHoursResponse.builder()
                .id(hours.getId())
                .dayOfWeek(hours.getDayOfWeek())
                .openTime(hours.getOpenTime())
                .closeTime(hours.getCloseTime())
                .isClosed(hours.getIsClosed())
                .isActive(hours.getIsActive())
                .build();
    }
}
