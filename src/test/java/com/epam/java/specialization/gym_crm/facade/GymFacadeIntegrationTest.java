package com.epam.java.specialization.gym_crm.facade;

import com.epam.java.specialization.gym_crm.config.TestConfig;
import com.epam.java.specialization.gym_crm.dto.TraineeCreateDto;
import com.epam.java.specialization.gym_crm.dto.TraineeResponseDto;
import com.epam.java.specialization.gym_crm.dto.TraineeUpdateDto;
import com.epam.java.specialization.gym_crm.dto.TrainerCreateDto;
import com.epam.java.specialization.gym_crm.dto.TrainerResponseDto;
import com.epam.java.specialization.gym_crm.dto.TrainerUpdateDto;
import com.epam.java.specialization.gym_crm.dto.TrainingCreateDto;
import com.epam.java.specialization.gym_crm.dto.TrainingResponseDto;
import com.epam.java.specialization.gym_crm.model.TrainingType;
import com.epam.java.specialization.gym_crm.service.interfaces.ITrainingTypeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class GymFacadeIntegrationTest {

    @Autowired
    private GymFacade gymFacade;

    @Autowired
    private ITrainingTypeService trainingTypeService;

    private TrainingType savedType;
    private final Date fixedBirthDate;
    private final Date fixedTrainingDate = new Date();

    public GymFacadeIntegrationTest() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(1998, Calendar.OCTOBER, 14);
        this.fixedBirthDate = calendar.getTime();
    }

    @BeforeEach
    void setUp() {
        TrainingType initialType = TrainingType.builder()
                .trainingTypeName("Aerobics core")
                .build();
        savedType = trainingTypeService.create(initialType);
    }

    @Test
    void testTraineeMethods_Create_Get_Update_Delete() {
        TraineeCreateDto createDto = TraineeCreateDto.builder()
                .firstName("Marta")
                .lastName("Kravets")
                .address("Lviv, Naukova St, 5")
                .dateOfBirth(fixedBirthDate)
                .build();

        TraineeResponseDto created = gymFacade.createTrainee(createDto);

        assertNotNull(created.getId());
        assertEquals("Marta", created.getFirstName());
        assertEquals("Marta.Kravets", created.getUsername());
        assertTrue(created.getIsActive());
        System.out.println(">>> [FACADE METHOD VERIFIED]: createTrainee -> Result: " + created);

        Optional<TraineeResponseDto> fetchedOpt = gymFacade.getTrainee(created.getId());

        assertTrue(fetchedOpt.isPresent());
        TraineeResponseDto fetched = fetchedOpt.get();
        assertEquals("Lviv, Naukova St, 5", fetched.getAddress());
        System.out.println(">>> [FACADE METHOD VERIFIED]: getTrainee -> Result: " + fetched);

        TraineeUpdateDto updateDto = TraineeUpdateDto.builder()
                .id(created.getId())
                .firstName("Marta")
                .lastName("Kravets")
                .address("Odesa, Deribasivska St, 1")
                .dateOfBirth(fixedBirthDate)
                .isActive(false) // Деактивуємо
                .build();

        TraineeResponseDto updated = gymFacade.updateTrainee(updateDto);

        assertEquals("Odesa, Deribasivska St, 1", updated.getAddress());
        assertFalse(updated.getIsActive());
        System.out.println(">>> [FACADE METHOD VERIFIED]: updateTrainee -> Result: " + updated);

        gymFacade.deleteTrainee(created.getId());
        Optional<TraineeResponseDto> afterDelete = gymFacade.getTrainee(created.getId());

        assertFalse(afterDelete.isPresent(), "Trainee must be deleted from memory storage maps");
        System.out.println(">>> [FACADE METHOD VERIFIED]: deleteTrainee -> Success (Profile verified absent)");
    }


    @Test
    void testTrainerMethods_Create_Get_Update() {
        TrainerCreateDto createDto = TrainerCreateDto.builder()
                .firstName("Oleh")
                .lastName("Petrenko")
                .trainingTypeId(savedType.getId())
                .build();

        TrainerResponseDto created = gymFacade.createTrainer(createDto);

        assertNotNull(created.getId());
        assertEquals("Oleh", created.getFirstName());
        assertEquals("Oleh.Petrenko", created.getUsername());
        assertNotNull(created.getSpecialization());
        assertEquals("Aerobics core", created.getSpecialization().getTrainingTypeName());
        System.out.println(">>> [FACADE METHOD VERIFIED]: createTrainer -> Result: " + created);

        Optional<TrainerResponseDto> fetchedOpt = gymFacade.getTrainer(created.getId());

        assertTrue(fetchedOpt.isPresent());
        TrainerResponseDto fetched = fetchedOpt.get();
        assertEquals("Oleh.Petrenko", fetched.getUsername());
        System.out.println(">>> [FACADE METHOD VERIFIED]: getTrainer -> Result: " + fetched);

        TrainerUpdateDto updateDto = TrainerUpdateDto.builder()
                .id(created.getId())
                .firstName("Oleh")
                .lastName("Petrenko")
                .trainingTypeId(savedType.getId())
                .isActive(false)
                .build();

        TrainerResponseDto updated = gymFacade.updateTrainer(updateDto);

        assertFalse(updated.getIsActive());
        System.out.println(">>> [FACADE METHOD VERIFIED]: updateTrainer -> Result: " + updated);
    }


    @Test
    void testTrainingMethods_Create_Get() {
        TraineeResponseDto trainee = gymFacade.createTrainee(TraineeCreateDto.builder()
                .firstName("Ivan").lastName("Sirko").address("Zaporizhia").dateOfBirth(fixedBirthDate).build());

        TrainerResponseDto trainer = gymFacade.createTrainer(TrainerCreateDto.builder()
                .firstName("Ivan").lastName("Sirko").trainingTypeId(savedType.getId()).build());

        TrainingCreateDto trainingCreateDto = TrainingCreateDto.builder()
                .trainingName("Functional Power Fit")
                .traineeId(trainee.getId())
                .trainerId(trainer.getId())
                .trainingTypeId(savedType.getId())
                .trainingDate(fixedTrainingDate)
                .trainingDuration(90)
                .build();

        TrainingResponseDto createdTraining = gymFacade.createTraining(trainingCreateDto);

        assertNotNull(createdTraining.getId());
        assertEquals("Functional Power Fit", createdTraining.getTrainingName());
        assertEquals(90, createdTraining.getTrainingDuration());

        assertNotNull(createdTraining.getTrainee());
        assertEquals(trainee.getId(), createdTraining.getTrainee().getId());

        assertNotNull(createdTraining.getTrainer());
        assertEquals("Ivan.Sirko1", createdTraining.getTrainer().getUsername(), "Must handle inner collision logic");

        assertNotNull(createdTraining.getTrainingType());
        assertEquals("Aerobics core", createdTraining.getTrainingType().getTrainingTypeName());
        System.out.println(">>> [FACADE METHOD VERIFIED]: createTraining -> Deep Graph Tree Result: " + createdTraining);

        Optional<TrainingResponseDto> fetchedTrainingOpt = gymFacade.getTraining(createdTraining.getId());

        assertTrue(fetchedTrainingOpt.isPresent());
        TrainingResponseDto fetchedTraining = fetchedTrainingOpt.get();
        assertEquals("Functional Power Fit", fetchedTraining.getTrainingName());
        assertEquals("Ivan.Sirko", fetchedTraining.getTrainee().getUsername());
        System.out.println(">>> [FACADE METHOD VERIFIED]: getTraining -> Deep Graph Tree Result: " + fetchedTraining);
    }
}