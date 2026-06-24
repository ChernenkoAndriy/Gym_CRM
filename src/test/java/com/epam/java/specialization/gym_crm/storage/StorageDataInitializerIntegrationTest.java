package com.epam.java.specialization.gym_crm.storage;

import com.epam.java.specialization.gym_crm.config.TestConfig;
import com.epam.java.specialization.gym_crm.model.Trainee;
import com.epam.java.specialization.gym_crm.model.Trainer;
import com.epam.java.specialization.gym_crm.model.Training;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class StorageDataInitializerIntegrationTest {

    @Autowired
    private Storage storage;

    @Test
    void postProcessAfterInitialization_ShouldPrePopulateStorageFromJsonFile() {
        assertNotNull(storage, "Storage bean must be present in application context");

        assertFalse(storage.getTraineeMap().isEmpty(), "Trainee map should not be empty after initial file processing");
        assertTrue(storage.getTraineeMap().containsKey(1L), "Should contain trainee with ID 1");

        Trainee alex = storage.getTraineeMap().get(1L);
        assertEquals("Alex", alex.getFirstName());
        assertEquals("Smith", alex.getLastName());
        assertEquals("Kyiv, Khreshchatyk 1", alex.getAddress());

        assertFalse(storage.getTrainerMap().isEmpty(), "Trainer map should not be empty after initial file processing");
        assertTrue(storage.getTrainerMap().containsKey(2L), "Should contain trainer with ID 2");

        Trainer elena = storage.getTrainerMap().get(2L);
        assertEquals("Elena", elena.getFirstName());
        assertEquals("Kostova", elena.getLastName());
        assertEquals(2L, elena.getTrainingTypeId(), "Trainer specialization reference mapping mismatch");

        assertFalse(storage.getTrainingMap().isEmpty(), "Training map should not be empty after initial file processing");
        assertEquals(3, storage.getTrainingMap().size(), "Should load exactly 3 training entries from init-data.json");

        Training firstTraining = storage.getTrainingMap().get(1L);
        assertEquals("Morning Yoga Session", firstTraining.getTrainingName());
        assertEquals(1L, firstTraining.getTraineeId());
        assertEquals(1L, firstTraining.getTrainerId());
        assertEquals(60, firstTraining.getTrainingDuration());

        System.out.println("=== Storage Data Pre-population Audit ===");
        System.out.println("Loaded Trainees count: " + storage.getTraineeMap().size());
        System.out.println("Loaded Trainers count:  " + storage.getTrainerMap().size());
        System.out.println("Loaded Trainings count: " + storage.getTrainingMap().size());
        System.out.println("=========================================");
    }
}