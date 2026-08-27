package com.epam.java.specialization.gym_crm.service.implementations;

import com.epam.java.specialization.common.dto.*;
import com.epam.java.specialization.gym_crm.client.TrainerWorkloadClient;
import com.epam.java.specialization.gym_crm.dto.TraineeTrainingResponseDto;
import com.epam.java.specialization.gym_crm.dto.TrainerTrainingResponseDto;
import com.epam.java.specialization.gym_crm.dto.TrainingAddRequestDto;
import com.epam.java.specialization.gym_crm.dto.TrainingTypeResponseDto;
import com.epam.java.specialization.gym_crm.exception.EntityNotFoundException;
import com.epam.java.specialization.gym_crm.exception.InactiveUserException;
import com.epam.java.specialization.gym_crm.mapper.TrainingMapper;
import com.epam.java.specialization.gym_crm.metrics.CrmMetrics;
import com.epam.java.specialization.gym_crm.model.Trainee;
import com.epam.java.specialization.gym_crm.model.Trainer;
import com.epam.java.specialization.gym_crm.model.Training;
import com.epam.java.specialization.gym_crm.repository.TraineeRepository;
import com.epam.java.specialization.gym_crm.repository.TrainerRepository;
import com.epam.java.specialization.gym_crm.repository.TrainingRepository;
import com.epam.java.specialization.gym_crm.repository.TrainingTypeRepository;
import com.epam.java.specialization.gym_crm.repository.specification.TraineeSpecifications;
import com.epam.java.specialization.gym_crm.repository.specification.TrainerSpecifications;
import com.epam.java.specialization.gym_crm.service.interfaces.TrainingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainingServiceImpl implements TrainingService {

    private final TrainingRepository trainingRepository;
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final TrainingTypeRepository trainingTypeRepository;
    private final TrainingMapper trainingMapper;
    private final CrmMetrics crmMetrics;
    private final TrainerWorkloadClient trainerWorkloadClient;

    @Override
    @Transactional(readOnly = true)
    public List<TraineeTrainingResponseDto> getTraineeTrainings(
            String username, Date periodFrom, Date periodTo, String trainerName, String trainingType) {

        log.info("Fetching trainings list for trainee: {} with filters [from: {}, to: {}, trainer: {}, type: {}]",
                username, periodFrom, periodTo, trainerName, trainingType);

        if (!traineeRepository.findByUserUsername(username).isPresent()) {
            log.warn("Trainings retrieval failed: Trainee not found with username: {}", username);
            throw new EntityNotFoundException("Trainee not found with username: " + username);
        }

        Specification<Training> spec = Specification.where(TraineeSpecifications.hasTraineeUsername(username))
                .and(TraineeSpecifications.dateGreaterThanOrEqualTo(periodFrom))
                .and(TraineeSpecifications.dateLessThanOrEqualTo(periodTo))
                .and(TraineeSpecifications.hasTrainerUsername(trainerName))
                .and(TraineeSpecifications.hasTrainingType(trainingType));

        List<Training> trainings = trainingRepository.findAll(spec);
        log.debug("Retrieved {} trainings for trainee: {}", trainings.size(), username);
        return trainingMapper.toTraineeReportResponseList(trainings);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainerTrainingResponseDto> getTrainerTrainings(
            String username, Date periodFrom, Date periodTo, String traineeName) {

        log.info("Fetching trainings list for trainer: {} with filters [from: {}, to: {}, trainee: {}]",
                username, periodFrom, periodTo, traineeName);

        if (!trainerRepository.findByUserUsername(username).isPresent()) {
            log.warn("Trainings retrieval failed: Trainer not found with username: {}", username);
            throw new EntityNotFoundException("Trainer not found with username: " + username);
        }

        Specification<Training> spec = Specification.where(TrainerSpecifications.hasTrainerUsername(username))
                .and(TrainerSpecifications.dateGreaterThanOrEqualTo(periodFrom))
                .and(TrainerSpecifications.dateLessThanOrEqualTo(periodTo))
                .and(TrainerSpecifications.hasTraineeUsername(traineeName));

        List<Training> trainings = trainingRepository.findAll(spec);
        log.debug("Retrieved {} trainings for trainer: {}", trainings.size(), username);
        return trainingMapper.toTrainerReportResponseList(trainings);
    }

    @Override
    @Transactional
    public void addTraining(TrainingAddRequestDto request) {
        log.info("Initiating addition of training: '{}' for trainee: {} and trainer: {}",
                request.getTrainingName(), request.getTraineeUsername(), request.getTrainerUsername());

        crmMetrics.getTrainingCreationTimer().record(() -> {
            Trainee trainee = traineeRepository.findByUserUsername(request.getTraineeUsername())
                    .orElseThrow(() -> {
                        log.warn("Training addition failed: Trainee not found with username: {}", request.getTraineeUsername());
                        return new EntityNotFoundException("Trainee not found with username: " + request.getTraineeUsername());
                    });

            Trainer trainer = trainerRepository.findByUserUsername(request.getTrainerUsername())
                    .orElseThrow(() -> {
                        log.warn("Training addition failed: Trainer not found with username: {}", request.getTrainerUsername());
                        return new EntityNotFoundException("Trainer not found with username: " + request.getTrainerUsername());
                    });

            if (!trainee.getUser().getIsActive()) {
                log.warn("Training addition rejected: Trainee profile {} is inactive", trainee.getUser().getUsername());
                throw new InactiveUserException("Cannot add training: Trainee profile is inactive.");
            }

            if (!trainer.getUser().getIsActive()) {
                log.warn("Training addition rejected: Trainer profile {} is inactive", trainer.getUser().getUsername());
                throw new InactiveUserException("Cannot add training: Trainer profile is inactive.");
            }

            if (trainee.getTrainers() == null) {
                trainee.setTrainers(new ArrayList<>());
            }

            if (!trainee.getTrainers().contains(trainer)) {
                log.debug("Establishing new association between trainee {} and trainer {}",
                        trainee.getUser().getUsername(), trainer.getUser().getUsername());
                trainee.getTrainers().add(trainer);
                traineeRepository.save(trainee);
            }

            Training training = Training.builder()
                    .trainee(trainee)
                    .trainer(trainer)
                    .trainingName(request.getTrainingName())
                    .trainingDate(request.getTrainingDate())
                    .trainingDuration(request.getTrainingDuration())
                    .trainingType(trainer.getSpecialization())
                    .build();

            Training savedTraining = trainingRepository.save(training);
            log.info("Training session successfully created with ID: {}", savedTraining.getId());

            String trainerUsername = trainer.getUser().getUsername();
            TrainerWorkloadRequestDto workloadRequest = TrainerWorkloadRequestDto.builder()
                    .username(trainerUsername)
                    .firstName(trainer.getUser().getFirstName())
                    .lastName(trainer.getUser().getLastName())
                    .isActive(trainer.getUser().getIsActive())
                    .trainingDate(training.getTrainingDate())
                    .trainingDuration(training.getTrainingDuration())
                    .actionType(ActionType.ADD)
                    .build();

            log.debug("Sending workload ADD event to microservice for trainer: {}", trainerUsername);
            trainerWorkloadClient.processWorkload(workloadRequest);
            log.info("Workload ADD event successfully processed for trainer: {}", trainerUsername);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainingTypeResponseDto> getTrainingTypes() {
        log.info("Fetching all available training types");
        List<TrainingTypeResponseDto> list = trainingMapper.toTypeResponseList(trainingTypeRepository.findAll());
        log.debug("Found {} training types", list.size());
        return list;
    }

    @Override
    @Transactional
    public void deleteTraining(Long id) {
        log.info("Starting deletion of training session with ID: {}", id);
        Training training = trainingRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Training deletion failed: Training session not found with ID: {}", id);
                    return new EntityNotFoundException("Training not found with ID: " + id);
                });

        Trainer trainer = training.getTrainer();
        String trainerUsername = trainer.getUser().getUsername();

        TrainerWorkloadRequestDto workloadRequest = TrainerWorkloadRequestDto.builder()
                .username(trainerUsername)
                .firstName(trainer.getUser().getFirstName())
                .lastName(trainer.getUser().getLastName())
                .isActive(trainer.getUser().getIsActive())
                .trainingDate(training.getTrainingDate())
                .trainingDuration(training.getTrainingDuration())
                .actionType(ActionType.DELETE)
                .build();

        trainingRepository.delete(training);
        log.info("Training session with ID {} successfully deleted from database", id);

        log.debug("Sending workload DELETE event to microservice for trainer: {}", trainerUsername);
        trainerWorkloadClient.processWorkload(workloadRequest);
        log.info("Workload DELETE event successfully processed for trainer: {}", trainerUsername);
    }
}