package com.epam.java.specialization.gym_crm.service;

import com.epam.java.specialization.gym_crm.dao.TraineeDao;
import com.epam.java.specialization.gym_crm.dao.TrainerDao;
import com.epam.java.specialization.gym_crm.model.Trainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class TrainerService {

    private static final Logger logger = LoggerFactory.getLogger(TrainerService.class);

    private TrainerDao trainerDao;
    private TraineeDao traineeDao;

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }

    @Autowired
    public void setTraineeDao(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
    }

    public Trainer createTrainer(Trainer trainer) {
        logger.info("Service: Creating Trainer profile for {} {}", trainer.getFirstName(), trainer.getLastName());

        String username = generateUsername(trainer.getFirstName(), trainer.getLastName());
        String password = generateRandomPassword();

        trainer.setUsername(username);
        trainer.setPassword(password);

        return trainerDao.save(trainer);
    }

    public Trainer updateTrainer(Trainer trainer) {
        logger.info("Service: Updating Trainer profile with ID: {}", trainer.getId());
        return trainerDao.save(trainer);
    }

    public Optional<Trainer> getTrainerById(Long id) {
        logger.info("Service: Selecting Trainer profile by ID: {}", id);
        return trainerDao.findById(id);
    }

    public List<Trainer> getAllTrainers() {
        return trainerDao.findAll();
    }

    private String generateUsername(String firstName, String lastName) {
        String baseUsername = firstName + "." + lastName;
        String username = baseUsername;
        int suffix = 1;

        while (usernameExists(username)) {
            username = baseUsername + suffix;
            suffix++;
        }
        return username;
    }

    private boolean usernameExists(String username) {
        boolean existsInTrainees = traineeDao.findAll().stream()
                .anyMatch(t -> username.equalsIgnoreCase(t.getUsername()));
        boolean existsInTrainers = trainerDao.findAll().stream()
                .anyMatch(t -> username.equalsIgnoreCase(t.getUsername()));
        return existsInTrainees || existsInTrainers;
    }

    private String generateRandomPassword() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder password = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            password.append(characters.charAt(random.nextInt(characters.length())));
        }
        return password.toString();
    }
}