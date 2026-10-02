package com.bank.mvp.exception;

public class InvalidAccountException extends InvalidAmountException {
    public InvalidAccountException(String message) {
        super(message);
    }
}
