package com.epam.java.specialization.gym_crm.mapper;

import com.epam.java.specialization.gym_crm.dto.TrainerCreateDto;
import com.epam.java.specialization.gym_crm.dto.TrainerResponseDto;
import com.epam.java.specialization.gym_crm.dto.TrainerUpdateDto;
import com.epam.java.specialization.gym_crm.mapper.implementations.TrainerMapper;
import com.epam.java.specialization.gym_crm.model.Trainer;
import com.epam.java.specialization.gym_crm.model.TrainingType;
import org.junit.jupiter.api.Test;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.*;

class TrainerMapperTest extends AbstractUpdateMapperTest<TrainerCreateDto, TrainerUpdateDto, Trainer> {

    private final TrainerMapper trainerMapper = new TrainerMapper();

    @Override protected TrainerMapper getCreateMapper() { return trainerMapper; }
    @Override protected TrainerMapper getUpdateMapper() { return trainerMapper; }

    @Override
    protected TrainerCreateDto createTestingDto() {
        return TrainerCreateDto.builder()
                .firstName("Elena")
                .lastName("Kostova")
                .trainingTypeId(2L)
                .build();
    }

    @Override
    protected TrainerUpdateDto createTestingUpdateDto() {
        return TrainerUpdateDto.builder()
                .id(1L)
                .firstName("Elena")
                .lastName("Kostova")
                .trainingTypeId(3L)
                .isActive(false)
                .build();
    }

    @Override
    protected BiConsumer<TrainerCreateDto, Trainer> getFieldsAssertion() {
        return (dto, entity) -> {
            assertEquals(dto.getFirstName(), entity.getFirstName());
            assertEquals(dto.getLastName(), entity.getLastName());
            assertEquals(dto.getTrainingTypeId(), entity.getTrainingTypeId());
            assertTrue(entity.getIsActive(), "New trainer profiles must be active by default");
            assertNull(entity.getId(), "Id must be null before saving to storage");
            assertNull(entity.getUsername(), "Username must be null before service processing");
            assertNull(entity.getPassword(), "Password must be null before service processing");
        };
    }

    @Override
    protected BiConsumer<TrainerUpdateDto, Trainer> getUpdateFieldsAssertion() {
        return (dto, entity) -> {
            assertEquals(dto.getId(), entity.getId());
            assertEquals(dto.getFirstName(), entity.getFirstName());
            assertEquals(dto.getLastName(), entity.getLastName());
            assertEquals(dto.getTrainingTypeId(), entity.getTrainingTypeId());
            assertFalse(entity.getIsActive(), "Status should match the update request");
        };
    }

    @Test
    void testToResponseDto_ShouldEmbedTrainingType_Specific() {
        Trainer trainer = Trainer.builder()
                .id(1L)
                .firstName("Elena")
                .lastName("Kostova")
                .username("Elena.Kostova")
                .password("securePass123")
                .isActive(true)
                .trainingTypeId(2L)
                .build();

        TrainingType specialization = TrainingType.builder()
                .id(2L)
                .trainingTypeName("Crossfit")
                .build();

        TrainerResponseDto response = trainerMapper.toResponseDto(trainer, specialization);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Elena", response.getFirstName());
        assertEquals("Kostova", response.getLastName());
        assertEquals("Elena.Kostova", response.getUsername());
        assertTrue(response.getIsActive());

        assertNotNull(response.getSpecialization(), "Nested specialization object should be mapped");
        assertEquals(2L, response.getSpecialization().getId());
        assertEquals("Crossfit", response.getSpecialization().getTrainingTypeName());

        assertNull(trainerMapper.toResponseDto(null, null));

        System.out.println("=== Full TrainerResponseDto Structure ===");
        System.out.println(response);
    }
}