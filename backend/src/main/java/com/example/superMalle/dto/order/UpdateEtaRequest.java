package com.example.superMalle.dto.order;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UpdateEtaRequest {
    @NotNull(message = "Estimated ready time is required")
    private LocalDateTime estimatedReadyAt;
}
