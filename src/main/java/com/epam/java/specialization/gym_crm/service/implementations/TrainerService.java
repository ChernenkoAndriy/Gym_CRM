package com.epam.java.specialization.gym_crm.service.implementations;

import com.epam.java.specialization.gym_crm.dto.TrainerCreateDto;
import com.epam.java.specialization.gym_crm.dto.TrainerResponseDto;
import com.epam.java.specialization.gym_crm.dto.TrainerUpdateDto;
import com.epam.java.specialization.gym_crm.dto.TrainingResponseDto;
import com.epam.java.specialization.gym_crm.mapper.interfaces.ITrainerMapper;
import com.epam.java.specialization.gym_crm.mapper.interfaces.ITrainingMapper;
import com.epam.java.specialization.gym_crm.model.Trainer;
import com.epam.java.specialization.gym_crm.model.Training;
import com.epam.java.specialization.gym_crm.model.TrainingType;
import com.epam.java.specialization.gym_crm.repository.TrainerRepository;
import com.epam.java.specialization.gym_crm.repository.TrainingRepository;
import com.epam.java.specialization.gym_crm.repository.TrainingTypeRepository;
import com.epam.java.specialization.gym_crm.repository.specification.TrainerSpecifications;
import com.epam.java.specialization.gym_crm.service.interfaces.ITrainerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class TrainerService extends AbstractUserService implements ITrainerService {

    private TrainingTypeRepository trainingTypeRepository;
    private ITrainerMapper trainerMapper;
    private ITrainingMapper trainingMapper;
    private TrainingRepository trainingRepository;

    @Autowired
    public void setTrainingTypeRepository(TrainingTypeRepository trainingTypeRepository) {
        this.trainingTypeRepository = trainingTypeRepository;
    }

    @Autowired
    public void setTrainerMapper(ITrainerMapper trainerMapper) {
        this.trainerMapper = trainerMapper;
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
    public TrainerResponseDto create(TrainerCreateDto dto) {
        logger.debug("Attempting to create trainer profile: {} {}", dto.getFirstName(), dto.getLastName());
        Trainer trainer = trainerMapper.toEntityFromCreate(dto);
        TrainingType trainingType = trainingTypeRepository.findById(dto.getTrainingTypeId())
                .orElseThrow(() -> new IllegalArgumentException("TrainingType not found with ID: " + dto.getTrainingTypeId()));
        trainer.setSpecialization(trainingType);
        prepareUserProfile(trainer.getUser());
        Trainer saved = trainerRepository.save(trainer);
        return trainerMapper.toResponseDto(saved, trainingType);
    }

    @Override
    public TrainerResponseDto update(TrainerUpdateDto dto) {
        logger.debug("Updating trainer profile with ID: {}", dto.getId());
        Trainer existing = trainerRepository.findById(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found with ID: " + dto.getId()));
        if (existing.getUser().getIsActive() != dto.getIsActive()) {
            toggleActivation(existing.getUser().getUsername(), dto.getIsActive());
        }
        TrainingType trainingType = trainingTypeRepository.findById(dto.getTrainingTypeId())
                .orElseThrow(() -> new IllegalArgumentException("TrainingType not found with ID: " + dto.getTrainingTypeId()));
        existing.getUser().setFirstName(dto.getFirstName());
        existing.getUser().setLastName(dto.getLastName());
        existing.setSpecialization(trainingType);
        Trainer saved = trainerRepository.save(existing);
        return trainerMapper.toResponseDto(saved, trainingType);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TrainerResponseDto> getById(Long id) {
        logger.debug("Selecting trainer profile with ID: {}", id);
        return trainerRepository.findById(id).map(t -> trainerMapper.toResponseDto(t, t.getSpecialization()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainingResponseDto> getTrainingsByCriteria(String username, Date fromDate, Date toDate, String traineeName, int page, int size) {
        logger.debug("Fetching trainer trainings by criteria for username: {}", username);
        Specification<Training> spec = Specification.where(TrainerSpecifications.hasTrainerUsername(username))
                .and(TrainerSpecifications.dateGreaterThanOrEqualTo(fromDate))
                .and(TrainerSpecifications.dateLessThanOrEqualTo(toDate))
                .and(TrainerSpecifications.hasTraineeUsername(traineeName));

        List<Training> trainings = trainingRepository.findAll(spec, PageRequest.of(page - 1, size)).getContent();
        return trainings.stream()
                .map(t -> trainingMapper.toResponseDto(t, t.getTrainee(), t.getTrainer(), t.getTrainingType()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainerResponseDto> getAvailableTrainersNotAssignedToTrainee(String traineeUsername) {
        logger.debug("Fetching available trainers not assigned to trainee: {}", traineeUsername);
        List<Trainer> trainers = trainerRepository.findAvailableTrainersNotAssignedToTrainee(traineeUsername);
        return trainers.stream()
                .map(t -> trainerMapper.toResponseDto(t, t.getSpecialization()))
                .collect(Collectors.toList());
    }

    @Override
    public void toggleActivation(String username, boolean isActive) {
        logger.info("Attempting to toggle activation status to {} for trainer username: {}", isActive, username);
        Trainer trainer = trainerRepository.findByUserUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found with username: " + username));
        if (trainer.getUser().getIsActive() == isActive) {
            logger.error("Failed to toggle activation. Status for trainer {} is already {}", username, isActive);
            throw new IllegalStateException("Status is already " + isActive);
        }
        trainer.getUser().setIsActive(isActive);
        trainerRepository.save(trainer);
        logger.info("Successfully changed activation status to {} for trainer username: {}", isActive, username);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TrainerResponseDto> getByUsername(String username) {
        logger.debug("Selecting trainer profile with username: {}", username);
        return trainerRepository.findByUserUsername(username)
                .map(t -> trainerMapper.toResponseDto(t, t.getSpecialization()));
    }
}