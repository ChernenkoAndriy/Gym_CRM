package com.epam.java.specialization.gym_crm.service;

import com.epam.java.specialization.gym_crm.dao.TraineeDao;
import com.epam.java.specialization.gym_crm.dao.TrainerDao;
import com.epam.java.specialization.gym_crm.model.Trainee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class TraineeService {

    private static final Logger logger = LoggerFactory.getLogger(TraineeService.class);

    private TraineeDao traineeDao;
    private TrainerDao trainerDao;

    @Autowired
    public void setTraineeDao(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
    }

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }

    public Trainee createTrainee(Trainee trainee) {
        logger.info("Service: Creating Trainee profile for {} {}", trainee.getFirstName(), trainee.getLastName());

        String username = generateUsername(trainee.getFirstName(), trainee.getLastName());
        String password = generateRandomPassword();

        trainee.setUsername(username);
        trainee.setPassword(password);

        return traineeDao.save(trainee);
    }

    public Trainee updateTrainee(Trainee trainee) {
        logger.info("Service: Updating Trainee profile with ID: {}", trainee.getId());
        return traineeDao.save(trainee);
    }

    public void deleteTrainee(Long id) {
        logger.info("Service: Deleting Trainee profile with ID: {}", id);
        traineeDao.deleteById(id);
    }

    public Optional<Trainee> getTraineeById(Long id) {
        logger.info("Service: Selecting Trainee profile by ID: {}", id);
        return traineeDao.findById(id);
    }

    public List<Trainee> getAllTrainees() {
        return traineeDao.findAll();
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