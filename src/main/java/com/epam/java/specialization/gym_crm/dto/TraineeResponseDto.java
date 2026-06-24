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
public class TraineeResponseDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    private Boolean isActive;
    private Date dateOfBirth;
    private String address;
}
