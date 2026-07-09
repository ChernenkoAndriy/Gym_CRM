package com.epam.java.specialization.gym_crm.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.epam.java.specialization.gym_crm.dao.intefaces.ITraineeDao;
import com.epam.java.specialization.gym_crm.dao.intefaces.ITrainerDao;
import com.epam.java.specialization.gym_crm.dto.TraineeCreateDto;
import com.epam.java.specialization.gym_crm.mapper.interfaces.ITraineeMapper;
import com.epam.java.specialization.gym_crm.model.Trainee;
import com.epam.java.specialization.gym_crm.model.User;
import com.epam.java.specialization.gym_crm.service.implementations.TraineeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceLoggingTest {

    @Mock
    private ITraineeDao traineeDao;

    @Mock
    private ITrainerDao trainerDao;

    @Mock
    private ITraineeMapper traineeMapper;

    @InjectMocks
    private TraineeService traineeService;

    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger;

    @BeforeEach
    void setUpLogging() {
        traineeService.setTraineeDao(traineeDao);
        traineeService.setTrainerDao(trainerDao);
        traineeService.setTraineeMapper(traineeMapper);

        
        logger = (Logger) LoggerFactory.getLogger(TraineeService.class);

        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @AfterEach
    void tearDownLogging() {
        logger.detachAppender(listAppender);
        listAppender.stop();
    }

    @Test
    @DisplayName("Logging: Should log INFO message upon successful profile generation")
    void testProfileGeneration_LogsInfo() {
        User userInstance = User.builder().firstName("Alex").lastName("Smith").build();
        Trainee trainee = Trainee.builder()
                .user(userInstance)
                .build();

        TraineeCreateDto dto = TraineeCreateDto.builder().firstName("Alex").lastName("Smith").build();

        when(traineeMapper.toEntityFromCreate(any(TraineeCreateDto.class))).thenReturn(trainee);
        when(traineeDao.findAll()).thenReturn(Collections.emptyList());
        when(traineeDao.create(any(Trainee.class))).thenReturn(trainee);

        traineeService.create(dto);

        ILoggingEvent logEvent = listAppender.list.stream()
                .filter(event -> event.getMessage().contains("Generated profile for user"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected INFO log message was not found"));

        assertEquals(Level.INFO, logEvent.getLevel());
        assertTrue(logEvent.getFormattedMessage().contains("username=Alex.Smith"));
    }

    @Test
    @DisplayName("Logging: Should log ERROR message when changePassword fails with exception")
    void testChangePasswordFailure_LogsError() {
        String username = "nonexistent.user";
        when(traineeDao.findByUsername(username)).thenReturn(Optional.empty());
        when(trainerDao.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                traineeService.changePassword(username, "newPassword")
        );

        ILoggingEvent errorEvent = listAppender.list.stream()
                .filter(event -> event.getLevel() == Level.ERROR)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected ERROR log message was not found"));

        assertTrue(errorEvent.getFormattedMessage().contains("Failed to change password"));
        assertTrue(errorEvent.getFormattedMessage().contains(username));
    }

    @Test
    @DisplayName("Logging: Should log INFO message upon successful password change")
    void testChangePasswordSuccess_LogsInfo() {
        String username = "Alex.Smith";
        User user = User.builder().username(username).password("oldPass").build();
        Trainee trainee = Trainee.builder().user(user).build();

        when(traineeDao.findByUsername(username)).thenReturn(Optional.of(trainee));

        traineeService.changePassword(username, "newPass");

        boolean hasSuccessLog = listAppender.list.stream()
                .anyMatch(event -> event.getLevel() == Level.INFO &&
                        event.getFormattedMessage().contains("Password successfully updated"));

        assertTrue(hasSuccessLog, "Expected INFO log about successful password change was missing");
    }
}