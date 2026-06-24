package com.epam.java.specialization.gym_crm.dao.implementations;

import com.epam.java.specialization.gym_crm.dao.intefaces.ITraineeDao;
import com.epam.java.specialization.gym_crm.model.Trainee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import java.util.Map;

@Repository
public class TraineeDao extends AbstractMapDao<Trainee> implements ITraineeDao {

    @Autowired
    @Qualifier("traineeStorage")
    @Override
    public void setStorage(Map<Long, Trainee> storage) {
        super.setStorage(storage);
    }
}