package com.vaishnavi.purchase_decision_api.controllers;


import com.vaishnavi.purchase_decision_api.dtos.AuthResponse;
import com.vaishnavi.purchase_decision_api.dtos.LoginRequest;
import com.vaishnavi.purchase_decision_api.dtos.RegisterRequest;
import com.vaishnavi.purchase_decision_api.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;



    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register (@Valid @RequestBody RegisterRequest registerRequest){

    AuthResponse response = authService. register(registerRequest);

    return  ResponseEntity.status(201).body(response);


    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login (@Valid @RequestBody LoginRequest loginRequest){

        AuthResponse response = authService.login(loginRequest);

        return  ResponseEntity.ok().body(response);

    }


}
