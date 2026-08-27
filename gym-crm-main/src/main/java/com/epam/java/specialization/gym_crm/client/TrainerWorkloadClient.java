package com.epam.java.specialization.gym_crm.client;

import com.epam.java.specialization.common.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "trainer-workload-service", fallbackFactory = TrainerWorkloadFallbackFactory.class)
public interface TrainerWorkloadClient {

    @PostMapping("/workloads")
    ResponseEntity<Void> processWorkload(@RequestBody TrainerWorkloadRequestDto request);

    @GetMapping("/workloads/{username}")
    ResponseEntity<TrainerWorkloadResponseDto> getTrainerWorkload(
            @PathVariable("username") String username,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "month", required = false) Integer month
    );
}