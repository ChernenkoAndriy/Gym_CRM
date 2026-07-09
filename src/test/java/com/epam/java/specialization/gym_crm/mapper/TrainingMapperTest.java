package com.epam.java.specialization.gym_crm.mapper;

import com.epam.java.specialization.gym_crm.dto.TraineeResponseDto;
import com.epam.java.specialization.gym_crm.dto.TrainerResponseDto;
import com.epam.java.specialization.gym_crm.dto.TrainingCreateDto;
import com.epam.java.specialization.gym_crm.dto.TrainingResponseDto;
import com.epam.java.specialization.gym_crm.mapper.implementations.TrainingMapper;
import com.epam.java.specialization.gym_crm.mapper.interfaces.ICreateMapper;
import com.epam.java.specialization.gym_crm.mapper.interfaces.ITraineeMapper;
import com.epam.java.specialization.gym_crm.mapper.interfaces.ITrainerMapper;
import com.epam.java.specialization.gym_crm.model.Trainee;
import com.epam.java.specialization.gym_crm.model.Trainer;
import com.epam.java.specialization.gym_crm.model.Training;
import com.epam.java.specialization.gym_crm.model.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

    @Mock
    private ITraineeMapper traineeMapper;

    @Mock
    private ITrainerMapper trainerMapper;

    private TrainingMapper trainingMapper;

    @BeforeEach
    void setUp() {
        trainingMapper = new TrainingMapper(traineeMapper, trainerMapper);
    }

    @Override
    protected ICreateMapper<TrainingCreateDto, Training> getCreateMapper() {
        
        
        return dto -> trainingMapper.toEntityFromCreate(dto, null, null, null);
    }

    @Override
    protected TrainingCreateDto getCreateDtoSample() {
        return TrainingCreateDto.builder()
                .trainingName("Morning Yoga")
                .trainingDate(new Date())
                .trainingDuration(60)
                .traineeId(1L)
                .trainerId(2L)
                .trainingTypeId(3L)
                .build();
    }

    @Override
    protected BiConsumer<TrainingCreateDto, Training> getCreateAssertor() {
        return (dto, entity) -> {
            assertNotNull(entity);
            assertEquals(dto.getTrainingName(), entity.getTrainingName());
            assertEquals(dto.getTrainingDate(), entity.getTrainingDate());
            assertEquals(dto.getTrainingDuration(), entity.getTrainingDuration());

            
            assertNull(entity.getTrainee());
            assertNull(entity.getTrainer());
            assertNull(entity.getTrainingType());
        };
    }

    

    @Test
    @DisplayName("toEntityFromCreate: Should correctly set provided Trainee, Trainer and TrainingType entities")
    void testToEntityFromCreate_WithEntities() {
        TrainingCreateDto dto = getCreateDtoSample();
        Trainee trainee = Trainee.builder().id(1L).build();
        Trainer trainer = Trainer.builder().id(2L).build();
        TrainingType type = TrainingType.builder().id(3L).trainingTypeName("Yoga").build();

        
        Training training = trainingMapper.toEntityFromCreate(dto, trainee, trainer, type);

        assertNotNull(training);
        assertEquals(dto.getTrainingName(), training.getTrainingName());
        assertSame(trainee, training.getTrainee());
        assertSame(trainer, training.getTrainer());
        assertSame(type, training.getTrainingType());
    }

    @Test
    @DisplayName("toResponseDto: Should return null when input training is null")
    void testToResponseDto_NullInput() {
        assertNull(trainingMapper.toResponseDto(null, null, null, null));
    }

    @Test
    @DisplayName("toResponseDto: Should invoke dependency mappers exactly once and embed their results")
    void testToResponseDto_ValidMapping() {
        Trainee trainee = Trainee.builder().id(10L).build();
        Trainer trainer = Trainer.builder().id(20L).build();
        TrainingType type = TrainingType.builder().id(30L).trainingTypeName("Crossfit").build();

        Training training = Training.builder()
                .id(100L)
                .trainingName("Intense Crossfit WOD")
                .trainingDuration(45)
                .trainingDate(new Date())
                .build();

        TraineeResponseDto mockTraineeDto = TraineeResponseDto.builder().id(10L).firstName("Alex").build();
        TrainerResponseDto mockTrainerDto = TrainerResponseDto.builder().id(20L).firstName("Elena").build();

        
        when(traineeMapper.toResponseDto(trainee)).thenReturn(mockTraineeDto);
        when(trainerMapper.toResponseDto(trainer, type)).thenReturn(mockTrainerDto);

        
        TrainingResponseDto response = trainingMapper.toResponseDto(training, trainee, trainer, type);

        
        assertNotNull(response);
        assertEquals(training.getId(), response.getId());
        assertEquals(training.getTrainingName(), response.getTrainingName());
        assertSame(type, response.getTrainingType());

        
        assertSame(mockTraineeDto, response.getTrainee());
        assertSame(mockTrainerDto, response.getTrainer());

        
        verify(traineeMapper, times(1)).toResponseDto(trainee);
        verify(trainerMapper, times(1)).toResponseDto(trainer, type);
    }
}