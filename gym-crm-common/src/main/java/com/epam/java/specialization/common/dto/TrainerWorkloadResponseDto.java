package com.epam.java.specialization.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerWorkloadResponseDto {
    private String username;
    private String firstName;
    private String lastName;
    private Boolean status;
    private List<YearWorkloadDto> years;
}