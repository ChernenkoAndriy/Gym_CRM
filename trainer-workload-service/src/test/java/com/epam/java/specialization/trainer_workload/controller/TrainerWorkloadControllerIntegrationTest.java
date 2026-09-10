package com.epam.java.specialization.trainer_workload.controller;

import com.epam.java.specialization.trainer_workload.model.MonthWorkload;
import com.epam.java.specialization.trainer_workload.model.TrainerWorkload;
import com.epam.java.specialization.trainer_workload.model.YearWorkload;
import com.epam.java.specialization.trainer_workload.repository.TrainerWorkloadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@EnableAutoConfiguration(exclude = {
        MongoAutoConfiguration.class,
        MongoDataAutoConfiguration.class,
        MongoRepositoriesAutoConfiguration.class
})
class TrainerWorkloadControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TrainerWorkloadRepository repository;

    private TrainerWorkload sampleWorkload;

    @BeforeEach
    void setUp() {
        MonthWorkload month = MonthWorkload.builder().monthNumber(8).summaryDuration(90).build();
        YearWorkload year = YearWorkload.builder().yearNumber(2026).months(new ArrayList<>(List.of(month))).build();
        sampleWorkload = TrainerWorkload.builder()
                .username("Trainer.Ten")
                .firstName("Trainer")
                .lastName("Ten")
                .isActive(true)
                .years(new ArrayList<>(List.of(year)))
                .build();
    }

    @Test
    @DisplayName("GET /workloads/{username} - Should return 403 Forbidden for unauthenticated requests")
    void getWorkload_ShouldReturn403_WhenNoAuth() throws Exception {
        mockMvc.perform(get("/workloads/Trainer.Ten"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "gym-crm-main", roles = "SERVICE")
    @DisplayName("GET /workloads/{username} - Should retrieve summary and support filtering")
    void getWorkload_Success_AndRetrieveFiltered() throws Exception {
        when(repository.findByUsername("Trainer.Ten")).thenReturn(Optional.of(sampleWorkload));

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
    @DisplayName("GET /workloads/{username} - Should return 404 when trainer not found")
    void getWorkload_NotFound() throws Exception {
        when(repository.findByUsername("NonExistent.Trainer")).thenReturn(Optional.empty());

        mockMvc.perform(get("/workloads/NonExistent.Trainer"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }
}