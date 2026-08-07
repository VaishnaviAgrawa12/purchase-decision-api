package com.vaishnavi.purchase_decision_api.exceptions;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.vaishnavi.purchase_decision_api.dtos.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
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
                // sorted so the same bad request always produces the same message —
                // otherwise the field order shifts between calls
                .sorted(Comparator.comparing(FieldError::getField))
                .map(error -> error.getField().startsWith("check")
                        // a cross-field check (see FinancialProfileRequest); its
                        // "field" is a derived getter, not something the caller sent
                        ? error.getDefaultMessage()
                        : error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return ResponseEntity.status(400).body(
                new ErrorResponse(400, message, LocalDateTime.now())
        );
    }


    @ExceptionHandler(ProfileNotSetException.class)
    public ResponseEntity<ErrorResponse> handleProfileNotSet(ProfileNotSetException ex) {
        return ResponseEntity.status(400).body(
                new ErrorResponse(400, ex.getMessage(), LocalDateTime.now())
        );
    }



    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex) {

        String message = "Request body is missing or malformed";

        if (ex.getCause() instanceof InvalidFormatException ife && ife.getTargetType().isEnum()) {
            String field = ife.getPath().get(ife.getPath().size() - 1).getFieldName();
            message = "Invalid value for " + field + ". Allowed values: "
                    + Arrays.toString(ife.getTargetType().getEnumConstants());
        }

        return ResponseEntity.status(400).body(
                new ErrorResponse(400, message, LocalDateTime.now())
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        String message = ex.getMethod() + " is not supported for this endpoint. Supported: "
                + ex.getSupportedHttpMethods();
        return ResponseEntity.status(405).body(
                new ErrorResponse(405, message, LocalDateTime.now())
        );
    }


    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException ex) {
        return ResponseEntity.status(404).body(
                new ErrorResponse(404, "Endpoint not found", LocalDateTime.now())
        );
    }


    // anything else unexpected
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        ex.printStackTrace();
        return ResponseEntity.status(500).body(
                new ErrorResponse(500, "Something went wrong", LocalDateTime.now())
        );
    }
}

