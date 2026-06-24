package com.epam.java.specialization.gym_crm.storage;

import com.epam.java.specialization.gym_crm.model.Trainee;
import com.epam.java.specialization.gym_crm.model.Trainer;
import com.epam.java.specialization.gym_crm.model.Training;
import lombok.Getter;
import org.springframework.stereotype.Component;
import java.util.Map;

@Getter
@Component
public class Storage {

    private final Map<Long, Trainee> traineeMap;
    private final Map<Long, Trainer> trainerMap;
    private final Map<Long, Training> trainingMap;

    public Storage(Map<Long, Trainee> traineeStorage,
                   Map<Long, Trainer> trainerStorage,
                   Map<Long, Training> trainingStorage) {
        this.traineeMap = traineeStorage;
        this.trainerMap = trainerStorage;
        this.trainingMap = trainingStorage;
    }


}