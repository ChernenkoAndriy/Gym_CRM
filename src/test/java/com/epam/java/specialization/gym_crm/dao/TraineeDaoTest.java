package com.epam.java.specialization.gym_crm.dao;

import com.epam.java.specialization.gym_crm.dao.implementations.AbstractMapDao;
import com.epam.java.specialization.gym_crm.dao.implementations.TraineeDao;
import com.epam.java.specialization.gym_crm.model.Trainee;

class TraineeDaoTest extends AbstractMapDaoTest<Trainee> {

    @Override
    protected AbstractMapDao<Trainee> getDaoInstance() {
        return new TraineeDao();
    }

    @Override
    protected Trainee createSampleEntity() {
        return Trainee.builder()
                .firstName("Alex")
                .lastName("Smith")
                .address("Kyiv")
                .build();
    }

    @Override
    protected void updateSampleEntity(Trainee entity) {
        entity.setAddress("Lviv");
    }
}