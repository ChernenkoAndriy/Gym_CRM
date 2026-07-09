package com.epam.java.specialization.gym_crm.service;

import com.epam.java.specialization.gym_crm.dao.intefaces.ITrainingTypeDao;
import com.epam.java.specialization.gym_crm.dto.TrainerCreateDto;
import com.epam.java.specialization.gym_crm.dto.TrainerResponseDto;
import com.epam.java.specialization.gym_crm.dto.TrainerUpdateDto;
import com.epam.java.specialization.gym_crm.mapper.interfaces.ITrainerMapper;
import com.epam.java.specialization.gym_crm.mapper.interfaces.ITrainingMapper;
import com.epam.java.specialization.gym_crm.model.Trainer;
import com.epam.java.specialization.gym_crm.model.TrainingType;
import com.epam.java.specialization.gym_crm.model.User;
import com.epam.java.specialization.gym_crm.service.implementations.TrainerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest extends AbstractUserServiceTest<Trainer> {

    @Mock
    private ITrainingTypeDao trainingTypeDao;

    @Mock
    private ITrainerMapper trainerMapper;

    @Mock
    private ITrainingMapper trainingMapper;

    @InjectMocks
    private TrainerService trainerService;

    @BeforeEach
    void initMocks() {
        
        trainerService.setTrainerDao(trainerDao);
        trainerService.setTraineeDao(traineeDao);
        trainerService.setTrainingTypeDao(trainingTypeDao);
        trainerService.setTrainerMapper(trainerMapper);
        trainerService.setTrainingMapper(trainingMapper);
    }

    

    @Override
    protected Trainer createEntityWithUser(String firstName, String lastName) {
        User user = User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .build();
        return Trainer.builder().user(user).build();
    }

    @Override
    protected User getUserFromEntity(Trainer entity) {
        return entity.getUser();
    }

    @Override
    protected Trainer executeCreate(Trainer entity) {
        TrainerCreateDto mockDto = TrainerCreateDto.builder().trainingTypeId(1L).build();
        TrainingType mockType = TrainingType.builder().id(1L).trainingTypeName("Yoga").build();

        when(trainerMapper.toEntityFromCreate(any(TrainerCreateDto.class))).thenReturn(entity);
        when(trainingTypeDao.findById(1L)).thenReturn(Optional.of(mockType));
        when(trainerDao.create(entity)).thenReturn(entity);

        trainerService.create(mockDto);
        return entity;
    }

    

    @Test
    @DisplayName("create: Should throw IllegalArgumentException when TrainingType ID is missing in DB")
    void testCreate_ShouldThrowException_WhenTrainingTypeNotFound() {
        
        TrainerCreateDto dto = TrainerCreateDto.builder()
                .firstName("Ivan")
                .lastName("Prokopchyk")
                .trainingTypeId(999L)
                .build();

        Trainer trainerEntity = createEntityWithUser("Ivan", "Prokopchyk");
        when(trainerMapper.toEntityFromCreate(dto)).thenReturn(trainerEntity);
        when(trainingTypeDao.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> trainerService.create(dto));
        verify(trainerDao, never()).create(any());
    }

    

    @Test
    @DisplayName("update: Should update successfully when trainer exists")
    void testUpdate_Success() {
        TrainerUpdateDto updateDto = TrainerUpdateDto.builder()
                .id(1L)
                .firstName("Ivan")
                .lastName("Prokopchyk")
                .trainingTypeId(2L)
                .isActive(true)
                .build();

        User user = User.builder().username("Ivan.Prokopchyk").isActive(true).build();
        Trainer existingTrainer = Trainer.builder().id(1L).user(user).build();
        TrainingType type = TrainingType.builder().id(2L).trainingTypeName("Crossfit").build();

        when(trainerDao.findById(1L)).thenReturn(Optional.of(existingTrainer));
        when(trainingTypeDao.findById(2L)).thenReturn(Optional.of(type));
        when(trainerDao.update(existingTrainer)).thenReturn(existingTrainer);

        assertDoesNotThrow(() -> trainerService.update(updateDto));
        verify(trainerDao, times(1)).update(existingTrainer);
    }

    @Test
    @DisplayName("update: Should throw IllegalArgumentException when trainer ID does not exist in DB")
    void testUpdate_ShouldThrowException_WhenTrainerNotFound() {
        
        TrainerUpdateDto updateDto = TrainerUpdateDto.builder().id(777L).build();
        when(trainerDao.findById(777L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> trainerService.update(updateDto));
        verify(trainerDao, never()).update(any());
        verify(trainingTypeDao, never()).findById(any());
    }

    

    @Test
    @DisplayName("toggleActivation: Should change status successfully when statuses differ")
    void testToggleActivation_Success() {
        String username = "Ivan.Prokopchyk";
        User user = User.builder().username(username).isActive(true).build();
        Trainer trainer = Trainer.builder().user(user).build();

        when(trainerDao.findByUsername(username)).thenReturn(Optional.of(trainer));

        assertDoesNotThrow(() -> trainerService.toggleActivation(username, false));
        assertFalse(user.getIsActive());
        verify(trainerDao, times(1)).update(trainer);
    }

    @Test
    @DisplayName("toggleActivation: Should throw IllegalStateException when target status is identical to current")
    void testToggleActivation_ShouldThrowIllegalStateException_WhenStatusIsIdentical() {
        
        String username = "Ivan.Prokopchyk";
        User user = User.builder().username(username).isActive(false).build();
        Trainer trainer = Trainer.builder().user(user).build();

        when(trainerDao.findByUsername(username)).thenReturn(Optional.of(trainer));

        
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> trainerService.toggleActivation(username, false));

        assertEquals("Status is already false", exception.getMessage());
        verify(trainerDao, never()).update(any());
    }

    

    @Test
    @DisplayName("getAvailableTrainersNotAssignedToTrainee: Should return list of unassigned trainers")
    void testGetAvailableTrainersNotAssignedToTrainee_Success() {
        String traineeUsername = "Andriy.Chernenko";
        TrainingType type = TrainingType.builder().id(1L).trainingTypeName("Fitness").build();
        Trainer freeTrainer = Trainer.builder()
                .id(5L)
                .user(User.builder().username("Ivan.Prokopchyk").build())
                .specialization(type)
                .build();

        TrainerResponseDto expectedDto = TrainerResponseDto.builder().id(5L).username("Ivan.Prokopchyk").build();

        when(trainerDao.findAvailableTrainersNotAssignedToTrainee(traineeUsername))
                .thenReturn(List.of(freeTrainer));
        when(trainerMapper.toResponseDto(freeTrainer, type)).thenReturn(expectedDto);

        List<TrainerResponseDto> result = trainerService.getAvailableTrainersNotAssignedToTrainee(traineeUsername);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Ivan.Prokopchyk", result.get(0).getUsername());
        verify(trainerDao, times(1)).findAvailableTrainersNotAssignedToTrainee(traineeUsername);
    }
}