package com.epam.java.specialization.gym_crm;

import com.epam.java.specialization.gym_crm.dto.TraineeCreateDto;
import com.epam.java.specialization.gym_crm.model.Trainee;
import com.epam.java.specialization.gym_crm.model.User;
import com.epam.java.specialization.gym_crm.dao.intefaces.ITraineeDao;
import com.epam.java.specialization.gym_crm.service.interfaces.ITraineeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TransactionManagementIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ITraineeService traineeService;

    @Autowired
    private ITraineeDao traineeDao;

    @Test
    @DisplayName("Transaction Rollback: Should roll back completely and leave DB empty when runtime exception occurs")
    void testCreateTrainee_ShouldRollbackEntireTransaction_WhenExceptionThrownAtTheEnd() {
        
        ITraineeDao spyTraineeDao = Mockito.spy(traineeDao);

        
        
        
        Mockito.doAnswer(invocation -> {
            Trainee trainee = invocation.getArgument(0);
            
            traineeDao.create(trainee);
            
            throw new RuntimeException("Simulated database failure during trainee persistence");
        }).when(spyTraineeDao).create(Mockito.any(Trainee.class));

        
        Object originalDao = ReflectionTestUtils.getField(traineeService, "traineeDao");
        ReflectionTestUtils.setField(traineeService, "traineeDao", spyTraineeDao);

        TraineeCreateDto dto = TraineeCreateDto.builder()
                .firstName("Transaction")
                .lastName("Test")
                .address("Kyiv")
                .build();

        
        assertThrows(RuntimeException.class, () -> traineeService.create(dto),
                "Expected RuntimeException to be thrown, triggering transactional rollback");

        
        ReflectionTestUtils.setField(traineeService, "traineeDao", originalDao);

        
        entityManager.clear();

        
        
        List<Trainee> allTrainees = entityManager.createQuery("SELECT t FROM Trainee t", Trainee.class).getResultList();
        List<User> allUsers = entityManager.createQuery("SELECT u FROM User u", User.class).getResultList();

        assertTrue(allTrainees.isEmpty(), "Transaction failed to roll back: Trainee record still exists in DB.");
        assertTrue(allUsers.isEmpty(), "Transaction failed to roll back: Cascaded User record still exists in DB.");
    }
}