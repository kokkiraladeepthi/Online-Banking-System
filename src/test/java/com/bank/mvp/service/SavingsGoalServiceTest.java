package com.bank.mvp.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.bank.mvp.dto.SavingsGoalRequest;
import com.bank.mvp.dto.SavingsGoalResponse;
import com.bank.mvp.dto.UpdateSavingsGoalRequest;
import com.bank.mvp.exception.AccountNotFoundException;
import com.bank.mvp.exception.InvalidAmountException;
import com.bank.mvp.exception.SavingsGoalNotFoundException;
import com.bank.mvp.model.Account;
import com.bank.mvp.repository.AccountRepository;
import com.bank.mvp.repository.SavingsGoalRepository;

@DataJpaTest
@Import(SavingsGoalService.class)
@ActiveProfiles("test")
class SavingsGoalServiceTest {

    @Autowired
    private SavingsGoalService savingsGoalService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private SavingsGoalRepository savingsGoalRepository;

    private Account testAccount;

    @BeforeEach
    void setUp() {
        savingsGoalRepository.deleteAll();
        accountRepository.deleteAll();
        testAccount = accountRepository.save(new Account("ACC-GOAL-01", "Alice Goal", "alice.goal@example.com", new BigDecimal("1000.00")));
    }

    @Test
    void shouldCreateSavingsGoalSuccessfully() {
        SavingsGoalRequest request = new SavingsGoalRequest(
                testAccount.getId(),
                "New Laptop",
                new BigDecimal("5000.00"),
                new BigDecimal("1000.00"),
                LocalDate.now().plusMonths(6)
        );

        SavingsGoalResponse response = savingsGoalService.createGoal(request);

        assertNotNull(response.getId());
        assertEquals("New Laptop", response.getGoalName());
        assertEquals(new BigDecimal("5000.00"), response.getTargetAmount());
        assertEquals(new BigDecimal("1000.00"), response.getCurrentAmount());
        assertEquals(20.0, response.getProgressPercentage());
        assertEquals("IN_PROGRESS", response.getStatus());
        assertEquals(testAccount.getId(), response.getAccountId());
        assertEquals("ACC-GOAL-01", response.getAccountNumber());
    }

    @Test
    void shouldAutoMarkGoalAsCompletedWhenTargetReachedOnCreation() {
        SavingsGoalRequest request = new SavingsGoalRequest(
                testAccount.getId(),
                "Emergency Fund",
                new BigDecimal("2000.00"),
                new BigDecimal("2000.00"),
                LocalDate.now().plusMonths(3)
        );

        SavingsGoalResponse response = savingsGoalService.createGoal(request);

        assertEquals("COMPLETED", response.getStatus());
        assertEquals(100.0, response.getProgressPercentage());
    }

    @Test
    void shouldCalculateProgressPercentageAccurately() {
        SavingsGoalRequest request = new SavingsGoalRequest(
                testAccount.getId(),
                "Vacation Trip",
                new BigDecimal("4000.00"),
                new BigDecimal("1500.00"),
                LocalDate.now().plusMonths(4)
        );

        SavingsGoalResponse response = savingsGoalService.createGoal(request);

        // 1500 / 4000 * 100 = 37.5%
        assertEquals(37.5, response.getProgressPercentage());
        assertEquals("IN_PROGRESS", response.getStatus());
    }

    @Test
    void shouldGetGoalById() {
        SavingsGoalRequest request = new SavingsGoalRequest(
                testAccount.getId(),
                "Car Down Payment",
                new BigDecimal("10000.00"),
                new BigDecimal("2500.00"),
                LocalDate.now().plusYears(1)
        );

        SavingsGoalResponse created = savingsGoalService.createGoal(request);
        SavingsGoalResponse fetched = savingsGoalService.getGoalById(created.getId());

        assertNotNull(fetched);
        assertEquals(created.getId(), fetched.getId());
        assertEquals("Car Down Payment", fetched.getGoalName());
    }

    @Test
    void shouldThrowWhenGoalNotFound() {
        assertThrows(SavingsGoalNotFoundException.class, () -> savingsGoalService.getGoalById(99999L));
    }

    @Test
    void shouldGetGoalsByAccountId() {
        savingsGoalService.createGoal(new SavingsGoalRequest(
                testAccount.getId(), "Goal 1", new BigDecimal("1000.00"), BigDecimal.ZERO, null));
        savingsGoalService.createGoal(new SavingsGoalRequest(
                testAccount.getId(), "Goal 2", new BigDecimal("2000.00"), new BigDecimal("500.00"), null));

        List<SavingsGoalResponse> goals = savingsGoalService.getGoalsByAccountId(testAccount.getId());

        assertEquals(2, goals.size());
    }

    @Test
    void shouldThrowWhenAccountNotFoundForGoals() {
        assertThrows(AccountNotFoundException.class, () -> savingsGoalService.getGoalsByAccountId(99999L));
    }

    @Test
    void shouldUpdateSavingsGoalAndAutoMarkCompleted() {
        SavingsGoalResponse created = savingsGoalService.createGoal(new SavingsGoalRequest(
                testAccount.getId(), "Bike", new BigDecimal("3000.00"), new BigDecimal("1000.00"), null));

        assertEquals("IN_PROGRESS", created.getStatus());

        UpdateSavingsGoalRequest updateReq = new UpdateSavingsGoalRequest();
        updateReq.setCurrentAmount(new BigDecimal("3000.00"));

        SavingsGoalResponse updated = savingsGoalService.updateGoal(created.getId(), updateReq);

        assertEquals("COMPLETED", updated.getStatus());
        assertEquals(100.0, updated.getProgressPercentage());
    }

    @Test
    void shouldDeleteSavingsGoal() {
        SavingsGoalResponse created = savingsGoalService.createGoal(new SavingsGoalRequest(
                testAccount.getId(), "To Delete", new BigDecimal("500.00"), BigDecimal.ZERO, null));

        savingsGoalService.deleteGoal(created.getId());

        assertFalse(savingsGoalRepository.existsById(created.getId()));
        assertThrows(SavingsGoalNotFoundException.class, () -> savingsGoalService.getGoalById(created.getId()));
    }

    @Test
    void shouldRejectInvalidAmounts() {
        SavingsGoalRequest zeroTarget = new SavingsGoalRequest(
                testAccount.getId(), "Invalid", BigDecimal.ZERO, BigDecimal.ZERO, null);
        assertThrows(InvalidAmountException.class, () -> savingsGoalService.createGoal(zeroTarget));

        SavingsGoalRequest negTarget = new SavingsGoalRequest(
                testAccount.getId(), "Invalid", new BigDecimal("-100.00"), BigDecimal.ZERO, null);
        assertThrows(InvalidAmountException.class, () -> savingsGoalService.createGoal(negTarget));

        SavingsGoalRequest negCurrent = new SavingsGoalRequest(
                testAccount.getId(), "Invalid", new BigDecimal("100.00"), new BigDecimal("-10.00"), null);
        assertThrows(InvalidAmountException.class, () -> savingsGoalService.createGoal(negCurrent));
    }
}

