package com.epam.java.specialization.gym_crm.mapper.implementations;

import com.epam.java.specialization.gym_crm.dto.TrainerCreateDto;
import com.epam.java.specialization.gym_crm.dto.TrainerResponseDto;
import com.epam.java.specialization.gym_crm.dto.TrainerUpdateDto;
import com.epam.java.specialization.gym_crm.mapper.interfaces.ITrainerMapper;
import com.epam.java.specialization.gym_crm.model.Trainer;
import com.epam.java.specialization.gym_crm.model.TrainingType;
import com.epam.java.specialization.gym_crm.model.User;
import org.springframework.stereotype.Component;

@Component
public class TrainerMapper implements ITrainerMapper {

    @Override
    public Trainer toEntityFromCreate(TrainerCreateDto dto) {
        if (dto == null) {
            return null;
        }

        User user = User.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .isActive(true)
                .build();

        return Trainer.builder()
                .user(user)
                .build();
    }

    @Override
    public Trainer toEntityFromUpdate(TrainerUpdateDto dto) {
        if (dto == null) {
            return null;
        }

        User user = User.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .isActive(dto.getIsActive())
                .build();

        return Trainer.builder()
                .id(dto.getId())
                .user(user)
                .build();
    }

    @Override
    public TrainerResponseDto toResponseDto(Trainer trainer, TrainingType trainingType) {
        if (trainer == null) {
            return null;
        }

        User user = trainer.getUser();

        return TrainerResponseDto.builder()
                .id(trainer.getId())
                .firstName(user != null ? user.getFirstName() : null)
                .lastName(user != null ? user.getLastName() : null)
                .username(user != null ? user.getUsername() : null)
                .isActive(user != null ? user.getIsActive() : null)
                .specialization(trainingType)
                .build();
    }
}