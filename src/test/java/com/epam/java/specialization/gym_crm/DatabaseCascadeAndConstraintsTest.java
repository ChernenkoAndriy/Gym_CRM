package com.epam.java.specialization.gym_crm;

import com.epam.java.specialization.gym_crm.model.*;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@Rollback
class DatabaseCascadeAndConstraintsTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("Cascade Delete: Should automatically delete Training when Trainee is removed via OnDeleteAction.CASCADE")
    void testTraineeDeletion_ShouldCascadeDeleteTrainings() {
        
        TrainingType type = TrainingType.builder().trainingTypeName("Yoga").build();
        entityManager.persist(type);

        User traineeUser = User.builder().firstName("Alex").lastName("Smith").username("Alex.Smith").password("pass").isActive(true).build();
        Trainee trainee = Trainee.builder().user(traineeUser).address("Kyiv").build();
        entityManager.persist(trainee);

        User trainerUser = User.builder().firstName("John").lastName("Smith").username("John.Smith").password("pass").isActive(true).build();
        Trainer trainer = Trainer.builder().user(trainerUser).specialization(type).build();
        entityManager.persist(trainer);

        Training training = Training.builder()
                .trainee(trainee)
                .trainer(trainer)
                .trainingType(type)
                .trainingName("Morning Session")
                .trainingDate(new Date())
                .trainingDuration(60)
                .build();
        entityManager.persist(training);

        entityManager.flush();
        entityManager.clear(); 

        
        Trainee managedTrainee = entityManager.find(Trainee.class, trainee.getId());
        assertNotNull(managedTrainee);
        entityManager.remove(managedTrainee);
        entityManager.flush(); 

        
        Training foundTraining = entityManager.find(Training.class, training.getId());
        assertNull(foundTraining, "Training record should be automatically deleted via cascade option.");
    }

    @Test
    @DisplayName("Unique Constraint: Should throw PersistenceException when saving users with identical usernames")
    void testDuplicateUsername_ShouldThrowPersistenceException() {
        
        User user1 = User.builder()
                .firstName("Andriy")
                .lastName("Chernenko")
                .username("unique.username")
                .password("password123")
                .isActive(true)
                .build();
        entityManager.persist(user1);
        entityManager.flush();

        
        User user2 = User.builder()
                .firstName("John")
                .lastName("Doe")
                .username("unique.username") 
                .password("securePassword")
                .isActive(true)
                .build();

        assertThrows(PersistenceException.class, () -> {
            entityManager.persist(user2);
            entityManager.flush(); 
        }, "Saving identical usernames must trigger a database unique constraint violation.");
    }
}