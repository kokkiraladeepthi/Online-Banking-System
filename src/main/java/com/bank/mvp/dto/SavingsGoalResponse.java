package com.bank.mvp.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.bank.mvp.model.SavingsGoal;

public class SavingsGoalResponse {

    private Long id;
    private Long accountId;
    private String accountNumber;
    private String goalName;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private LocalDate targetDate;
    private String status;
    private double progressPercentage;
    private LocalDateTime createdAt;

    public SavingsGoalResponse() {
    }

    public SavingsGoalResponse(Long id, Long accountId, String accountNumber, String goalName,
                               BigDecimal targetAmount, BigDecimal currentAmount, LocalDate targetDate,
                               String status, double progressPercentage, LocalDateTime createdAt) {
        this.id = id;
        this.accountId = accountId;
        this.accountNumber = accountNumber;
        this.goalName = goalName;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
        this.targetDate = targetDate;
        this.status = status;
        this.progressPercentage = progressPercentage;
        this.createdAt = createdAt;
    }

    public static SavingsGoalResponse fromEntity(SavingsGoal goal) {
        return new SavingsGoalResponse(
                goal.getId(),
                goal.getAccount() != null ? goal.getAccount().getId() : null,
                goal.getAccount() != null ? goal.getAccount().getAccountNumber() : null,
                goal.getGoalName(),
                goal.getTargetAmount(),
                goal.getCurrentAmount(),
                goal.getTargetDate(),
                goal.getStatus(),
                goal.calculateProgressPercentage(),
                goal.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(double progressPercentage) {
        this.progressPercentage = progressPercentage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

