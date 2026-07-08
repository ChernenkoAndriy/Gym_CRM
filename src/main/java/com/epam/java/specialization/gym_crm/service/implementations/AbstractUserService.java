package com.epam.java.specialization.gym_crm.service.implementations;

import com.epam.java.specialization.gym_crm.dao.intefaces.ITraineeDao;
import com.epam.java.specialization.gym_crm.dao.intefaces.ITrainerDao;
import com.epam.java.specialization.gym_crm.model.Trainee;
import com.epam.java.specialization.gym_crm.model.Trainer;
import com.epam.java.specialization.gym_crm.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractUserService {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    protected ITraineeDao traineeDao;
    protected ITrainerDao trainerDao;

    @Autowired
    public void setTraineeDao(ITraineeDao traineeDao) {
        this.traineeDao = traineeDao;
    }

    @Autowired
    public void setTrainerDao(ITrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }

    @Transactional
    protected synchronized void prepareUserProfile(User user) {
        String baseUsername = user.getFirstName() + "." + user.getLastName();
        List<Trainee> existing = traineeDao.findAll();
        Set<String> usernamesSet = existing.stream()
                .map(t -> t.getUser().getUsername())
                .collect(Collectors.toSet());

        String finalUsername = baseUsername;
        if (usernamesSet.contains(baseUsername)) {
            int suffix = 1;
            while (true) {
                String candidateUsername = baseUsername + suffix;
                if (!usernamesSet.contains(candidateUsername)) {
                    finalUsername = candidateUsername;
                    break;
                }
                suffix++;
            }
        }
        user.setUsername(finalUsername);
        user.setPassword(generateRandomPassword());
        logger.info("Generated profile for user: username={}, password=[PROTECTED]", finalUsername);
    }

    @Transactional(readOnly = true)
    public boolean authenticate(String username, String password) {
        logger.debug("Attempting authentication for username: {}", username);
        Optional<Trainee> trainee = traineeDao.findByUsername(username);
        if (trainee.isPresent()) {
            boolean success = trainee.get().getUser().getPassword().equals(password);
            if (success) {
                logger.info("Authentication successful for username: {}", username);
            } else {
                logger.warn("Authentication failed for username: {}", username);
            }
            return success;
        }

        Optional<Trainer> trainer = trainerDao.findByUsername(username);
        if (trainer.isPresent()) {
            boolean success = trainer.get().getUser().getPassword().equals(password);
            if (success) {
                logger.info("Authentication successful for username: {}", username);
            } else {
                logger.warn("Authentication failed for username: {}", username);
            }
            return success;
        }

        logger.warn("Authentication failed for username: {}", username);
        return false;
    }

    @Transactional
    public void changePassword(String username, String newPassword) {
        logger.info("Changing password for username: {}", username);
        Optional<Trainee> trainee = traineeDao.findByUsername(username);
        if (trainee.isPresent()) {
            trainee.get().getUser().setPassword(newPassword);
            traineeDao.update(trainee.get());
            logger.info("Password successfully updated for username: {}", username);
            return;
        }

        Optional<Trainer> trainer = trainerDao.findByUsername(username);
        if (trainer.isPresent()) {
            trainer.get().getUser().setPassword(newPassword);
            trainerDao.update(trainer.get());
            logger.info("Password successfully updated for username: {}", username);
            return;
        }

        logger.error("Failed to change password. User with username {} not found", username);
        throw new IllegalArgumentException("User not found: " + username);
    }

    private String generateRandomPassword() {
        StringBuilder sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }
}