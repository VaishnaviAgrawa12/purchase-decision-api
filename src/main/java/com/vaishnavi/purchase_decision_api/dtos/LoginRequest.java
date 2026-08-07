package com.vaishnavi.purchase_decision_api.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "LoginRequest", description = "Credentials for an existing account.")
public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Schema(description = "The email you registered with. Defaults to the shared demo "
            + "account, which works as-is.",
            example = "demo@purchasedecision.app",
            format = "email", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "Password is required")
    @Schema(description = "Your password.", example = "Demo@1234", format = "password",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}
