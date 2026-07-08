package com.epam.java.specialization.gym_crm.service.implementations;

import com.epam.java.specialization.gym_crm.dao.intefaces.ITraineeDao;
import com.epam.java.specialization.gym_crm.dao.intefaces.ITrainerDao;
import com.epam.java.specialization.gym_crm.model.Trainer;
import com.epam.java.specialization.gym_crm.model.User;
import com.epam.java.specialization.gym_crm.service.interfaces.ITrainerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import com.epam.java.specialization.gym_crm.model.Training;
import com.epam.java.specialization.gym_crm.model.Trainee;
import java.util.Date;

@Service
@Transactional
public class TrainerService extends AbstractUserService implements ITrainerService {

    private ITrainerDao trainerDao;
    private ITraineeDao traineeDao;

    @Autowired
    public void setTrainerDao(ITrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }

    @Autowired
    public void setTraineeDao(ITraineeDao traineeDao) {
        this.traineeDao = traineeDao;
    }

    @Override
    public Trainer create(Trainer trainer) {
        logger.debug("Attempting to create trainer profile: {} {}", trainer.getUser().getFirstName(), trainer.getUser().getLastName());
        prepareUserProfile(trainer.getUser());
        return trainerDao.create(trainer);
    }

    @Override
    public Trainer update(Trainer trainer) {
        logger.debug("Updating trainer profile with ID: {}", trainer.getId());
        return trainerDao.update(trainer);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Trainer> getById(Long id) {
        logger.debug("Selecting trainer profile with ID: {}", id);
        return trainerDao.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Training> getTrainingsByCriteria(String username, Date fromDate, Date toDate, String traineeName, int page, int size) {
        logger.debug("Fetching trainer trainings by criteria for username: {}", username);
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Training> query = cb.createQuery(Training.class);
        Root<Training> root = query.from(Training.class);
        root.fetch("trainee", jakarta.persistence.criteria.JoinType.INNER).fetch("user", jakarta.persistence.criteria.JoinType.INNER);
        root.fetch("trainer", jakarta.persistence.criteria.JoinType.INNER).fetch("user", jakarta.persistence.criteria.JoinType.INNER);
        root.fetch("trainingType", jakarta.persistence.criteria.JoinType.INNER);

        Join<Training, Trainer> trainerJoin = root.join("trainer", jakarta.persistence.criteria.JoinType.INNER);
        Join<Trainer, User> trainerUserJoin = trainerJoin.join("user", jakarta.persistence.criteria.JoinType.INNER);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(trainerUserJoin.get("username"), username));

        if (fromDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("trainingDate"), fromDate));
        }
        if (toDate != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("trainingDate"), toDate));
        }
        if (traineeName != null && !traineeName.trim().isEmpty()) {
            Join<Training, Trainee> traineeJoin = root.join("trainee", jakarta.persistence.criteria.JoinType.INNER);
            Join<Trainee, User> traineeUserJoin = traineeJoin.join("user", jakarta.persistence.criteria.JoinType.INNER);
            predicates.add(cb.equal(traineeUserJoin.get("username"), traineeName));
        }

        query.select(root).distinct(true).where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(query)
                .setFirstResult((page - 1) * size)
                .setMaxResults(size)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Trainer> getAvailableTrainersNotAssignedToTrainee(String traineeUsername) {
        logger.debug("Fetching available trainers not assigned to trainee: {}", traineeUsername);
        String jpql = "SELECT t FROM Trainer t WHERE t.user.isActive = true AND NOT EXISTS " +
                "(SELECT 1 FROM Trainee tr JOIN tr.trainers trt WHERE tr.user.username = :username AND trt.id = t.id)";
        return entityManager.createQuery(jpql, Trainer.class)
                .setParameter("username", traineeUsername)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean authenticate(String username, String password) {
        return super.authenticate(username, password);
    }

    @Override
    public void toggleActivation(String username, boolean isActive) {
        logger.info("Attempting to toggle activation status to {} for trainer username: {}", isActive, username);
        List<Trainer> result = entityManager.createQuery(
                        "SELECT t FROM Trainer t WHERE t.user.username = :username", Trainer.class)
                .setParameter("username", username)
                .getResultList();
        Trainer trainer = result.stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found with username: " + username));
        if (trainer.getUser().getIsActive() == isActive) {
            logger.error("Failed to toggle activation. Status for trainer {} is already {}", username, isActive);
            throw new IllegalStateException("Status is already " + isActive);
        }
        trainer.getUser().setIsActive(isActive);
        trainerDao.update(trainer);
        logger.info("Successfully changed activation status to {} for trainer username: {}", isActive, username);
    }
}