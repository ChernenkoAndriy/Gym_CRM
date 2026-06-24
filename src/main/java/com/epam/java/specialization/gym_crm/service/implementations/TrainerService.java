package com.epam.java.specialization.gym_crm.service.implementations;

import com.epam.java.specialization.gym_crm.dao.intefaces.ITraineeDao;
import com.epam.java.specialization.gym_crm.dao.intefaces.ITrainerDao;
import com.epam.java.specialization.gym_crm.model.Trainer;
import com.epam.java.specialization.gym_crm.model.User;
import com.epam.java.specialization.gym_crm.service.interfaces.ITrainerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TrainerService extends AbstractUserService<Trainer> implements ITrainerService {

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
        logger.debug("Attempting to create trainer profile: {} {}", trainer.getFirstName(), trainer.getLastName());

        List<User> allUsers = new ArrayList<>();
        allUsers.addAll(traineeDao.findAll());
        allUsers.addAll(trainerDao.findAll());

        prepareUserProfile(trainer, allUsers);
        return trainerDao.create(trainer);
    }

    @Override
    public Trainer update(Trainer trainer) {
        logger.debug("Updating trainer profile with ID: {}", trainer.getId());
        return trainerDao.update(trainer);
    }

    @Override
    public Optional<Trainer> getById(Long id) {
        logger.debug("Selecting trainer profile with ID: {}", id);
        return trainerDao.findById(id);
    }
}