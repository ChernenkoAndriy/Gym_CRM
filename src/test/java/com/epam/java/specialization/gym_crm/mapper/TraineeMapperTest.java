package com.epam.java.specialization.gym_crm.mapper;

import com.epam.java.specialization.gym_crm.dto.TraineeCreateDto;
import com.epam.java.specialization.gym_crm.dto.TraineeResponseDto;
import com.epam.java.specialization.gym_crm.dto.TraineeUpdateDto;
import com.epam.java.specialization.gym_crm.mapper.implementations.TraineeMapper;
import com.epam.java.specialization.gym_crm.model.Trainee;
import org.junit.jupiter.api.Test;
import java.util.Date;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.*;

class TraineeMapperTest extends AbstractUpdateMapperTest<TraineeCreateDto, TraineeUpdateDto, Trainee> {

    private final TraineeMapper traineeMapper = new TraineeMapper();

    @Override protected TraineeMapper getCreateMapper() { return traineeMapper; }
    @Override protected TraineeMapper getUpdateMapper() { return traineeMapper; }

    @Override
    protected TraineeCreateDto createTestingDto() {
        return TraineeCreateDto.builder().
                firstName("Alex").
                lastName("Smith").
                address("Kyiv").
                dateOfBirth(new Date()).build();
    }

    @Override
    protected TraineeUpdateDto createTestingUpdateDto() {
        return TraineeUpdateDto.builder()
                .id(10L)
                .firstName("Ivan")
                .lastName("Petrov")
                .isActive(false)
                .build();
    }

    @Override
    protected BiConsumer<TraineeCreateDto, Trainee> getFieldsAssertion() {
        return (dto, entity) -> {
            assertEquals(dto.getFirstName(), entity.getFirstName());
            assertEquals(dto.getLastName(), entity.getLastName());
            assertEquals(dto.getAddress(), entity.getAddress());
            assertTrue(entity.getIsActive(), "New profiles should be active by default");
        };
    }

    @Override
    protected BiConsumer<TraineeUpdateDto, Trainee> getUpdateFieldsAssertion() {
        return (dto, entity) -> {
            assertEquals(dto.getId(), entity.getId());
            assertEquals(dto.getFirstName(), entity.getFirstName());
            assertFalse(entity.getIsActive());
        };
    }

    @Test
    void testToResponseDto_Specific() {
        Trainee entity = Trainee.builder()
                .id(1L)
                .username("Alex.Smith")
                .isActive(true)
                .build();

        TraineeResponseDto response = traineeMapper.toResponseDto(entity);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Alex.Smith", response.getUsername());
        assertNull(traineeMapper.toResponseDto(null));
    }
}