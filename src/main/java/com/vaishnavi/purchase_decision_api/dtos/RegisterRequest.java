package com.vaishnavi.purchase_decision_api.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "RegisterRequest", description = "Details for creating a new account.")
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 100,message = "Name cannot exceed 100 characters")
    @Schema(description = "Display name.", example = "Vaishnavi", maxLength = 100,
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Schema(description = "Change this to your own — an email that's already registered "
            + "returns 409.",
            example = "your-name@example.com", format = "email",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).*$",
            message = "Password must contain at least one number, one uppercase letter, and one symbol"
    )
    @Schema(description = "At least 8 characters, with one uppercase letter, one number and one symbol.",
            example = "Test@1234", minLength = 8, format = "password",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;



}
