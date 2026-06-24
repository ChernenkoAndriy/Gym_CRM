package com.epam.java.specialization.gym_crm.mapper.implementations;

import com.epam.java.specialization.gym_crm.dto.TrainerCreateDto;
import com.epam.java.specialization.gym_crm.dto.TrainerResponseDto;
import com.epam.java.specialization.gym_crm.dto.TrainerUpdateDto;
import com.epam.java.specialization.gym_crm.mapper.interfaces.ITrainerMapper;
import com.epam.java.specialization.gym_crm.model.Trainer;
import com.epam.java.specialization.gym_crm.model.TrainingType;
import org.springframework.stereotype.Component;

@Component
public class TrainerMapper implements ITrainerMapper {

    public Trainer toEntityFromCreate(TrainerCreateDto dto) {
        if (dto == null) return null;
        return Trainer.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .trainingTypeId(dto.getTrainingTypeId())
                .isActive(true)
                .build();
    }

    public Trainer toEntityFromUpdate(TrainerUpdateDto dto) {
        if (dto == null) return null;
        return Trainer.builder()
                .id(dto.getId())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .trainingTypeId(dto.getTrainingTypeId())
                .isActive(dto.getIsActive())
                .build();
    }

    public TrainerResponseDto toResponseDto(Trainer trainer, TrainingType trainingType) {
        if (trainer == null) return null;
        return TrainerResponseDto.builder()
                .id(trainer.getId())
                .firstName(trainer.getFirstName())
                .lastName(trainer.getLastName())
                .username(trainer.getUsername())
                .isActive(trainer.getIsActive())
                .specialization(trainingType)
                .build();
    }
}