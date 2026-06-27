package com.epam.java.specialization.gym_crm.dao.implementations;

import com.epam.java.specialization.gym_crm.dao.intefaces.ITrainingTypeDao;
import com.epam.java.specialization.gym_crm.model.TrainingType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import java.util.Map;

@Repository
public class TrainingTypeDao extends AbstractMapDao<TrainingType> implements ITrainingTypeDao {

    @Autowired
    @Override
    public void setStorage(@Qualifier("trainingTypeStorage") Map<Long, TrainingType> storage) {
        super.setStorage(storage);
    }
}