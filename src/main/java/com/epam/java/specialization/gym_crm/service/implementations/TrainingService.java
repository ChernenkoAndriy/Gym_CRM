package com.epam.java.specialization.gym_crm.service.implementations;

import com.epam.java.specialization.gym_crm.dto.TrainingCreateDto;
import com.epam.java.specialization.gym_crm.dto.TrainingResponseDto;
import com.epam.java.specialization.gym_crm.mapper.interfaces.ITrainingMapper;
import com.epam.java.specialization.gym_crm.model.Trainee;
import com.epam.java.specialization.gym_crm.model.Trainer;
import com.epam.java.specialization.gym_crm.model.Training;
import com.epam.java.specialization.gym_crm.model.TrainingType;
import com.epam.java.specialization.gym_crm.repository.TraineeRepository;
import com.epam.java.specialization.gym_crm.repository.TrainerRepository;
import com.epam.java.specialization.gym_crm.repository.TrainingRepository;
import com.epam.java.specialization.gym_crm.repository.TrainingTypeRepository;
import com.epam.java.specialization.gym_crm.service.interfaces.ITrainingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Optional;

@Service
@Transactional
public class TrainingService implements ITrainingService {
    private static final Logger logger = LoggerFactory.getLogger(TrainingService.class);

    private TrainingRepository trainingRepository;
    private TraineeRepository traineeRepository;
    private TrainerRepository trainerRepository;
    private TrainingTypeRepository trainingTypeRepository;
    private ITrainingMapper trainingMapper;

    @Autowired
    public void setTrainingRepository(TrainingRepository trainingRepository) {
        this.trainingRepository = trainingRepository;
    }

    @Autowired
    public void setTraineeRepository(TraineeRepository traineeRepository) {
        this.traineeRepository = traineeRepository;
    }

    @Autowired
    public void setTrainerRepository(TrainerRepository trainerRepository) {
        this.trainerRepository = trainerRepository;
    }

    @Autowired
    public void setTrainingTypeRepository(TrainingTypeRepository trainingTypeRepository) {
        this.trainingTypeRepository = trainingTypeRepository;
    }

    @Autowired
    public void setTrainingMapper(ITrainingMapper trainingMapper) {
        this.trainingMapper = trainingMapper;
    }

    @Override
    public TrainingResponseDto create(TrainingCreateDto dto) {
        logger.info("Creating new training profile: {}", dto.getTrainingName());
        Trainee trainee = traineeRepository.findById(dto.getTraineeId())
                .orElseThrow(() -> new IllegalArgumentException("Trainee explicitly missing associated records in DB"));
        Trainer trainer = trainerRepository.findById(dto.getTrainerId())
                .orElseThrow(() -> new IllegalArgumentException("Trainer explicitly missing associated records in DB"));
        TrainingType trainingType = trainingTypeRepository.findById(dto.getTrainingTypeId())
                .orElseThrow(() -> new IllegalArgumentException("TrainingType explicitly missing associated records in DB"));

        if (!trainee.getUser().getIsActive() || !trainer.getUser().getIsActive()) {
            throw new IllegalArgumentException("Cannot create training with inactive trainee or trainer");
        }

        if (trainee.getTrainers() == null) {
            trainee.setTrainers(new ArrayList<>());
        }

        if (!trainee.getTrainers().contains(trainer)) {
            logger.info("Automatically assigning trainer {} to trainee {} because of a new training session",
                    trainer.getUser().getUsername(), trainee.getUser().getUsername());
            trainee.getTrainers().add(trainer);
            traineeRepository.save(trainee);
        }

        Training training = trainingMapper.toEntityFromCreate(dto, trainee, trainer, trainingType);
        Training saved = trainingRepository.save(training);
        return trainingMapper.toResponseDto(saved, trainee, trainer, trainingType);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TrainingResponseDto> getById(Long id) {
        logger.debug("Selecting training profile with ID: {}", id);
        return trainingRepository.findById(id).map(t -> trainingMapper.toResponseDto(t, t.getTrainee(), t.getTrainer(), t.getTrainingType()));
    }
}