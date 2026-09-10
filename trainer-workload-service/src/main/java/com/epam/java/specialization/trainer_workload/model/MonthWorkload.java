package com.epam.java.specialization.trainer_workload.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthWorkload {

    @Field("month_number")
    private int monthNumber;

    @Field("summary_duration")
    private int summaryDuration;
}