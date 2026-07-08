package com.epam.java.specialization.gym_crm.facade;

import com.epam.java.specialization.gym_crm.dto.*;
import com.epam.java.specialization.gym_crm.service.interfaces.ITraineeService;
import com.epam.java.specialization.gym_crm.service.interfaces.ITrainerService;
import com.epam.java.specialization.gym_crm.service.interfaces.ITrainingService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class GymFacade {
    private final ITraineeService traineeService;
    private final ITrainerService trainerService;
    private final ITrainingService trainingService;
    private final Validator validator;

    public GymFacade(ITraineeService traineeService,
                     ITrainerService trainerService,
                     ITrainingService trainingService,
                     Validator validator) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
        this.validator = validator;
    }

    private <T> void validateDto(T dto) {
        Set<ConstraintViolation<T>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }

    private void validateAuth(String username, String password) {
        if (!traineeService.authenticate(username, password) && !trainerService.authenticate(username, password)) {
            throw new SecurityException("Access Denied: Invalid username or password.");
        }
    }

    @Transactional
    public TraineeResponseDto createTrainee(TraineeCreateDto dto) {
        validateDto(dto);
        return traineeService.create(dto);
    }

    @Transactional
    public TrainerResponseDto createTrainer(TrainerCreateDto dto) {
        validateDto(dto);
        return trainerService.create(dto);
    }

    @Transactional
    public TraineeResponseDto updateTrainee(TraineeUpdateDto dto, String authUsername, String authPassword) {
        validateAuth(authUsername, authPassword);
        validateDto(dto);
        return traineeService.update(dto);
    }

    @Transactional
    public TrainerResponseDto updateTrainer(TrainerUpdateDto dto, String authUsername, String authPassword) {
        validateAuth(authUsername, authPassword);
        validateDto(dto);
        return trainerService.update(dto);
    }

    @Transactional
    public void deleteTrainee(String targetUsername, String authUsername, String authPassword) {
        validateAuth(authUsername, authPassword);
        traineeService.deleteByUsername(targetUsername);
    }

    @Transactional(readOnly = true)
    public Optional<TraineeResponseDto> getTraineeByUsername(String targetUsername, String authUsername, String authPassword) {
        validateAuth(authUsername, authPassword);
        return traineeService.getByUsername(targetUsername);
    }

    @Transactional
    public TrainingResponseDto createTraining(TrainingCreateDto dto, String authUsername, String authPassword) {
        validateAuth(authUsername, authPassword);
        validateDto(dto);
        return trainingService.create(dto);
    }

    @Transactional(readOnly = true)
    public List<TrainingResponseDto> getTraineeTrainings(String targetUsername, Date fromDate, Date toDate, String trainerName, String trainingType, String authUsername, String authPassword) {
        validateAuth(authUsername, authPassword);
        return traineeService.getTrainingsByCriteria(targetUsername, fromDate, toDate, trainerName, trainingType, 1, 20);
    }

    @Transactional(readOnly = true)
    public List<TrainingResponseDto> getTrainerTrainings(String targetUsername, Date fromDate, Date toDate, String traineeName, String authUsername, String authPassword) {
        validateAuth(authUsername, authPassword);
        return trainerService.getTrainingsByCriteria(targetUsername, fromDate, toDate, traineeName, 1, 20);
    }

    @Transactional
    public void changeUserPassword(String targetUsername, String newPassword, String authUsername, String authPassword) {
        validateAuth(authUsername, authPassword);
        traineeService.changePassword(targetUsername, newPassword);
    }

    @Transactional
    public void toggleTraineeActivation(String targetUsername, boolean isActive, String authUsername, String authPassword) {
        validateAuth(authUsername, authPassword);
        traineeService.toggleActivation(targetUsername, isActive);
    }

    @Transactional
    public void toggleTrainerActivation(String targetUsername, boolean isActive, String authUsername, String authPassword) {
        validateAuth(authUsername, authPassword);
        trainerService.toggleActivation(targetUsername, isActive);
    }

    @Transactional(readOnly = true)
    public List<TrainerResponseDto> getAvailableTrainers(String traineeUsername, String authUsername, String authPassword) {
        validateAuth(authUsername, authPassword);
        return trainerService.getAvailableTrainersNotAssignedToTrainee(traineeUsername);
    }

    @Transactional
    public void updateTraineeTrainers(String traineeUsername, List<String> trainerUsernames, String authUsername, String authPassword) {
        validateAuth(authUsername, authPassword);
        traineeService.updateTrainersList(traineeUsername, trainerUsernames);
    }
}