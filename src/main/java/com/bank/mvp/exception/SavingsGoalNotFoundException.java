package com.bank.mvp.exception;

public class SavingsGoalNotFoundException extends ResourceNotFoundException {
    public SavingsGoalNotFoundException(String message) {
        super(message);
    }
}
