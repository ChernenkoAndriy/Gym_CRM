package com.epam.java.specialization.gym_crm.service;

import com.epam.java.specialization.gym_crm.dao.intefaces.IBaseDao;
import com.epam.java.specialization.gym_crm.dao.intefaces.ITraineeDao;
import com.epam.java.specialization.gym_crm.dao.intefaces.ITrainerDao;
import com.epam.java.specialization.gym_crm.model.Trainee;
import com.epam.java.specialization.gym_crm.service.implementations.TraineeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeServiceTest extends AbstractUserServiceTest<Trainee> {

    @Mock private ITraineeDao traineeDao;
    @Mock private ITrainerDao trainerDao;

    private TraineeService traineeService;

    @BeforeEach
    void setUp() {
        traineeService = new TraineeService();
        traineeService.setTraineeDao(traineeDao);
        traineeService.setTrainerDao(trainerDao);
    }

    @Override protected TraineeService getService() { return traineeService; }
    @Override protected IBaseDao<Trainee, Long> getDaoMock() { return traineeDao; }
    @Override protected ITraineeDao getTraineeDaoMock() { return traineeDao; }
    @Override protected ITrainerDao getTrainerDaoMock() { return trainerDao; }
    @Override protected Trainee createSampleEntity() { return Trainee.builder().firstName("Test").lastName("User").build(); }

    @Test
    void testDelete_SpecificMethod() {
        traineeService.delete(5L);
        verify(traineeDao, times(1)).delete(5L);
    }
}