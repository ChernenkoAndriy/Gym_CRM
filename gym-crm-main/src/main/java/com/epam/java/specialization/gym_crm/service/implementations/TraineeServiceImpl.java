package com.epam.java.specialization.gym_crm.service.implementations;

import com.epam.java.specialization.common.dto.*;
import com.epam.java.specialization.gym_crm.client.TrainerWorkloadClient;
import com.epam.java.specialization.gym_crm.dto.*;
import com.epam.java.specialization.gym_crm.exception.EntityNotFoundException;
import com.epam.java.specialization.gym_crm.mapper.TraineeMapper;
import com.epam.java.specialization.gym_crm.metrics.CrmMetrics;
import com.epam.java.specialization.gym_crm.model.Trainee;
import com.epam.java.specialization.gym_crm.model.Trainer;
import com.epam.java.specialization.gym_crm.model.Training;
import com.epam.java.specialization.gym_crm.repository.TraineeRepository;
import com.epam.java.specialization.gym_crm.repository.TrainerRepository;
import com.epam.java.specialization.gym_crm.security.JwtService;
import com.epam.java.specialization.gym_crm.service.interfaces.TraineeService;
import com.epam.java.specialization.gym_crm.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TraineeServiceImpl implements TraineeService {

    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final UserService userService;
    private final TraineeMapper traineeMapper;
    private final CrmMetrics crmMetrics;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final TrainerWorkloadClient trainerWorkloadClient;

    @Override
    @Transactional
    public RegistrationResponseDto register(TraineeRegisterRequestDto request) {
        log.info("Starting trainee registration for: {} {}", request.getFirstName(), request.getLastName());
        Trainee trainee = traineeMapper.toEntity(request);
        String rawPassword = userService.prepareUserCredentials(trainee.getUser());
        traineeRepository.save(trainee);
        crmMetrics.incrementTraineeRegistrations();

        String username = trainee.getUser().getUsername();
        log.info("Trainee successfully persisted with username: {}", username);

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        String token = jwtService.generateToken(userDetails);
        log.debug("JWT token generated for registered trainee: {}", username);

        return new RegistrationResponseDto(username, rawPassword, token);
    }

    @Override
    @Transactional(readOnly = true)
    public TraineeProfileResponseDto getProfile(String username) {
        log.info("Fetching profile for trainee: {}", username);
        Trainee trainee = traineeRepository.findByUserUsername(username)
                .orElseThrow(() -> {
                    log.warn("Trainee profile retrieval failed: Trainee not found with username: {}", username);
                    return new EntityNotFoundException("Trainee not found with username: " + username);
                });
        return traineeMapper.toProfileResponse(trainee);
    }

    @Override
    @Transactional
    public TraineeUpdateResponseDto updateProfile(String username, TraineeUpdateRequestDto request) {
        log.info("Updating profile for trainee: {}", username);
        Trainee trainee = traineeRepository.findByUserUsername(username)
                .orElseThrow(() -> {
                    log.warn("Trainee update failed: Trainee not found with username: {}", username);
                    return new EntityNotFoundException("Trainee not found with username: " + username);
                });

        traineeMapper.updateEntityFromDto(request, trainee);
        traineeRepository.save(trainee);
        log.info("Trainee profile successfully updated for username: {}", username);
        return traineeMapper.toUpdateResponse(trainee);
    }

    @Override
    @Transactional
    public void deleteProfile(String username) {
        log.info("Starting deletion process for trainee profile: {}", username);
        Trainee trainee = traineeRepository.findByUserUsername(username)
                .orElseThrow(() -> {
                    log.warn("Trainee deletion failed: Trainee not found with username: {}", username);
                    return new EntityNotFoundException("Trainee not found with username: " + username);
                });

        if (trainee.getTrainings() != null && !trainee.getTrainings().isEmpty()) {
            log.info("Initiating workload deletion notifications for {} associated training sessions", trainee.getTrainings().size());
            for (Training training : trainee.getTrainings()) {
                if (training.getTrainer() != null && training.getTrainer().getUser() != null) {
                    String trainerUsername = training.getTrainer().getUser().getUsername();
                    TrainerWorkloadRequestDto workloadRequest = TrainerWorkloadRequestDto.builder()
                            .username(trainerUsername)
                            .firstName(training.getTrainer().getUser().getFirstName())
                            .lastName(training.getTrainer().getUser().getLastName())
                            .isActive(training.getTrainer().getUser().getIsActive())
                            .trainingDate(training.getTrainingDate())
                            .trainingDuration(training.getTrainingDuration())
                            .actionType(ActionType.DELETE)
                            .build();

                    log.debug("Sending workload DELETE event to microservice for trainer: {}", trainerUsername);
                    trainerWorkloadClient.processWorkload(workloadRequest);
                    log.info("Successfully processed workload DELETE event for trainer: {}", trainerUsername);
                }
            }
        }

        traineeRepository.delete(trainee);
        log.info("Trainee profile and associations successfully removed for username: {}", username);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainerShortResponseDto> getUnassignedActiveTrainers(String username) {
        log.info("Fetching unassigned active trainers for trainee: {}", username);
        if (!traineeRepository.findByUserUsername(username).isPresent()) {
            log.warn("Unassigned trainers query failed: Trainee not found with username: {}", username);
            throw new EntityNotFoundException("Trainee not found with username: " + username);
        }
        List<Trainer> trainers = trainerRepository.findAvailableTrainersNotAssignedToTrainee(username);
        log.debug("Found {} unassigned active trainers for trainee: {}", trainers.size(), username);
        return traineeMapper.toTrainerShortResponseList(trainers);
    }

    @Override
    @Transactional
    public List<TrainerShortResponseDto> updateTrainersList(String username, List<TrainerUsernameRequestDto> request) {
        log.info("Updating trainer list for trainee: {}", username);
        Trainee trainee = traineeRepository.findByUserUsername(username)
                .orElseThrow(() -> {
                    log.warn("Updating trainer list failed: Trainee not found with username: {}", username);
                    return new EntityNotFoundException("Trainee not found with username: " + username);
                });

        if (request == null || request.isEmpty()) {
            log.debug("Clearing trainer list for trainee: {}", username);
            trainee.getTrainers().clear();
        } else {
            List<String> usernames = request.stream()
                    .map(TrainerUsernameRequestDto::getUsername)
                    .collect(Collectors.toList());
            List<Trainer> newTrainers = trainerRepository.findByUserUsernameIn(usernames);
            log.debug("Assigning {} trainers to trainee: {}", newTrainers.size(), username);
            trainee.setTrainers(newTrainers);
        }

        Trainee savedTrainee = traineeRepository.save(trainee);
        log.info("Trainer list updated successfully for trainee: {}", username);
        return traineeMapper.toTrainerShortResponseList(savedTrainee.getTrainers());
    }

    @Override
    @Transactional
    public void toggleActivation(String username, ActivationRequestDto request) {
        log.info("Toggling activation status for trainee: {} to isActive={}", username, request.getIsActive());
        userService.toggleActivation(username, request.getIsActive());
    }
}