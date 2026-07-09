package com.epam.java.specialization.gym_crm.facade;

import com.epam.java.specialization.gym_crm.AbstractIntegrationTest;
import com.epam.java.specialization.gym_crm.dto.TraineeCreateDto;
import com.epam.java.specialization.gym_crm.dto.TraineeResponseDto;
import com.epam.java.specialization.gym_crm.dto.TraineeUpdateDto;
import com.epam.java.specialization.gym_crm.dto.TrainerCreateDto;
import com.epam.java.specialization.gym_crm.dto.TrainerResponseDto;
import com.epam.java.specialization.gym_crm.model.Trainee;
import com.epam.java.specialization.gym_crm.model.Trainer;
import com.epam.java.specialization.gym_crm.model.TrainingType;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
class GymFacadeIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private GymFacade gymFacade;

    private TrainingType yogaType;
    private TraineeResponseDto existingTrainee;
    private String traineeRawPassword;

    @BeforeEach
    void setUpIntegrationData() {
        
        yogaType = TrainingType.builder().trainingTypeName("Yoga").build();
        entityManager.persist(yogaType);
        entityManager.flush();

        
        TraineeCreateDto createDto = TraineeCreateDto.builder()
                .firstName("Andriy")
                .lastName("Chernenko")
                .address("Kyiv")
                .dateOfBirth(new Date())
                .build();

        existingTrainee = gymFacade.createTrainee(createDto);

        
        Trainee rawEntity = entityManager.find(Trainee.class, existingTrainee.getId());
        traineeRawPassword = rawEntity.getUser().getPassword();

        entityManager.clear();
    }

    

    @Test
    @DisplayName("DTO Validation: Should throw ConstraintViolationException when firstName is blank")
    void testCreateTrainee_ShouldThrowException_WhenFirstNameIsBlank() {
        
        TraineeCreateDto invalidDto = TraineeCreateDto.builder()
                .firstName("   ") 
                .lastName("Chernenko")
                .address("Kyiv")
                .build();

        assertThrows(ConstraintViolationException.class, () -> gymFacade.createTrainee(invalidDto),
                "Facade must catch invalid DTO properties via JSR-380 Validator before running business logic");
    }

    @Test
    @DisplayName("DTO Validation: Should throw ConstraintViolationException when lastName is missing")
    void testCreateTrainee_ShouldThrowException_WhenLastNameIsMissing() {
        
        TraineeCreateDto invalidDto = TraineeCreateDto.builder()
                .firstName("Andriy")
                .lastName(null) 
                .address("Kyiv")
                .build();

        assertThrows(ConstraintViolationException.class, () -> gymFacade.createTrainee(invalidDto));
    }

    

    @Test
    @DisplayName("createTrainee: Should register trainee and generate credentials successfully")
    void testCreateTrainee_Success() {
        TraineeCreateDto validDto = TraineeCreateDto.builder()
                .firstName("Ivan")
                .lastName("Sirko")
                .address("Lviv")
                .build();

        TraineeResponseDto response = gymFacade.createTrainee(validDto);

        assertNotNull(response);
        assertEquals("Ivan.Sirko", response.getUsername());
        assertTrue(response.getIsActive());
    }

    @Test
    @DisplayName("createTrainer: Should register trainer successfully")
    void testCreateTrainer_Success() {
        TrainerCreateDto validDto = TrainerCreateDto.builder()
                .firstName("Elena")
                .lastName("Kostova")
                .trainingTypeId(yogaType.getId())
                .build();

        TrainerResponseDto response = gymFacade.createTrainer(validDto);

        assertNotNull(response);
        assertEquals("Elena.Kostova", response.getUsername());
        assertEquals("Yoga", response.getSpecialization().getTrainingTypeName());
    }

    

    @Test
    @DisplayName("validateAuth: Should throw SecurityException and block update when credentials are invalid")
    void testUpdateTrainee_ShouldThrowSecurityException_WhenCredentialsAreWrong() {
        
        TraineeUpdateDto updateDto = TraineeUpdateDto.builder()
                .id(existingTrainee.getId())
                .firstName("Andriy")
                .lastName("Chernenko")
                .address("New Address Kyiv")
                .isActive(true)
                .build();

        assertThrows(SecurityException.class, () ->
                        gymFacade.updateTrainee(updateDto, existingTrainee.getUsername(), "wrong_password_123"),
                "Facade must execute validateAuth and reject execution immediately upon incorrect password"
        );

        
        entityManager.clear();
        Trainee dbTrainee = entityManager.find(Trainee.class, existingTrainee.getId());
        assertEquals("Kyiv", dbTrainee.getAddress(), "Database record must remain unmodified after auth failure");
    }

    @Test
    @DisplayName("updateTrainee: Should update successfully when credentials and DTO are correct")
    void testUpdateTrainee_Success() {
        TraineeUpdateDto updateDto = TraineeUpdateDto.builder()
                .id(existingTrainee.getId())
                .firstName("Andriy")
                .lastName("Chernenko")
                .address("New Address Kyiv")
                .isActive(true)
                .build();

        TraineeResponseDto response = gymFacade.updateTrainee(updateDto, existingTrainee.getUsername(), traineeRawPassword);
        assertNotNull(response);
        assertEquals("New Address Kyiv", response.getAddress());

        entityManager.flush();
        entityManager.clear();

        Trainee dbTrainee = entityManager.find(Trainee.class, existingTrainee.getId());
        assertEquals("New Address Kyiv", dbTrainee.getAddress());
    }

    @Test
    @DisplayName("End-to-End Scenario: Registration -> Password Change -> Authentication -> Profile Update")
    void testEndToEnd_UserLifecycleScenario() {
        
        TraineeCreateDto traineeCreateDto = TraineeCreateDto.builder()
                .firstName("Maksym")
                .lastName("Semeniuk")
                .address("Kyiv")
                .dateOfBirth(new Date())
                .build();

        TraineeResponseDto traineeRes = gymFacade.createTrainee(traineeCreateDto);
        assertNotNull(traineeRes);
        String traineeUsername = traineeRes.getUsername();
        assertEquals("Maksym.Semeniuk", traineeUsername);

        
        entityManager.flush();
        Trainee dbTraineeBefore = entityManager.createQuery(
                        "SELECT t FROM Trainee t WHERE t.user.username = :username", Trainee.class)
                .setParameter("username", traineeUsername)
                .getSingleResult();
        String initialPassword = dbTraineeBefore.getUser().getPassword();
        assertNotNull(initialPassword);
        assertEquals(10, initialPassword.length());

        
        TrainerCreateDto trainerCreateDto = TrainerCreateDto.builder()
                .firstName("Danylo")
                .lastName("Shlapak")
                .trainingTypeId(yogaType.getId())
                .build();

        TrainerResponseDto trainerRes = gymFacade.createTrainer(trainerCreateDto);
        assertNotNull(trainerRes);
        assertEquals("Danylo.Shlapak", trainerRes.getUsername());


        
        String newPassword = "MyNewSecurePassword2026";

        
        assertDoesNotThrow(() -> gymFacade.changeUserPassword(
                traineeUsername,
                newPassword,
                traineeUsername,
                initialPassword
        ));


        
        entityManager.flush();
        entityManager.clear();

        
        TraineeUpdateDto updateDto = TraineeUpdateDto.builder()
                .id(traineeRes.getId())
                .firstName("Maksym")
                .lastName("Semeniuk")
                .address("Kyiv Podil") 
                .isActive(true)
                .build();

        assertThrows(SecurityException.class, () ->
                gymFacade.updateTrainee(updateDto, traineeUsername, initialPassword)
        );


        
        
        TraineeResponseDto updatedTraineeRes = gymFacade.updateTrainee(
                updateDto,
                traineeUsername,
                newPassword
        );

        assertNotNull(updatedTraineeRes);
        assertEquals("Kyiv Podil", updatedTraineeRes.getAddress());

        
        entityManager.flush();
        Trainee dbTraineeAfter = entityManager.find(Trainee.class, traineeRes.getId());
        assertEquals("Kyiv Podil", dbTraineeAfter.getAddress());
        assertEquals(newPassword, dbTraineeAfter.getUser().getPassword());
    }

    

    @Test
    @DisplayName("createTraining: Should throw IllegalArgumentException when Trainee or Trainer is inactive")
    void testCreateTraining_ShouldThrowException_WhenUserIsInactive() {
        
        Trainee traineeEntity = entityManager.find(Trainee.class, existingTrainee.getId());
        String traineeUsername = traineeEntity.getUser().getUsername();

        
        TrainerCreateDto trainerDto = TrainerCreateDto.builder()
                .firstName("Ivan")
                .lastName("Prokopchyk")
                .trainingTypeId(yogaType.getId())
                .build();
        TrainerResponseDto trainerRes = gymFacade.createTrainer(trainerDto);


        Trainer rawTrainerEntity = entityManager.find(Trainer.class, trainerRes.getId());
        String trainerRawPassword = rawTrainerEntity.getUser().getPassword();

        gymFacade.toggleTrainerActivation(
                trainerRes.getUsername(),
                false,
                trainerRes.getUsername(),
                trainerRawPassword 
        );

        entityManager.flush();
        entityManager.clear();

        
        com.epam.java.specialization.gym_crm.dto.TrainingCreateDto trainingDto =
                com.epam.java.specialization.gym_crm.dto.TrainingCreateDto.builder()
                        .traineeId(existingTrainee.getId())
                        .trainerId(trainerRes.getId())
                        .trainingName("Power Yoga Morning")
                        .trainingTypeId(yogaType.getId())
                        .trainingDate(new Date())
                        .trainingDuration(90)
                        .build();

        assertThrows(IllegalArgumentException.class, () ->
                        gymFacade.createTraining(trainingDto, traineeUsername, traineeRawPassword),
                "Cannot create training with inactive trainee or trainer according to business rules"
        );
    }

    @Test
    @DisplayName("createTraining: Should automatically link Trainer to Trainee's trainers list")
    void testCreateTraining_ShouldAutomaticallyAssignTrainerToTrainee() {
        
        TrainerCreateDto trainerDto = TrainerCreateDto.builder()
                .firstName("Danylo")
                .lastName("Shlapak")
                .trainingTypeId(yogaType.getId())
                .build();
        TrainerResponseDto trainerRes = gymFacade.createTrainer(trainerDto);

        
        Trainee traineeBefore = entityManager.find(Trainee.class, existingTrainee.getId());
        assertTrue(traineeBefore.getTrainers() == null || traineeBefore.getTrainers().isEmpty());

        
        com.epam.java.specialization.gym_crm.dto.TrainingCreateDto trainingDto =
                com.epam.java.specialization.gym_crm.dto.TrainingCreateDto.builder()
                        .traineeId(existingTrainee.getId())
                        .trainerId(trainerRes.getId())
                        .trainingName("First Joint Session")
                        .trainingTypeId(yogaType.getId())
                        .trainingDate(new Date())
                        .trainingDuration(60)
                        .build();

        String traineeUsername = traineeBefore.getUser().getUsername();
        gymFacade.createTraining(trainingDto, traineeUsername, traineeRawPassword);

        entityManager.flush();
        entityManager.clear();

        
        Trainee traineeAfter = entityManager.find(Trainee.class, existingTrainee.getId());
        assertNotNull(traineeAfter.getTrainers());
        assertEquals(1, traineeAfter.getTrainers().size());
        assertEquals(trainerRes.getId(), traineeAfter.getTrainers().get(0).getId());
    }

    @Test
    @DisplayName("getTraineeTrainings & getTrainerTrainings: Should retrieve filtered training lists")
    void testGetTrainings_ShouldReturnCorrectLists() {
        
        TrainerCreateDto trainerDto = TrainerCreateDto.builder()
                .firstName("Elena")
                .lastName("Kostova")
                .trainingTypeId(yogaType.getId())
                .build();
        TrainerResponseDto trainerRes = gymFacade.createTrainer(trainerDto);

        com.epam.java.specialization.gym_crm.dto.TrainingCreateDto trainingDto =
                com.epam.java.specialization.gym_crm.dto.TrainingCreateDto.builder()
                        .traineeId(existingTrainee.getId())
                        .trainerId(trainerRes.getId())
                        .trainingName("Yoga Core")
                        .trainingTypeId(yogaType.getId())
                        .trainingDate(new Date())
                        .trainingDuration(45)
                        .build();

        String traineeUsername = existingTrainee.getUsername();
        gymFacade.createTraining(trainingDto, traineeUsername, traineeRawPassword);

        entityManager.flush();
        entityManager.clear();

        
        java.util.List<com.epam.java.specialization.gym_crm.dto.TrainingResponseDto> traineeTrainings =
                gymFacade.getTraineeTrainings(traineeUsername, null, null, null, null, traineeUsername, traineeRawPassword);
        assertFalse(traineeTrainings.isEmpty());
        assertEquals("Yoga Core", traineeTrainings.get(0).getTrainingName());

        
        java.util.List<com.epam.java.specialization.gym_crm.dto.TrainingResponseDto> trainerTrainings =
                gymFacade.getTrainerTrainings(trainerRes.getUsername(), null, null, null, traineeUsername, traineeRawPassword);
        assertFalse(trainerTrainings.isEmpty());
    }

    

    @Test
    @DisplayName("updateTraineeTrainers: Should completely replace old trainers list with new ones")
    void testUpdateTraineeTrainers_ShouldReplaceOldTrainersCompletely() {
        String traineeUsername = existingTrainee.getUsername();

        
        TrainerCreateDto trainerDto1 = TrainerCreateDto.builder()
                .firstName("Ivan")
                .lastName("Prokopchyk")
                .trainingTypeId(yogaType.getId())
                .build();
        TrainerResponseDto trainer1 = gymFacade.createTrainer(trainerDto1);

        TrainerCreateDto trainerDto2 = TrainerCreateDto.builder()
                .firstName("Elena")
                .lastName("Kostova")
                .trainingTypeId(yogaType.getId())
                .build();
        TrainerResponseDto trainer2 = gymFacade.createTrainer(trainerDto2);

        
        com.epam.java.specialization.gym_crm.dto.TrainingCreateDto trainingDto =
                com.epam.java.specialization.gym_crm.dto.TrainingCreateDto.builder()
                        .traineeId(existingTrainee.getId())
                        .trainerId(trainer1.getId())
                        .trainingName("Initial Training")
                        .trainingTypeId(yogaType.getId())
                        .trainingDate(new Date())
                        .trainingDuration(60)
                        .build();
        gymFacade.createTraining(trainingDto, traineeUsername, traineeRawPassword);

        entityManager.flush();
        entityManager.clear();

        
        Trainee traineeMid = entityManager.find(Trainee.class, existingTrainee.getId());
        assertEquals(1, traineeMid.getTrainers().size());
        assertEquals(trainer1.getId(), traineeMid.getTrainers().get(0).getId());

        
        
        java.util.List<String> newTrainersUsernames = java.util.List.of(trainer2.getUsername());
        gymFacade.updateTraineeTrainers(traineeUsername, newTrainersUsernames, traineeUsername, traineeRawPassword);

        entityManager.flush();
        entityManager.clear();

        
        Trainee traineeAfter = entityManager.find(Trainee.class, existingTrainee.getId());
        assertNotNull(traineeAfter.getTrainers());
        assertEquals(1, traineeAfter.getTrainers().size(), "Old trainers list must be completely overridden");
        assertEquals(trainer2.getId(), traineeAfter.getTrainers().get(0).getId(), "The list must contain only the newly assigned trainer");
    }

    @Test
    @DisplayName("getTraineeTrainings: Should strictly respect hardcoded pagination boundaries (20 records)")
    void testGetTraineeTrainings_ShouldApplyPaginationLimits() {
        String traineeUsername = existingTrainee.getUsername();

        
        TrainerCreateDto trainerDto = TrainerCreateDto.builder()
                .firstName("Danylo")
                .lastName("Shlapak")
                .trainingTypeId(yogaType.getId())
                .build();
        TrainerResponseDto trainerRes = gymFacade.createTrainer(trainerDto);

        
        
        for (int i = 1; i <= 22; i++) {
            com.epam.java.specialization.gym_crm.dto.TrainingCreateDto trainingDto =
                    com.epam.java.specialization.gym_crm.dto.TrainingCreateDto.builder()
                            .traineeId(existingTrainee.getId())
                            .trainerId(trainerRes.getId())
                            .trainingName("Mass Training Session " + i)
                            .trainingTypeId(yogaType.getId())
                            .trainingDate(new Date())
                            .trainingDuration(45)
                            .build();
            gymFacade.createTraining(trainingDto, traineeUsername, traineeRawPassword);
        }

        entityManager.flush();
        entityManager.clear();

        
        java.util.List<com.epam.java.specialization.gym_crm.dto.TrainingResponseDto> trainingsResult =
                gymFacade.getTraineeTrainings(traineeUsername, null, null, null, null, traineeUsername, traineeRawPassword);

        
        assertNotNull(trainingsResult);
        assertEquals(20, trainingsResult.size(), "The facade method must limit the returned list to a maximum of 20 elements");
    }

    @Test
    @DisplayName("createTraining: Should preserve exact hours, minutes, and seconds when trainingDate is stored as TIMESTAMP")
    void testCreateTraining_ShouldPreserveExactTime_WhenUsingTimestampType() {
        String traineeUsername = existingTrainee.getUsername();

        
        TrainerCreateDto trainerDto = TrainerCreateDto.builder()
                .firstName("Danylo")
                .lastName("Shlapak")
                .trainingTypeId(yogaType.getId())
                .build();
        TrainerResponseDto trainerRes = gymFacade.createTrainer(trainerDto);

        
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.set(2026, java.util.Calendar.JULY, 8, 18, 30, 15);
        calendar.set(java.util.Calendar.MILLISECOND, 0); 
        Date exactTargetDate = calendar.getTime();

        com.epam.java.specialization.gym_crm.dto.TrainingCreateDto trainingDto =
                com.epam.java.specialization.gym_crm.dto.TrainingCreateDto.builder()
                        .traineeId(existingTrainee.getId())
                        .trainerId(trainerRes.getId())
                        .trainingName("Timestamp Precision Yoga")
                        .trainingTypeId(yogaType.getId())
                        .trainingDate(exactTargetDate)
                        .trainingDuration(60)
                        .build();

        
        com.epam.java.specialization.gym_crm.dto.TrainingResponseDto savedResponse =
                gymFacade.createTraining(trainingDto, traineeUsername, traineeRawPassword);

        entityManager.flush();
        
        entityManager.clear();

        
        com.epam.java.specialization.gym_crm.model.Training dbTraining =
                entityManager.find(com.epam.java.specialization.gym_crm.model.Training.class, savedResponse.getId());

        assertNotNull(dbTraining, "Training record must exist in the database");
        assertNotNull(dbTraining.getTrainingDate(), "Stored training date must not be null");

        
        java.util.Calendar resultCalendar = java.util.Calendar.getInstance();
        resultCalendar.setTime(dbTraining.getTrainingDate());

        assertEquals(2026, resultCalendar.get(java.util.Calendar.YEAR));
        assertEquals(java.util.Calendar.JULY, resultCalendar.get(java.util.Calendar.MONTH));
        assertEquals(8, resultCalendar.get(java.util.Calendar.DAY_OF_MONTH));

        
        assertEquals(18, resultCalendar.get(java.util.Calendar.HOUR_OF_DAY), "Hours must match exactly and not be truncated");
        assertEquals(30, resultCalendar.get(java.util.Calendar.MINUTE), "Minutes must match exactly and not be truncated");
        assertEquals(15, resultCalendar.get(java.util.Calendar.SECOND), "Seconds must match exactly and not be truncated");
    }
}