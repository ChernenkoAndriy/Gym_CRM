package com.epam.java.specialization.gym_crm.service.implementations;

import com.epam.java.specialization.gym_crm.dao.intefaces.ITraineeDao;
import com.epam.java.specialization.gym_crm.dao.intefaces.ITrainerDao;
import com.epam.java.specialization.gym_crm.model.Trainee;
import com.epam.java.specialization.gym_crm.model.User;
import com.epam.java.specialization.gym_crm.service.interfaces.ITraineeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TraineeService extends AbstractUserService<Trainee> implements ITraineeService {

    private ITraineeDao traineeDao;
    private ITrainerDao trainerDao;

    @Autowired
    public void setTraineeDao(ITraineeDao traineeDao) {
        this.traineeDao = traineeDao;
    }

    @Autowired
    public void setTrainerDao(ITrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }

    @Override
    public Trainee create(Trainee trainee) {
        logger.debug("Attempting to create trainee profile: {} {}", trainee.getFirstName(), trainee.getLastName());

        List<User> allUsers = new ArrayList<>();
        allUsers.addAll(traineeDao.findAll());
        allUsers.addAll(trainerDao.findAll());

        prepareUserProfile(trainee, allUsers);
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
    public Optional<Trainee> getById(Long id) {
        logger.debug("Selecting trainee profile with ID: {}", id);
        return traineeDao.findById(id);
    }
}