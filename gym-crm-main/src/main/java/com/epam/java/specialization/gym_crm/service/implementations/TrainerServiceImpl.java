package com.epam.java.specialization.gym_crm.service.implementations;

import com.epam.java.specialization.gym_crm.client.TrainerWorkloadClient;
import com.epam.java.specialization.gym_crm.dto.*;
import com.epam.java.specialization.gym_crm.dto.external.TrainerWorkloadResponseDto;
import com.epam.java.specialization.gym_crm.exception.EntityNotFoundException;
import com.epam.java.specialization.gym_crm.mapper.TrainerMapper;
import com.epam.java.specialization.gym_crm.metrics.CrmMetrics;
import com.epam.java.specialization.gym_crm.model.Trainer;
import com.epam.java.specialization.gym_crm.model.TrainingType;
import com.epam.java.specialization.gym_crm.repository.TrainerRepository;
import com.epam.java.specialization.gym_crm.repository.TrainingTypeRepository;
import com.epam.java.specialization.gym_crm.security.JwtService;
import com.epam.java.specialization.gym_crm.service.interfaces.TrainerService;
import com.epam.java.specialization.gym_crm.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainerServiceImpl implements TrainerService {

    private final TrainerRepository trainerRepository;
    private final TrainingTypeRepository trainingTypeRepository;
    private final UserService userService;
    private final TrainerMapper trainerMapper;
    private final CrmMetrics crmMetrics;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final TrainerWorkloadClient trainerWorkloadClient;

    @Override
    @Transactional
    public RegistrationResponseDto register(TrainerRegisterRequestDto request) {
        log.info("Starting trainer registration for: {} {}, specialization ID: {}",
                request.getFirstName(), request.getLastName(), request.getSpecializationId());

        TrainingType specialization = trainingTypeRepository.findById(request.getSpecializationId())
                .orElseThrow(() -> {
                    log.warn("Trainer registration failed: TrainingType not found with ID: {}", request.getSpecializationId());
                    return new EntityNotFoundException("TrainingType not found with ID: " + request.getSpecializationId());
                });

        Trainer trainer = trainerMapper.toEntity(request);
        trainer.setSpecialization(specialization);
        String rawPassword = userService.prepareUserCredentials(trainer.getUser());
        trainerRepository.save(trainer);
        crmMetrics.incrementTrainerRegistrations();

        String username = trainer.getUser().getUsername();
        log.info("Trainer successfully persisted with username: {}", username);

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        String token = jwtService.generateToken(userDetails);
        log.debug("JWT token generated for registered trainer: {}", username);

        return new RegistrationResponseDto(username, rawPassword, token);
    }

    @Override
    @Transactional(readOnly = true)
    public TrainerProfileResponseDto getProfile(String username) {
        log.info("Fetching profile for trainer: {}", username);
        Trainer trainer = trainerRepository.findByUserUsername(username)
                .orElseThrow(() -> {
                    log.warn("Trainer profile retrieval failed: Trainer not found with username: {}", username);
                    return new EntityNotFoundException("Trainer not found with username: " + username);
                });
        return trainerMapper.toProfileResponse(trainer);
    }

    @Override
    @Transactional
    public TrainerUpdateResponseDto updateProfile(String username, TrainerUpdateRequestDto request) {
        log.info("Updating profile for trainer: {}", username);
        Trainer trainer = trainerRepository.findByUserUsername(username)
                .orElseThrow(() -> {
                    log.warn("Trainer update failed: Trainer not found with username: {}", username);
                    return new EntityNotFoundException("Trainer not found with username: " + username);
                });

        trainerMapper.updateEntityFromDto(request, trainer);
        trainerRepository.save(trainer);
        log.info("Trainer profile successfully updated for username: {}", username);
        return trainerMapper.toUpdateResponse(trainer);
    }

    @Override
    @Transactional
    public void toggleActivation(String username, ActivationRequestDto request) {
        log.info("Toggling activation status for trainer: {} to isActive={}", username, request.getIsActive());
        userService.toggleActivation(username, request.getIsActive());
    }

    @Override
    @Transactional(readOnly = true)
    public TrainerWorkloadResponseDto getWorkloadSummary(String username, Integer year, Integer month) {
        log.info("Requesting workload summary from microservice for trainer: {} (year: {}, month: {})", username, year, month);
        if (!trainerRepository.findByUserUsername(username).isPresent()) {
            log.warn("Workload request rejected: Trainer not found with username: {}", username);
            throw new EntityNotFoundException("Trainer not found with username: " + username);
        }
        ResponseEntity<TrainerWorkloadResponseDto> response = trainerWorkloadClient.getTrainerWorkload(username, year, month);
        return response != null ? response.getBody() : null;
    }
}