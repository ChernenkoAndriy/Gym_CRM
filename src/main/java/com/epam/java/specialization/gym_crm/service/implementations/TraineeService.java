package com.epam.java.specialization.gym_crm.service.implementations;

import com.epam.java.specialization.gym_crm.dao.intefaces.ITraineeDao;
import com.epam.java.specialization.gym_crm.model.Trainee;
import com.epam.java.specialization.gym_crm.service.interfaces.ITraineeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import com.epam.java.specialization.gym_crm.model.Training;
import com.epam.java.specialization.gym_crm.model.Trainer;
import com.epam.java.specialization.gym_crm.model.TrainingType;
import com.epam.java.specialization.gym_crm.model.User;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TraineeService extends AbstractUserService implements ITraineeService {

    private ITraineeDao traineeDao;

    @Autowired
    public void setTraineeDao(ITraineeDao traineeDao) {
        this.traineeDao = traineeDao;
    }

    @Override
    public Trainee create(Trainee trainee) {
        logger.debug("Attempting to create trainee profile: {} {}",
                trainee.getUser().getFirstName(), trainee.getUser().getLastName());
        prepareUserProfile(trainee.getUser());
        return traineeDao.create(trainee);
    }

    @Override
    public Trainee update(Trainee trainee) {
        logger.debug("Updating trainee profile with ID: {}", trainee.getId());
        return traineeDao.update(trainee);
    }

    @Override
    public void delete(Long id) {
        logger.warn("Deleting trainee profile with ID: {}", id);
        traineeDao.delete(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Trainee> getById(Long id) {
        logger.debug("Selecting trainee profile with ID: {}", id);
        return traineeDao.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Trainee> getByUsername(String username) {
        logger.debug("Selecting trainee profile with username: {}", username);
        List<Trainee> result = entityManager.createQuery(
                        "SELECT t FROM Trainee t WHERE t.user.username = :username", Trainee.class)
                .setParameter("username", username)
                .getResultList();
        return result.stream().findFirst();
    }

    @Override
    public void deleteByUsername(String username) {
        logger.warn("Attempting hard delete of trainee profile by username: {}", username);
        Trainee trainee = getByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found with username: " + username));
        if (trainee.getTrainers() != null) {
            trainee.getTrainers().clear();
        }
        traineeDao.update(trainee);
        entityManager.flush();
        traineeDao.delete(trainee.getId());
        logger.info("Successfully deleted trainee and all associated trainings for username: {}", username);
    }

    @Override
    public void toggleActivation(String username, boolean isActive) {
        logger.info("Attempting to toggle activation status to {} for username: {}", isActive, username);
        Trainee trainee = getByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found with username: " + username));
        if (trainee.getUser().getIsActive() == isActive) {
            logger.error("Failed to toggle activation. Status for {} is already {}", username, isActive);
            throw new IllegalStateException("Status is already " + isActive);
        }
        trainee.getUser().setIsActive(isActive);
        traineeDao.update(trainee);
        logger.info("Successfully changed activation status to {} for username: {}", isActive, username);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Training> getTrainingsByCriteria(String username, Date fromDate, Date toDate, String trainerName, String trainingType, int page, int size) {
        logger.debug("Fetching trainee trainings by criteria for username: {}", username);
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Training> query = cb.createQuery(Training.class);
        Root<Training> root = query.from(Training.class);
        root.fetch("trainee", jakarta.persistence.criteria.JoinType.INNER).fetch("user", jakarta.persistence.criteria.JoinType.INNER);
        root.fetch("trainer", jakarta.persistence.criteria.JoinType.INNER).fetch("user", jakarta.persistence.criteria.JoinType.INNER);
        root.fetch("trainingType", jakarta.persistence.criteria.JoinType.INNER);

        Join<Training, Trainee> traineeJoin = root.join("trainee", jakarta.persistence.criteria.JoinType.INNER);
        Join<Trainee, User> traineeUserJoin = traineeJoin.join("user", jakarta.persistence.criteria.JoinType.INNER);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(traineeUserJoin.get("username"), username));

        if (fromDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("trainingDate"), fromDate));
        }
        if (toDate != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("trainingDate"), toDate));
        }
        if (trainerName != null && !trainerName.trim().isEmpty()) {
            Join<Training, Trainer> trainerJoin = root.join("trainer", jakarta.persistence.criteria.JoinType.INNER);
            Join<Trainer, User> trainerUserJoin = trainerJoin.join("user", jakarta.persistence.criteria.JoinType.INNER);
            predicates.add(cb.equal(trainerUserJoin.get("username"), trainerName));
        }
        if (trainingType != null && !trainingType.trim().isEmpty()) {
            Join<Training, TrainingType> typeJoin = root.join("trainingType", jakarta.persistence.criteria.JoinType.INNER);
            predicates.add(cb.equal(typeJoin.get("trainingTypeName"), trainingType));
        }

        query.select(root).distinct(true).where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(query)
                .setFirstResult((page - 1) * size)
                .setMaxResults(size)
                .getResultList();
    }

    @Override
    public void updateTrainersList(String traineeUsername, List<String> trainerUsernames) {
        logger.info("Updating trainers list for trainee: {}", traineeUsername);
        Trainee trainee = getByUsername(traineeUsername)
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found with username: " + traineeUsername));
        if (trainerUsernames == null || trainerUsernames.isEmpty()) {
            trainee.getTrainers().clear();
            traineeDao.update(trainee);
            entityManager.flush();
            return;
        }
        String jpql = "SELECT t FROM Trainer t WHERE t.user.username IN :usernames";
        List<Trainer> newTrainers = entityManager.createQuery(jpql, Trainer.class)
                .setParameter("usernames", trainerUsernames)
                .getResultList();
        trainee.setTrainers(new ArrayList<>(newTrainers));
        traineeDao.update(trainee);
        entityManager.flush();
        logger.info("Successfully updated trainers list for trainee {}. Total trainers assigned: {}",
                traineeUsername, newTrainers.size());
    }
}