package com.epam.java.specialization.hello_docker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class HelloDockerApplication {

    private static final Logger log = LoggerFactory.getLogger(HelloDockerApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(HelloDockerApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        log.info("Hello from Docker! hello-docker started without any integrations");
    }

    @GetMapping("/hello")
    public String hello() {
        log.info("GET /hello called");
        return "Hello from Docker!";
    }
}
