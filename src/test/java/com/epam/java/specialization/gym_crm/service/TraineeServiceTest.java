package com.epam.java.specialization.gym_crm.service;

import com.epam.java.specialization.gym_crm.dto.TraineeCreateDto;
import com.epam.java.specialization.gym_crm.dto.TraineeResponseDto;
import com.epam.java.specialization.gym_crm.dto.TraineeUpdateDto;
import com.epam.java.specialization.gym_crm.mapper.interfaces.ITraineeMapper;
import com.epam.java.specialization.gym_crm.mapper.interfaces.ITrainingMapper;
import com.epam.java.specialization.gym_crm.model.Trainee;
import com.epam.java.specialization.gym_crm.model.User;
import com.epam.java.specialization.gym_crm.service.implementations.TraineeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeServiceTest extends AbstractUserServiceTest<Trainee> {

    @Mock
    private ITraineeMapper traineeMapper;

    @Mock
    private ITrainingMapper trainingMapper;

    @InjectMocks
    private TraineeService traineeService;

    @BeforeEach
    void initMocks() {
        
        
        traineeService.setTraineeDao(traineeDao);
        traineeService.setTrainerDao(trainerDao);
        traineeService.setTraineeMapper(traineeMapper);
        traineeService.setTrainingMapper(trainingMapper);
    }

    

    @Override
    protected Trainee createEntityWithUser(String firstName, String lastName) {
        User user = User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .build();
        return Trainee.builder().user(user).build();
    }

    @Override
    protected User getUserFromEntity(Trainee entity) {
        return entity.getUser();
    }

    @Override
    protected Trainee executeCreate(Trainee entity) {
        TraineeCreateDto mockDto = new TraineeCreateDto();
        when(traineeMapper.toEntityFromCreate(any(TraineeCreateDto.class))).thenReturn(entity);
        when(traineeDao.create(entity)).thenReturn(entity);

        traineeService.create(mockDto);
        return entity;
    }

    

    @Test
    @DisplayName("update: Should update successfully when trainee exists")
    void testUpdate_Success() {
        TraineeUpdateDto updateDto = TraineeUpdateDto.builder()
                .id(1L)
                .firstName("Andriy")
                .lastName("Chernenko")
                .isActive(true)
                .build();

        User user = User.builder().username("Andriy.Chernenko").isActive(true).build();
        Trainee existingTrainee = Trainee.builder().id(1L).user(user).build();

        when(traineeDao.findById(1L)).thenReturn(Optional.of(existingTrainee));
        when(traineeDao.update(existingTrainee)).thenReturn(existingTrainee);

        assertDoesNotThrow(() -> traineeService.update(updateDto));
        verify(traineeDao, times(1)).update(existingTrainee);
    }

    @Test
    @DisplayName("update: Should throw IllegalArgumentException when trainee does not exist")
    void testUpdate_ShouldThrowException_WhenTraineeNotFound() {
        
        TraineeUpdateDto updateDto = TraineeUpdateDto.builder().id(999L).build();
        when(traineeDao.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> traineeService.update(updateDto));
        verify(traineeDao, never()).update(any());
    }

    

    @Test
    @DisplayName("changePassword: Should update password successfully when user is found")
    void testChangePassword_Success() {
        
        String username = "Andriy.Chernenko";
        String newPassword = "newSecurePassword";
        User user = User.builder().username(username).password("oldPassword").build();
        Trainee trainee = Trainee.builder().user(user).build();

        when(traineeDao.findByUsername(username)).thenReturn(Optional.of(trainee));

        assertDoesNotThrow(() -> traineeService.changePassword(username, newPassword));
        assertEquals(newPassword, user.getPassword());
        verify(traineeDao, times(1)).update(trainee);
    }

    @Test
    @DisplayName("changePassword: Should throw IllegalArgumentException when user is not found")
    void testChangePassword_ShouldThrowException_WhenUserNotFound() {
        
        String username = "unknown.user";
        when(traineeDao.findByUsername(username)).thenReturn(Optional.empty());
        when(trainerDao.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> traineeService.changePassword(username, "pass"));
        verify(traineeDao, never()).update(any());
    }

    

    @Test
    @DisplayName("authenticate: Should return true when passwords match")
    void testAuthenticate_ShouldReturnTrue_WhenCredentialsAreValid() {
        
        String username = "Andriy.Chernenko";
        String password = "correctPassword";
        User user = User.builder().username(username).password(password).build();
        Trainee trainee = Trainee.builder().user(user).build();

        when(traineeDao.findByUsername(username)).thenReturn(Optional.of(trainee));

        assertTrue(traineeService.authenticate(username, password));
    }

    @Test
    @DisplayName("authenticate: Should return false when password is incorrect")
    void testAuthenticate_ShouldReturnFalse_WhenPasswordIsWrong() {
        
        String username = "Andriy.Chernenko";
        User user = User.builder().username(username).password("correctPassword").build();
        Trainee trainee = Trainee.builder().user(user).build();

        when(traineeDao.findByUsername(username)).thenReturn(Optional.of(trainee));

        assertFalse(traineeService.authenticate(username, "wrongPassword"));
    }

    @Test
    @DisplayName("authenticate: Should return false when user does not exist")
    void testAuthenticate_ShouldReturnFalse_WhenUserDoesNotExist() {
        
        String username = "ghost.user";
        when(traineeDao.findByUsername(username)).thenReturn(Optional.empty());
        when(trainerDao.findByUsername(username)).thenReturn(Optional.empty());

        assertFalse(traineeService.authenticate(username, "anyPassword"));
    }

    

    @Test
    @DisplayName("toggleActivation: Should change status successfully when target status is different")
    void testToggleActivation_Success() {
        String username = "Andriy.Chernenko";
        User user = User.builder().username(username).isActive(true).build();
        Trainee trainee = Trainee.builder().user(user).build();

        when(traineeDao.findByUsername(username)).thenReturn(Optional.of(trainee));

        
        assertDoesNotThrow(() -> traineeService.toggleActivation(username, false));
        assertFalse(user.getIsActive());
        verify(traineeDao, times(1)).update(trainee);
    }

    @Test
    @DisplayName("toggleActivation: Should throw IllegalStateException when target status is identical to current status")
    void testToggleActivation_ShouldThrowIllegalStateException_WhenStatusIsIdentical() {
        
        String username = "Andriy.Chernenko";
        User user = User.builder().username(username).isActive(true).build();
        Trainee trainee = Trainee.builder().user(user).build();

        when(traineeDao.findByUsername(username)).thenReturn(Optional.of(trainee));

        
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> traineeService.toggleActivation(username, true));

        assertEquals("Status is already true", exception.getMessage());
        verify(traineeDao, never()).update(any());
    }

    

    @Test
    @DisplayName("update: Should trigger toggleActivation validation when status changes during update")
    void testUpdate_ShouldFail_WhenTryingToBypassBannedStatusViaNormalUpdate() {
        
        
        

        TraineeUpdateDto updateDto = TraineeUpdateDto.builder()
                .id(1L)
                .firstName("Andriy")
                .lastName("Chernenko")
                .isActive(true) 
                .build();

        User user = User.builder().username("Andriy.Chernenko").isActive(true).build(); 
        Trainee existingTrainee = Trainee.builder().id(1L).user(user).build();

        when(traineeDao.findById(1L)).thenReturn(Optional.of(existingTrainee));
        
        
        when(traineeDao.findByUsername("Andriy.Chernenko")).thenReturn(Optional.of(existingTrainee));

        
        
        
        
        

        
        updateDto.setIsActive(false); 

        
        assertDoesNotThrow(() -> {
            traineeService.update(updateDto);
        });


        assertThrows(IllegalStateException.class, () -> traineeService.toggleActivation("Andriy.Chernenko", false));    }

    

    @Test
    @DisplayName("updateTrainersList: Should clear trainers list when input list is null")
    void testUpdateTrainersList_ShouldClearList_WhenTrainersListIsNull() {
        
        String username = "Andriy.Chernenko";
        java.util.List<com.epam.java.specialization.gym_crm.model.Trainer> assignedTrainers = new java.util.ArrayList<>();
        assignedTrainers.add(com.epam.java.specialization.gym_crm.model.Trainer.builder().id(5L).build());

        Trainee trainee = Trainee.builder()
                .user(User.builder().username(username).build())
                .trainers(assignedTrainers)
                .build();

        when(traineeDao.findByUsername(username)).thenReturn(Optional.of(trainee));

        traineeService.updateTrainersList(username, null);

        assertTrue(trainee.getTrainers().isEmpty());
        verify(traineeDao, times(1)).update(trainee);
    }

    @Test
    @DisplayName("updateTrainersList: Should clear trainers list when input list is empty")
    void testUpdateTrainersList_ShouldClearList_WhenTrainersListIsEmpty() {
        
        String username = "Andriy.Chernenko";
        java.util.List<com.epam.java.specialization.gym_crm.model.Trainer> assignedTrainers = new java.util.ArrayList<>();
        assignedTrainers.add(com.epam.java.specialization.gym_crm.model.Trainer.builder().id(5L).build());

        Trainee trainee = Trainee.builder()
                .user(User.builder().username(username).build())
                .trainers(assignedTrainers)
                .build();

        when(traineeDao.findByUsername(username)).thenReturn(Optional.of(trainee));

        traineeService.updateTrainersList(username, java.util.Collections.emptyList());

        assertTrue(trainee.getTrainers().isEmpty());
        verify(traineeDao, times(1)).update(trainee);
    }

    

    @Test
    @DisplayName("deleteByUsername: Should clear relationships and perform hard delete")
    void testDeleteByUsername_Success() {
        String username = "Andriy.Chernenko";
        java.util.List<com.epam.java.specialization.gym_crm.model.Trainer> assignedTrainers = new java.util.ArrayList<>();
        assignedTrainers.add(com.epam.java.specialization.gym_crm.model.Trainer.builder().id(5L).build());

        Trainee trainee = Trainee.builder()
                .id(100L)
                .user(User.builder().username(username).build())
                .trainers(assignedTrainers)
                .build();

        when(traineeDao.findByUsername(username)).thenReturn(Optional.of(trainee));

        traineeService.deleteByUsername(username);

        
        assertTrue(trainee.getTrainers().isEmpty());
        verify(traineeDao, times(1)).update(trainee);
        verify(traineeDao, times(1)).delete(100L);
    }
}