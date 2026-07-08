package com.epam.java.specialization.gym_crm.dao.implementations;

import com.epam.java.specialization.gym_crm.dao.intefaces.ITrainingTypeDao;
import com.epam.java.specialization.gym_crm.model.TrainingType;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public class TrainingTypeDao extends AbstractJpaDao<TrainingType> implements ITrainingTypeDao {
    public TrainingTypeDao() {
        super(TrainingType.class);
    }

    @Override
    public Optional<TrainingType> findByName(String name) {
        return entityManager.createQuery("SELECT t FROM TrainingType t WHERE t.trainingTypeName = :name", TrainingType.class)
                .setParameter("name", name)
                .getResultList()
                .stream()
                .findFirst();
    }
}