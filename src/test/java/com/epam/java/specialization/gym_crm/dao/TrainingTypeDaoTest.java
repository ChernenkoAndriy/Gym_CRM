package com.epam.java.specialization.gym_crm.dao;

import com.epam.java.specialization.gym_crm.dao.implementations.AbstractMapDao;
import com.epam.java.specialization.gym_crm.dao.implementations.TrainingTypeDao;
import com.epam.java.specialization.gym_crm.model.TrainingType;

class TrainingTypeDaoTest extends AbstractMapDaoTest<TrainingType> {

    @Override
    protected AbstractMapDao<TrainingType> getDaoInstance() {
        return new TrainingTypeDao();
    }

    @Override
    protected TrainingType createSampleEntity() {
        return TrainingType.builder()
                .trainingTypeName("Crossfit")
                .build();
    }

    @Override
    protected void updateSampleEntity(TrainingType entity) {
        entity.setTrainingTypeName("Crossfit WOD");
    }
}