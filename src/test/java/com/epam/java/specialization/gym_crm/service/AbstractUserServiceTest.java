package com.epam.java.specialization.gym_crm.service;

import com.epam.java.specialization.gym_crm.dao.intefaces.ITraineeDao;
import com.epam.java.specialization.gym_crm.dao.intefaces.ITrainerDao;
import com.epam.java.specialization.gym_crm.model.Trainee;
import com.epam.java.specialization.gym_crm.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public abstract class AbstractUserServiceTest<T> {

    @Mock
    protected ITraineeDao traineeDao;

    @Mock
    protected ITrainerDao trainerDao;

    
    protected abstract T createEntityWithUser(String firstName, String lastName);
    protected abstract User getUserFromEntity(T entity);
    protected abstract T executeCreate(T entity);

    @Test
    @DisplayName("prepareUserProfile: Should generate clean username and 10-char password when no collisions exist")
    void testCreate_ShouldGenerateCleanProfile_WhenNoCollisions() {
        
        T sample = createEntityWithUser("John", "Doe");
        when(traineeDao.findAll()).thenReturn(Collections.emptyList());

        
        T result = executeCreate(sample);
        User generatedUser = getUserFromEntity(result);

        
        assertNotNull(generatedUser);
        assertEquals("John.Doe", generatedUser.getUsername());
        assertNotNull(generatedUser.getPassword());
        assertEquals(10, generatedUser.getPassword().length());
    }

    @Test
    @DisplayName("prepareUserProfile: Should append incremental suffix '1' when base username collision is detected")
    void testCreate_ShouldAppendIncrementalSuffix_WhenUsernameCollisionDetected() {
        
        T sample = createEntityWithUser("John", "Doe");

        Trainee existingTrainee = Trainee.builder()
                .user(User.builder().username("John.Doe").build())
                .build();
        when(traineeDao.findAll()).thenReturn(List.of(existingTrainee));

        
        T result = executeCreate(sample);
        User generatedUser = getUserFromEntity(result);

        
        assertNotNull(generatedUser);
        assertEquals("John.Doe1", generatedUser.getUsername());
    }

    @Test
    @DisplayName("prepareUserProfile: Should fill the hole in sequence when middle index is deleted")
    void testCreate_ShouldFillHoleInSequence_WhenMiddleIndexIsDeleted() {
        
        T sample = createEntityWithUser("John", "Doe");

        Trainee existing1 = Trainee.builder()
                .user(User.builder().username("John.Doe").build())
                .build();
        Trainee existing3 = Trainee.builder()
                .user(User.builder().username("John.Doe2").build())
                .build();

        
        when(traineeDao.findAll()).thenReturn(List.of(existing1, existing3));

        
        T result = executeCreate(sample);
        User generatedUser = getUserFromEntity(result);

        
        assertNotNull(generatedUser);
        assertEquals("John.Doe1", generatedUser.getUsername());
    }
}