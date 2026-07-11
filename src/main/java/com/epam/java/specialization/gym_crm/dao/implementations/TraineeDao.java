package com.epam.java.specialization.gym_crm.dao.implementations;

import com.epam.java.specialization.gym_crm.dao.intefaces.ITraineeDao;
import com.epam.java.specialization.gym_crm.model.*;
import jakarta.persistence.criteria.*;
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

        root.fetch(Training_.trainee, JoinType.INNER).fetch(Trainee_.user, JoinType.INNER);
        root.fetch(Training_.trainer, JoinType.INNER).fetch(Trainer_.user, JoinType.INNER);
        root.fetch(Training_.trainingType, JoinType.INNER);

        Join<Training, Trainee> traineeJoin = root.join(Training_.trainee, JoinType.INNER);
        Join<Trainee, User> traineeUserJoin = traineeJoin.join(Trainee_.user, JoinType.INNER);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(traineeUserJoin.get(User_.username), username));

        if (fromDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get(Training_.trainingDate), fromDate));
        }
        if (toDate != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get(Training_.trainingDate), toDate));
        }
        if (trainerName != null && !trainerName.trim().isEmpty()) {
            Join<Training, Trainer> trainerJoin = root.join(Training_.trainer, JoinType.INNER);
            Join<Trainer, User> trainerUserJoin = trainerJoin.join(Trainer_.user, JoinType.INNER);
            predicates.add(cb.equal(trainerUserJoin.get(User_.username), trainerName));
        }
        if (trainingType != null && !trainingType.trim().isEmpty()) {
            Join<Training, TrainingType> typeJoin = root.join(Training_.trainingType, JoinType.INNER);
            predicates.add(cb.equal(typeJoin.get(TrainingType_.trainingTypeName), trainingType));
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