package com.bank.mvp.dto;

import java.math.BigDecimal;
import java.util.List;

public class AccountAnalyticsResponse {

    private Long accountId;
    private String accountNumber;
    private BigDecimal currentBalance;
    private BigDecimal totalDeposits;
    private BigDecimal totalWithdrawals;
    private BigDecimal totalTransfersSent;
    private BigDecimal totalTransfersReceived;
    private BigDecimal totalTransfers;
    private BigDecimal totalMoneyReceived;
    private BigDecimal totalMoneySpent;
    private BigDecimal netSavings;
    private long transactionCount;
    private List<MonthlyAnalyticsResponse> monthlySummary;

    public AccountAnalyticsResponse() {
    }

    public AccountAnalyticsResponse(Long accountId, String accountNumber, BigDecimal currentBalance,
                                    BigDecimal totalDeposits, BigDecimal totalWithdrawals,
                                    BigDecimal totalTransfersSent, BigDecimal totalTransfersReceived,
                                    BigDecimal totalTransfers, BigDecimal totalMoneyReceived,
                                    BigDecimal totalMoneySpent, BigDecimal netSavings,
                                    long transactionCount, List<MonthlyAnalyticsResponse> monthlySummary) {
        this.accountId = accountId;
        this.accountNumber = accountNumber;
        this.currentBalance = currentBalance;
        this.totalDeposits = totalDeposits;
        this.totalWithdrawals = totalWithdrawals;
        this.totalTransfersSent = totalTransfersSent;
        this.totalTransfersReceived = totalTransfersReceived;
        this.totalTransfers = totalTransfers;
        this.totalMoneyReceived = totalMoneyReceived;
        this.totalMoneySpent = totalMoneySpent;
        this.netSavings = netSavings;
        this.transactionCount = transactionCount;
        this.monthlySummary = monthlySummary;
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

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(BigDecimal currentBalance) {
        this.currentBalance = currentBalance;
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

    public BigDecimal getTotalTransfersSent() {
        return totalTransfersSent;
    }

    public void setTotalTransfersSent(BigDecimal totalTransfersSent) {
        this.totalTransfersSent = totalTransfersSent;
    }

    public BigDecimal getTotalTransfersReceived() {
        return totalTransfersReceived;
    }

    public void setTotalTransfersReceived(BigDecimal totalTransfersReceived) {
        this.totalTransfersReceived = totalTransfersReceived;
    }

    public BigDecimal getTotalTransfers() {
        return totalTransfers;
    }

    public void setTotalTransfers(BigDecimal totalTransfers) {
        this.totalTransfers = totalTransfers;
    }

    public BigDecimal getTotalMoneyReceived() {
        return totalMoneyReceived;
    }

    public void setTotalMoneyReceived(BigDecimal totalMoneyReceived) {
        this.totalMoneyReceived = totalMoneyReceived;
    }

    public BigDecimal getTotalMoneySpent() {
        return totalMoneySpent;
    }

    public void setTotalMoneySpent(BigDecimal totalMoneySpent) {
        this.totalMoneySpent = totalMoneySpent;
    }

    public BigDecimal getNetSavings() {
        return netSavings;
    }

    public void setNetSavings(BigDecimal netSavings) {
        this.netSavings = netSavings;
    }

    public long getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(long transactionCount) {
        this.transactionCount = transactionCount;
    }

    public List<MonthlyAnalyticsResponse> getMonthlySummary() {
        return monthlySummary;
    }

    public void setMonthlySummary(List<MonthlyAnalyticsResponse> monthlySummary) {
        this.monthlySummary = monthlySummary;
    }
}
