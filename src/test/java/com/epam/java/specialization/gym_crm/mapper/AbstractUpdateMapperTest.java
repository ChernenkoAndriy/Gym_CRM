package com.epam.java.specialization.gym_crm.mapper;

import com.epam.java.specialization.gym_crm.mapper.interfaces.IUpdateMapper;
import org.junit.jupiter.api.Test;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public abstract class AbstractUpdateMapperTest<CD, UD, E> extends AbstractCreateMapperTest<CD, E> {

    protected abstract IUpdateMapper<UD, E> getUpdateMapper();
    protected abstract UD createTestingUpdateDto();
    protected abstract BiConsumer<UD, E> getUpdateFieldsAssertion();

    @Test
    void testToEntityFromUpdate_Generic() {
        UD dto = createTestingUpdateDto();

        E entity = getUpdateMapper().toEntityFromUpdate(dto);

        assertNotNull(entity, "Mapped entity should not be null");
        getUpdateFieldsAssertion().accept(dto, entity);
    }

    @Test
    void testToEntityFromUpdate_ShouldReturnNull_WhenDtoIsNull() {
        assertNull(getUpdateMapper().toEntityFromUpdate(null));
    }
}