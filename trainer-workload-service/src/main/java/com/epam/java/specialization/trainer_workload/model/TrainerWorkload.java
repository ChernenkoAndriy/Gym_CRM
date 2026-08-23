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
public class TrainerWorkload {
    private String username;
    private String firstName;
    private String lastName;
    private Boolean isActive;
    @Builder.Default
    private List<YearWorkload> years = new ArrayList<>();
}