package com.vaishnavi.purchase_decision_api.controllers;


import com.vaishnavi.purchase_decision_api.dtos.AuthResponse;
import com.vaishnavi.purchase_decision_api.dtos.ErrorResponse;
import com.vaishnavi.purchase_decision_api.dtos.LoginRequest;
import com.vaishnavi.purchase_decision_api.dtos.RegisterRequest;
import com.vaishnavi.purchase_decision_api.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "1. Authentication",
        description = "Start here. These endpoints are public and each returns the JWT you "
                + "paste into the Authorize button. In a hurry? Use /api/auth/demo.")
// Overrides the global bearer requirement — these two endpoints take no token.
@SecurityRequirements
public class AuthController {

    private final AuthService authService;



    @Operation(
            summary = "Register and get a token",
            description = "Creates an account and returns a JWT valid for 24 hours. "
                    + "Copy the `token` from the response, click **Authorize** at the top of "
                    + "this page and paste it (no `Bearer ` prefix).\n\n"
                    + "**Change the example email to your own** — it has to be unique. "
                    + "Just kicking the tyres? `POST /api/auth/demo` skips all of this and "
                    + "hands you an account with a financial profile already set."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Account created; token returned"),
            @ApiResponse(responseCode = "400", description = "Validation failed — e.g. weak password or malformed email",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Email already registered — use /api/auth/login instead",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register (@Valid @RequestBody RegisterRequest registerRequest){

    AuthResponse response = authService. register(registerRequest);

    return  ResponseEntity.status(201).body(response);


    }

    @Operation(
            summary = "Log in and get a token",
            description = "Authenticates an existing account and returns a fresh JWT valid "
                    + "for 24 hours.\n\n"
                    + "The example body is the shared demo login "
                    + "(`demo@purchasedecision.app` / `Demo@1234`), which already has a "
                    + "financial profile — Execute it as-is to get a usable token."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authenticated; token returned"),
            @ApiResponse(responseCode = "400", description = "Validation failed — email or password missing",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Incorrect password",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No account with that email",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login (@Valid @RequestBody LoginRequest loginRequest){

        AuthResponse response = authService.login(loginRequest);

        return  ResponseEntity.ok().body(response);

    }


}
