package com.epam.java.specialization.gym_crm.dao.implementations;

import com.epam.java.specialization.gym_crm.dao.intefaces.ITrainerDao;
import com.epam.java.specialization.gym_crm.model.Trainee;
import com.epam.java.specialization.gym_crm.model.Trainer;
import com.epam.java.specialization.gym_crm.model.Training;
import com.epam.java.specialization.gym_crm.model.User;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public class TrainerDao extends AbstractJpaDao<Trainer> implements ITrainerDao {

    public TrainerDao() {
        super(Trainer.class);
    }

    @Override
    public Optional<Trainer> findByUsername(String username) {
        List<Trainer> result = entityManager.createQuery(
                        "SELECT t FROM Trainer t WHERE t.user.username = :username", Trainer.class)
                .setParameter("username", username)
                .getResultList();
        return result.stream().findFirst();
    }

    @Override
    public List<Trainer> findAvailableTrainersNotAssignedToTrainee(String traineeUsername) {
        String jpql = "SELECT t FROM Trainer t WHERE t.user.isActive = true AND NOT EXISTS " +
                "(SELECT 1 FROM Trainee tr JOIN tr.trainers trt WHERE tr.user.username = :username AND trt.id = t.id)";
        return entityManager.createQuery(jpql, Trainer.class)
                .setParameter("username", traineeUsername)
                .getResultList();
    }

    @Override
    public List<Training> findTrainingsByCriteria(String username, Date fromDate, Date toDate, String traineeName) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Training> query = cb.createQuery(Training.class);
        Root<Training> root = query.from(Training.class);

        root.fetch("trainee", JoinType.INNER).fetch("user", JoinType.INNER);
        root.fetch("trainer", JoinType.INNER).fetch("user", JoinType.INNER);
        root.fetch("trainingType", JoinType.INNER);

        Join<Training, Trainer> trainerJoin = root.join("trainer", JoinType.INNER);
        Join<Trainer, User> trainerUserJoin = trainerJoin.join("user", JoinType.INNER);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(trainerUserJoin.get("username"), username));

        if (fromDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("trainingDate"), fromDate));
        }
        if (toDate != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("trainingDate"), toDate));
        }
        if (traineeName != null && !traineeName.trim().isEmpty()) {
            Join<Training, Trainee> traineeJoin = root.join("trainee", JoinType.INNER);
            Join<Trainee, User> traineeUserJoin = traineeJoin.join("user", JoinType.INNER);
            predicates.add(cb.equal(traineeUserJoin.get("username"), traineeName));
        }

        query.select(root).distinct(true).where(predicates.toArray(new Predicate[0]));
        return entityManager.createQuery(query).getResultList();
    }
}