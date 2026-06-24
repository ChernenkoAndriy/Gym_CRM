package com.epam.java.specialization.gym_crm.mapper.interfaces;

import com.epam.java.specialization.gym_crm.dto.TrainerCreateDto;
import com.epam.java.specialization.gym_crm.dto.TrainerResponseDto;
import com.epam.java.specialization.gym_crm.dto.TrainerUpdateDto;
import com.epam.java.specialization.gym_crm.model.Trainer;
import com.epam.java.specialization.gym_crm.model.TrainingType;

public interface ITrainerMapper extends
        ICreateMapper<TrainerCreateDto, Trainer>,
        IUpdateMapper<TrainerUpdateDto, Trainer> {

    TrainerResponseDto toResponseDto(Trainer trainer, TrainingType trainingType);
}