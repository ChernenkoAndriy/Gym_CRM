package com.epam.java.specialization.gym_crm.dao;

import com.epam.java.specialization.gym_crm.AbstractIntegrationTest;
import com.epam.java.specialization.gym_crm.dao.implementations.TraineeDao;
import com.epam.java.specialization.gym_crm.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
class TraineeDaoIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TraineeDao traineeDao;

    private Trainee trainee;
    private Trainer trainer1;
    private Trainer trainer2;
    private TrainingType yogaType;
    private TrainingType crossfitType;
    private Date date1;
    private Date date2;

    @BeforeEach
    void setUpData() {

        yogaType = TrainingType.builder().trainingTypeName("Yoga").build();
        crossfitType = TrainingType.builder().trainingTypeName("Crossfit").build();
        entityManager.persist(yogaType);
        entityManager.persist(crossfitType);


        User userTrainee = User.builder().firstName("Andriy").lastName("Chernenko").username("Andriy.Chernenko").password("pass").isActive(true).build();
        trainee = Trainee.builder().user(userTrainee).address("Kyiv").build();
        entityManager.persist(trainee);


        User userTrainer1 = User.builder().firstName("Ivan").lastName("Prokopchyk").username("Ivan.Prokopchyk").password("pass").isActive(true).build();
        trainer1 = Trainer.builder().user(userTrainer1).specialization(yogaType).build();
        entityManager.persist(trainer1);

        User userTrainer2 = User.builder().firstName("Elena").lastName("Kostova").username("Elena.Kostova").password("pass").isActive(true).build();
        trainer2 = Trainer.builder().user(userTrainer2).specialization(crossfitType).build();
        entityManager.persist(trainer2);


        Calendar cal = Calendar.getInstance();
        cal.set(2026, Calendar.JUNE, 10, 10, 0);
        date1 = cal.getTime();
        cal.set(2026, Calendar.JUNE, 20, 18, 0);
        date2 = cal.getTime();


        Training t1 = Training.builder().trainee(trainee).trainer(trainer1).trainingType(yogaType).trainingName("Morning Yoga").trainingDate(date1).trainingDuration(60).build();
        Training t2 = Training.builder().trainee(trainee).trainer(trainer2).trainingType(crossfitType).trainingName("Power Crossfit").trainingDate(date2).trainingDuration(45).build();
        entityManager.persist(t1);
        entityManager.persist(t2);

        entityManager.flush();
    }

    

    @Test
    @DisplayName("findByUsername: Should find Trainee by valid username")
    void testFindByUsername_Success() {
        Optional<Trainee> found = traineeDao.findByUsername("Andriy.Chernenko");
        assertTrue(found.isPresent());
        assertEquals("Kyiv", found.get().getAddress());
    }

    @Test
    @DisplayName("findByUsername: Should return empty Optional when username does not exist")
    void testFindByUsername_NotFound() {
        Optional<Trainee> found = traineeDao.findByUsername("NonExistent.User");
        assertFalse(found.isPresent());
    }

    

    @Test
    @DisplayName("findTrainersByUsernames: Should return correct trainers matching list of usernames")
    void testFindTrainersByUsernames_Success() {
        List<Trainer> trainers = traineeDao.findTrainersByUsernames(List.of("Ivan.Prokopchyk", "Elena.Kostova"));
        assertEquals(2, trainers.size());
    }

    

    @Test
    @DisplayName("findTrainingsByCriteria: Should filter dynamically by combination of all parameters")
    void testFindTrainingsByCriteria_WithFullFilter() {
        
        List<Training> results = traineeDao.findTrainingsByCriteria(
                "Andriy.Chernenko",
                date1,
                null,
                "Ivan.Prokopchyk",
                "Yoga"
        );

        assertEquals(1, results.size());
        assertEquals("Morning Yoga", results.get(0).getTrainingName());
    }

    @Test
    @DisplayName("findTrainingsByCriteria: Should filter dynamically only by 'toDate' date constraint")
    void testFindTrainingsByCriteria_WithToDateOnly() {
        
        List<Training> results = traineeDao.findTrainingsByCriteria(
                "Andriy.Chernenko",
                null,
                date1,
                null,
                null
        );

        assertEquals(1, results.size());
        assertEquals("Morning Yoga", results.get(0).getTrainingName());
    }

    @Test
    @DisplayName("findTrainingsByCriteria: Should return all trainings for trainee when all optional filters are null or empty")
    void testFindTrainingsByCriteria_WithNullAndEmptyFilters() {
        
        List<Training> results = traineeDao.findTrainingsByCriteria(
                "Andriy.Chernenko",
                null,
                null,
                "   ",
                ""
        );

        
        assertEquals(2, results.size());
    }
}