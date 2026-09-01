package com.epam.java.specialization.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerWorkloadRequestDto implements Serializable {

    @NotBlank(message = "Trainer username is required")
    private String username;

    @NotBlank(message = "Trainer firstname is required")
    private String firstName;

    @NotBlank(message = "Trainer lastname is required")
    private String lastName;

    @NotNull(message = "Trainer active status is required")
    private Boolean isActive;

    @NotNull(message = "Training date is required")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "UTC")
    private Date trainingDate;

    @NotNull(message = "Training duration is required")
    @Min(value = 1, message = "Training duration must be at least 1 minute")
    private Integer trainingDuration;

    @NotNull(message = "Action type is required")
    private ActionType actionType;

    public String getTrainerUsername() {
        return username;
    }
}