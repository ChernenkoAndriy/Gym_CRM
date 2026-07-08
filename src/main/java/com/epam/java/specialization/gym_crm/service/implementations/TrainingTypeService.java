package com.epam.java.specialization.gym_crm.service.implementations;

import com.epam.java.specialization.gym_crm.dao.intefaces.ITrainingTypeDao;
import com.epam.java.specialization.gym_crm.model.TrainingType;
import com.epam.java.specialization.gym_crm.service.interfaces.ITrainingTypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TrainingTypeService implements ITrainingTypeService {
    private static final Logger logger = LoggerFactory.getLogger(TrainingTypeService.class);
    private final List<String> ALLOWED_CONSTANTS = Arrays.asList("Yoga", "Crossfit", "Fitness");
    private ITrainingTypeDao trainingTypeDao;

    @Autowired
    public void setTrainingTypeDao(ITrainingTypeDao trainingTypeDao) {
        this.trainingTypeDao = trainingTypeDao;
    }

    @Override
    public TrainingType create(TrainingType entity) {
        logger.info("Checking training type: {}", entity.getTrainingTypeName());
        Optional<TrainingType> existing = trainingTypeDao.findByName(entity.getTrainingTypeName());
        if (existing.isPresent()) {
            return existing.get();
        }
        if (ALLOWED_CONSTANTS.contains(entity.getTrainingTypeName())) {
            return trainingTypeDao.create(entity);
        }
        throw new UnsupportedOperationException("Creation of new training types from the application is strictly prohibited.");
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TrainingType> getById(Long id) {
        return trainingTypeDao.findById(id);
    }

    @Override
    public void delete(Long id) {
        logger.warn("Deleting training type with ID: {}", id);
        trainingTypeDao.delete(id);
    }
}