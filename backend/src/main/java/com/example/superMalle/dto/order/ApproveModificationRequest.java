package com.example.superMalle.dto.order;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApproveModificationRequest {

    @NotNull(message = "Modification ID is required")
    private Long modificationId;

    @Size(max = 500, message = "Note cannot exceed 500 characters")
    private String note;
}
