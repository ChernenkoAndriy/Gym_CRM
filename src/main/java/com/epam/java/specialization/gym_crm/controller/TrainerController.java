package com.epam.java.specialization.gym_crm.controller;

import com.epam.java.specialization.gym_crm.dto.*;
import com.epam.java.specialization.gym_crm.service.interfaces.TrainerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/trainers")
@RequiredArgsConstructor
@Tag(name = "Trainer Management API")
public class TrainerController {

    private final TrainerService trainerService;


    @PostMapping
    @Operation(summary = "Trainer Registration")
    public ResponseEntity<RegistrationResponseDto> registerTrainer(@Valid @RequestBody TrainerRegisterRequestDto request) {
        return ResponseEntity.ok(trainerService.register(request));
    }


    @GetMapping("/{username}")
    @Operation(summary = "Get Trainer Profile")
    public ResponseEntity<TrainerProfileResponseDto> getTrainerProfile(@PathVariable String username) {
        return ResponseEntity.ok(trainerService.getProfile(username));
    }


    @PutMapping("/{username}")
    @Operation(summary = "Update Trainer Profile")
    public ResponseEntity<TrainerUpdateResponseDto> updateTrainerProfile(
            @PathVariable String username,
            @Valid @RequestBody TrainerUpdateRequestDto request) {
        return ResponseEntity.ok(trainerService.updateProfile(username, request));
    }


    @PatchMapping("/{username}/activation")
    @Operation(summary = "Activate/De-Activate Trainer Profile")
    public ResponseEntity<Void> toggleTrainerActivation(
            @PathVariable String username,
            @Valid @RequestBody ActivationRequestDto request) {
        trainerService.toggleActivation(username, request);
        return ResponseEntity.ok().build();
    }
}