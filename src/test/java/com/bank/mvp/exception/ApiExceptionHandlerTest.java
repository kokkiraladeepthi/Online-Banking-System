package com.bank.mvp.exception;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;

class ApiExceptionHandlerTest {

    private ApiExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ApiExceptionHandler();
    }

    @Test
    void shouldHandleUserNotFoundException() {
        UserNotFoundException ex = new UserNotFoundException("User not found with id: 42");
        ResponseEntity<Map<String, String>> response = handler.handleUserNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("User not found with id: 42", response.getBody().get("message"));
    }

    @Test
    void shouldHandleAccountNotFoundException() {
        AccountNotFoundException ex = new AccountNotFoundException("Account not found with id: 101");
        ResponseEntity<Map<String, String>> response = handler.handleAccountNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Account not found with id: 101", response.getBody().get("message"));
    }

    @Test
    void shouldHandleSavingsGoalNotFoundException() {
        SavingsGoalNotFoundException ex = new SavingsGoalNotFoundException("Savings goal not found with id: 5");
        ResponseEntity<Map<String, String>> response = handler.handleSavingsGoalNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Savings goal not found with id: 5", response.getBody().get("message"));
    }

    @Test
    void shouldHandleInvalidAccountException() {
        InvalidAccountException ex = new InvalidAccountException("Cannot transfer money to the same account");
        ResponseEntity<Map<String, String>> response = handler.handleInvalidAccount(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Cannot transfer money to the same account", response.getBody().get("message"));
    }

    @Test
    void shouldHandleDuplicateUserException() {
        DuplicateUserException ex = new DuplicateUserException("User with email 'test@example.com' already exists");
        ResponseEntity<Map<String, String>> response = handler.handleDuplicateUser(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("User with email 'test@example.com' already exists", response.getBody().get("message"));
    }

    @Test
    void shouldHandleInsufficientBalanceException() {
        InsufficientBalanceException ex = new InsufficientBalanceException("Insufficient balance for fund transfer");
        ResponseEntity<Map<String, String>> response = handler.handleInsufficientBalance(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Insufficient balance for fund transfer", response.getBody().get("message"));
    }

    @Test
    void shouldHandleInvalidAmountException() {
        InvalidAmountException ex = new InvalidAmountException("Amount must be greater than zero");
        ResponseEntity<Map<String, String>> response = handler.handleInvalidAmount(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Amount must be greater than zero", response.getBody().get("message"));
    }

    @Test
    void shouldHandleInvalidCredentialsException() {
        InvalidCredentialsException ex = new InvalidCredentialsException("Invalid email or password");
        ResponseEntity<Map<String, String>> response = handler.handleInvalidCredentials(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid email or password", response.getBody().get("message"));
    }

    @Test
    void shouldHandleHttpMessageNotReadableException() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("Invalid JSON");
        ResponseEntity<Map<String, String>> response = handler.handleHttpMessageNotReadable(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Malformed JSON request body", response.getBody().get("message"));
    }
}
