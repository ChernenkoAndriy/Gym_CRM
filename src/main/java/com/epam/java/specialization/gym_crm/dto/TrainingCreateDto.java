package com.epam.java.specialization.gym_crm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingCreateDto {
    private Long traineeId;
    private Long trainerId;
    private String trainingName;
    private Long trainingTypeId;
    private Date trainingDate;
    private Integer trainingDuration;
}