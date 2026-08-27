package com.epam.java.specialization.trainer_workload.controller;

import com.epam.java.specialization.common.dto.*;
import com.epam.java.specialization.trainer_workload.repository.TrainerWorkloadRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TrainerWorkloadControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TrainerWorkloadRepository repository;

    @BeforeEach
    void clear() {
        repository.clear();
    }

    @Test
    @DisplayName("POST /workloads - Should return 403 Forbidden for unauthenticated requests")
    void processWorkload_ShouldReturn403_WhenNoAuth() throws Exception {
        TrainerWorkloadRequestDto request = TrainerWorkloadRequestDto.builder()
                .username("Trainer.Ten")
                .firstName("Trainer")
                .lastName("Ten")
                .isActive(true)
                .trainingDate(new Date())
                .trainingDuration(60)
                .actionType(ActionType.ADD)
                .build();

        mockMvc.perform(post("/workloads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "gym-crm-main", roles = "SERVICE")
    @DisplayName("POST & GET /workloads - Should accept ADD workload, retrieve summary and support filtering")
    void processWorkload_Success_AndRetrieveFiltered() throws Exception {
        TrainerWorkloadRequestDto request = TrainerWorkloadRequestDto.builder()
                .username("Trainer.Ten")
                .firstName("Trainer")
                .lastName("Ten")
                .isActive(true)
                .trainingDate(new Date())
                .trainingDuration(90)
                .actionType(ActionType.ADD)
                .build();

        mockMvc.perform(post("/workloads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/workloads/Trainer.Ten"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Trainer.Ten"))
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.years").isArray());

        mockMvc.perform(get("/workloads/Trainer.Ten")
                        .param("year", "2026")
                        .param("month", "8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Trainer.Ten"))
                .andExpect(jsonPath("$.years[0].year").value(2026))
                .andExpect(jsonPath("$.years[0].months[0].month").value(8));
    }

    @Test
    @WithMockUser(username = "gym-crm-main", roles = "SERVICE")
    @DisplayName("POST /workloads - Fail on invalid payload validation")
    void processWorkload_Fail_Validation() throws Exception {
        TrainerWorkloadRequestDto request = TrainerWorkloadRequestDto.builder()
                .username("")
                .firstName("")
                .lastName("")
                .build();

        mockMvc.perform(post("/workloads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }
}