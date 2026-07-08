package com.epam.java.specialization.gym_crm.facade;

import com.epam.java.specialization.gym_crm.dto.*;
import com.epam.java.specialization.gym_crm.mapper.interfaces.ITraineeMapper;
import com.epam.java.specialization.gym_crm.mapper.interfaces.ITrainerMapper;
import com.epam.java.specialization.gym_crm.mapper.interfaces.ITrainingMapper;
import com.epam.java.specialization.gym_crm.model.*;
import com.epam.java.specialization.gym_crm.service.interfaces.ITraineeService;
import com.epam.java.specialization.gym_crm.service.interfaces.ITrainerService;
import com.epam.java.specialization.gym_crm.service.interfaces.ITrainingService;
import com.epam.java.specialization.gym_crm.service.interfaces.ITrainingTypeService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class GymFacade {

    private final ITraineeService traineeService;
    private final ITrainerService trainerService;
    private final ITrainingService trainingService;
    private final ITrainingTypeService trainingTypeService;
    private final ITraineeMapper traineeMapper;
    private final ITrainerMapper trainerMapper;
    private final ITrainingMapper trainingMapper;
    private final Validator validator;

    public GymFacade(ITraineeService traineeService,
                     ITrainerService trainerService,
                     ITrainingService trainingService,
                     ITrainingTypeService trainingTypeService,
                     ITraineeMapper traineeMapper,
                     ITrainerMapper trainerMapper,
                     ITrainingMapper trainingMapper,
                     Validator validator) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
        this.trainingTypeService = trainingTypeService;
        this.traineeMapper = traineeMapper;
        this.trainerMapper = trainerMapper;
        this.trainingMapper = trainingMapper;
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
        Trainee trainee = traineeMapper.toEntityFromCreate(dto);
        Trainee saved = traineeService.create(trainee);
        return traineeMapper.toResponseDto(saved);
    }

    @Transactional
    public TrainerResponseDto createTrainer(TrainerCreateDto dto) {
        validateDto(dto);
        Trainer trainer = trainerMapper.toEntityFromCreate(dto);
        TrainingType trainingType = trainingTypeService.getById(dto.getTrainingTypeId())
                .orElseThrow(() -> new IllegalArgumentException("TrainingType not found"));
        trainer.setSpecialization(trainingType);
        Trainer saved = trainerService.create(trainer);
        return trainerMapper.toResponseDto(saved, trainingType);
    }

    @Transactional
    public TraineeResponseDto updateTrainee(TraineeUpdateDto dto, String authUsername, String authPassword) {
        validateAuth(authUsername, authPassword);
        validateDto(dto);
        Trainee existing = traineeService.getById(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found"));
        Trainee updatedData = traineeMapper.toEntityFromUpdate(dto);
        existing.getUser().setFirstName(updatedData.getUser().getFirstName());
        existing.getUser().setLastName(updatedData.getUser().getLastName());
        existing.getUser().setIsActive(updatedData.getUser().getIsActive());
        existing.setDateOfBirth(updatedData.getDateOfBirth());
        existing.setAddress(updatedData.getAddress());
        Trainee saved = traineeService.update(existing);
        return traineeMapper.toResponseDto(saved);
    }

    @Transactional
    public TrainerResponseDto updateTrainer(TrainerUpdateDto dto, String authUsername, String authPassword) {
        validateAuth(authUsername, authPassword);
        validateDto(dto);
        Trainer existing = trainerService.getById(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found"));
        Trainer updatedData = trainerMapper.toEntityFromUpdate(dto);
        TrainingType trainingType = trainingTypeService.getById(dto.getTrainingTypeId())
                .orElseThrow(() -> new IllegalArgumentException("TrainingType not found"));
        existing.getUser().setFirstName(updatedData.getUser().getFirstName());
        existing.getUser().setLastName(updatedData.getUser().getLastName());
        existing.getUser().setIsActive(updatedData.getUser().getIsActive());
        existing.setSpecialization(trainingType);
        Trainer saved = trainerService.update(existing);
        return trainerMapper.toResponseDto(saved, trainingType);
    }

    @Transactional
    public void deleteTrainee(String targetUsername, String authUsername, String authPassword) {
        validateAuth(authUsername, authPassword);
        traineeService.deleteByUsername(targetUsername);
    }

    @Transactional(readOnly = true)
    public Optional<TraineeResponseDto> getTraineeByUsername(String targetUsername, String authUsername, String authPassword) {
        validateAuth(authUsername, authPassword);
        return traineeService.getByUsername(targetUsername).map(traineeMapper::toResponseDto);
    }

    @Transactional
    public TrainingResponseDto createTraining(TrainingCreateDto dto, String authUsername, String authPassword) {
        validateAuth(authUsername, authPassword);
        validateDto(dto);
        Trainee trainee = traineeService.getById(dto.getTraineeId())
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found"));
        Trainer trainer = trainerService.getById(dto.getTrainerId())
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found"));
        TrainingType trainingType = trainingTypeService.getById(dto.getTrainingTypeId())
                .orElseThrow(() -> new IllegalArgumentException("TrainingType not found"));
        Training training = trainingMapper.toEntityFromCreate(dto, trainee, trainer, trainingType);
        Training saved = trainingService.create(training);
        return trainingMapper.toResponseDto(saved, trainee, trainer, trainingType);
    }

    @Transactional(readOnly = true)
    public List<TrainingResponseDto> getTraineeTrainings(String targetUsername, Date fromDate, Date toDate, String trainerName, String trainingType, String authUsername, String authPassword) {
        validateAuth(authUsername, authPassword);
        return traineeService.getTrainingsByCriteria(targetUsername, fromDate, toDate, trainerName, trainingType, 1, 20)
                .stream()
                .map(t -> trainingMapper.toResponseDto(t, t.getTrainee(), t.getTrainer(), t.getTrainingType()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TrainingResponseDto> getTrainerTrainings(String targetUsername, Date fromDate, Date toDate, String traineeName, String authUsername, String authPassword) {
        validateAuth(authUsername, authPassword);
        return trainerService.getTrainingsByCriteria(targetUsername, fromDate, toDate, traineeName, 1, 20)
                .stream()
                .map(t -> trainingMapper.toResponseDto(t, t.getTrainee(), t.getTrainer(), t.getTrainingType()))
                .collect(Collectors.toList());
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
        return trainerService.getAvailableTrainersNotAssignedToTrainee(traineeUsername)
                .stream()
                .map(t -> trainerMapper.toResponseDto(t, t.getSpecialization()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateTraineeTrainers(String traineeUsername, List<String> trainerUsernames, String authUsername, String authPassword) {
        validateAuth(authUsername, authPassword);
        traineeService.updateTrainersList(traineeUsername, trainerUsernames);
    }
}