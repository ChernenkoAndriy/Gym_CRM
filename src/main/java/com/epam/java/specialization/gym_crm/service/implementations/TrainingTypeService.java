package com.epam.java.specialization.gym_crm.service.implementations;

import com.epam.java.specialization.gym_crm.dao.intefaces.ITrainingTypeDao;
import com.epam.java.specialization.gym_crm.model.TrainingType;
import com.epam.java.specialization.gym_crm.service.interfaces.ITrainingTypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class TrainingTypeService implements ITrainingTypeService {

    private static final Logger logger = LoggerFactory.getLogger(TrainingTypeService.class);
    private ITrainingTypeDao trainingTypeDao;

    @Autowired
    public void setTrainingTypeDao(ITrainingTypeDao trainingTypeDao) {
        this.trainingTypeDao = trainingTypeDao;
    }

    @Override
    public TrainingType create(TrainingType entity) {
        logger.info("Creating new training type: {}", entity.getTrainingTypeName());
        return trainingTypeDao.create(entity);
    }

    @Override
    public Optional<TrainingType> getById(Long id) {
        return trainingTypeDao.findById(id);
    }

    @Override
    public void delete(Long id) {
        logger.warn("Deleting training type with ID: {}", id);
        trainingTypeDao.delete(id);
    }
}