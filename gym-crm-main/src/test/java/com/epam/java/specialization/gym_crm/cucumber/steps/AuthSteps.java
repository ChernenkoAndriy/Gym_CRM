package com.epam.java.specialization.gym_crm.cucumber.steps;

import com.epam.java.specialization.gym_crm.cucumber.TestContext;
import com.epam.java.specialization.gym_crm.dto.LoginRequestDto;
import com.epam.java.specialization.gym_crm.security.JwtService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private TestContext testContext;

    @When("the user logs in with username {string} and password {string}")
    public void theUserLogsInWithUsernameAndPassword(String username, String password) throws Exception {
        LoginRequestDto request = LoginRequestDto.builder()
                .username(username)
                .password(password)
                .build();

        ResultActions response = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        testContext.setLatestResponse(response);
    }

    @When("the user logs in {int} times with username {string} and password {string}")
    public void theUserLogsInTimesWithUsernameAndPassword(int times, String username, String password) throws Exception {
        for (int i = 0; i < times; i++) {
            theUserLogsInWithUsernameAndPassword(username, password);
        }
    }

    @Given("the user is authenticated with username {string} and password {string}")
    public void theUserIsAuthenticatedWithUsernameAndPassword(String username, String password) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        String token = jwtService.generateToken(userDetails);
        testContext.setToken(token);
    }

    @When("the user logs out")
    public void theUserLogsOut() throws Exception {
        ResultActions response = mockMvc.perform(post("/auth/logout")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + testContext.getToken()));

        testContext.setLatestResponse(response);
    }

    @When("the user accesses a protected endpoint with the previous token")
    public void theUserAccessesAProtectedEndpointWithThePreviousToken() throws Exception {
        ResultActions response = mockMvc.perform(get("/trainees/Trainee.Ten")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + testContext.getToken()));

        testContext.setLatestResponse(response);
    }

    @Then("the response status code is {int}")
    public void theResponseStatusCodeIs(int expectedStatus) throws Exception {
        testContext.getLatestResponse().andExpect(status().is(expectedStatus));
    }

    @And("the response contains a valid JWT token")
    public void theResponseContainsAValidJwtToken() throws Exception {
        String content = testContext.getLatestResponse().andReturn().getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(content);
        JsonNode tokenNode = jsonNode.get("token");

        assertNotNull(tokenNode);
        String token = tokenNode.asText();
        assertFalse(token.isBlank());
        testContext.setToken(token);
    }

    @And("the token type is {string}")
    public void theTokenTypeIs(String type) throws Exception {
        testContext.getLatestResponse().andExpect(jsonPath("$.type").value(type));
    }

    @And("the response username is {string}")
    public void theResponseUsernameIs(String username) throws Exception {
        testContext.getLatestResponse().andExpect(jsonPath("$.username").value(username));
    }
}