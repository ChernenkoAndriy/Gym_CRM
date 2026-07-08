package com.epam.java.specialization.gym_crm.service.implementations;

import com.epam.java.specialization.gym_crm.dao.intefaces.ITraineeDao;
import com.epam.java.specialization.gym_crm.dao.intefaces.ITrainerDao;
import com.epam.java.specialization.gym_crm.dao.intefaces.ITrainingTypeDao;
import com.epam.java.specialization.gym_crm.dto.TrainerCreateDto;
import com.epam.java.specialization.gym_crm.dto.TrainerResponseDto;
import com.epam.java.specialization.gym_crm.dto.TrainerUpdateDto;
import com.epam.java.specialization.gym_crm.dto.TrainingResponseDto;
import com.epam.java.specialization.gym_crm.mapper.interfaces.ITrainerMapper;
import com.epam.java.specialization.gym_crm.mapper.interfaces.ITrainingMapper;
import com.epam.java.specialization.gym_crm.model.Trainer;
import com.epam.java.specialization.gym_crm.model.Training;
import com.epam.java.specialization.gym_crm.model.TrainingType;
import com.epam.java.specialization.gym_crm.service.interfaces.ITrainerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class TrainerService extends AbstractUserService implements ITrainerService {

    private ITrainerDao trainerDao;
    private ITrainingTypeDao trainingTypeDao;
    private ITrainerMapper trainerMapper;
    private ITrainingMapper trainingMapper;

    @Autowired
    public void setTrainerDao(ITrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }

    @Autowired
    public void setTrainingTypeDao(ITrainingTypeDao trainingTypeDao) {
        this.trainingTypeDao = trainingTypeDao;
    }

    @Autowired
    public void setTrainerMapper(ITrainerMapper trainerMapper) {
        this.trainerMapper = trainerMapper;
    }

    @Autowired
    public void setTrainingMapper(ITrainingMapper trainingMapper) {
        this.trainingMapper = trainingMapper;
    }

    @Override
    public TrainerResponseDto create(TrainerCreateDto dto) {
        logger.debug("Attempting to create trainer profile: {} {}", dto.getFirstName(), dto.getLastName());
        Trainer trainer = trainerMapper.toEntityFromCreate(dto);
        TrainingType trainingType = trainingTypeDao.findById(dto.getTrainingTypeId())
                .orElseThrow(() -> new IllegalArgumentException("TrainingType not found with ID: " + dto.getTrainingTypeId()));
        trainer.setSpecialization(trainingType);
        prepareUserProfile(trainer.getUser());
        Trainer saved = trainerDao.create(trainer);
        return trainerMapper.toResponseDto(saved, trainingType);
    }

    @Override
    public TrainerResponseDto update(TrainerUpdateDto dto) {
        logger.debug("Updating trainer profile with ID: {}", dto.getId());
        Trainer existing = trainerDao.findById(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found with ID: " + dto.getId()));
        Trainer updatedData = trainerMapper.toEntityFromUpdate(dto);
        TrainingType trainingType = trainingTypeDao.findById(dto.getTrainingTypeId())
                .orElseThrow(() -> new IllegalArgumentException("TrainingType not found with ID: " + dto.getTrainingTypeId()));

        existing.getUser().setFirstName(updatedData.getUser().getFirstName());
        existing.getUser().setLastName(updatedData.getUser().getLastName());
        existing.getUser().setIsActive(updatedData.getUser().getIsActive());
        existing.setSpecialization(trainingType);

        Trainer saved = trainerDao.update(existing);
        return trainerMapper.toResponseDto(saved, trainingType);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TrainerResponseDto> getById(Long id) {
        logger.debug("Selecting trainer profile with ID: {}", id);
        return trainerDao.findById(id).map(t -> trainerMapper.toResponseDto(t, t.getSpecialization()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainingResponseDto> getTrainingsByCriteria(String username, Date fromDate, Date toDate, String traineeName, int page, int size) {
        logger.debug("Fetching trainer trainings by criteria for username: {}", username);
        List<Training> trainings = trainerDao.findTrainingsByCriteria(username, fromDate, toDate, traineeName);
        return trainings.stream()
                .skip((long) (page - 1) * size)
                .limit(size)
                .map(t -> trainingMapper.toResponseDto(t, t.getTrainee(), t.getTrainer(), t.getTrainingType()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainerResponseDto> getAvailableTrainersNotAssignedToTrainee(String traineeUsername) {
        logger.debug("Fetching available trainers not assigned to trainee: {}", traineeUsername);
        List<Trainer> trainers = trainerDao.findAvailableTrainersNotAssignedToTrainee(traineeUsername);
        return trainers.stream()
                .map(t -> trainerMapper.toResponseDto(t, t.getSpecialization()))
                .collect(Collectors.toList());
    }

    @Override
    public void toggleActivation(String username, boolean isActive) {
        logger.info("Attempting to toggle activation status to {} for trainer username: {}", isActive, username);
        Trainer trainer = trainerDao.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found with username: " + username));
        if (trainer.getUser().getIsActive() == isActive) {
            logger.error("Failed to toggle activation. Status for trainer {} is already {}", username, isActive);
            throw new IllegalStateException("Status is already " + isActive);
        }
        trainer.getUser().setIsActive(isActive);
        trainerDao.update(trainer);
        logger.info("Successfully changed activation status to {} for trainer username: {}", isActive, username);
    }
}