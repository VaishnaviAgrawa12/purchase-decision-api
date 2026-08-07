package com.vaishnavi.purchase_decision_api.controllers;

import com.vaishnavi.purchase_decision_api.dtos.AuthResponse;
import com.vaishnavi.purchase_decision_api.service.DemoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "1. Authentication")
@SecurityRequirements
@ConditionalOnProperty(name = "app.demo.enabled", havingValue = "true", matchIfMissing = true)
public class DemoController {

    private final DemoService demoService;

    @Operation(
            summary = "One-click demo — no signup, no request body",
            description = """
                    The fastest way to try this API. Hit **Try it out** → **Execute**
                    with an empty body and you get back a token for a fresh account that
                    **already has a financial profile set**, so you can skip straight to
                    `POST /api/decision`.

                    The profile is: income bracket `FROM_50K_TO_75K` (₹62,500), fixed
                    expenses ₹25,500, savings target 20% — leaving **₹24,500 disposable
                    income** per month. Sample purchases are picked to land on different
                    verdicts against that number.

                    Every call creates its own throwaway account, so several people can
                    test at once without overwriting each other's profile. The password is
                    `Demo@1234` if you want to log back into the same account later.
                    """
    )
    @ApiResponses(@ApiResponse(responseCode = "201",
            description = "Demo account created; token returned"))
    @PostMapping("/demo")
    public ResponseEntity<AuthResponse> demo() {
        return ResponseEntity.status(201).body(demoService.createThrowawayAccount());
    }
}
