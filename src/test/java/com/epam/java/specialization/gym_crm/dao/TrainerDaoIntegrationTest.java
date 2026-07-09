package com.epam.java.specialization.gym_crm.dao;

import com.epam.java.specialization.gym_crm.AbstractIntegrationTest;
import com.epam.java.specialization.gym_crm.dao.implementations.TrainerDao;
import com.epam.java.specialization.gym_crm.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
class TrainerDaoIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TrainerDao trainerDao;

    private Trainee trainee;
    private Trainer assignedTrainer;
    private Trainer unassignedActiveTrainer;
    private Trainer unassignedInactiveTrainer;
    private TrainingType fitnessType;
    private Date trainingDate;

    @BeforeEach
    void setUpData() {
        
        fitnessType = TrainingType.builder().trainingTypeName("Fitness").build();
        entityManager.persist(fitnessType);

        
        User traineeUser = User.builder().firstName("Andriy").lastName("Chernenko").username("Andriy.Chernenko").password("pass").isActive(true).build();
        trainee = Trainee.builder().user(traineeUser).address("Kyiv").trainers(new ArrayList<>()).build();
        entityManager.persist(trainee);

        
        
        User user1 = User.builder().firstName("Ivan").lastName("Prokopchyk").username("Ivan.Prokopchyk").password("pass").isActive(true).build();
        assignedTrainer = Trainer.builder().user(user1).specialization(fitnessType).build();
        entityManager.persist(assignedTrainer);

        
        User user2 = User.builder().firstName("Elena").lastName("Kostova").username("Elena.Kostova").password("pass").isActive(true).build();
        unassignedActiveTrainer = Trainer.builder().user(user2).specialization(fitnessType).build();
        entityManager.persist(unassignedActiveTrainer);

        
        User user3 = User.builder().firstName("Alex").lastName("Smith").username("Alex.Smith").password("pass").isActive(false).build();
        unassignedInactiveTrainer = Trainer.builder().user(user3).specialization(fitnessType).build();
        entityManager.persist(unassignedInactiveTrainer);

        
        trainee.getTrainers().add(assignedTrainer);
        entityManager.merge(trainee);

        
        Calendar cal = Calendar.getInstance();
        cal.set(2026, Calendar.JUNE, 15, 12, 0);
        trainingDate = cal.getTime();

        Training training = Training.builder()
                .trainee(trainee)
                .trainer(assignedTrainer)
                .trainingType(fitnessType)
                .trainingName("Personal Fitness Workout")
                .trainingDate(trainingDate)
                .trainingDuration(60)
                .build();
        entityManager.persist(training);

        entityManager.flush();
    }

    

    @Test
    @DisplayName("findByUsername: Should find Trainer by valid username")
    void testFindByUsername_Success() {
        Optional<Trainer> found = trainerDao.findByUsername("Ivan.Prokopchyk");
        assertTrue(found.isPresent());
        assertEquals("Fitness", found.get().getSpecialization().getTrainingTypeName());
    }

    @Test
    @DisplayName("findByUsername: Should return empty Optional when username does not exist")
    void testFindByUsername_NotFound() {
        Optional<Trainer> found = trainerDao.findByUsername("Ghost.Trainer");
        assertFalse(found.isPresent());
    }

    

    @Test
    @DisplayName("findAvailableTrainersNotAssignedToTrainee: Should return only unassigned and active trainers")
    void testFindAvailableTrainersNotAssignedToTrainee_FiltersCorrectly() {
        
        List<Trainer> availableTrainers = trainerDao.findAvailableTrainersNotAssignedToTrainee("Andriy.Chernenko");

        
        
        assertEquals(1, availableTrainers.size(), "Should return exactly 1 trainer");

        Trainer resultTrainer = availableTrainers.get(0);
        assertEquals("Elena.Kostova", resultTrainer.getUser().getUsername(), "Should return Elena.Kostova as she is active and unassigned");
        assertTrue(resultTrainer.getUser().getIsActive(), "Returned trainer must be active");
    }

    

    @Test
    @DisplayName("findTrainingsByCriteria: Should filter trainer's trainings by criteria properly")
    void testFindTrainingsByCriteria_Success() {
        List<Training> trainings = trainerDao.findTrainingsByCriteria(
                "Ivan.Prokopchyk",
                trainingDate,
                null,
                "Andriy.Chernenko"
        );

        assertEquals(1, trainings.size());
        assertEquals("Personal Fitness Workout", trainings.get(0).getTrainingName());
    }

    @Test
    @DisplayName("findTrainingsByCriteria: Should return all trainer's trainings when dates and student filters are null")
    void testFindTrainingsByCriteria_WithNullFilters() {
        List<Training> trainings = trainerDao.findTrainingsByCriteria(
                "Ivan.Prokopchyk",
                null,
                null,
                null
        );

        assertEquals(1, trainings.size());
    }
}