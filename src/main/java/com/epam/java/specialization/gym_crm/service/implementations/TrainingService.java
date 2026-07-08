package com.epam.java.specialization.gym_crm.service.implementations;

import com.epam.java.specialization.gym_crm.dao.intefaces.ITrainingDao;
import com.epam.java.specialization.gym_crm.model.Trainee;
import com.epam.java.specialization.gym_crm.model.Trainer;
import com.epam.java.specialization.gym_crm.model.Training;
import com.epam.java.specialization.gym_crm.service.interfaces.ITrainingService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public void setTrainingDao(ITrainingDao trainingDao) {
        this.trainingDao = trainingDao;
    }

    @Override
    public Training create(Training training) {
        logger.info("Creating new training profile: {}", training.getTrainingName());

        Trainee trainee = entityManager.find(Trainee.class, training.getTrainee().getId());
        Trainer trainer = entityManager.find(Trainer.class, training.getTrainer().getId());

        if (trainee == null || trainer == null) {
            throw new IllegalArgumentException("Trainee or Trainer explicitly missing associated records in DB");
        }

        if (trainee.getTrainers() == null) {
            trainee.setTrainers(new ArrayList<>());
        }

        if (!trainee.getTrainers().contains(trainer)) {
            logger.info("Automatically assigning trainer {} to trainee {} because of a new training session",
                    trainer.getUser().getUsername(), trainee.getUser().getUsername());
            trainee.getTrainers().add(trainer);
        }
        return trainingDao.create(training);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Training> getById(Long id) {
        logger.debug("Selecting training profile with ID: {}", id);
        return trainingDao.findById(id);
    }
}