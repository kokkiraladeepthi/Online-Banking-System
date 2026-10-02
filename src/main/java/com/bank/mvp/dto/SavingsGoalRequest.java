package com.bank.mvp.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class SavingsGoalRequest {

    @NotNull(message = "Account ID is required")
    private Long accountId;

    @NotBlank(message = "Goal name is required")
    @Size(max = 100, message = "Goal name must not exceed 100 characters")
    private String goalName;

    @NotNull(message = "Target amount is required")
    @DecimalMin(value = "1.00", message = "Target amount must be at least 1.00")
    private BigDecimal targetAmount;

    @DecimalMin(value = "0.00", message = "Current amount cannot be negative")
    private BigDecimal currentAmount;

    private LocalDate targetDate;

    public SavingsGoalRequest() {
    }

    public SavingsGoalRequest(Long accountId, String goalName, BigDecimal targetAmount, BigDecimal currentAmount, LocalDate targetDate) {
        this.accountId = accountId;
        this.goalName = goalName;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
        this.targetDate = targetDate;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getGoalName() {
        return goalName;
    }

    public void setGoalName(String goalName) {
        this.goalName = goalName;
    }

    public BigDecimal getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(BigDecimal targetAmount) {
        this.targetAmount = targetAmount;
    }

    public BigDecimal getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(BigDecimal currentAmount) {
        this.currentAmount = currentAmount;
    }

    public LocalDate getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(LocalDate targetDate) {
        this.targetDate = targetDate;
    }
}
