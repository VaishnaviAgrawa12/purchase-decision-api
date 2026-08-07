package com.vaishnavi.purchase_decision_api.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "AuthResponse",
        description = "The authenticated user plus the JWT to paste into the Authorize dialog.")
public class AuthResponse {

    @Schema(description = "Display name.", example = "Vaishnavi")
    private String name;

    @Schema(description = "Account email.", example = "tester@example.com")
    private String email;

    @Schema(description = "JWT, valid for 24 hours. Paste this into Authorize — without \"Bearer \".",
            example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0ZXJAZXhhbXBsZS5jb20ifQ.signature")
    private String token;

}
