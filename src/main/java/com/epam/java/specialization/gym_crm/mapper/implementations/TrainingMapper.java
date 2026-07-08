package com.epam.java.specialization.gym_crm.mapper.implementations;

import com.epam.java.specialization.gym_crm.dto.TrainingCreateDto;
import com.epam.java.specialization.gym_crm.dto.TrainingResponseDto;
import com.epam.java.specialization.gym_crm.mapper.interfaces.ITraineeMapper;
import com.epam.java.specialization.gym_crm.mapper.interfaces.ITrainerMapper;
import com.epam.java.specialization.gym_crm.mapper.interfaces.ITrainingMapper;
import com.epam.java.specialization.gym_crm.model.Trainee;
import com.epam.java.specialization.gym_crm.model.Trainer;
import com.epam.java.specialization.gym_crm.model.Training;
import com.epam.java.specialization.gym_crm.model.TrainingType;
import org.springframework.stereotype.Component;

@Component
public class TrainingMapper implements ITrainingMapper {

    private final ITraineeMapper traineeMapper;
    private final ITrainerMapper trainerMapper;

    public TrainingMapper(ITraineeMapper traineeMapper, ITrainerMapper trainerMapper) {
        this.traineeMapper = traineeMapper;
        this.trainerMapper = trainerMapper;
    }

    @Override
    public Training toEntityFromCreate(TrainingCreateDto dto, Trainee trainee, Trainer trainer, TrainingType trainingType) {
        if (dto == null) {
            return null;
        }

        return Training.builder()
                .trainee(trainee)
                .trainer(trainer)
                .trainingType(trainingType)
                .trainingName(dto.getTrainingName())
                .trainingDate(dto.getTrainingDate())
                .trainingDuration(dto.getTrainingDuration())
                .build();
    }

    @Override
    public TrainingResponseDto toResponseDto(Training training, Trainee trainee, Trainer trainer, TrainingType trainingType) {
        if (training == null) {
            return null;
        }

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