package com.epam.java.specialization.gym_crm.dao;

import com.epam.java.specialization.gym_crm.model.Training;
import com.epam.java.specialization.gym_crm.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class TrainingDao {

    private static final Logger logger = LoggerFactory.getLogger(TrainingDao.class);
    private Storage storage;

    @Autowired
    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    public Training save(Training training) {
        logger.info("DAO: Saving Training with ID: {}", training.getId());
        storage.getTrainings().put(training.getId(), training);
        return training;
    }

    public Optional<Training> findById(Long id) {
        logger.info("DAO: Finding Training by ID: {}", id);
        return Optional.ofNullable(storage.getTrainings().get(id));
    }

    public List<Training> findAll() {
        logger.info("DAO: Fetching all Trainings");
        return new ArrayList<>(storage.getTrainings().values());
    }
}