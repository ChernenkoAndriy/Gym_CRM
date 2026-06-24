package com.epam.java.specialization.gym_crm.mapper;

import com.epam.java.specialization.gym_crm.dto.TraineeResponseDto;
import com.epam.java.specialization.gym_crm.dto.TrainerResponseDto;
import com.epam.java.specialization.gym_crm.dto.TrainingCreateDto;
import com.epam.java.specialization.gym_crm.dto.TrainingResponseDto;
import com.epam.java.specialization.gym_crm.mapper.implementations.TraineeMapper;
import com.epam.java.specialization.gym_crm.mapper.implementations.TrainerMapper;
import com.epam.java.specialization.gym_crm.mapper.implementations.TrainingMapper;
import com.epam.java.specialization.gym_crm.model.Trainee;
import com.epam.java.specialization.gym_crm.model.Trainer;
import com.epam.java.specialization.gym_crm.model.Training;
import com.epam.java.specialization.gym_crm.model.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Date;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingMapperTest extends AbstractCreateMapperTest<TrainingCreateDto, Training> {

    @Mock private TraineeMapper traineeMapper;
    @Mock private TrainerMapper trainerMapper;

    private TrainingMapper trainingMapper;
    private final Date testDate = new Date();

    @BeforeEach
    void setUp() {
        trainingMapper = new TrainingMapper(traineeMapper, trainerMapper);
    }

    @Override protected TrainingMapper getCreateMapper() { return trainingMapper; }

    @Override
    protected TrainingCreateDto createTestingDto() {
        return TrainingCreateDto.builder()
                .trainingName("Yoga Class")
                .traineeId(1L)
                .trainerId(2L)
                .trainingTypeId(5L)
                .trainingDate(testDate)
                .trainingDuration(60)
                .build();
    }

    @Override
    protected BiConsumer<TrainingCreateDto, Training> getFieldsAssertion() {
        return (dto, entity) -> {
            assertEquals(dto.getTrainingName(), entity.getTrainingName());
            assertEquals(dto.getTraineeId(), entity.getTraineeId());
            assertEquals(dto.getTrainerId(), entity.getTrainerId());
            assertEquals(dto.getTrainingTypeId(), entity.getTrainingTypeId());
            assertEquals(dto.getTrainingDate(), entity.getTrainingDate());
            assertEquals(dto.getTrainingDuration(), entity.getTrainingDuration());
        };
    }

    @Test
    void testToResponseDto_WithMultipleEntities_Specific() {
        TrainingType type = TrainingType.builder()
                .id(5L)
                .trainingTypeName("Yoga")
                .build();

        Training training = Training.builder()
                .id(100L)
                .trainingName("Yoga Class")
                .traineeId(1L)
                .trainerId(2L)
                .trainingTypeId(5L)
                .trainingDate(testDate)
                .trainingDuration(60)
                .build();

        Trainee trainee = Trainee.builder()
                .id(1L)
                .firstName("Alex")
                .lastName("Smith")
                .username("Alex.Smith")
                .password("securePass1")
                .isActive(true)
                .dateOfBirth(new Date())
                .address("Kyiv, Khreshchatyk 1")
                .build();

        Trainer trainer = Trainer.builder()
                .id(2L)
                .firstName("John")
                .lastName("Doe")
                .username("John.Doe")
                .password("securePass2")
                .isActive(true)
                .trainingTypeId(5L)
                .build();

        TraineeResponseDto expectedTraineeDto = TraineeResponseDto.builder()
                .id(1L)
                .firstName("Alex")
                .lastName("Smith")
                .username("Alex.Smith")
                .isActive(true)
                .dateOfBirth(trainee.getDateOfBirth())
                .address("Kyiv, Khreshchatyk 1")
                .build();
        when(traineeMapper.toResponseDto(trainee)).thenReturn(expectedTraineeDto);

        TrainerResponseDto expectedTrainerDto = TrainerResponseDto.builder()
                .id(2L)
                .firstName("John")
                .lastName("Doe")
                .username("John.Doe")
                .isActive(true)
                .specialization(type)
                .build();
        when(trainerMapper.toResponseDto(trainer, type)).thenReturn(expectedTrainerDto);

        TrainingResponseDto response = trainingMapper.toResponseDto(training, trainee, trainer, type);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals("Yoga Class", response.getTrainingName());
        assertEquals(testDate, response.getTrainingDate());
        assertEquals(60, response.getTrainingDuration());

        assertNotNull(response.getTrainee());
        assertEquals(1L, response.getTrainee().getId());
        assertEquals("Alex", response.getTrainee().getFirstName());
        assertEquals("Alex.Smith", response.getTrainee().getUsername());
        assertEquals("Kyiv, Khreshchatyk 1", response.getTrainee().getAddress());

        assertNotNull(response.getTrainer());
        assertEquals(2L, response.getTrainer().getId());
        assertEquals("John", response.getTrainer().getFirstName());
        assertEquals("John.Doe", response.getTrainer().getUsername());
        assertTrue(response.getTrainer().getIsActive());

        assertNotNull(response.getTrainingType());
        assertEquals(5L, response.getTrainingType().getId());
        assertEquals("Yoga", response.getTrainingType().getTrainingTypeName());

        verify(traineeMapper, times(1)).toResponseDto(trainee);
        verify(trainerMapper, times(1)).toResponseDto(trainer, type);
    }
}