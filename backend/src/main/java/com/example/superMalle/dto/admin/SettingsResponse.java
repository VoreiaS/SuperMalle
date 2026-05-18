package com.example.superMalle.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettingsResponse {
    private Long id;
    private String key;
    private String value;
    private String description;
    private LocalDateTime updatedAt;
}
