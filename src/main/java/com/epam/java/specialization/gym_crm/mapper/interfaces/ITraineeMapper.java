package com.epam.java.specialization.gym_crm.mapper.interfaces;

import com.epam.java.specialization.gym_crm.dto.TraineeCreateDto;
import com.epam.java.specialization.gym_crm.dto.TraineeResponseDto;
import com.epam.java.specialization.gym_crm.dto.TraineeUpdateDto;
import com.epam.java.specialization.gym_crm.model.Trainee;

public interface ITraineeMapper extends
        ICreateMapper<TraineeCreateDto, Trainee>,
        IUpdateMapper<TraineeUpdateDto, Trainee> {

    TraineeResponseDto toResponseDto(Trainee entity);
}