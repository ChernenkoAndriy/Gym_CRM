package com.epam.java.specialization.gym_crm.mapper;

import com.epam.java.specialization.gym_crm.dto.TraineeCreateDto;
import com.epam.java.specialization.gym_crm.dto.TraineeResponseDto;
import com.epam.java.specialization.gym_crm.dto.TraineeUpdateDto;
import com.epam.java.specialization.gym_crm.mapper.implementations.TraineeMapper;
import com.epam.java.specialization.gym_crm.mapper.interfaces.ICreateMapper;
import com.epam.java.specialization.gym_crm.mapper.interfaces.IUpdateMapper;
import com.epam.java.specialization.gym_crm.model.Trainee;
import com.epam.java.specialization.gym_crm.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.*;

class TraineeMapperTest extends AbstractUpdateMapperTest<TraineeCreateDto, TraineeUpdateDto, Trainee> {

    private TraineeMapper traineeMapper;

    @BeforeEach
    void setUp() {
        traineeMapper = new TraineeMapper();
    }

    @Override
    protected ICreateMapper<TraineeCreateDto, Trainee> getCreateMapper() {
        return traineeMapper;
    }

    @Override
    protected IUpdateMapper<TraineeUpdateDto, Trainee> getUpdateMapper() {
        return traineeMapper;
    }

    @Override
    protected TraineeCreateDto getCreateDtoSample() {
        return TraineeCreateDto.builder()
                .firstName("Andriy")
                .lastName("Chernenko")
                .address("Kyiv")
                .dateOfBirth(new Date())
                .build();
    }

    @Override
    protected BiConsumer<TraineeCreateDto, Trainee> getCreateAssertor() {
        return (dto, entity) -> {
            assertNotNull(entity);
            assertEquals(dto.getAddress(), entity.getAddress());
            assertEquals(dto.getDateOfBirth(), entity.getDateOfBirth());

            User user = entity.getUser();
            assertNotNull(user);
            assertEquals(dto.getFirstName(), user.getFirstName());
            assertEquals(dto.getLastName(), user.getLastName());
            assertTrue(user.getIsActive());
            assertNull(user.getUsername());
            assertNull(user.getPassword());
        };
    }

    @Override
    protected TraineeUpdateDto getUpdateDtoSample() {
        return TraineeUpdateDto.builder()
                .id(42L)
                .firstName("Andriy")
                .lastName("Chernenko")
                .address("Lviv")
                .dateOfBirth(new Date())
                .isActive(false)
                .build();
    }

    @Override
    protected BiConsumer<TraineeUpdateDto, Trainee> getUpdateAssertor() {
        return (dto, entity) -> {
            assertNotNull(entity);
            assertEquals(dto.getId(), entity.getId());
            assertEquals(dto.getAddress(), entity.getAddress());
            assertEquals(dto.getDateOfBirth(), entity.getDateOfBirth());

            User user = entity.getUser();
            assertNotNull(user);
            assertEquals(dto.getFirstName(), user.getFirstName());
            assertEquals(dto.getLastName(), user.getLastName());
            assertEquals(dto.getIsActive(), user.getIsActive());
        };
    }


    @Test
    @DisplayName("toResponseDto: Should return null when input is null")
    void testToResponseDto_Null() {
        assertNull(traineeMapper.toResponseDto(null));
    }

    @Test
    @DisplayName("toResponseDto: Should correctly extract data from nested User object")
    void testToResponseDto_ValidEntity() {
        User user = User.builder()
                .firstName("Andriy")
                .lastName("Chernenko")
                .username("andriy.chernenko")
                .password("secure123")
                .isActive(true)
                .build();

        Trainee trainee = Trainee.builder()
                .id(100L)
                .user(user)
                .address("Kyiv")
                .dateOfBirth(new Date())
                .build();

        TraineeResponseDto response = traineeMapper.toResponseDto(trainee);

        assertNotNull(response);
        assertEquals(trainee.getId(), response.getId());
        assertEquals(trainee.getAddress(), response.getAddress());
        assertEquals(trainee.getDateOfBirth(), response.getDateOfBirth());
        assertEquals(user.getFirstName(), response.getFirstName());
        assertEquals(user.getLastName(), response.getLastName());
        assertEquals(user.getUsername(), response.getUsername());
        assertEquals(user.getIsActive(), response.getIsActive());
    }

    @Test
    @DisplayName("toResponseDto: Should not throw NullPointerException when nested User is null")
    void testToResponseDto_NullUserInsideEntity() {
        Trainee trainee = Trainee.builder()
                .id(100L)
                .user(null)
                .address("Kyiv")
                .dateOfBirth(new Date())
                .build();

        assertDoesNotThrow(() -> {
            TraineeResponseDto response = traineeMapper.toResponseDto(trainee);

            assertNotNull(response);
            assertEquals(trainee.getId(), response.getId());
            assertEquals(trainee.getAddress(), response.getAddress());

            assertNull(response.getFirstName());
            assertNull(response.getLastName());
            assertNull(response.getUsername());
            assertNull(response.getIsActive());
        });
    }
}