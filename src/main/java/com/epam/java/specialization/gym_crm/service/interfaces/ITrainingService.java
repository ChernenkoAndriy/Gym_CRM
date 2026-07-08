package com.epam.java.specialization.gym_crm.service.interfaces;

import com.epam.java.specialization.gym_crm.dto.TrainingCreateDto;
import com.epam.java.specialization.gym_crm.dto.TrainingResponseDto;
import java.util.Optional;

public interface ITrainingService {
    TrainingResponseDto create(TrainingCreateDto dto);
    Optional<TrainingResponseDto> getById(Long id);
}