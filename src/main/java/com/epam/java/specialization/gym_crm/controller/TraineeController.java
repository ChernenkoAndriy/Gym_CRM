package com.epam.java.specialization.gym_crm.controller;

import com.epam.java.specialization.gym_crm.dto.*;
import com.epam.java.specialization.gym_crm.service.interfaces.TraineeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/trainees")
@RequiredArgsConstructor
@Tag(name = "Trainee Management API")
public class TraineeController {

    private final TraineeService traineeService;


    @PostMapping
    @Operation(summary = "Trainee Registration")
    public ResponseEntity<RegistrationResponseDto> registerTrainee(@Valid @RequestBody TraineeRegisterRequestDto request) {
        return ResponseEntity.ok(traineeService.register(request));
    }


    @GetMapping("/{username}")
    @Operation(summary = "Get Trainee Profile")
    public ResponseEntity<TraineeProfileResponseDto> getTraineeProfile(@PathVariable String username) {
        return ResponseEntity.ok(traineeService.getProfile(username));
    }


    @PutMapping("/{username}")
    @Operation(summary = "Update Trainee Profile")
    public ResponseEntity<TraineeUpdateResponseDto> updateTraineeProfile(
            @PathVariable String username,
            @Valid @RequestBody TraineeUpdateRequestDto request) {
        return ResponseEntity.ok(traineeService.updateProfile(username, request));
    }


    @DeleteMapping("/{username}")
    @Operation(summary = "Delete Trainee Profile")
    public ResponseEntity<Void> deleteTraineeProfile(@PathVariable String username) {
        traineeService.deleteProfile(username);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/{username}/unassigned-trainers")
    @Operation(summary = "Get active trainers not assigned on trainee")
    public ResponseEntity<List<TrainerShortResponseDto>> getUnassignedActiveTrainers(@PathVariable String username) {
        return ResponseEntity.ok(traineeService.getUnassignedActiveTrainers(username));
    }


    @PutMapping("/{username}/trainers")
    @Operation(summary = "Update Trainee's Trainer List")
    public ResponseEntity<List<TrainerShortResponseDto>> updateTraineesTrainersList(
            @PathVariable String username,
            @Valid @RequestBody List<TrainerUsernameRequestDto> request) {
        return ResponseEntity.ok(traineeService.updateTrainersList(username, request));
    }


    @PatchMapping("/{username}/activation")
    @Operation(summary = "Activate/De-Activate Trainee Profile")
    public ResponseEntity<Void> toggleTraineeActivation(
            @PathVariable String username,
            @Valid @RequestBody ActivationRequestDto request) {
        traineeService.toggleActivation(username, request);
        return ResponseEntity.ok().build();
    }
}