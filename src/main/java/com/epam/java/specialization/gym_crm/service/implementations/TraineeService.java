package com.epam.java.specialization.gym_crm.service.implementations;

import com.epam.java.specialization.gym_crm.dto.TraineeCreateDto;
import com.epam.java.specialization.gym_crm.dto.TraineeResponseDto;
import com.epam.java.specialization.gym_crm.dto.TraineeUpdateDto;
import com.epam.java.specialization.gym_crm.dto.TrainingResponseDto;
import com.epam.java.specialization.gym_crm.mapper.interfaces.ITraineeMapper;
import com.epam.java.specialization.gym_crm.mapper.interfaces.ITrainingMapper;
import com.epam.java.specialization.gym_crm.model.Trainee;
import com.epam.java.specialization.gym_crm.model.Trainer;
import com.epam.java.specialization.gym_crm.model.Training;
import com.epam.java.specialization.gym_crm.repository.TrainingRepository;
import com.epam.java.specialization.gym_crm.repository.specification.TraineeSpecifications;
import com.epam.java.specialization.gym_crm.service.interfaces.ITraineeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class TraineeService extends AbstractUserService implements ITraineeService {

    private ITraineeMapper traineeMapper;
    private ITrainingMapper trainingMapper;
    private TrainingRepository trainingRepository;

    @Autowired
    public void setTraineeMapper(ITraineeMapper traineeMapper) {
        this.traineeMapper = traineeMapper;
    }

    @Autowired
    public void setTrainingMapper(ITrainingMapper trainingMapper) {
        this.trainingMapper = trainingMapper;
    }

    @Autowired
    public void setTrainingRepository(TrainingRepository trainingRepository) {
        this.trainingRepository = trainingRepository;
    }

    @Override
    public TraineeResponseDto create(TraineeCreateDto dto) {
        logger.debug("Attempting to create trainee profile: {} {}", dto.getFirstName(), dto.getLastName());
        Trainee trainee = traineeMapper.toEntityFromCreate(dto);
        prepareUserProfile(trainee.getUser());
        Trainee saved = traineeRepository.save(trainee);
        return traineeMapper.toResponseDto(saved);
    }

    @Override
    public TraineeResponseDto update(TraineeUpdateDto dto) {
        logger.debug("Updating trainee profile with ID: {}", dto.getId());
        Trainee existing = traineeRepository.findById(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found with ID: " + dto.getId()));
        if (existing.getUser().getIsActive() != dto.getIsActive()) {
            toggleActivation(existing.getUser().getUsername(), dto.getIsActive());
        }
        existing.getUser().setFirstName(dto.getFirstName());
        existing.getUser().setLastName(dto.getLastName());
        existing.setDateOfBirth(dto.getDateOfBirth());
        existing.setAddress(dto.getAddress());
        Trainee saved = traineeRepository.save(existing);
        return traineeMapper.toResponseDto(saved);
    }

    @Override
    public void delete(Long id) {
        logger.warn("Deleting trainee profile with ID: {}", id);
        traineeRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TraineeResponseDto> getById(Long id) {
        logger.debug("Selecting trainee profile with ID: {}", id);
        return traineeRepository.findById(id).map(traineeMapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TraineeResponseDto> getByUsername(String username) {
        logger.debug("Selecting trainee profile with username: {}", username);
        return traineeRepository.findByUserUsername(username).map(traineeMapper::toResponseDto);
    }

    @Override
    public void deleteByUsername(String username) {
        logger.warn("Attempting hard delete of trainee profile by username: {}", username);
        Trainee trainee = traineeRepository.findByUserUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found with username: " + username));
        if (trainee.getTrainers() != null) {
            trainee.getTrainers().clear();
        }
        traineeRepository.save(trainee);
        traineeRepository.delete(trainee);
        logger.info("Successfully deleted trainee and all associated trainings for username: {}", username);
    }

    @Override
    public void toggleActivation(String username, boolean isActive) {
        logger.info("Attempting to toggle activation status to {} for username: {}", isActive, username);
        Trainee trainee = traineeRepository.findByUserUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found with username: " + username));
        if (trainee.getUser().getIsActive() == isActive) {
            logger.error("Failed to toggle activation. Status for {} is already {}", username, isActive);
            throw new IllegalStateException("Status is already " + isActive);
        }
        trainee.getUser().setIsActive(isActive);
        traineeRepository.save(trainee);
        logger.info("Successfully changed activation status to {} for username: {}", isActive, username);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainingResponseDto> getTrainingsByCriteria(String username, Date fromDate, Date toDate, String trainerName, String trainingType, int page, int size) {
        logger.debug("Fetching trainee trainings by criteria for username: {}", username);

        Specification<Training> spec = Specification.where(TraineeSpecifications.hasTraineeUsername(username))
                .and(TraineeSpecifications.dateGreaterThanOrEqualTo(fromDate))
                .and(TraineeSpecifications.dateLessThanOrEqualTo(toDate))
                .and(TraineeSpecifications.hasTrainerUsername(trainerName))
                .and(TraineeSpecifications.hasTrainingType(trainingType));

        List<Training> trainings = trainingRepository.findAll(spec, PageRequest.of(page - 1, size)).getContent();

        return trainings.stream()
                .map(t -> trainingMapper.toResponseDto(t, t.getTrainee(), t.getTrainer(), t.getTrainingType()))
                .collect(Collectors.toList());
    }

    @Override
    public void updateTrainersList(String traineeUsername, List<String> trainerUsernames) {
        logger.info("Updating trainers list for trainee: {}", traineeUsername);
        Trainee trainee = traineeRepository.findByUserUsername(traineeUsername)
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found with username: " + traineeUsername));
        if (trainerUsernames == null || trainerUsernames.isEmpty()) {
            trainee.getTrainers().clear();
            traineeRepository.save(trainee);
            return;
        }
        List<Trainer> newTrainers = trainerRepository.findByUserUsernameIn(trainerUsernames);
        trainee.setTrainers(new ArrayList<>(newTrainers));
        traineeRepository.save(trainee);
        logger.info("Successfully updated trainers list for trainee {}. Total trainers assigned: {}",
                traineeUsername, newTrainers.size());
    }
}