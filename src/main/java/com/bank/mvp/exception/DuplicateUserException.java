package com.bank.mvp.exception;

public class DuplicateUserException extends UserAlreadyExistsException {
    public DuplicateUserException(String message) {
        super(message);
    }
}
