package com.epam.java.specialization.common.dto;

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