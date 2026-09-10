package com.epam.java.specialization.trainer_workload.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YearWorkload {

    @Field("year_number")
    private int yearNumber;

    @Builder.Default
    @Field("months")
    private List<MonthWorkload> months = new ArrayList<>();
}