package com.epam.java.specialization.gym_crm.mapper.implementations;

import com.epam.java.specialization.gym_crm.dto.TraineeCreateDto;
import com.epam.java.specialization.gym_crm.dto.TraineeResponseDto;
import com.epam.java.specialization.gym_crm.dto.TraineeUpdateDto;
import com.epam.java.specialization.gym_crm.mapper.interfaces.ITraineeMapper;
import com.epam.java.specialization.gym_crm.model.Trainee;
import org.springframework.stereotype.Component;

@Component
public class TraineeMapper implements ITraineeMapper {

    public Trainee toEntityFromCreate(TraineeCreateDto dto) {
        if (dto == null) return null;
        return Trainee.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .dateOfBirth(dto.getDateOfBirth())
                .address(dto.getAddress())
                .isActive(true)
                .build();
    }

    public Trainee toEntityFromUpdate(TraineeUpdateDto dto) {
        if (dto == null) return null;
        return Trainee.builder()
                .id(dto.getId())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .dateOfBirth(dto.getDateOfBirth())
                .address(dto.getAddress())
                .isActive(dto.getIsActive())
                .build();
    }

    public TraineeResponseDto toResponseDto(Trainee entity) {
        if (entity == null) return null;
        return TraineeResponseDto.builder()
                .id(entity.getId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .username(entity.getUsername())
                .isActive(entity.getIsActive())
                .dateOfBirth(entity.getDateOfBirth())
                .address(entity.getAddress())
                .build();
    }
}