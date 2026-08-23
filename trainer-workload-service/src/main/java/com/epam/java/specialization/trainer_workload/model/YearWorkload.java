package com.epam.java.specialization.trainer_workload.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YearWorkload {
    private int yearNumber;
    @Builder.Default
    private List<MonthWorkload> months = new ArrayList<>();
}