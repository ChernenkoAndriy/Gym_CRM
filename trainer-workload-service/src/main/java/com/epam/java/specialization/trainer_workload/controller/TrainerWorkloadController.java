package com.epam.java.specialization.trainer_workload.controller;

import com.epam.java.specialization.common.dto.*;
import com.epam.java.specialization.trainer_workload.service.interfaces.TrainerWorkloadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/workloads")
@RequiredArgsConstructor
@Tag(name = "Trainer Workload API", description = "Endpoints for managing and calculating trainer workload")
public class TrainerWorkloadController {

    private final TrainerWorkloadService workloadService;

    @PostMapping
    @Operation(summary = "Accept trainer workload action (ADD / DELETE training duration)")
    public ResponseEntity<Void> processWorkload(@Valid @RequestBody TrainerWorkloadRequestDto request) {
        workloadService.processTrainingWorkload(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{username}")
    @Operation(summary = "Get trainer monthly workload summary (optionally filtered by year and month)")
    public ResponseEntity<TrainerWorkloadResponseDto> getTrainerWorkload(
            @PathVariable String username,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(workloadService.getTrainerWorkload(username, year, month));
    }
}