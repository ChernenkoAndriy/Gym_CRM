package com.epam.java.specialization.trainer_workload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthWorkloadDto {
    private int month;
    private int trainingSummaryDuration;
}