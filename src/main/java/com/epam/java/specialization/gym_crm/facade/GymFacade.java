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
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Optional;
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

    public GymFacade(ITraineeService traineeService,
                     ITrainerService trainerService,
                     ITrainingService trainingService,
                     ITrainingTypeService trainingTypeService,
                     ITraineeMapper traineeMapper,
                     ITrainerMapper trainerMapper,
                     ITrainingMapper trainingMapper) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
        this.trainingTypeService = trainingTypeService;
        this.traineeMapper = traineeMapper;
        this.trainerMapper = trainerMapper;
        this.trainingMapper = trainingMapper;
    }

    private void validateAuth(String username, String password) {
        if (!traineeService.authenticate(username, password)) {
            throw new SecurityException("Access Denied: Invalid username or password.");
        }
    }

    public TraineeResponseDto createTrainee(TraineeCreateDto dto) {
        Trainee trainee = traineeMapper.toEntityFromCreate(dto);
        Trainee saved = traineeService.create(trainee);
        return traineeMapper.toResponseDto(saved);
    }

    public TrainerResponseDto createTrainer(TrainerCreateDto dto) {
        Trainer trainer = trainerMapper.toEntityFromCreate(dto);
        TrainingType trainingType = trainingTypeService.getById(dto.getTrainingTypeId())
                .orElseThrow(() -> new IllegalArgumentException("TrainingType not found"));
        trainer.setSpecialization(trainingType);
        Trainer saved = trainerService.create(trainer);
        return trainerMapper.toResponseDto(saved, trainingType);
    }

    public TraineeResponseDto updateTrainee(TraineeUpdateDto dto, String authUsername, String authPassword) {
        validateAuth(authUsername, authPassword);
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

    public TrainerResponseDto updateTrainer(TrainerUpdateDto dto, String authUsername, String authPassword) {
        validateAuth(authUsername, authPassword);
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

    public void deleteTrainee(String targetUsername, String authUsername, String authPassword) {
        validateAuth(authUsername, authPassword);
        traineeService.deleteByUsername(targetUsername);
    }

    public Optional<TraineeResponseDto> getTraineeByUsername(String targetUsername, String authUsername, String authPassword) {
        validateAuth(authUsername, authPassword);
        return traineeService.getByUsername(targetUsername).map(traineeMapper::toResponseDto);
    }

    public TrainingResponseDto createTraining(TrainingCreateDto dto, String authUsername, String authPassword) {
        validateAuth(authUsername, authPassword);
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

    public List<TrainingResponseDto> getTraineeTrainings(String targetUsername, Date fromDate, Date toDate, String trainerName, String trainingType, String authUsername, String authPassword) {
        validateAuth(authUsername, authPassword);
        return traineeService.getTrainingsByCriteria(targetUsername, fromDate, toDate, trainerName, trainingType)
                .stream()
                .map(t -> trainingMapper.toResponseDto(t, t.getTrainee(), t.getTrainer(), t.getTrainingType()))
                .collect(Collectors.toList());
    }

    public void changeUserPassword(String targetUsername, String newPassword, String authUsername, String authPassword) {
        validateAuth(authUsername, authPassword);
        traineeService.changePassword(targetUsername, newPassword);
    }

    public void toggleTraineeActivation(String targetUsername, boolean isActive, String authUsername, String authPassword) {
        validateAuth(authUsername, authPassword);
        traineeService.toggleActivation(targetUsername, isActive);
    }

    public List<TrainerResponseDto> getAvailableTrainers(String traineeUsername, String authUsername, String authPassword) {
        validateAuth(authUsername, authPassword);
        return trainerService.getAvailableTrainersNotAssignedToTrainee(traineeUsername)
                .stream()
                .map(t -> trainerMapper.toResponseDto(t, t.getSpecialization()))
                .collect(Collectors.toList());
    }

    public void updateTraineeTrainers(String traineeUsername, List<String> trainerUsernames, String authUsername, String authPassword) {
        validateAuth(authUsername, authPassword);
        traineeService.updateTrainersList(traineeUsername, trainerUsernames);
    }
}