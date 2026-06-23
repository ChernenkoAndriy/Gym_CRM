package com.epam.java.specialization.gym_crm.service;

import com.epam.java.specialization.gym_crm.dao.TrainingDao;
import com.epam.java.specialization.gym_crm.model.Training;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TrainingService {

    private static final Logger logger = LoggerFactory.getLogger(TrainingService.class);
    private TrainingDao trainingDao;

    @Autowired
    public void setTrainingDao(TrainingDao trainingDao) {
        this.trainingDao = trainingDao;
    }

    public Training createTraining(Training training) {
        logger.info("Service: Creating Training profile: {}", training.getTrainingName());
        return trainingDao.save(training);
    }

    public Optional<Training> getTrainingById(Long id) {
        logger.info("Service: Selecting Training profile by ID: {}", id);
        return trainingDao.findById(id);
    }

    public List<Training> getAllTrainings() {
        return trainingDao.findAll();
    }
}