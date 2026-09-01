package com.epam.java.specialization.trainer_workload.controller;

import com.epam.java.specialization.common.dto.TrainerWorkloadResponseDto;
import com.epam.java.specialization.trainer_workload.service.interfaces.TrainerWorkloadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/workloads")
@RequiredArgsConstructor
@Tag(name = "Trainer Workload API", description = "Endpoints for managing and calculating trainer workload")
public class TrainerWorkloadController {

    private final TrainerWorkloadService workloadService;

    @GetMapping("/{username}")
    @Operation(summary = "Get trainer monthly workload summary (optionally filtered by year and month)")
    public ResponseEntity<TrainerWorkloadResponseDto> getTrainerWorkload(
            @PathVariable String username,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        log.info("Fetching workload summary via REST for trainer: {} (Year: {}, Month: {})", username, year, month);
        return ResponseEntity.ok(workloadService.getTrainerWorkload(username, year, month));
    }
}