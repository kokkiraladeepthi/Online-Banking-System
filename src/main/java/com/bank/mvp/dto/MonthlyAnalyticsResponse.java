package com.bank.mvp.dto;

import java.math.BigDecimal;

public class MonthlyAnalyticsResponse {

    private String month;
    private BigDecimal totalDeposits;
    private BigDecimal totalWithdrawals;
    private BigDecimal totalTransfersSent;
    private BigDecimal totalTransfersReceived;
    private BigDecimal inflow;
    private BigDecimal outflow;
    private long transactionCount;

    public MonthlyAnalyticsResponse() {
    }

    public MonthlyAnalyticsResponse(String month, BigDecimal totalDeposits, BigDecimal totalWithdrawals,
                                    BigDecimal totalTransfersSent, BigDecimal totalTransfersReceived,
                                    BigDecimal inflow, BigDecimal outflow, long transactionCount) {
        this.month = month;
        this.totalDeposits = totalDeposits;
        this.totalWithdrawals = totalWithdrawals;
        this.totalTransfersSent = totalTransfersSent;
        this.totalTransfersReceived = totalTransfersReceived;
        this.inflow = inflow;
        this.outflow = outflow;
        this.transactionCount = transactionCount;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
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

    public BigDecimal getInflow() {
        return inflow;
    }

    public void setInflow(BigDecimal inflow) {
        this.inflow = inflow;
    }

    public BigDecimal getOutflow() {
        return outflow;
    }

    public void setOutflow(BigDecimal outflow) {
        this.outflow = outflow;
    }

    public long getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(long transactionCount) {
        this.transactionCount = transactionCount;
    }
}
