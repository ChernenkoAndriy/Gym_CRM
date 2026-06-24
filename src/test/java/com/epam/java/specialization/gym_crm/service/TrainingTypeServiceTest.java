package com.epam.java.specialization.gym_crm.service;

import com.epam.java.specialization.gym_crm.dao.intefaces.IBaseDao;
import com.epam.java.specialization.gym_crm.dao.intefaces.ITrainingTypeDao;
import com.epam.java.specialization.gym_crm.model.TrainingType;
import com.epam.java.specialization.gym_crm.service.implementations.TrainingTypeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingTypeServiceTest extends AbstractServiceTest<TrainingType> {

    @Mock private ITrainingTypeDao trainingTypeDao;
    private TrainingTypeService trainingTypeService;

    @BeforeEach
    void setUp() {
        trainingTypeService = new TrainingTypeService();
        trainingTypeService.setTrainingTypeDao(trainingTypeDao);
    }

    @Override protected TrainingTypeService getService() { return trainingTypeService; }
    @Override protected IBaseDao<TrainingType, Long> getDaoMock() { return trainingTypeDao; }
    @Override protected TrainingType createSampleEntity() { return TrainingType.builder().trainingTypeName("Zumba").build(); }

    @Test
    void testCreate_SpecificMethod() {
        TrainingType type = createSampleEntity();
        when(trainingTypeDao.create(type)).thenReturn(type);

        TrainingType result = trainingTypeService.create(type);

        assertNotNull(result);
        verify(trainingTypeDao, times(1)).create(type);
    }

    @Test
    void testDelete_SpecificMethod() {
        trainingTypeService.delete(1L);
        verify(trainingTypeDao, times(1)).delete(1L);
    }
}