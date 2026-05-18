package com.example.superMalle.dto.menu;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptionGroupResponse {
    private Long id;
    private String name;
    private Boolean isRequired;
    private Integer maxSelections;
    private Integer sortOrder;
    private List<OptionResponse> options;
}
