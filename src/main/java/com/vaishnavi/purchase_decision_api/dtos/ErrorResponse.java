package com.vaishnavi.purchase_decision_api.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Schema(name = "ErrorResponse", description = "The shape every error in this API returns.")
public class ErrorResponse {

    @Schema(description = "HTTP status code, repeated in the body.", example = "400")
    private int status;

    @Schema(description = "What went wrong. Validation failures list every offending field.",
            example = "price: Price must be greater than zero")
    private String message;

    @Schema(description = "Server time the error was produced.", example = "2026-08-07T14:32:10.123")
    private LocalDateTime timestamp;
}
