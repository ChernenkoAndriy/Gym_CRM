package com.epam.java.specialization.gym_crm.integration;

import com.epam.java.specialization.gym_crm.client.TrainerWorkloadClient;
import com.epam.java.specialization.gym_crm.dto.ActivationRequestDto;
import com.epam.java.specialization.gym_crm.dto.TrainerRegisterRequestDto;
import com.epam.java.specialization.gym_crm.dto.TrainerUpdateRequestDto;
import com.epam.java.specialization.gym_crm.dto.external.MonthWorkloadDto;
import com.epam.java.specialization.gym_crm.dto.external.TrainerWorkloadResponseDto;
import com.epam.java.specialization.gym_crm.dto.external.YearWorkloadDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Transactional
class TrainerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TrainerWorkloadClient trainerWorkloadClient;

    @Test
    @DisplayName("POST /trainers - Success registration (PermitAll)")
    void registerTrainer_Success() throws Exception {
        TrainerRegisterRequestDto request = TrainerRegisterRequestDto.builder()
                .firstName("Maksym")
                .lastName("Semeniuk")
                .specializationId(1L)
                .build();

        mockMvc.perform(post("/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Maksym.Semeniuk"))
                .andExpect(jsonPath("$.password").isNotEmpty())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("POST /trainers - Fail (EntityNotFoundException - Invalid Specialization ID)")
    void registerTrainer_Fail_InvalidSpecialization() throws Exception {
        TrainerRegisterRequestDto request = TrainerRegisterRequestDto.builder()
                .firstName("Maksym")
                .lastName("Semeniuk")
                .specializationId(999L)
                .build();

        mockMvc.perform(post("/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("TrainingType not found with ID: 999"));
    }

    @Test
    @DisplayName("POST /trainers - Fail (Validation Failed)")
    void registerTrainer_Fail_Validation() throws Exception {
        TrainerRegisterRequestDto invalidRequest = TrainerRegisterRequestDto.builder()
                .firstName("")
                .lastName("   ")
                .specializationId(null)
                .build();

        mockMvc.perform(post("/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.validationErrors.firstName").isNotEmpty())
                .andExpect(jsonPath("$.validationErrors.lastName").isNotEmpty())
                .andExpect(jsonPath("$.validationErrors.specializationId").isNotEmpty());
    }

    @Test
    @WithMockUser(username = "Trainer.Ten", roles = "TRAINER")
    @DisplayName("GET /trainers/{username} - Success fetching profile")
    void getTrainerProfile_Success() throws Exception {
        mockMvc.perform(get("/trainers/Trainer.Ten"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Trainer"))
                .andExpect(jsonPath("$.lastName").value("Ten"))
                .andExpect(jsonPath("$.specialization").value("Yoga"));
    }

    @Test
    @WithMockUser(username = "Trainer.Ten", roles = "TRAINER")
    @DisplayName("PUT /trainers/{username} - Success update profile")
    void updateTrainerProfile_Success() throws Exception {
        TrainerUpdateRequestDto updateRequest = TrainerUpdateRequestDto.builder()
                .username("Trainer.Ten")
                .firstName("UpdatedTrainer")
                .lastName("Ten")
                .isActive(true)
                .build();

        mockMvc.perform(put("/trainers/Trainer.Ten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("UpdatedTrainer"))
                .andExpect(jsonPath("$.lastName").value("Ten"))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    @WithMockUser(username = "Trainer.Ten", roles = "TRAINER")
    @DisplayName("PUT /trainers/{username} - Fail (Validation Failed)")
    void updateTrainerProfile_Fail_Validation() throws Exception {
        TrainerUpdateRequestDto invalidRequest = TrainerUpdateRequestDto.builder()
                .username("")
                .firstName("")
                .lastName("")
                .isActive(null)
                .build();

        mockMvc.perform(put("/trainers/Trainer.Ten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    @WithMockUser(username = "Trainer.Ten", roles = "TRAINER")
    @DisplayName("PATCH /trainers/{username}/activation - Success deactivation")
    void toggleTrainerActivation_Success() throws Exception {
        ActivationRequestDto request = ActivationRequestDto.builder()
                .isActive(false)
                .build();

        mockMvc.perform(patch("/trainers/Trainer.Ten/activation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "Trainer.Ten", roles = "TRAINER")
    @DisplayName("PATCH /trainers/{username}/activation - Fail (IllegalStateException - Already same status)")
    void toggleTrainerActivation_Fail_AlreadySameStatus() throws Exception {
        ActivationRequestDto request = ActivationRequestDto.builder()
                .isActive(true)
                .build();

        mockMvc.perform(patch("/trainers/Trainer.Ten/activation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User profile active status is already true"));
    }

    @Test
    @WithMockUser(username = "Trainer.Ten", roles = "TRAINER")
    @DisplayName("GET /trainers/{username}/workload - Success retrieving workload summary via Feign Client")
    void getTrainerWorkload_Success() throws Exception {
        MonthWorkloadDto monthWorkload = MonthWorkloadDto.builder()
                .month(8)
                .trainingSummaryDuration(120)
                .build();

        YearWorkloadDto yearWorkload = YearWorkloadDto.builder()
                .year(2026)
                .months(List.of(monthWorkload))
                .build();

        TrainerWorkloadResponseDto mockResponse = TrainerWorkloadResponseDto.builder()
                .username("Trainer.Ten")
                .firstName("Trainer")
                .lastName("Ten")
                .status(true)
                .years(List.of(yearWorkload))
                .build();

        when(trainerWorkloadClient.getTrainerWorkload(eq("Trainer.Ten"), eq(2026), eq(8)))
                .thenReturn(ResponseEntity.ok(mockResponse));

        mockMvc.perform(get("/trainers/Trainer.Ten/workload")
                        .param("year", "2026")
                        .param("month", "8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Trainer.Ten"))
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.years[0].year").value(2026))
                .andExpect(jsonPath("$.years[0].months[0].month").value(8))
                .andExpect(jsonPath("$.years[0].months[0].trainingSummaryDuration").value(120));
    }
}