package com.epam.java.specialization.gym_crm.facade;

import com.epam.java.specialization.gym_crm.dto.TraineeCreateDto;
import com.epam.java.specialization.gym_crm.dto.TraineeResponseDto;
import com.epam.java.specialization.gym_crm.dto.TraineeUpdateDto;
import com.epam.java.specialization.gym_crm.dto.TrainerCreateDto;
import com.epam.java.specialization.gym_crm.dto.TrainerResponseDto;
import com.epam.java.specialization.gym_crm.dto.TrainerUpdateDto;
import com.epam.java.specialization.gym_crm.dto.TrainingCreateDto;
import com.epam.java.specialization.gym_crm.dto.TrainingResponseDto;
import com.epam.java.specialization.gym_crm.mapper.interfaces.ITraineeMapper;
import com.epam.java.specialization.gym_crm.mapper.interfaces.ITrainerMapper;
import com.epam.java.specialization.gym_crm.mapper.interfaces.ITrainingMapper;
import com.epam.java.specialization.gym_crm.model.Trainee;
import com.epam.java.specialization.gym_crm.model.Trainer;
import com.epam.java.specialization.gym_crm.model.Training;
import com.epam.java.specialization.gym_crm.model.TrainingType;
import com.epam.java.specialization.gym_crm.service.interfaces.ITraineeService;
import com.epam.java.specialization.gym_crm.service.interfaces.ITrainerService;
import com.epam.java.specialization.gym_crm.service.interfaces.ITrainingService;
import com.epam.java.specialization.gym_crm.service.interfaces.ITrainingTypeService;
import org.springframework.stereotype.Component;

import java.util.Optional;

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

    public TraineeResponseDto createTrainee(TraineeCreateDto dto) {
        Trainee trainee = traineeMapper.toEntityFromCreate(dto);
        Trainee saved = traineeService.create(trainee);
        return traineeMapper.toResponseDto(saved);
    }

    public TraineeResponseDto updateTrainee(TraineeUpdateDto dto) {
        Trainee trainee = traineeMapper.toEntityFromUpdate(dto);
        Trainee updated = traineeService.update(trainee);
        return traineeMapper.toResponseDto(updated);
    }

    public void deleteTrainee(Long id) {
        traineeService.delete(id);
    }

    public Optional<TraineeResponseDto> getTrainee(Long id) {
        return traineeService.getById(id).map(traineeMapper::toResponseDto);
    }

    public TrainerResponseDto createTrainer(TrainerCreateDto dto) {
        Trainer trainer = trainerMapper.toEntityFromCreate(dto);
        Trainer saved = trainerService.create(trainer);
        return getTrainerResponseWithRelations(saved);
    }

    public TrainerResponseDto updateTrainer(TrainerUpdateDto dto) {
        Trainer trainer = trainerMapper.toEntityFromUpdate(dto);
        Trainer updated = trainerService.update(trainer);
        return getTrainerResponseWithRelations(updated);
    }

    public Optional<TrainerResponseDto> getTrainer(Long id) {
        return trainerService.getById(id).map(this::getTrainerResponseWithRelations);
    }

    public TrainingResponseDto createTraining(TrainingCreateDto dto) {
        Training training = trainingMapper.toEntityFromCreate(dto);
        Training saved = trainingService.create(training);
        return assembleTrainingResponse(saved);
    }

    public Optional<TrainingResponseDto> getTraining(Long id) {
        return trainingService.getById(id).map(this::assembleTrainingResponse);
    }

    private TrainingResponseDto assembleTrainingResponse(Training training) {
        Trainee trainee = traineeService.getById(training.getTraineeId()).orElse(null);
        Trainer trainer = trainerService.getById(training.getTrainerId()).orElse(null);
        TrainingType trainingType = trainingTypeService.getById(training.getTrainingTypeId()).orElse(null);

        return trainingMapper.toResponseDto(training, trainee, trainer, trainingType);
    }

    private TrainerResponseDto getTrainerResponseWithRelations(Trainer trainer) {
        if (trainer == null) return null;
        TrainingType trainingType = trainingTypeService.getById(trainer.getTrainingTypeId()).orElse(null);
        return trainerMapper.toResponseDto(trainer, trainingType);
    }
}