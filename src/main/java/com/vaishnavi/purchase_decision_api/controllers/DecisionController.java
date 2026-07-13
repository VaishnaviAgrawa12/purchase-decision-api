package com.vaishnavi.purchase_decision_api.controllers;

import com.vaishnavi.purchase_decision_api.dtos.AuthResponse;
import com.vaishnavi.purchase_decision_api.dtos.DecisionRequest;
import com.vaishnavi.purchase_decision_api.dtos.DecisionResponse;
import com.vaishnavi.purchase_decision_api.entity.User;
import com.vaishnavi.purchase_decision_api.exceptions.UserNotFoundException;
import com.vaishnavi.purchase_decision_api.repository.DecisionRepository;
import com.vaishnavi.purchase_decision_api.repository.UserRepository;
import com.vaishnavi.purchase_decision_api.service.DecisionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DecisionController {

    private final DecisionService decisionService;
    private final UserRepository userRepository;

    @PostMapping("/decision")
    public ResponseEntity<DecisionResponse>  makeDecision(@Valid
                                                            @RequestBody DecisionRequest decisionRequest,
                                                            Authentication authentication){


        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        DecisionResponse response = decisionService.makeDecision(user, decisionRequest);

        return ResponseEntity.status(201).body(response);

    }
}
