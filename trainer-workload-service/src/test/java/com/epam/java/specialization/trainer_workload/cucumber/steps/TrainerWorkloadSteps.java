package com.epam.java.specialization.trainer_workload.cucumber.steps;

import com.epam.java.specialization.common.dto.ActionType;
import com.epam.java.specialization.common.dto.TrainerWorkloadRequestDto;
import com.epam.java.specialization.trainer_workload.cucumber.TestContext;
import com.epam.java.specialization.trainer_workload.model.MonthWorkload;
import com.epam.java.specialization.trainer_workload.model.TrainerWorkload;
import com.epam.java.specialization.trainer_workload.model.YearWorkload;
import com.epam.java.specialization.trainer_workload.repository.TrainerWorkloadRepository;
import com.epam.java.specialization.trainer_workload.service.interfaces.TrainerWorkloadService;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TrainerWorkloadSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TrainerWorkloadRepository repository;

    @Autowired
    private TrainerWorkloadService workloadService;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private TestContext testContext;

    @Value("${app.kafka.topics.trainer-workload:trainer-workload-topic-test}")
    private String workloadTopic;

    @Given("a trainer workload exists for {string} with year {int}, month {int} and duration {int}")
    public void aTrainerWorkloadExistsWithYearMonthDuration(String username, int year, int month, int duration) {
        MonthWorkload monthWorkload = MonthWorkload.builder()
                .monthNumber(month)
                .summaryDuration(duration)
                .build();

        YearWorkload yearWorkload = YearWorkload.builder()
                .yearNumber(year)
                .months(new ArrayList<>(List.of(monthWorkload)))
                .build();

        TrainerWorkload workload = TrainerWorkload.builder()
                .username(username)
                .firstName("Trainer")
                .lastName("Ten")
                .isActive(true)
                .years(new ArrayList<>(List.of(yearWorkload)))
                .build();

        when(repository.findByUsername(username)).thenReturn(Optional.of(workload));
    }

    @Given("no workload exists for {string}")
    public void noWorkloadExistsFor(String username) {
        when(repository.findByUsername(username)).thenReturn(Optional.empty());
    }

    @Given("the request is authenticated with service role")
    public void theRequestIsAuthenticatedWithServiceRole() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "gym-crm-main",
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_SERVICE"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @When("the client requests workload for trainer {string}")
    public void theClientRequestsWorkloadForTrainer(String username) throws Exception {
        ResultActions response = mockMvc.perform(get("/workloads/{username}", username));
        testContext.setLatestResponse(response);
    }

    @When("the client requests workload for trainer {string} for year {int} and month {int}")
    public void theClientRequestsWorkloadForTrainerForYearAndMonth(String username, int year, int month) throws Exception {
        ResultActions response = mockMvc.perform(get("/workloads/{username}", username)
                .param("year", String.valueOf(year))
                .param("month", String.valueOf(month)));
        testContext.setLatestResponse(response);
    }

    @When("the client requests workload for trainer {string} without authentication")
    public void theClientRequestsWorkloadForTrainerWithoutAuthentication(String username) throws Exception {
        SecurityContextHolder.clearContext();
        ResultActions response = mockMvc.perform(get("/workloads/{username}", username));
        testContext.setLatestResponse(response);
    }

    @Then("the response status code is {int}")
    public void theResponseStatusCodeIs(int statusCode) throws Exception {
        testContext.getLatestResponse().andExpect(status().is(statusCode));
    }

    @And("the response workload username is {string}")
    public void theResponseWorkloadUsernameIs(String username) throws Exception {
        testContext.getLatestResponse().andExpect(jsonPath("$.username").value(username));
    }

    @And("the response workload status is {word}")
    public void theResponseWorkloadStatusIs(String status) throws Exception {
        testContext.getLatestResponse().andExpect(jsonPath("$.status").value(Boolean.parseBoolean(status)));
    }

    @And("the workload year is {int}")
    public void theWorkloadYearIs(int year) throws Exception {
        testContext.getLatestResponse().andExpect(jsonPath("$.years[0].year").value(year));
    }

    @And("the workload month is {int}")
    public void theWorkloadMonthIs(int month) throws Exception {
        testContext.getLatestResponse().andExpect(jsonPath("$.years[0].months[0].month").value(month));
    }

    @And("the error title is {string}")
    public void theErrorTitleIs(String errorTitle) throws Exception {
        testContext.getLatestResponse().andExpect(jsonPath("$.error").value(errorTitle));
    }

    @When("a workload event is published to Kafka with action {string}, username {string}, duration {int}")
    public void aWorkloadEventIsPublishedToKafkaWithActionUsernameDuration(String action, String username, int duration) {
        LocalDate localDate = LocalDate.of(2026, 9, 15);
        Date trainingDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        TrainerWorkloadRequestDto requestDto = TrainerWorkloadRequestDto.builder()
                .username(username)
                .firstName("Active")
                .lastName("Trainer")
                .isActive(true)
                .trainingDate(trainingDate)
                .trainingDuration(duration)
                .actionType(ActionType.valueOf(action))
                .build();

        doAnswer(invocation -> null).when(workloadService).processTrainingWorkload(any(TrainerWorkloadRequestDto.class));
        kafkaTemplate.send(workloadTopic, username, requestDto);
    }

    @Then("the workload service processes the event for {string}")
    public void theWorkloadServiceProcessesTheEventFor(String username) {
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
                verify(workloadService, atLeast(1)).processTrainingWorkload(any(TrainerWorkloadRequestDto.class))
        );
    }
}