package com.epam.java.specialization.gym_crm.mapper;

import com.epam.java.specialization.gym_crm.mapper.interfaces.IUpdateMapper;
import org.junit.jupiter.api.Test;
import java.util.function.BiConsumer;
import static org.junit.jupiter.api.Assertions.assertNull;

public abstract class AbstractUpdateMapperTest<CD, UD, E> extends AbstractCreateMapperTest<CD, E> {

    protected abstract IUpdateMapper<UD, E> getUpdateMapper();

    protected abstract UD getUpdateDtoSample();

    protected abstract BiConsumer<UD, E> getUpdateAssertor();

    @Test
    void testToEntityFromUpdate_Null() {
        assertNull(getUpdateMapper().toEntityFromUpdate(null));
    }

    @Test
    void testToEntityFromUpdate_Generic() {
        UD dto = getUpdateDtoSample();
        E entity = getUpdateMapper().toEntityFromUpdate(dto);
        getUpdateAssertor().accept(dto, entity);
    }
}