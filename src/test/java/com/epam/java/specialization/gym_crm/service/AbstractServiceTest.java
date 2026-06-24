package com.epam.java.specialization.gym_crm.service;

import com.epam.java.specialization.gym_crm.dao.intefaces.IBaseDao;
import com.epam.java.specialization.gym_crm.model.AbstractEntity;
import com.epam.java.specialization.gym_crm.service.interfaces.ICRService;
import org.junit.jupiter.api.Test;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public abstract class AbstractServiceTest<T extends AbstractEntity<Long>> {

    protected abstract ICRService<T, Long> getService();
    protected abstract IBaseDao<T, Long> getDaoMock();
    protected abstract T createSampleEntity();

    @Test
    void testGetById_Generic() {
        T entity = createSampleEntity();
        entity.setId(99L);

        when(getDaoMock().findById(99L)).thenReturn(Optional.of(entity));

        Optional<T> result = getService().getById(99L);

        assertTrue(result.isPresent(), "Entity should be found by ID");
        assertEquals(99L, result.get().getId());
        verify(getDaoMock(), times(1)).findById(99L);
    }

    @Test
    void testGetById_ShouldReturnEmptyOptional_WhenEntityNotFound() {
        when(getDaoMock().findById(404L)).thenReturn(Optional.empty());

        Optional<T> result = getService().getById(404L);

        assertTrue(result.isEmpty(), "Should return empty optional for non-existing ID");
    }
}