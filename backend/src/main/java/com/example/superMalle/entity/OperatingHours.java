package com.example.superMalle.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "operating_hours", uniqueConstraints = {
    @UniqueConstraint(columnNames = "day_of_week")
})
public class OperatingHours {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 20)
    private DayOfWeek dayOfWeek;

    @Column(name = "open_time", nullable = false)
    private LocalTime openTime;

    @Column(name = "close_time", nullable = false)
    private LocalTime closeTime;

    @Builder.Default
    @Column(name = "is_closed", nullable = false)
    private Boolean isClosed = false;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Transient
    public boolean isOpenNow() {
        if (Boolean.TRUE.equals(isClosed) || !Boolean.TRUE.equals(isActive)) {
            return false;
        }
        LocalTime now = LocalTime.now();
        return !now.isBefore(openTime) && !now.isAfter(closeTime);
    }

    @PrePersist
    @PreUpdate
    public void validate() {
        if (openTime != null && closeTime != null && !openTime.isBefore(closeTime)) {
            throw new IllegalArgumentException("openTime must be before closeTime for " + dayOfWeek);
        }
    }
}
