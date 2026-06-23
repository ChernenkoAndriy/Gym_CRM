package com.epam.java.specialization.gym_crm.storage;

import com.epam.java.specialization.gym_crm.model.Training;
import org.springframework.stereotype.Component;
import java.util.List;

@Component("trainingStorage")
public class TrainingStorage extends EntityStorage<Training, Long> {

    @Override
    public void initializeData(List<Training> entities) {
        if (entities == null) return;
        for (Training training : entities) {
            put(training.getId(), training);
        }
    }

    @Override
    public Class<Training> getEntityClass() {
        return Training.class;
    }

    @Override
    public String getJsonStorageKey() { return "trainings"; }
}