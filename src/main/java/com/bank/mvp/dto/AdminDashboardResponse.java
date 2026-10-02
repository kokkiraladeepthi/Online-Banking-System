package com.bank.mvp.dto;

import java.math.BigDecimal;

public class AdminDashboardResponse {

    private long totalUsers;
    private long totalAccounts;
    private long totalTransactions;
    private BigDecimal totalDeposits;
    private BigDecimal totalWithdrawals;
    private BigDecimal totalTransfers;
    private BigDecimal totalSystemBalance;
    private long totalSavingsGoals;

    public AdminDashboardResponse() {
    }

    public AdminDashboardResponse(long totalUsers, long totalAccounts, long totalTransactions,
                                  BigDecimal totalDeposits, BigDecimal totalWithdrawals,
                                  BigDecimal totalTransfers, BigDecimal totalSystemBalance,
                                  long totalSavingsGoals) {
        this.totalUsers = totalUsers;
        this.totalAccounts = totalAccounts;
        this.totalTransactions = totalTransactions;
        this.totalDeposits = totalDeposits != null ? totalDeposits : BigDecimal.ZERO;
        this.totalWithdrawals = totalWithdrawals != null ? totalWithdrawals : BigDecimal.ZERO;
        this.totalTransfers = totalTransfers != null ? totalTransfers : BigDecimal.ZERO;
        this.totalSystemBalance = totalSystemBalance != null ? totalSystemBalance : BigDecimal.ZERO;
        this.totalSavingsGoals = totalSavingsGoals;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalAccounts() {
        return totalAccounts;
    }

    public void setTotalAccounts(long totalAccounts) {
        this.totalAccounts = totalAccounts;
    }

    public long getTotalTransactions() {
        return totalTransactions;
    }

    public void setTotalTransactions(long totalTransactions) {
        this.totalTransactions = totalTransactions;
    }

    public BigDecimal getTotalDeposits() {
        return totalDeposits;
    }

    public void setTotalDeposits(BigDecimal totalDeposits) {
        this.totalDeposits = totalDeposits;
    }

    public BigDecimal getTotalWithdrawals() {
        return totalWithdrawals;
    }

    public void setTotalWithdrawals(BigDecimal totalWithdrawals) {
        this.totalWithdrawals = totalWithdrawals;
    }

    public BigDecimal getTotalTransfers() {
        return totalTransfers;
    }

    public void setTotalTransfers(BigDecimal totalTransfers) {
        this.totalTransfers = totalTransfers;
    }

    public BigDecimal getTotalSystemBalance() {
        return totalSystemBalance;
    }

    public void setTotalSystemBalance(BigDecimal totalSystemBalance) {
        this.totalSystemBalance = totalSystemBalance;
    }

    public long getTotalSavingsGoals() {
        return totalSavingsGoals;
    }

    public void setTotalSavingsGoals(long totalSavingsGoals) {
        this.totalSavingsGoals = totalSavingsGoals;
    }
}
