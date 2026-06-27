package com.epam.java.specialization.gym_crm.dao.implementations;

import com.epam.java.specialization.gym_crm.dao.intefaces.ITrainerDao;
import com.epam.java.specialization.gym_crm.model.Trainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import java.util.Map;

@Repository
public class TrainerDao extends AbstractMapDao<Trainer> implements ITrainerDao {

    @Autowired
    @Override
    public void setStorage(@Qualifier("trainerStorage") Map<Long, Trainer> storage) {
        super.setStorage(storage);
    }
}