package com.epam.java.specialization.gym_crm.cucumber.steps;

import com.epam.java.specialization.gym_crm.cucumber.TestContext;
import com.epam.java.specialization.gym_crm.dto.ActivationRequestDto;
import com.epam.java.specialization.gym_crm.dto.TrainerRegisterRequestDto;
import com.epam.java.specialization.gym_crm.dto.TrainerUpdateRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.And;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class TrainerSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestContext testContext;

    @When("the user registers a trainer with first name {string} and last name {string} and specialization id {long}")
    public void theUserRegistersATrainerWithFirstNameAndLastNameAndSpecializationId(
            String firstName, String lastName, Long specializationId) throws Exception {
        TrainerRegisterRequestDto request = TrainerRegisterRequestDto.builder()
                .firstName(firstName)
                .lastName(lastName)
                .specializationId(specializationId)
                .build();

        ResultActions response = mockMvc.perform(post("/trainers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        testContext.setLatestResponse(response);
    }

    @When("the user registers an invalid trainer with empty fields")
    public void theUserRegistersAnInvalidTrainerWithEmptyFields() throws Exception {
        TrainerRegisterRequestDto invalidRequest = TrainerRegisterRequestDto.builder()
                .firstName("")
                .lastName("   ")
                .specializationId(null)
                .build();

        ResultActions response = mockMvc.perform(post("/trainers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)));

        testContext.setLatestResponse(response);
    }

    @When("the user requests the trainer profile for {string}")
    public void theUserRequestsTheTrainerProfileFor(String username) throws Exception {
        ResultActions response = mockMvc.perform(get("/trainers/{username}", username)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + testContext.getToken()));

        testContext.setLatestResponse(response);
    }

    @When("the user requests the trainer profile for {string} without token")
    public void theUserRequestsTheTrainerProfileForWithoutToken(String username) throws Exception {
        ResultActions response = mockMvc.perform(get("/trainers/{username}", username));

        testContext.setLatestResponse(response);
    }

    @And("the trainer profile first name is {string}")
    public void theTrainerProfileFirstNameIs(String firstName) throws Exception {
        testContext.getLatestResponse().andExpect(jsonPath("$.firstName").value(firstName));
    }

    @And("the trainer profile last name is {string}")
    public void theTrainerProfileLastNameIs(String lastName) throws Exception {
        testContext.getLatestResponse().andExpect(jsonPath("$.lastName").value(lastName));
    }

    @And("the trainer specialization is {string}")
    public void theTrainerSpecializationIs(String specialization) throws Exception {
        testContext.getLatestResponse().andExpect(jsonPath("$.specialization").value(specialization));
    }

    @When("the user updates the profile for {string} with first name {string} and last name {string} and active status {word}")
    public void theUserUpdatesTheProfileForWithFirstNameAndLastNameAndActiveStatus(
            String username, String firstName, String lastName, String isActive) throws Exception {
        TrainerUpdateRequestDto updateRequest = TrainerUpdateRequestDto.builder()
                .username(username)
                .firstName(firstName)
                .lastName(lastName)
                .isActive(Boolean.parseBoolean(isActive))
                .build();

        ResultActions response = mockMvc.perform(put("/trainers/{username}", username)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + testContext.getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)));

        testContext.setLatestResponse(response);
    }

    @When("the user updates the profile for {string} with invalid empty fields")
    public void theUserUpdatesTheProfileForWithInvalidEmptyFields(String username) throws Exception {
        TrainerUpdateRequestDto invalidRequest = TrainerUpdateRequestDto.builder()
                .username("")
                .firstName("")
                .lastName("")
                .isActive(null)
                .build();

        ResultActions response = mockMvc.perform(put("/trainers/{username}", username)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + testContext.getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)));

        testContext.setLatestResponse(response);
    }

    @When("the user sets trainer activation status to {word} for username {string}")
    public void theUserSetsTrainerActivationStatusToForUsername(String isActive, String username) throws Exception {
        ActivationRequestDto request = ActivationRequestDto.builder()
                .isActive(Boolean.parseBoolean(isActive))
                .build();

        ResultActions response = mockMvc.perform(patch("/trainers/{username}/activation", username)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + testContext.getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        testContext.setLatestResponse(response);
    }

    @And("the error message is {string}")
    public void theErrorMessageIs(String message) throws Exception {
        testContext.getLatestResponse().andExpect(jsonPath("$.message").value(message));
    }

    @And("the error title is {string}")
    public void theErrorTitleIs(String title) throws Exception {
        testContext.getLatestResponse().andExpect(jsonPath("$.error").value(title));
    }
}