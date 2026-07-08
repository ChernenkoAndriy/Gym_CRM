package com.epam.java.specialization.gym_crm.service.interfaces;

import com.epam.java.specialization.gym_crm.dto.TraineeCreateDto;
import com.epam.java.specialization.gym_crm.dto.TraineeResponseDto;
import com.epam.java.specialization.gym_crm.dto.TraineeUpdateDto;
import com.epam.java.specialization.gym_crm.dto.TrainingResponseDto;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface ITraineeService {
    TraineeResponseDto create(TraineeCreateDto dto);
    TraineeResponseDto update(TraineeUpdateDto dto);
    void delete(Long id);
    Optional<TraineeResponseDto> getById(Long id);
    Optional<TraineeResponseDto> getByUsername(String username);
    void deleteByUsername(String username);
    void toggleActivation(String username, boolean isActive);
    List<TrainingResponseDto> getTrainingsByCriteria(String username, Date fromDate, Date toDate, String trainerName, String trainingType, int page, int size);
    void updateTrainersList(String traineeUsername, List<String> trainerUsernames);
    boolean authenticate(String username, String password);
    void changePassword(String username, String newPassword);
}