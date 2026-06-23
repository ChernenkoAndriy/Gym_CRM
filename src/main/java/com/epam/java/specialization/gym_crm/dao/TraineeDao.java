package com.epam.java.specialization.gym_crm.dao;

import com.epam.java.specialization.gym_crm.model.Trainee;
import org.springframework.stereotype.Repository;

@Repository
public class TraineeDao extends AbstractDao<Trainee> implements IDeleteDao<Trainee> {

    @Override
    protected Class<Trainee> getEntityClass() {
        return Trainee.class;
    }

    @Override
    public void deleteById(Long id) {
        logger.info("DAO: Deleting Trainee with ID: {}", id);
        getStorageMap().remove(id);
    }
}