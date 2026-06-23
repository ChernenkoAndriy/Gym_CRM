package com.epam.java.specialization.gym_crm.dao;

import com.epam.java.specialization.gym_crm.model.Trainer;
import com.epam.java.specialization.gym_crm.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class TrainerDao {

    private static final Logger logger = LoggerFactory.getLogger(TrainerDao.class);
    private Storage storage;

    @Autowired
    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    public Trainer save(Trainer trainer) {
        logger.info("DAO: Saving/Updating Trainer with ID: {}", trainer.getId());
        storage.getTrainers().put(trainer.getId(), trainer);
        return trainer;
    }

    public Optional<Trainer> findById(Long id) {
        logger.info("DAO: Finding Trainer by ID: {}", id);
        return Optional.ofNullable(storage.getTrainers().get(id));
    }

    public List<Trainer> findAll() {
        logger.info("DAO: Fetching all Trainers");
        return new ArrayList<>(storage.getTrainers().values());
    }
}