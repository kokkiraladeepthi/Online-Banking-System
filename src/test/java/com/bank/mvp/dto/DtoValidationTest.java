package com.bank.mvp.dto;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class DtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldValidateRegisterUserRequest() {
        RegisterUserRequest valid = new RegisterUserRequest("John Doe", "john@example.com", "password123", "9876543210");
        assertTrue(validator.validate(valid).isEmpty());

        RegisterUserRequest blankName = new RegisterUserRequest("", "john@example.com", "password123", null);
        assertFalse(validator.validate(blankName).isEmpty());

        RegisterUserRequest invalidEmail = new RegisterUserRequest("John", "not-an-email", "password123", null);
        assertFalse(validator.validate(invalidEmail).isEmpty());

        RegisterUserRequest shortPassword = new RegisterUserRequest("John", "john@example.com", "123", null);
        assertFalse(validator.validate(shortPassword).isEmpty());
    }

    @Test
    void shouldValidateCreateAccountRequest() {
        CreateAccountRequest valid = new CreateAccountRequest();
        valid.setName("Alice");
        valid.setEmail("alice@example.com");
        valid.setInitialBalance(new BigDecimal("100.00"));
        assertTrue(validator.validate(valid).isEmpty());

        CreateAccountRequest blankEmail = new CreateAccountRequest();
        blankEmail.setName("Alice");
        blankEmail.setEmail("");
        blankEmail.setInitialBalance(new BigDecimal("100.00"));
        assertFalse(validator.validate(blankEmail).isEmpty());

        CreateAccountRequest negativeBalance = new CreateAccountRequest();
        negativeBalance.setName("Alice");
        negativeBalance.setEmail("alice@example.com");
        negativeBalance.setInitialBalance(new BigDecimal("-50.00"));
        assertFalse(validator.validate(negativeBalance).isEmpty());
    }

    @Test
    void shouldValidateMoneyRequest() {
        MoneyRequest valid = new MoneyRequest();
        valid.setAmount(new BigDecimal("50.00"));
        assertTrue(validator.validate(valid).isEmpty());

        MoneyRequest nullAmount = new MoneyRequest();
        assertFalse(validator.validate(nullAmount).isEmpty());

        MoneyRequest zeroAmount = new MoneyRequest();
        zeroAmount.setAmount(BigDecimal.ZERO);
        assertFalse(validator.validate(zeroAmount).isEmpty());

        MoneyRequest negativeAmount = new MoneyRequest();
        negativeAmount.setAmount(new BigDecimal("-10.00"));
        assertFalse(validator.validate(negativeAmount).isEmpty());
    }

    @Test
    void shouldValidateTransferRequest() {
        TransferRequest valid = new TransferRequest(1L, 2L, new BigDecimal("100.00"), "Payment");
        assertTrue(validator.validate(valid).isEmpty());

        TransferRequest nullAmount = new TransferRequest();
        nullAmount.setFromAccountId(1L);
        nullAmount.setToAccountId(2L);
        assertFalse(validator.validate(nullAmount).isEmpty());

        TransferRequest zeroAmount = new TransferRequest(1L, 2L, BigDecimal.ZERO, null);
        assertFalse(validator.validate(zeroAmount).isEmpty());

        TransferRequest negAmount = new TransferRequest(1L, 2L, new BigDecimal("-25.00"), null);
        assertFalse(validator.validate(negAmount).isEmpty());
    }

    @Test
    void shouldValidateSavingsGoalRequest() {
        SavingsGoalRequest valid = new SavingsGoalRequest(1L, "New Car", new BigDecimal("5000.00"), BigDecimal.ZERO, null);
        assertTrue(validator.validate(valid).isEmpty());

        SavingsGoalRequest missingAccount = new SavingsGoalRequest(null, "Vacation", new BigDecimal("1000.00"), BigDecimal.ZERO, null);
        assertFalse(validator.validate(missingAccount).isEmpty());

        SavingsGoalRequest blankName = new SavingsGoalRequest(1L, "  ", new BigDecimal("1000.00"), BigDecimal.ZERO, null);
        assertFalse(validator.validate(blankName).isEmpty());

        SavingsGoalRequest invalidTarget = new SavingsGoalRequest(1L, "Emergency Fund", BigDecimal.ZERO, BigDecimal.ZERO, null);
        assertFalse(validator.validate(invalidTarget).isEmpty());

        SavingsGoalRequest negativeCurrent = new SavingsGoalRequest(1L, "Emergency Fund", new BigDecimal("1000.00"), new BigDecimal("-10.00"), null);
        assertFalse(validator.validate(negativeCurrent).isEmpty());
    }
}
