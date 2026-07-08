package com.epam.java.specialization.gym_crm.dao.implementations;

import com.epam.java.specialization.gym_crm.dao.intefaces.ITraineeDao;
import com.epam.java.specialization.gym_crm.model.Trainee;
import com.epam.java.specialization.gym_crm.model.Trainer;
import com.epam.java.specialization.gym_crm.model.Training;
import com.epam.java.specialization.gym_crm.model.TrainingType;
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
public class TraineeDao extends AbstractJpaDao<Trainee> implements ITraineeDao {
    public TraineeDao() {
        super(Trainee.class);
    }

    @Override
    public Optional<Trainee> findByUsername(String username) {
        List<Trainee> result = entityManager.createQuery(
                        "SELECT t FROM Trainee t WHERE t.user.username = :username", Trainee.class)
                .setParameter("username", username)
                .getResultList();
        return result.stream().findFirst();
    }

    @Override
    public List<Training> findTrainingsByCriteria(String username, Date fromDate, Date toDate, String trainerName, String trainingType) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Training> query = cb.createQuery(Training.class);
        Root<Training> root = query.from(Training.class);
        root.fetch("trainee", JoinType.INNER).fetch("user", JoinType.INNER);
        root.fetch("trainer", JoinType.INNER).fetch("user", JoinType.INNER);
        root.fetch("trainingType", JoinType.INNER);
        Join<Training, Trainee> traineeJoin = root.join("trainee", JoinType.INNER);
        Join<Trainee, User> traineeUserJoin = traineeJoin.join("user", JoinType.INNER);
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(traineeUserJoin.get("username"), username));
        if (fromDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("trainingDate"), fromDate));
        }
        if (toDate != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("trainingDate"), toDate));
        }
        if (trainerName != null && !trainerName.trim().isEmpty()) {
            Join<Training, Trainer> trainerJoin = root.join("trainer", JoinType.INNER);
            Join<Trainer, User> trainerUserJoin = trainerJoin.join("user", JoinType.INNER);
            predicates.add(cb.equal(trainerUserJoin.get("username"), trainerName));
        }
        if (trainingType != null && !trainingType.trim().isEmpty()) {
            Join<Training, TrainingType> typeJoin = root.join("trainingType", JoinType.INNER);
            predicates.add(cb.equal(typeJoin.get("trainingTypeName"), trainingType));
        }
        query.select(root).distinct(true).where(predicates.toArray(new Predicate[0]));
        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public List<Trainer> findTrainersByUsernames(List<String> usernames) {
        return entityManager.createQuery("SELECT t FROM Trainer t WHERE t.user.username IN :usernames", Trainer.class)
                .setParameter("usernames", usernames)
                .getResultList();
    }
}