package com.epam.java.specialization.gym_crm.service;

import com.epam.java.specialization.gym_crm.dao.intefaces.IBaseDao;
import com.epam.java.specialization.gym_crm.dao.intefaces.ITrainingDao;
import com.epam.java.specialization.gym_crm.model.Training;
import com.epam.java.specialization.gym_crm.service.implementations.TrainingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest extends AbstractServiceTest<Training> {

    @Mock private ITrainingDao trainingDao;
    private TrainingService trainingService;

    @BeforeEach
    void setUp() {
        trainingService = new TrainingService();
        trainingService.setTrainingDao(trainingDao);
    }

    @Override protected TrainingService getService() { return trainingService; }
    @Override protected IBaseDao<Training, Long> getDaoMock() { return trainingDao; }
    @Override protected Training createSampleEntity() { return Training.builder().trainingName("Yoga Match").build(); }

    @Test
    void testCreate_SpecificMethod() {
        Training training = createSampleEntity();
        when(trainingDao.create(training)).thenReturn(training);

        Training result = trainingService.create(training);

        assertNotNull(result);
        verify(trainingDao, times(1)).create(training);
    }
}