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
public class YearWorkloadDto {
    private int year;
    private List<MonthWorkloadDto> months;
}