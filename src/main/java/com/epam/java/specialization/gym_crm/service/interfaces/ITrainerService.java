package com.epam.java.specialization.gym_crm.service.interfaces;

import com.epam.java.specialization.gym_crm.dto.TrainerCreateDto;
import com.epam.java.specialization.gym_crm.dto.TrainerResponseDto;
import com.epam.java.specialization.gym_crm.dto.TrainerUpdateDto;
import com.epam.java.specialization.gym_crm.dto.TrainingResponseDto;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface ITrainerService {
    TrainerResponseDto create(TrainerCreateDto dto);
    TrainerResponseDto update(TrainerUpdateDto dto);
    Optional<TrainerResponseDto> getById(Long id);
    Optional<TrainerResponseDto> getByUsername(String username);
    List<TrainingResponseDto> getTrainingsByCriteria(String username, Date fromDate, Date toDate, String traineeName, int page, int size);
    List<TrainerResponseDto> getAvailableTrainersNotAssignedToTrainee(String traineeUsername);
    boolean authenticate(String username, String password);
    void toggleActivation(String username, boolean isActive);
}