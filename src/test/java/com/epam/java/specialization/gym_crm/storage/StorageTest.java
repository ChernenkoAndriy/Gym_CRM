package com.epam.java.specialization.gym_crm.storage;

import com.epam.java.specialization.gym_crm.model.Trainee;
import com.epam.java.specialization.gym_crm.model.Trainer;
import com.epam.java.specialization.gym_crm.model.Training;
import org.junit.jupiter.api.Test;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

class StorageTest {

    @Test
    void constructor_ShouldInitializeInternalMapsCorrectly() {
        Map<Long, Trainee> traineeMap = new ConcurrentHashMap<>();
        Map<Long, Trainer> trainerMap = new ConcurrentHashMap<>();
        Map<Long, Training> trainingMap = new ConcurrentHashMap<>();

        Storage storage = new Storage(traineeMap, trainerMap, trainingMap);

        assertNotNull(storage.getTraineeMap(), "Trainee map should be accessible");
        assertNotNull(storage.getTrainerMap(), "Trainer map should be accessible");
        assertNotNull(storage.getTrainingMap(), "Training map should be accessible");

        assertSame(traineeMap, storage.getTraineeMap(), "Should hold references to passed maps");
    }
}