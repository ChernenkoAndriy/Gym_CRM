package com.epam.java.specialization.trainer_workload.dto;

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