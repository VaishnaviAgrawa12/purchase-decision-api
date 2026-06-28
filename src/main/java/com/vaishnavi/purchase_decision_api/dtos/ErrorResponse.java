package com.vaishnavi.purchase_decision_api.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ErrorResponse {

    private int status;
    private String message;
    private LocalDateTime timestamp;
}
