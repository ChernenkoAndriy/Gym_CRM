package com.epam.java.specialization.gym_crm.mapper;

import com.epam.java.specialization.gym_crm.dto.TrainerCreateDto;
import com.epam.java.specialization.gym_crm.dto.TrainerResponseDto;
import com.epam.java.specialization.gym_crm.dto.TrainerUpdateDto;
import com.epam.java.specialization.gym_crm.mapper.implementations.TrainerMapper;
import com.epam.java.specialization.gym_crm.mapper.interfaces.ICreateMapper;
import com.epam.java.specialization.gym_crm.mapper.interfaces.IUpdateMapper;
import com.epam.java.specialization.gym_crm.model.Trainer;
import com.epam.java.specialization.gym_crm.model.TrainingType;
import com.epam.java.specialization.gym_crm.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.*;

class TrainerMapperTest extends AbstractUpdateMapperTest<TrainerCreateDto, TrainerUpdateDto, Trainer> {

    private TrainerMapper trainerMapper;

    @BeforeEach
    void setUp() {
        trainerMapper = new TrainerMapper();
    }

    @Override
    protected ICreateMapper<TrainerCreateDto, Trainer> getCreateMapper() {
        return trainerMapper;
    }

    @Override
    protected IUpdateMapper<TrainerUpdateDto, Trainer> getUpdateMapper() {
        return trainerMapper;
    }

    @Override
    protected TrainerCreateDto getCreateDtoSample() {
        return TrainerCreateDto.builder()
                .firstName("Ivan")
                .lastName("Prokopchyk")
                .trainingTypeId(1L) 
                .build();
    }

    @Override
    protected BiConsumer<TrainerCreateDto, Trainer> getCreateAssertor() {
        return (dto, entity) -> {
            assertNotNull(entity);

            
            User user = entity.getUser();
            assertNotNull(user);
            assertEquals(dto.getFirstName(), user.getFirstName());
            assertEquals(dto.getLastName(), user.getLastName());
            assertTrue(user.getIsActive());

            
            assertNull(entity.getSpecialization());
            assertNull(user.getUsername());
            assertNull(user.getPassword());
        };
    }

    @Override
    protected TrainerUpdateDto getUpdateDtoSample() {
        return TrainerUpdateDto.builder()
                .id(10L)
                .firstName("Ivan")
                .lastName("Prokopchyk")
                .trainingTypeId(2L)
                .isActive(false)
                .build();
    }

    @Override
    protected BiConsumer<TrainerUpdateDto, Trainer> getUpdateAssertor() {
        return (dto, entity) -> {
            assertNotNull(entity);
            assertEquals(dto.getId(), entity.getId());

            User user = entity.getUser();
            assertNotNull(user);
            assertEquals(dto.getFirstName(), user.getFirstName());
            assertEquals(dto.getLastName(), user.getLastName());
            assertEquals(dto.getIsActive(), user.getIsActive());

            
            assertNull(entity.getSpecialization());
        };
    }

    

    @Test
    @DisplayName("toResponseDto: Should return null when trainer input is null")
    void testToResponseDto_NullTrainer() {
        
        TrainingType type = TrainingType.builder().id(1L).trainingTypeName("Yoga").build();
        assertNull(trainerMapper.toResponseDto(null, type));
    }

    @Test
    @DisplayName("toResponseDto: Should process successfully even if TrainingType is null")
    void testToResponseDto_NullTrainingType() {
        
        User user = User.builder().firstName("Ivan").lastName("Prokopchyk").username("ivan.p").isActive(true).build();
        Trainer trainer = Trainer.builder().id(5L).user(user).build();

        assertDoesNotThrow(() -> {
            TrainerResponseDto response = trainerMapper.toResponseDto(trainer, null);
            assertNotNull(response);
            assertEquals(trainer.getId(), response.getId());
            assertNull(response.getSpecialization());
        });
    }

    @Test
    @DisplayName("toResponseDto: Should fully map Trainer and TrainingType into TrainerResponseDto")
    void testToResponseDto_ValidFields() {
        User user = User.builder()
                .firstName("Elena")
                .lastName("Kostova")
                .username("elena.kostova")
                .isActive(true)
                .build();

        Trainer trainer = Trainer.builder()
                .id(2L)
                .user(user)
                .build();

        
        TrainingType type = TrainingType.builder()
                .id(1L)
                .trainingTypeName("Crossfit")
                .build();

        
        TrainerResponseDto response = trainerMapper.toResponseDto(trainer, type);

        assertNotNull(response);
        assertEquals(trainer.getId(), response.getId());
        assertEquals(user.getFirstName(), response.getFirstName());
        assertEquals(user.getLastName(), response.getLastName());
        assertEquals(user.getUsername(), response.getUsername());
        assertEquals(user.getIsActive(), response.getIsActive());

        
        assertNotNull(response.getSpecialization());
        assertEquals(type.getId(), response.getSpecialization().getId());
        assertEquals(type.getTrainingTypeName(), response.getSpecialization().getTrainingTypeName());
    }

    @Test
    @DisplayName("toResponseDto: Should not throw NullPointerException when nested User is null")
    void testToResponseDto_NullUserInsideEntity() {
        Trainer trainer = Trainer.builder()
                .id(2L)
                .user(null) 
                .build();
        TrainingType type = TrainingType.builder().id(1L).trainingTypeName("Yoga").build();

        assertDoesNotThrow(() -> {
            TrainerResponseDto response = trainerMapper.toResponseDto(trainer, type);
            assertNotNull(response);
            assertEquals(trainer.getId(), response.getId());
            assertNull(response.getFirstName());
            assertNull(response.getLastName());
            assertNull(response.getUsername());
            assertNull(response.getIsActive());
            assertNotNull(response.getSpecialization());
        });
    }
}