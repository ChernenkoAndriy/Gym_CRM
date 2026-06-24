package com.epam.java.specialization.gym_crm.mapper;

import com.epam.java.specialization.gym_crm.mapper.interfaces.ICreateMapper;
import org.junit.jupiter.api.Test;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public abstract class AbstractCreateMapperTest<CD, E> {

    protected abstract ICreateMapper<CD, E> getCreateMapper();

    protected abstract CD createTestingDto();

    protected abstract BiConsumer<CD, E> getFieldsAssertion();

    @Test
    void testToEntityFromCreate_Generic() {
        CD dto = createTestingDto();

        E entity = getCreateMapper().toEntityFromCreate(dto);

        assertNotNull(entity, "Mapped entity should not be null");
        getFieldsAssertion().accept(dto, entity);
    }

    @Test
    void testToEntityFromCreate_ShouldReturnNull_WhenDtoIsNull() {
        assertNull(getCreateMapper().toEntityFromCreate(null));
    }
}