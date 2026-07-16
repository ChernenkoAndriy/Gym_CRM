package com.epam.java.specialization.gym_crm.service.implementations;

import com.epam.java.specialization.gym_crm.dto.*;
import com.epam.java.specialization.gym_crm.exception.EntityNotFoundException;
import com.epam.java.specialization.gym_crm.mapper.TrainerMapper;
import com.epam.java.specialization.gym_crm.model.Trainer;
import com.epam.java.specialization.gym_crm.model.TrainingType;
import com.epam.java.specialization.gym_crm.repository.TrainerRepository;
import com.epam.java.specialization.gym_crm.repository.TrainingTypeRepository;
import com.epam.java.specialization.gym_crm.service.interfaces.TrainerService;
import com.epam.java.specialization.gym_crm.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TrainerServiceImpl implements TrainerService {
    private final TrainerRepository trainerRepository;
    private final TrainingTypeRepository trainingTypeRepository;
    private final UserService userService; 
    private final TrainerMapper trainerMapper;

    @Override
    @Transactional
    public RegistrationResponseDto register(TrainerRegisterRequestDto request) {
        TrainingType specialization = trainingTypeRepository.findById(request.getSpecializationId())
                .orElseThrow(() -> new EntityNotFoundException("TrainingType not found with ID: " + request.getSpecializationId()));

        Trainer trainer = trainerMapper.toEntity(request);
        trainer.setSpecialization(specialization);

        userService.prepareUserCredentials(trainer.getUser());

        trainerRepository.save(trainer);
        return new RegistrationResponseDto(trainer.getUser().getUsername(), trainer.getUser().getPassword());
    }

    @Override
    @Transactional(readOnly = true)
    public TrainerProfileResponseDto getProfile(String username) {
        Trainer trainer = trainerRepository.findByUserUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found with username: " + username));
        return trainerMapper.toProfileResponse(trainer);
    }

    @Override
    @Transactional
    public TrainerUpdateResponseDto updateProfile(String username, TrainerUpdateRequestDto request) {
        Trainer trainer = trainerRepository.findByUserUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found with username: " + username));
        trainerMapper.updateEntityFromDto(request, trainer);
        trainerRepository.save(trainer);
        return trainerMapper.toUpdateResponse(trainer);
    }

    @Override
    @Transactional
    public void toggleActivation(String username, ActivationRequestDto request) {
        userService.toggleActivation(username, request.getIsActive());
    }
}