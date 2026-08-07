package com.vaishnavi.purchase_decision_api.controllers;

import com.vaishnavi.purchase_decision_api.configs.OpenApiConfig;
import com.vaishnavi.purchase_decision_api.dtos.DecisionRequest;
import com.vaishnavi.purchase_decision_api.dtos.DecisionResponse;
import com.vaishnavi.purchase_decision_api.dtos.ErrorResponse;
import com.vaishnavi.purchase_decision_api.entity.User;
import com.vaishnavi.purchase_decision_api.exceptions.UserNotFoundException;
import com.vaishnavi.purchase_decision_api.repository.UserRepository;
import com.vaishnavi.purchase_decision_api.service.DecisionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "3. Decisions",
        description = "\"Should I buy this?\" — requires a financial profile.")
@SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
public class DecisionController {

    private final DecisionService decisionService;
    private final UserRepository userRepository;

    @Operation(
            summary = "Score a purchase and get a verdict",
            description = """
                    Scores the purchase against your disposable income and returns
                    **BUY**, **WAIT** or **SKIP**, an affordability score out of 100, and a
                    plain-English explanation. When the verdict is WAIT you also get a
                    savings plan with the shortfall, monthly pace and target date.

                    Requires `PUT /api/users/profile` to have been called first —
                    otherwise this returns `400`.

                    The verdict is computed deterministically; the LLM only writes the
                    explanation, and falls back to a rule-based sentence if it is
                    unavailable.
                    """,
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = DecisionRequest.class),
                    examples = {
                            @ExampleObject(
                                    name = "WAIT — just over budget, returns a savings plan",
                                    description = "On the demo profile (₹24,500 disposable) this "
                                            + "scores 65 and comes back with a two-month savings plan.",
                                    value = """
                                            {
                                              "itemName": "Sony WH-1000XM5 headphones",
                                              "price": 30000,
                                              "purchaseType": "WANT",
                                              "category": "Electronics",
                                              "reason": "My current pair broke and I take calls all day",
                                              "usageFrequency": "DAILY"
                                            }"""),
                            @ExampleObject(
                                    name = "BUY — affordable, and only the required fields",
                                    description = "usageFrequency defaults to MONTHLY and recurring "
                                            + "cost to 0. Scores 75 on the demo profile.",
                                    value = """
                                            {
                                              "itemName": "Standing desk",
                                              "price": 15000,
                                              "purchaseType": "NEED"
                                            }"""),
                            @ExampleObject(
                                    name = "SKIP — way out of reach, with a recurring cost",
                                    description = "A car adds fuel, insurance and EMI every month, "
                                            + "which is subtracted before scoring. Scores 10.",
                                    value = """
                                            {
                                              "itemName": "Second-hand car",
                                              "price": 450000,
                                              "purchaseType": "LUXURY",
                                              "category": "Vehicle",
                                              "usageFrequency": "WEEKLY",
                                              "monthlyRecurringCost": 9000
                                            }""")
                    })
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Decision computed and saved"),
            @ApiResponse(responseCode = "400", description = "Validation failed, or no financial "
                    + "profile set yet — call PUT /api/users/profile first",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing, expired or invalid token — click Authorize",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/decision")
    public ResponseEntity<DecisionResponse>  makeDecision(@Valid
                                                            @RequestBody DecisionRequest decisionRequest,
                                                            @Parameter(hidden = true)
                                                            Authentication authentication){


        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        DecisionResponse response = decisionService.makeDecision(user, decisionRequest);

        return ResponseEntity.status(201).body(response);

    }
}
