package com.epam.java.specialization.gym_crm.mapper;

import com.epam.java.specialization.gym_crm.mapper.interfaces.ICreateMapper;
import org.junit.jupiter.api.Test;
import java.util.function.BiConsumer;
import static org.junit.jupiter.api.Assertions.assertNull;

public abstract class AbstractCreateMapperTest<CD, E> {

    protected abstract ICreateMapper<CD, E> getCreateMapper();

    protected abstract CD getCreateDtoSample();

    protected abstract BiConsumer<CD, E> getCreateAssertor();

    @Test
    void testToEntityFromCreate_Null() {
        assertNull(getCreateMapper().toEntityFromCreate(null));
    }

    @Test
    void testToEntityFromCreate_Generic() {
        CD dto = getCreateDtoSample();
        E entity = getCreateMapper().toEntityFromCreate(dto);
        getCreateAssertor().accept(dto, entity);
    }
}