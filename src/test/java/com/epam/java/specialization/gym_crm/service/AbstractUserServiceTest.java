package com.epam.java.specialization.gym_crm.service;

import com.epam.java.specialization.gym_crm.dao.intefaces.ITraineeDao;
import com.epam.java.specialization.gym_crm.dao.intefaces.ITrainerDao;
import com.epam.java.specialization.gym_crm.model.Trainee;
import com.epam.java.specialization.gym_crm.model.Trainer;
import com.epam.java.specialization.gym_crm.model.User;
import org.junit.jupiter.api.Test;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public abstract class AbstractUserServiceTest<T extends User> extends AbstractServiceTest<T> {

    protected abstract ITraineeDao getTraineeDaoMock();
    protected abstract ITrainerDao getTrainerDaoMock();

    @Test
    void testCreate_ShouldGenerateCleanProfile_WhenNoCollisions() {
        T newEntity = createSampleEntity();
        newEntity.setFirstName("Marta");
        newEntity.setLastName("Kravets");

        when(getTraineeDaoMock().findAll()).thenReturn(Collections.emptyList());
        when(getTrainerDaoMock().findAll()).thenReturn(Collections.emptyList());
        when(getDaoMock().create(any())).thenAnswer(invocation -> invocation.getArgument(0));

        T result = getService().create(newEntity);

        assertNotNull(result);
        assertEquals("Marta.Kravets", result.getUsername(), "Username must follow FirstName.LastName pattern");
        assertNotNull(result.getPassword(), "Secure random password must be initialized");
        assertEquals(10, result.getPassword().length(), "Generated credentials password string must be 10 chars long");
        verify(getDaoMock(), times(1)).create(newEntity);
    }

    @Test
    void testCreate_ShouldAppendIncrementalSuffix_WhenUsernameCollisionDetected() {
        Trainee existingTrainee = mock(Trainee.class);
        when(existingTrainee.getUsername()).thenReturn("Marta.Kravets");

        Trainer existingTrainer = mock(Trainer.class);
        when(existingTrainer.getUsername()).thenReturn("Marta.Kravets1");

        when(getTraineeDaoMock().findAll()).thenReturn(List.of(existingTrainee));
        when(getTrainerDaoMock().findAll()).thenReturn(List.of(existingTrainer));
        when(getDaoMock().create(any())).thenAnswer(invocation -> invocation.getArgument(0));

        T duplicateEntity = createSampleEntity();
        duplicateEntity.setFirstName("Marta");
        duplicateEntity.setLastName("Kravets");

        T result = getService().create(duplicateEntity);

        assertEquals("Marta.Kravets2", result.getUsername(), "Should increment username serialization collision index");
    }

    @Test
    void testCreate_ShouldIgnorePartialPrefixMatches_WhenGeneratingUsername() {
        Trainee partialMatchTrainee = mock(Trainee.class);
        when(partialMatchTrainee.getUsername()).thenReturn("Marta.Kravetsson");

        when(getTraineeDaoMock().findAll()).thenReturn(List.of(partialMatchTrainee));
        when(getTrainerDaoMock().findAll()).thenReturn(Collections.emptyList());
        when(getDaoMock().create(any())).thenAnswer(invocation -> invocation.getArgument(0));

        T newEntity = createSampleEntity();
        newEntity.setFirstName("Marta");
        newEntity.setLastName("Kravets");

        T result = getService().create(newEntity);

        assertEquals("Marta.Kravets", result.getUsername(),
                "Username generator must not conflict with users whose names only partially share a prefix");
    }

    @Test
    void testCreate_ShouldFillHoleInSequence_WhenMiddleIndexIsDeleted() {
        Trainee baseTrainee = mock(Trainee.class);
        when(baseTrainee.getUsername()).thenReturn("Marta.Kravets");

        Trainer outerTrainer = mock(Trainer.class);
        when(outerTrainer.getUsername()).thenReturn("Marta.Kravets2");

        when(getTraineeDaoMock().findAll()).thenReturn(List.of(baseTrainee));
        when(getTrainerDaoMock().findAll()).thenReturn(List.of(outerTrainer));
        when(getDaoMock().create(any())).thenAnswer(invocation -> invocation.getArgument(0));

        T newEntity = createSampleEntity();
        newEntity.setFirstName("Marta");
        newEntity.setLastName("Kravets");

        T result = getService().create(newEntity);

        assertEquals("Marta.Kravets1", result.getUsername(),
                "Username generator must fill sequence gaps (holes) resulting from past record deletions");
    }
}