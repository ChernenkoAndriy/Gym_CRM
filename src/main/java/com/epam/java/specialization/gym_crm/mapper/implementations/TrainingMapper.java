package com.epam.java.specialization.gym_crm.mapper.implementations;

import com.epam.java.specialization.gym_crm.dto.TrainingCreateDto;
import com.epam.java.specialization.gym_crm.dto.TrainingResponseDto;
import com.epam.java.specialization.gym_crm.mapper.interfaces.ITrainingMapper;
import com.epam.java.specialization.gym_crm.model.Trainee;
import com.epam.java.specialization.gym_crm.model.Trainer;
import com.epam.java.specialization.gym_crm.model.Training;
import com.epam.java.specialization.gym_crm.model.TrainingType;
import org.springframework.stereotype.Component;

@Component
public class TrainingMapper implements ITrainingMapper {

    private final TraineeMapper traineeMapper;
    private final TrainerMapper trainerMapper;

    public TrainingMapper(TraineeMapper traineeMapper, TrainerMapper trainerMapper) {
        this.traineeMapper = traineeMapper;
        this.trainerMapper = trainerMapper;
    }

    public Training toEntityFromCreate(TrainingCreateDto dto) {
        if (dto == null) return null;
        return Training.builder()
                .traineeId(dto.getTraineeId())
                .trainerId(dto.getTrainerId())
                .trainingName(dto.getTrainingName())
                .trainingTypeId(dto.getTrainingTypeId())
                .trainingDate(dto.getTrainingDate())
                .trainingDuration(dto.getTrainingDuration())
                .build();
    }

    public TrainingResponseDto toResponseDto(Training training, Trainee trainee, Trainer trainer, TrainingType trainingType) {
        if (training == null) return null;

        return TrainingResponseDto.builder()
                .id(training.getId())
                .trainingName(training.getTrainingName())
                .trainingDate(training.getTrainingDate())
                .trainingDuration(training.getTrainingDuration())
                .trainee(traineeMapper.toResponseDto(trainee))
                .trainer(trainerMapper.toResponseDto(trainer, trainingType))
                .trainingType(trainingType)
                .build();
    }
}