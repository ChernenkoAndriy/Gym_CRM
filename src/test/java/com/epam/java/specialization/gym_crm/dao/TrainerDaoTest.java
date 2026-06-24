package com.epam.java.specialization.gym_crm.dao;

import com.epam.java.specialization.gym_crm.dao.implementations.AbstractMapDao;
import com.epam.java.specialization.gym_crm.dao.implementations.TrainerDao;
import com.epam.java.specialization.gym_crm.model.Trainer;

class TrainerDaoTest extends AbstractMapDaoTest<Trainer> {

    @Override
    protected AbstractMapDao<Trainer> getDaoInstance() {
        return new TrainerDao();
    }

    @Override
    protected Trainer createSampleEntity() {
        return Trainer.builder()
                .firstName("Elena")
                .lastName("Kostova")
                .trainingTypeId(1L) // ID спеціалізації
                .build();
    }

    @Override
    protected void updateSampleEntity(Trainer entity) {
        entity.setTrainingTypeId(2L);
    }
}