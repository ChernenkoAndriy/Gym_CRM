package com.epam.java.specialization.gym_crm.service;

import com.epam.java.specialization.gym_crm.dao.intefaces.IBaseDao;
import com.epam.java.specialization.gym_crm.dao.intefaces.ITraineeDao;
import com.epam.java.specialization.gym_crm.dao.intefaces.ITrainerDao;
import com.epam.java.specialization.gym_crm.model.Trainer;
import com.epam.java.specialization.gym_crm.service.implementations.TrainerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest extends AbstractUserServiceTest<Trainer> {

    @Mock private ITrainerDao trainerDao;
    @Mock private ITraineeDao traineeDao;

    private TrainerService trainerService;

    @BeforeEach
    void setUp() {
        trainerService = new TrainerService();
        trainerService.setTrainerDao(trainerDao);
        trainerService.setTraineeDao(traineeDao);
    }

    @Override protected TrainerService getService() { return trainerService; }
    @Override protected IBaseDao<Trainer, Long> getDaoMock() { return trainerDao; }
    @Override protected ITraineeDao getTraineeDaoMock() { return traineeDao; }
    @Override protected ITrainerDao getTrainerDaoMock() { return trainerDao; }
    @Override protected Trainer createSampleEntity() { return Trainer.builder().firstName("Test").lastName("Trainer").trainingTypeId(1L).build(); }

    @Test
    void testUpdate_SpecificMethod() {
        Trainer trainer = createSampleEntity();
        when(trainerDao.update(trainer)).thenReturn(trainer);

        Trainer result = trainerService.update(trainer);

        assertNotNull(result);
        verify(trainerDao, times(1)).update(trainer);
    }
}