package com.epam.java.specialization.trainer_workload.dto;

import com.epam.java.specialization.trainer_workload.model.ActionType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerWorkloadRequestDto {

    @NotBlank(message = "Trainer username is required")
    private String username;

    @NotBlank(message = "Trainer first name is required")
    private String firstName;

    @NotBlank(message = "Trainer last name is required")
    private String lastName;

    @NotNull(message = "Trainer status (isActive) is required")
    private Boolean isActive;

    @NotNull(message = "Training date is required")
    @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
    private Date trainingDate;

    @NotNull(message = "Training duration is required")
    @Positive(message = "Training duration must be positive")
    private Integer trainingDuration;

    @NotNull(message = "Action type (ADD/DELETE) is required")
    private ActionType actionType;
}