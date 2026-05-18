package com.example.superMalle.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SettingsRequest {
    
    @NotBlank(message = "Setting key is required")
    @Size(min = 1, max = 100, message = "Key must be between 1 and 100 characters")
    private String key;
    
    @NotBlank(message = "Setting value is required")
    @Size(max = 500, message = "Value must not exceed 500 characters")
    private String value;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
}
