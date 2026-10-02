package com.bank.mvp.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bank.mvp.dto.SavingsGoalRequest;
import com.bank.mvp.dto.SavingsGoalResponse;
import com.bank.mvp.dto.UpdateSavingsGoalRequest;
import com.bank.mvp.exception.AccountNotFoundException;
import com.bank.mvp.exception.InvalidAmountException;
import com.bank.mvp.exception.SavingsGoalNotFoundException;
import com.bank.mvp.model.Account;
import com.bank.mvp.model.SavingsGoal;
import com.bank.mvp.repository.AccountRepository;
import com.bank.mvp.repository.SavingsGoalRepository;

@Service
public class SavingsGoalService {

    private final SavingsGoalRepository savingsGoalRepository;
    private final AccountRepository accountRepository;

    public SavingsGoalService(SavingsGoalRepository savingsGoalRepository, AccountRepository accountRepository) {
        this.savingsGoalRepository = savingsGoalRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional
    public SavingsGoalResponse createGoal(SavingsGoalRequest request) {
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new AccountNotFoundException("Account not found with ID: " + request.getAccountId()));

        if (request.getTargetAmount() == null || request.getTargetAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Target amount must be greater than 0");
        }

        BigDecimal currentAmount = request.getCurrentAmount() != null ? request.getCurrentAmount() : BigDecimal.ZERO;
        if (currentAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidAmountException("Current amount cannot be negative");
        }

        SavingsGoal goal = new SavingsGoal(
                account,
                request.getGoalName().trim(),
                request.getTargetAmount(),
                currentAmount,
                request.getTargetDate()
        );

        SavingsGoal saved = savingsGoalRepository.save(goal);
        return SavingsGoalResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public SavingsGoalResponse getGoalById(Long id) {
        SavingsGoal goal = savingsGoalRepository.findById(id)
                .orElseThrow(() -> new SavingsGoalNotFoundException("Savings goal not found with ID: " + id));
        return SavingsGoalResponse.fromEntity(goal);
    }

    @Transactional(readOnly = true)
    public List<SavingsGoalResponse> getGoalsByAccountId(Long accountId) {
        if (!accountRepository.existsById(accountId)) {
            throw new AccountNotFoundException("Account not found with ID: " + accountId);
        }

        return savingsGoalRepository.findByAccountIdOrderByCreatedAtDesc(accountId)
                .stream()
                .map(SavingsGoalResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public SavingsGoalResponse updateGoal(Long id, UpdateSavingsGoalRequest request) {
        SavingsGoal goal = savingsGoalRepository.findById(id)
                .orElseThrow(() -> new SavingsGoalNotFoundException("Savings goal not found with ID: " + id));

        if (request.getGoalName() != null && !request.getGoalName().trim().isEmpty()) {
            goal.setGoalName(request.getGoalName().trim());
        }

        if (request.getTargetAmount() != null) {
            if (request.getTargetAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new InvalidAmountException("Target amount must be greater than 0");
            }
            goal.setTargetAmount(request.getTargetAmount());
        }

        if (request.getCurrentAmount() != null) {
            if (request.getCurrentAmount().compareTo(BigDecimal.ZERO) < 0) {
                throw new InvalidAmountException("Current amount cannot be negative");
            }
            goal.setCurrentAmount(request.getCurrentAmount());
        }

        if (request.getTargetDate() != null) {
            goal.setTargetDate(request.getTargetDate());
        }

        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            goal.setStatus(request.getStatus().trim().toUpperCase());
        }

        // Automatic completion check
        goal.updateStatus();

        SavingsGoal updated = savingsGoalRepository.save(goal);
        return SavingsGoalResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteGoal(Long id) {
        SavingsGoal goal = savingsGoalRepository.findById(id)
                .orElseThrow(() -> new SavingsGoalNotFoundException("Savings goal not found with ID: " + id));
        savingsGoalRepository.delete(goal);
    }
}

