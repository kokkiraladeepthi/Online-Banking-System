package com.bank.mvp.service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bank.mvp.dto.AccountAnalyticsResponse;
import com.bank.mvp.dto.MonthlyAnalyticsResponse;
import com.bank.mvp.exception.AccountNotFoundException;
import com.bank.mvp.model.Account;
import com.bank.mvp.model.Transaction;
import com.bank.mvp.repository.AccountRepository;
import com.bank.mvp.repository.TransactionRepository;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    public AnalyticsService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    public AccountAnalyticsResponse getAccountAnalytics(Long accountId) {
        if (accountId == null) {
            throw new AccountNotFoundException("Account ID is required");
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with id: " + accountId));

        List<Transaction> transactions = transactionRepository.findByAccountIdOrderByCreatedAtDesc(accountId);

        BigDecimal totalDeposits = BigDecimal.ZERO;
        BigDecimal totalWithdrawals = BigDecimal.ZERO;
        BigDecimal totalTransfersSent = BigDecimal.ZERO;
        BigDecimal totalTransfersReceived = BigDecimal.ZERO;

        Map<String, List<Transaction>> monthlyGroups = new LinkedHashMap<>();

        for (Transaction tx : transactions) {
            BigDecimal amt = tx.getAmount() != null ? tx.getAmount() : BigDecimal.ZERO;
            String type = tx.getType() != null ? tx.getType().toUpperCase() : "";

            switch (type) {
                case "DEPOSIT" -> totalDeposits = totalDeposits.add(amt);
                case "WITHDRAW" -> totalWithdrawals = totalWithdrawals.add(amt);
                case "TRANSFER_OUT" -> totalTransfersSent = totalTransfersSent.add(amt);
                case "TRANSFER_IN" -> totalTransfersReceived = totalTransfersReceived.add(amt);
                default -> {
                    // For legacy or generic TRANSFER records
                    if (type.contains("TRANSFER")) {
                        totalTransfersSent = totalTransfersSent.add(amt);
                    }
                }
            }

            if (tx.getCreatedAt() != null) {
                String monthKey = tx.getCreatedAt().format(MONTH_FORMATTER);
                monthlyGroups.computeIfAbsent(monthKey, k -> new ArrayList<>()).add(tx);
            }
        }

        BigDecimal totalTransfers = totalTransfersSent.add(totalTransfersReceived);
        BigDecimal totalMoneyReceived = totalDeposits.add(totalTransfersReceived);
        BigDecimal totalMoneySpent = totalWithdrawals.add(totalTransfersSent);
        BigDecimal netSavings = totalMoneyReceived.subtract(totalMoneySpent);
        long count = transactions.size();

        List<MonthlyAnalyticsResponse> monthlySummary = new ArrayList<>();
        for (Map.Entry<String, List<Transaction>> entry : monthlyGroups.entrySet()) {
            String month = entry.getKey();
            BigDecimal mDeposits = BigDecimal.ZERO;
            BigDecimal mWithdrawals = BigDecimal.ZERO;
            BigDecimal mSent = BigDecimal.ZERO;
            BigDecimal mRecv = BigDecimal.ZERO;

            for (Transaction tx : entry.getValue()) {
                BigDecimal amt = tx.getAmount() != null ? tx.getAmount() : BigDecimal.ZERO;
                String type = tx.getType() != null ? tx.getType().toUpperCase() : "";

                switch (type) {
                    case "DEPOSIT" -> mDeposits = mDeposits.add(amt);
                    case "WITHDRAW" -> mWithdrawals = mWithdrawals.add(amt);
                    case "TRANSFER_OUT" -> mSent = mSent.add(amt);
                    case "TRANSFER_IN" -> mRecv = mRecv.add(amt);
                    default -> {
                        if (type.contains("TRANSFER")) {
                            mSent = mSent.add(amt);
                        }
                    }
                }
            }

            BigDecimal mInflow = mDeposits.add(mRecv);
            BigDecimal mOutflow = mWithdrawals.add(mSent);

            monthlySummary.add(new MonthlyAnalyticsResponse(
                    month,
                    mDeposits,
                    mWithdrawals,
                    mSent,
                    mRecv,
                    mInflow,
                    mOutflow,
                    entry.getValue().size()
            ));
        }

        return new AccountAnalyticsResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getBalance(),
                totalDeposits,
                totalWithdrawals,
                totalTransfersSent,
                totalTransfersReceived,
                totalTransfers,
                totalMoneyReceived,
                totalMoneySpent,
                netSavings,
                count,
                monthlySummary
        );
    }
}
