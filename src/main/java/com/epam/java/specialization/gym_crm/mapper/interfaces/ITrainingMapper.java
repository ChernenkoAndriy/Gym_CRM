package com.epam.java.specialization.gym_crm.mapper.interfaces;

import com.epam.java.specialization.gym_crm.dto.TrainingCreateDto;
import com.epam.java.specialization.gym_crm.dto.TrainingResponseDto;
import com.epam.java.specialization.gym_crm.model.Trainee;
import com.epam.java.specialization.gym_crm.model.Trainer;
import com.epam.java.specialization.gym_crm.model.Training;
import com.epam.java.specialization.gym_crm.model.TrainingType;

public interface ITrainingMapper {

    Training toEntityFromCreate(TrainingCreateDto dto, Trainee trainee, Trainer trainer, TrainingType trainingType);

    TrainingResponseDto toResponseDto(Training training, Trainee trainee, Trainer trainer, TrainingType trainingType);
}