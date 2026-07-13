package com.epam.java.specialization.gym_crm.service.implementations;

import com.epam.java.specialization.gym_crm.dto.*;
import com.epam.java.specialization.gym_crm.exception.EntityNotFoundException;
import com.epam.java.specialization.gym_crm.exception.UserAlreadyExistsException;
import com.epam.java.specialization.gym_crm.mapper.TrainerMapper;
import com.epam.java.specialization.gym_crm.model.Trainer;
import com.epam.java.specialization.gym_crm.model.TrainingType;
import com.epam.java.specialization.gym_crm.repository.TraineeRepository;
import com.epam.java.specialization.gym_crm.repository.TrainerRepository;
import com.epam.java.specialization.gym_crm.repository.TrainingTypeRepository;
import com.epam.java.specialization.gym_crm.service.interfaces.TrainerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class TrainerServiceImpl implements TrainerService {

    private final TrainerRepository trainerRepository;
    private final TraineeRepository traineeRepository;
    private final TrainingTypeRepository trainingTypeRepository;
    private final TrainerMapper trainerMapper;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    @Transactional
    public RegistrationResponseDto register(TrainerRegisterRequestDto request) {
        
        TrainingType specialization = trainingTypeRepository.findById(request.getSpecializationId())
                .orElseThrow(() -> new EntityNotFoundException("TrainingType not found with ID: " + request.getSpecializationId()));

        
        Trainer trainer = trainerMapper.toEntity(request);
        trainer.setSpecialization(specialization);

        
        String baseUsername = request.getFirstName() + "." + request.getLastName();
        String finalUsername = generateUniqueUsername(baseUsername);
        String generatedPassword = generateRandomPassword();

        
        if (traineeRepository.findByUserUsername(finalUsername).isPresent()) {
            throw new UserAlreadyExistsException("Cannot register trainer: Username already taken by a trainee.");
        }

        trainer.getUser().setUsername(finalUsername);
        trainer.getUser().setPassword(generatedPassword);

        
        trainerRepository.save(trainer);

        return new RegistrationResponseDto(finalUsername, generatedPassword);
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
        Trainer trainer = trainerRepository.findByUserUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found with username: " + username));

        
        if (trainer.getUser().getIsActive().equals(request.getIsActive())) {
            throw new IllegalStateException("Trainer profile active status is already " + request.getIsActive());
        }

        trainer.getUser().setIsActive(request.getIsActive());
        trainerRepository.save(trainer);
    }

    
    private String generateUniqueUsername(String baseUsername) {
        String candidate = baseUsername;
        int suffix = 1;

        
        while (trainerRepository.findByUserUsername(candidate).isPresent() ||
                traineeRepository.findByUserUsername(candidate).isPresent()) {
            candidate = baseUsername + suffix;
            suffix++;
        }
        return candidate;
    }

    
    private String generateRandomPassword() {
        StringBuilder sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }
}