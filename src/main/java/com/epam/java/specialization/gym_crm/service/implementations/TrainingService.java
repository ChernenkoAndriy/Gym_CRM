package com.epam.java.specialization.gym_crm.service.implementations;

import com.epam.java.specialization.gym_crm.dao.intefaces.ITraineeDao;
import com.epam.java.specialization.gym_crm.dao.intefaces.ITrainerDao;
import com.epam.java.specialization.gym_crm.dao.intefaces.ITrainingDao;
import com.epam.java.specialization.gym_crm.dao.intefaces.ITrainingTypeDao;
import com.epam.java.specialization.gym_crm.dto.TrainingCreateDto;
import com.epam.java.specialization.gym_crm.dto.TrainingResponseDto;
import com.epam.java.specialization.gym_crm.mapper.interfaces.ITrainingMapper;
import com.epam.java.specialization.gym_crm.model.Trainee;
import com.epam.java.specialization.gym_crm.model.Trainer;
import com.epam.java.specialization.gym_crm.model.Training;
import com.epam.java.specialization.gym_crm.model.TrainingType;
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

    private ITrainingDao trainingDao;
    private ITraineeDao traineeDao;
    private ITrainerDao trainerDao;
    private ITrainingTypeDao trainingTypeDao;
    private ITrainingMapper trainingMapper;

    @Autowired
    public void setTrainingDao(ITrainingDao trainingDao) {
        this.trainingDao = trainingDao;
    }

    @Autowired
    public void setTraineeDao(ITraineeDao traineeDao) {
        this.traineeDao = traineeDao;
    }

    @Autowired
    public void setTrainerDao(ITrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }

    @Autowired
    public void setTrainingTypeDao(ITrainingTypeDao trainingTypeDao) {
        this.trainingTypeDao = trainingTypeDao;
    }

    @Autowired
    public void setTrainingMapper(ITrainingMapper trainingMapper) {
        this.trainingMapper = trainingMapper;
    }

    @Override
    public TrainingResponseDto create(TrainingCreateDto dto) {
        logger.info("Creating new training profile: {}", dto.getTrainingName());
        Trainee trainee = traineeDao.findById(dto.getTraineeId())
                .orElseThrow(() -> new IllegalArgumentException("Trainee explicitly missing associated records in DB"));
        Trainer trainer = trainerDao.findById(dto.getTrainerId())
                .orElseThrow(() -> new IllegalArgumentException("Trainer explicitly missing associated records in DB"));
        TrainingType trainingType = trainingTypeDao.findById(dto.getTrainingTypeId())
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
            traineeDao.update(trainee);
        }

        Training training = trainingMapper.toEntityFromCreate(dto, trainee, trainer, trainingType);
        Training saved = trainingDao.create(training);
        return trainingMapper.toResponseDto(saved, trainee, trainer, trainingType);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TrainingResponseDto> getById(Long id) {
        logger.debug("Selecting training profile with ID: {}", id);
        return trainingDao.findById(id).map(t -> trainingMapper.toResponseDto(t, t.getTrainee(), t.getTrainer(), t.getTrainingType()));
    }
}