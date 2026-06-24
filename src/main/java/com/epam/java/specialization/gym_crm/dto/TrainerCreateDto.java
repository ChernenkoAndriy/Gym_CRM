package com.epam.java.specialization.gym_crm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerCreateDto {
    private String firstName;
    private String lastName;
    private Long trainingTypeId;
}