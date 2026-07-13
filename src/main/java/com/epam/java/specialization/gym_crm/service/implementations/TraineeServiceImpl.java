package com.epam.java.specialization.gym_crm.service.implementations;

import com.epam.java.specialization.gym_crm.dto.*;
import com.epam.java.specialization.gym_crm.exception.EntityNotFoundException;
import com.epam.java.specialization.gym_crm.exception.UserAlreadyExistsException;
import com.epam.java.specialization.gym_crm.mapper.TraineeMapper;
import com.epam.java.specialization.gym_crm.model.Trainee;
import com.epam.java.specialization.gym_crm.model.Trainer;
import com.epam.java.specialization.gym_crm.repository.TraineeRepository;
import com.epam.java.specialization.gym_crm.repository.TrainerRepository;
import com.epam.java.specialization.gym_crm.service.interfaces.TraineeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TraineeServiceImpl implements TraineeService {

    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final TraineeMapper traineeMapper;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    @Transactional
    public RegistrationResponseDto register(TraineeRegisterRequestDto request) {
        
        Trainee trainee = traineeMapper.toEntity(request);

        
        String baseUsername = request.getFirstName() + "." + request.getLastName();
        String finalUsername = generateUniqueUsername(baseUsername);
        String generatedPassword = generateRandomPassword();

        
        if (trainerRepository.findByUserUsername(finalUsername).isPresent()) {
            throw new UserAlreadyExistsException("Cannot register trainee: Username already taken by a trainer.");
        }

        trainee.getUser().setUsername(finalUsername);
        trainee.getUser().setPassword(generatedPassword);

        
        traineeRepository.save(trainee);

        return new RegistrationResponseDto(finalUsername, generatedPassword);
    }

    @Override
    @Transactional(readOnly = true)
    public TraineeProfileResponseDto getProfile(String username) {
        Trainee trainee = traineeRepository.findByUserUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found with username: " + username));
        return traineeMapper.toProfileResponse(trainee);
    }

    @Override
    @Transactional
    public TraineeUpdateResponseDto updateProfile(String username, TraineeUpdateRequestDto request) {
        Trainee trainee = traineeRepository.findByUserUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found with username: " + username));

        
        traineeMapper.updateEntityFromDto(request, trainee);

        traineeRepository.save(trainee);
        return traineeMapper.toUpdateResponse(trainee);
    }

    @Override
    @Transactional
    public void deleteProfile(String username) {
        Trainee trainee = traineeRepository.findByUserUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found with username: " + username));

        
        if (trainee.getTrainers() != null) {
            trainee.getTrainers().clear();
        }

        
        
        traineeRepository.delete(trainee);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainerShortResponseDto> getUnassignedActiveTrainers(String username) {
        
        if (!traineeRepository.findByUserUsername(username).isPresent()) {
            throw new EntityNotFoundException("Trainee not found with username: " + username);
        }

        
        List<Trainer> trainers = trainerRepository.findAvailableTrainersNotAssignedToTrainee(username);
        return traineeMapper.toTrainerShortResponseList(trainers);
    }

    @Override
    @Transactional
    public List<TrainerShortResponseDto> updateTrainersList(String username, List<TrainerUsernameRequestDto> request) {
        Trainee trainee = traineeRepository.findByUserUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found with username: " + username));

        if (request == null || request.isEmpty()) {
            trainee.getTrainers().clear();
        } else {
            
            List<String> usernames = request.stream()
                    .map(TrainerUsernameRequestDto::getUsername)
                    .collect(Collectors.toList());

            
            List<Trainer> newTrainers = trainerRepository.findByUserUsernameIn(usernames);

            
            trainee.setTrainers(newTrainers);
        }

        Trainee savedTrainee = traineeRepository.save(trainee);
        return traineeMapper.toTrainerShortResponseList(savedTrainee.getTrainers());
    }

    @Override
    @Transactional
    public void toggleActivation(String username, ActivationRequestDto request) {
        Trainee trainee = traineeRepository.findByUserUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found with username: " + username));

        
        if (trainee.getUser().getIsActive().equals(request.getIsActive())) {
            throw new IllegalStateException("Trainee profile active status is already " + request.getIsActive());
        }

        trainee.getUser().setIsActive(request.getIsActive());
        traineeRepository.save(trainee);
    }

    
    private String generateUniqueUsername(String baseUsername) {
        String candidate = baseUsername;
        int suffix = 1;

        while (traineeRepository.findByUserUsername(candidate).isPresent() ||
                trainerRepository.findByUserUsername(candidate).isPresent()) {
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