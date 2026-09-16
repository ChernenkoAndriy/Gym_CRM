package com.epam.java.specialization.gym_crm.cucumber.steps;

import com.epam.java.specialization.gym_crm.cucumber.TestContext;
import com.epam.java.specialization.gym_crm.dto.TrainingAddRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.And;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class TrainingSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestContext testContext;

    @When("the user creates a training with trainee {string}, trainer {string}, name {string}, duration {int}")
    public void theUserCreatesATrainingWithTraineeTrainerNameDuration(
            String traineeUsername, String trainerUsername, String trainingName, int duration) throws Exception {
        TrainingAddRequestDto request = TrainingAddRequestDto.builder()
                .traineeUsername(traineeUsername)
                .trainerUsername(trainerUsername)
                .trainingName(trainingName)
                .trainingDate(new Date())
                .trainingDuration(duration)
                .build();

        ResultActions response = mockMvc.perform(post("/trainings")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + testContext.getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        testContext.setLatestResponse(response);
    }

    @When("the user creates a training with trainee {string}, trainer {string}, name {string}, duration {int} without token")
    public void theUserCreatesATrainingWithTraineeTrainerNameDurationWithoutToken(
            String traineeUsername, String trainerUsername, String trainingName, int duration) throws Exception {
        TrainingAddRequestDto request = TrainingAddRequestDto.builder()
                .traineeUsername(traineeUsername)
                .trainerUsername(trainerUsername)
                .trainingName(trainingName)
                .trainingDate(new Date())
                .trainingDuration(duration)
                .build();

        ResultActions response = mockMvc.perform(post("/trainings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        testContext.setLatestResponse(response);
    }

    @When("the user creates an invalid training with empty fields")
    public void theUserCreatesAnInvalidTrainingWithEmptyFields() throws Exception {
        TrainingAddRequestDto request = TrainingAddRequestDto.builder()
                .traineeUsername("")
                .trainerUsername("")
                .trainingName(" ")
                .trainingDate(null)
                .trainingDuration(null)
                .build();

        ResultActions response = mockMvc.perform(post("/trainings")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + testContext.getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        testContext.setLatestResponse(response);
    }

    @When("the user requests filtered trainings for trainee {string} and type {string}")
    public void theUserRequestsFilteredTrainingsForTraineeAndType(String username, String trainingType) throws Exception {
        ResultActions response = mockMvc.perform(get("/trainings/trainee")
                .param("username", username)
                .param("trainingType", trainingType)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + testContext.getToken()));

        testContext.setLatestResponse(response);
    }

    @When("the user requests filtered trainings for trainer {string} and trainee {string}")
    public void theUserRequestsFilteredTrainingsForTrainerAndTrainee(String username, String traineeName) throws Exception {
        ResultActions response = mockMvc.perform(get("/trainings/trainer")
                .param("username", username)
                .param("traineeName", traineeName)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + testContext.getToken()));

        testContext.setLatestResponse(response);
    }

    @And("the first training name is {string}")
    public void theFirstTrainingNameIs(String name) throws Exception {
        testContext.getLatestResponse().andExpect(jsonPath("$[0].trainingName").value(name));
    }

    @And("the first training type is {string}")
    public void theFirstTrainingTypeIs(String type) throws Exception {
        testContext.getLatestResponse().andExpect(jsonPath("$[0].trainingType").value(type));
    }

    @And("the first trainer name is {string}")
    public void theFirstTrainerNameIs(String trainerName) throws Exception {
        testContext.getLatestResponse().andExpect(jsonPath("$[0].trainerName").value(trainerName));
    }

    @And("the first trainee name is {string}")
    public void theFirstTraineeNameIs(String traineeName) throws Exception {
        testContext.getLatestResponse().andExpect(jsonPath("$[0].traineeName").value(traineeName));
    }

    @When("the user requests training types")
    public void theUserRequestsTrainingTypes() throws Exception {
        ResultActions response = mockMvc.perform(get("/trainings/types")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + testContext.getToken()));

        testContext.setLatestResponse(response);
    }

    @And("training types contain {string} and {string}")
    public void trainingTypesContainAnd(String type1, String type2) throws Exception {
        testContext.getLatestResponse()
                .andExpect(jsonPath("$[0].trainingType").value(type1))
                .andExpect(jsonPath("$[1].trainingType").value(type2));
    }

    @When("the user deletes training with id {long}")
    public void theUserDeletesTrainingWithId(Long id) throws Exception {
        ResultActions response = mockMvc.perform(delete("/trainings/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + testContext.getToken()));

        testContext.setLatestResponse(response);
    }
}