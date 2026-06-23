package com.epam.java.specialization.gym_crm.dao;

import com.epam.java.specialization.gym_crm.model.Trainee;
import com.epam.java.specialization.gym_crm.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class TraineeDao {

    private static final Logger logger = LoggerFactory.getLogger(TraineeDao.class);
    private Storage storage;

    @Autowired
    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    public Trainee save(Trainee trainee) {
        logger.info("DAO: Saving/Updating Trainee with ID: {}", trainee.getId());
        storage.getTrainees().put(trainee.getId(), trainee);
        return trainee;
    }

    public Optional<Trainee> findById(Long id) {
        logger.info("DAO: Finding Trainee by ID: {}", id);
        return Optional.ofNullable(storage.getTrainees().get(id));
    }

    public void deleteById(Long id) {
        logger.info("DAO: Deleting Trainee with ID: {}", id);
        storage.getTrainees().remove(id);
    }

    public List<Trainee> findAll() {
        logger.info("DAO: Fetching all Trainees");
        return new ArrayList<>(storage.getTrainees().values());
    }
}