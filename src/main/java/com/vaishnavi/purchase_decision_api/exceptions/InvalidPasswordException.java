package com.vaishnavi.purchase_decision_api.exceptions;

public class InvalidPasswordException extends RuntimeException{
    public InvalidPasswordException(String message) {
        super(message);
    }
}
