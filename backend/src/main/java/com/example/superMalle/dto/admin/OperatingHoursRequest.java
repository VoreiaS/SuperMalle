package com.example.superMalle.dto.admin;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.DayOfWeek;
import java.time.LocalTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OperatingHoursRequest {
    @NotNull
    private DayOfWeek dayOfWeek;

    @NotNull
    private String openTime;

    @NotNull
    private String closeTime;

    private Boolean isClosed;
}
