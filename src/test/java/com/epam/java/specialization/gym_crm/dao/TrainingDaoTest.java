package com.epam.java.specialization.gym_crm.dao;

import com.epam.java.specialization.gym_crm.dao.implementations.AbstractMapDao;
import com.epam.java.specialization.gym_crm.dao.implementations.TrainingDao;
import com.epam.java.specialization.gym_crm.model.Training;
import java.util.Date;

class TrainingDaoTest extends AbstractMapDaoTest<Training> {

    private final Date trainingDate = new Date();

    @Override
    protected AbstractMapDao<Training> getDaoInstance() {
        return new TrainingDao();
    }

    @Override
    protected Training createSampleEntity() {
        return Training.builder()
                .trainingName("Morning Yoga Session")
                .traineeId(10L)
                .trainerId(20L)
                .trainingTypeId(1L)
                .trainingDate(trainingDate)
                .trainingDuration(60)
                .build();
    }

    @Override
    protected void updateSampleEntity(Training entity) {
        entity.setTrainingName("Intense Power Yoga");
        entity.setTrainingDuration(90);
    }
}