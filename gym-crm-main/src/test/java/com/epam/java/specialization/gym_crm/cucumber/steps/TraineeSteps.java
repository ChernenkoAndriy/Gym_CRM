package com.epam.java.specialization.gym_crm.cucumber.steps;

import com.epam.java.specialization.gym_crm.cucumber.TestContext;
import com.epam.java.specialization.gym_crm.dto.ActivationRequestDto;
import com.epam.java.specialization.gym_crm.dto.RegistrationResponseDto;
import com.epam.java.specialization.gym_crm.dto.TraineeRegisterRequestDto;
import com.epam.java.specialization.gym_crm.dto.TraineeUpdateRequestDto;
import com.epam.java.specialization.gym_crm.dto.TrainerUsernameRequestDto;
import com.epam.java.specialization.gym_crm.service.interfaces.TraineeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class TraineeSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TraineeService traineeService;

    @Autowired
    private TestContext testContext;

    @When("the user registers a trainee with first name {string} and last name {string} and address {string}")
    public void theUserRegistersATraineeWithFirstNameAndLastNameAndAddress(String firstName, String lastName, String address) throws Exception {
        TraineeRegisterRequestDto request = TraineeRegisterRequestDto.builder()
                .firstName(firstName)
                .lastName(lastName)
                .address(address)
                .dateOfBirth(new Date())
                .build();

        ResultActions response = mockMvc.perform(post("/trainees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        testContext.setLatestResponse(response);
    }

    @When("the user registers an invalid trainee with empty fields")
    public void theUserRegistersAnInvalidTrainerWithEmptyFields() throws Exception {
        TraineeRegisterRequestDto invalidRequest = TraineeRegisterRequestDto.builder()
                .firstName("")
                .lastName("  ")
                .build();

        ResultActions response = mockMvc.perform(post("/trainees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)));

        testContext.setLatestResponse(response);
    }

    @And("the response contains a generated password")
    public void theResponseContainsAGeneratedPassword() throws Exception {
        testContext.getLatestResponse().andExpect(jsonPath("$.password", notNullValue()));
    }

    @When("the user requests the trainee profile for {string}")
    public void theUserRequestsTheTraineeProfileFor(String username) throws Exception {
        ResultActions response = mockMvc.perform(get("/trainees/{username}", username)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + testContext.getToken()));

        testContext.setLatestResponse(response);
    }

    @When("the user requests the trainee profile for {string} without token")
    public void theUserRequestsTheTraineeProfileForWithoutToken(String username) throws Exception {
        ResultActions response = mockMvc.perform(get("/trainees/{username}", username));

        testContext.setLatestResponse(response);
    }

    @And("the profile first name is {string}")
    public void theProfileFirstNameIs(String firstName) throws Exception {
        testContext.getLatestResponse().andExpect(jsonPath("$.firstName").value(firstName));
    }

    @And("the profile last name is {string}")
    public void theProfileLastNameIs(String lastName) throws Exception {
        testContext.getLatestResponse().andExpect(jsonPath("$.lastName").value(lastName));
    }

    @And("the profile address is {string}")
    public void theProfileAddressIs(String address) throws Exception {
        testContext.getLatestResponse().andExpect(jsonPath("$.address").value(address));
    }

    @When("the user updates the profile for {string} with first name {string} and last name {string} and address {string} and active status {word}")
    public void theUserUpdatesTheProfileForWithFirstNameAndLastNameAndAddressAndActiveStatus(
            String username, String firstName, String lastName, String address, String isActive) throws Exception {
        TraineeUpdateRequestDto request = TraineeUpdateRequestDto.builder()
                .username(username)
                .firstName(firstName)
                .lastName(lastName)
                .isActive(Boolean.parseBoolean(isActive))
                .address(address)
                .build();

        ResultActions response = mockMvc.perform(put("/trainees/{username}", username)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + testContext.getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        testContext.setLatestResponse(response);
    }

    @When("the user updates trainee profile for {string} with invalid empty fields")
    public void theUserUpdatesTraineeProfileForWithInvalidEmptyFields(String username) throws Exception {
        TraineeUpdateRequestDto invalidRequest = TraineeUpdateRequestDto.builder()
                .username("")
                .firstName("")
                .lastName("")
                .isActive(null)
                .build();

        ResultActions response = mockMvc.perform(put("/trainees/{username}", username)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + testContext.getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)));

        testContext.setLatestResponse(response);
    }

    @When("the user sets trainee activation status to {word} for username {string}")
    public void theUserSetsTraineeActivationStatusToForUsername(String isActive, String username) throws Exception {
        ActivationRequestDto request = ActivationRequestDto.builder()
                .isActive(Boolean.parseBoolean(isActive))
                .build();

        ResultActions response = mockMvc.perform(patch("/trainees/{username}/activation", username)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + testContext.getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        testContext.setLatestResponse(response);
    }

    @When("the user requests unassigned trainers for {string}")
    public void theUserRequestsUnassignedTrainersFor(String username) throws Exception {
        ResultActions response = mockMvc.perform(get("/trainees/{username}/unassigned-trainers", username)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + testContext.getToken()));

        testContext.setLatestResponse(response);
    }

    @And("the trainers list contains {string}")
    public void theTrainersListContains(String trainerUsername) throws Exception {
        testContext.getLatestResponse().andExpect(jsonPath("$[*].username", hasItem(trainerUsername)));
    }

    @When("the user updates trainee trainers list for {string} with {string}")
    public void theUserUpdatesTraineeTrainersListForWith(String username, String trainerUsername) throws Exception {
        List<TrainerUsernameRequestDto> request = Collections.singletonList(
                TrainerUsernameRequestDto.builder().username(trainerUsername).build()
        );

        ResultActions response = mockMvc.perform(put("/trainees/{username}/trainers", username)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + testContext.getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        testContext.setLatestResponse(response);
    }

    @And("the assigned trainer is {string}")
    public void theAssignedTrainerIs(String trainerUsername) throws Exception {
        testContext.getLatestResponse().andExpect(jsonPath("$[0].username").value(trainerUsername));
    }

    @Given("a new registered trainee exists for deletion")
    public void aNewRegisteredTraineeExistsForDeletion() {
        TraineeRegisterRequestDto request = TraineeRegisterRequestDto.builder()
                .firstName("ToDelete")
                .lastName("Trainee")
                .address("Temporary St")
                .dateOfBirth(new Date())
                .build();

        RegistrationResponseDto registered = traineeService.register(request);
        testContext.setUsername(registered.getUsername());
        testContext.setToken(registered.getToken());
    }

    @When("the user deletes the registered trainee profile")
    public void theUserDeletesTheRegisteredTraineeProfile() throws Exception {
        ResultActions response = mockMvc.perform(delete("/trainees/{username}", testContext.getUsername())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + testContext.getToken()));

        testContext.setLatestResponse(response);
    }
}