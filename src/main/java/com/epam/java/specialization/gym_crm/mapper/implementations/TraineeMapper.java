package com.epam.java.specialization.gym_crm.mapper.implementations;

import com.epam.java.specialization.gym_crm.dto.TraineeCreateDto;
import com.epam.java.specialization.gym_crm.dto.TraineeResponseDto;
import com.epam.java.specialization.gym_crm.dto.TraineeUpdateDto;
import com.epam.java.specialization.gym_crm.mapper.interfaces.ITraineeMapper;
import com.epam.java.specialization.gym_crm.model.Trainee;
import com.epam.java.specialization.gym_crm.model.User;
import org.springframework.stereotype.Component;

@Component
public class TraineeMapper implements ITraineeMapper {

    @Override
    public Trainee toEntityFromCreate(TraineeCreateDto dto) {
        if (dto == null) {
            return null;
        }

        User user = User.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .isActive(true)
                .build();

        return Trainee.builder()
                .user(user)
                .dateOfBirth(dto.getDateOfBirth())
                .address(dto.getAddress())
                .build();
    }

    @Override
    public Trainee toEntityFromUpdate(TraineeUpdateDto dto) {
        if (dto == null) {
            return null;
        }

        User user = User.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .isActive(dto.getIsActive())
                .build();

        return Trainee.builder()
                .id(dto.getId())
                .user(user)
                .dateOfBirth(dto.getDateOfBirth())
                .address(dto.getAddress())
                .build();
    }

    @Override
    public TraineeResponseDto toResponseDto(Trainee entity) {
        if (entity == null) {
            return null;
        }

        User user = entity.getUser();

        return TraineeResponseDto.builder()
                .id(entity.getId())
                .firstName(user != null ? user.getFirstName() : null)
                .lastName(user != null ? user.getLastName() : null)
                .username(user != null ? user.getUsername() : null)
                .isActive(user != null ? user.getIsActive() : null)
                .dateOfBirth(entity.getDateOfBirth())
                .address(entity.getAddress())
                .build();
    }
}