package com.vaishnavi.purchase_decision_api.exceptions;

import com.vaishnavi.purchase_decision_api.dtos.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailExists (EmailAlreadyExistsException ex){
        return ResponseEntity.status(409).body(
                new ErrorResponse(409, ex.getMessage(), LocalDateTime.now())
        );
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(404).body(
                new ErrorResponse(404, ex.getMessage(), LocalDateTime.now())
        );
    }

    // wrong password
    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPassword(InvalidPasswordException ex) {
        return ResponseEntity.status(401).body(
                new ErrorResponse(401, ex.getMessage(), LocalDateTime.now())
        );
    }

    // @Valid validation failures (blank email, short password etc)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return ResponseEntity.status(400).body(
                new ErrorResponse(400, message, LocalDateTime.now())
        );
    }

    // anything else unexpected
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return ResponseEntity.status(500).body(
                new ErrorResponse(500, "Something went wrong", LocalDateTime.now())
        );
    }
}

