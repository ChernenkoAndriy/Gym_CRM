package com.epam.java.specialization.gym_crm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerUpdateDto {
    @NotNull(message = "Id is required")
    private Long id;
    @NotBlank(message = "First name is required")
    private String firstName;
    @NotBlank(message = "Last name is required")
    private String lastName;
    @NotNull(message = "Training type ID is required")
    private Long trainingTypeId;
    @NotNull(message = "Active status is required")
    private Boolean isActive;
}