package com.epam.java.specialization.trainer_workload.cucumber;

import com.epam.java.specialization.trainer_workload.repository.TrainerWorkloadRepository;
import com.epam.java.specialization.trainer_workload.service.interfaces.TrainerWorkloadService;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = {
        MongoAutoConfiguration.class,
        MongoDataAutoConfiguration.class,
        MongoRepositoriesAutoConfiguration.class
})
@EmbeddedKafka(
        partitions = 1,
        topics = {
                "${app.kafka.topics.trainer-workload:trainer-workload-topic-test}",
                "${app.kafka.topics.trainer-workload-dlt:trainer-workload-topic-test.DLT}"
        },
        brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"}
)
public class CucumberSpringConfiguration {

    @MockBean
    protected TrainerWorkloadRepository repository;

    @SpyBean
    protected TrainerWorkloadService workloadService;
}