package com.epam.java.specialization.gym_crm.dto;

import com.epam.java.specialization.gym_crm.model.TrainingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingResponseDto {
    private Long id;
    private String trainingName;
    private Date trainingDate;
    private Integer trainingDuration;
    private TraineeResponseDto trainee;
    private TrainerResponseDto trainer;
    private TrainingType trainingType;
}