package com.epam.java.specialization.gym_crm.service.implementations;

import com.epam.java.specialization.gym_crm.dao.intefaces.ITrainingDao;
import com.epam.java.specialization.gym_crm.model.Training;
import com.epam.java.specialization.gym_crm.service.interfaces.ITrainingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class TrainingService implements ITrainingService {

    private static final Logger logger = LoggerFactory.getLogger(TrainingService.class);
    private ITrainingDao trainingDao;

    @Autowired
    public void setTrainingDao(ITrainingDao trainingDao) {
        this.trainingDao = trainingDao;
    }

    @Override
    public Training create(Training training) {
        logger.info("Creating new training profile: {}", training.getTrainingName());
        return trainingDao.create(training);
    }

    @Override
    public Optional<Training> getById(Long id) {
        logger.debug("Selecting training profile with ID: {}", id);
        return trainingDao.findById(id);
    }
}