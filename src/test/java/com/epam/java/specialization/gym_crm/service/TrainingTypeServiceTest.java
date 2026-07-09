package com.epam.java.specialization.gym_crm.service;

import com.epam.java.specialization.gym_crm.dao.intefaces.ITrainingTypeDao;
import com.epam.java.specialization.gym_crm.model.TrainingType;
import com.epam.java.specialization.gym_crm.service.implementations.TrainingTypeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingTypeServiceTest {

    @Mock
    private ITrainingTypeDao trainingTypeDao;

    @InjectMocks
    private TrainingTypeService trainingTypeService;

    @Test
    @DisplayName("create: Should return existing entity from DB when type name is allowed constant and already exists")
    void testCreate_ShouldReturnExisting_WhenTypeAlreadyExists() {
        
        String allowedName = "Yoga";
        TrainingType inputType = TrainingType.builder().trainingTypeName(allowedName).build();
        TrainingType existingType = TrainingType.builder().id(1L).trainingTypeName(allowedName).build();

        when(trainingTypeDao.findByName(allowedName)).thenReturn(Optional.of(existingType));

        TrainingType result = trainingTypeService.create(inputType);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(allowedName, result.getTrainingTypeName());

        
        verify(trainingTypeDao, never()).create(any());
    }

    @Test
    @DisplayName("create: Should call dao.create when type name is allowed constant but missing in DB")
    void testCreate_ShouldCallDaoCreate_WhenAllowedTypeIsMissingInDb() {
        
        String allowedName = "Crossfit";
        TrainingType inputType = TrainingType.builder().trainingTypeName(allowedName).build();
        TrainingType savedType = TrainingType.builder().id(2L).trainingTypeName(allowedName).build();

        when(trainingTypeDao.findByName(allowedName)).thenReturn(Optional.empty());
        when(trainingTypeDao.create(inputType)).thenReturn(savedType);

        TrainingType result = trainingTypeService.create(inputType);

        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals(allowedName, result.getTrainingTypeName());

        
        verify(trainingTypeDao, times(1)).create(inputType);
    }

    @Test
    @DisplayName("create: Should throw UnsupportedOperationException when type name is not in allowed constants list")
    void testCreate_ShouldThrowUnsupportedOperationException_WhenTypeNameIsProhibited() {
        
        String prohibitedName = "Zumba";
        TrainingType inputType = TrainingType.builder().trainingTypeName(prohibitedName).build();

        when(trainingTypeDao.findByName(prohibitedName)).thenReturn(Optional.empty());

        
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class, () -> {
            trainingTypeService.create(inputType);
        });

        assertEquals("Creation of new training types from the application is strictly prohibited.", exception.getMessage());
        verify(trainingTypeDao, never()).create(any());
    }
}