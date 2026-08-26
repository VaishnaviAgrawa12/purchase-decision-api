package com.vaishnavi.purchase_decision_api.controllers;


import com.vaishnavi.purchase_decision_api.configs.OpenApiConfig;
import com.vaishnavi.purchase_decision_api.dtos.ErrorResponse;
import com.vaishnavi.purchase_decision_api.dtos.FinancialProfileRequest;
import com.vaishnavi.purchase_decision_api.dtos.UserProfileResponse;
import com.vaishnavi.purchase_decision_api.entity.User;
import com.vaishnavi.purchase_decision_api.exceptions.ProfileNotSetException;
import com.vaishnavi.purchase_decision_api.exceptions.UserNotFoundException;
import com.vaishnavi.purchase_decision_api.repository.UserRepository;
import com.vaishnavi.purchase_decision_api.service.UserService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "2. Financial profile",
        description = "Set this before asking for a decision — the score is computed from it.")
@SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    @Operation(
            summary = "Read your saved financial profile",
            description = """
                    Returns the same shape as `PUT /profile`. Responds `400` when no
                    profile has been set yet, which is how a client can tell whether to
                    send the user through profile setup before asking for a decision.
                    """,
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The saved profile"),
            @ApiResponse(responseCode = "400", description = "No profile set yet",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing, expired or invalid token",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile(
            @Parameter(hidden = true) Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        if (user.getMonthlyIncome() == null || user.getSavingsTarget() == null) {
            throw new ProfileNotSetException("No financial profile set yet");
        }
        return ResponseEntity.ok(UserProfileResponse.from(user));
    }

    @Operation(
            summary = "Create or replace your financial profile",
            description = """
                    Stores your income bracket, savings target and fixed expenses, and
                    returns the derived monthly figures — including the **disposable
                    income** every decision is scored against.

                    Calling this again fully replaces the previous profile, including the
                    fixed-expense list. The account comes from your token, so there is no
                    user id in the request.
                    """,
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = FinancialProfileRequest.class),
                    examples = {
                            @ExampleObject(
                                    name = "Itemised expenses, savings as a percentage",
                                    description = "Most common shape: list your bills, save 20% of income.",
                                    value = """
                                            {
                                              "incomeBracket": "FROM_50K_TO_75K",
                                              "savingsInputMode": "PERCENTAGE",
                                              "savingsValue": 20,
                                              "fixedInputMode": "AMOUNT",
                                              "fixedExpenses": [
                                                { "name": "Rent", "amount": 18000 },
                                                { "name": "Phone and internet", "amount": 1500 }
                                              ]
                                            }"""),
                            @ExampleObject(
                                    name = "Everything as percentages",
                                    description = "Quickest to fill in — no line items needed.",
                                    value = """
                                            {
                                              "incomeBracket": "FROM_75K_TO_1L",
                                              "savingsInputMode": "PERCENTAGE",
                                              "savingsValue": 25,
                                              "fixedInputMode": "PERCENTAGE",
                                              "fixedPercentageValue": 40
                                            }"""),
                            @ExampleObject(
                                    name = "Everything as rupee amounts",
                                    value = """
                                            {
                                              "incomeBracket": "FROM_25K_TO_50K",
                                              "savingsInputMode": "AMOUNT",
                                              "savingsValue": 5000,
                                              "fixedInputMode": "AMOUNT",
                                              "fixedExpenses": [
                                                { "name": "Rent", "amount": 12000 }
                                              ]
                                            }""")
                    })
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile saved"),
            @ApiResponse(responseCode = "400", description = "Validation failed — e.g. missing income bracket, "
                    + "or a percentage mode with no value",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing, expired or invalid token — click Authorize",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> setProfile(
            @Valid @RequestBody FinancialProfileRequest financialProfileRequest,
            @Parameter(hidden = true) Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        User updated =  userService.setFinancialProfile(user, financialProfileRequest);

        return ResponseEntity.ok(UserProfileResponse.from(updated));


    }

}
