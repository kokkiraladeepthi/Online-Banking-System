package com.bank.mvp.exception;

public class AccountNotFoundException extends ResourceNotFoundException {
    public AccountNotFoundException(String message) {
        super(message);
    }
}
