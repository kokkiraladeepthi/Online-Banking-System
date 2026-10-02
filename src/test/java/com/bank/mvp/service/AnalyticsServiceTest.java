package com.bank.mvp.service;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.bank.mvp.dto.AccountAnalyticsResponse;
import com.bank.mvp.dto.MoneyRequest;
import com.bank.mvp.dto.TransferRequest;
import com.bank.mvp.exception.AccountNotFoundException;
import com.bank.mvp.model.Account;
import com.bank.mvp.repository.AccountRepository;

@DataJpaTest
@Import({AccountService.class, AnalyticsService.class})
@ActiveProfiles("test")
class AnalyticsServiceTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void shouldReturnZeroAnalyticsForNewAccountWithoutTransactions() {
        Account account = accountRepository.save(new Account("ACC-AN-0", "Zero Tx", "zero@example.com", new BigDecimal("1000.00")));

        AccountAnalyticsResponse analytics = analyticsService.getAccountAnalytics(account.getId());

        assertNotNull(analytics);
        assertEquals(account.getId(), analytics.getAccountId());
        assertEquals(account.getAccountNumber(), analytics.getAccountNumber());
        assertEquals(0, analytics.getTransactionCount());
        assertEquals(BigDecimal.ZERO, analytics.getTotalDeposits());
        assertEquals(BigDecimal.ZERO, analytics.getTotalWithdrawals());
        assertEquals(BigDecimal.ZERO, analytics.getTotalTransfers());
        assertEquals(BigDecimal.ZERO, analytics.getTotalMoneyReceived());
        assertEquals(BigDecimal.ZERO, analytics.getTotalMoneySpent());
        assertEquals(0, analytics.getMonthlySummary().size());
    }

    @Test
    void shouldComputeCorrectAnalyticsForMultipleTransactions() {
        Account sender = accountRepository.save(new Account("ACC-AN-S", "Analytics Sender", "sender.an@example.com", new BigDecimal("5000.00")));
        Account receiver = accountRepository.save(new Account("ACC-AN-R", "Analytics Receiver", "receiver.an@example.com", new BigDecimal("2000.00")));

        // 1. Deposit 2000 into sender
        MoneyRequest depReq = new MoneyRequest();
        depReq.setAmount(new BigDecimal("2000.00"));
        accountService.deposit(sender.getId(), depReq);

        // 2. Deposit 1000 into sender
        depReq.setAmount(new BigDecimal("1000.00"));
        accountService.deposit(sender.getId(), depReq);

        // 3. Withdraw 500 from sender
        MoneyRequest wdReq = new MoneyRequest();
        wdReq.setAmount(new BigDecimal("500.00"));
        accountService.withdraw(sender.getId(), wdReq);

        // 4. Transfer 1200 from sender to receiver
        TransferRequest tfReq = new TransferRequest(sender.getId(), receiver.getId(), new BigDecimal("1200.00"), "Project payout");
        accountService.transferMoney(tfReq);

        // Check sender analytics
        AccountAnalyticsResponse senderAnalytics = analyticsService.getAccountAnalytics(sender.getId());
        assertEquals(4, senderAnalytics.getTransactionCount());
        assertEquals(new BigDecimal("3000.00"), senderAnalytics.getTotalDeposits());
        assertEquals(new BigDecimal("500.00"), senderAnalytics.getTotalWithdrawals());
        assertEquals(new BigDecimal("1200.00"), senderAnalytics.getTotalTransfersSent());
        assertEquals(BigDecimal.ZERO, senderAnalytics.getTotalTransfersReceived());
        assertEquals(new BigDecimal("1200.00"), senderAnalytics.getTotalTransfers());
        assertEquals(new BigDecimal("3000.00"), senderAnalytics.getTotalMoneyReceived());
        assertEquals(new BigDecimal("1700.00"), senderAnalytics.getTotalMoneySpent());
        assertEquals(new BigDecimal("1300.00"), senderAnalytics.getNetSavings());
        assertFalse(senderAnalytics.getMonthlySummary().isEmpty());

        // Check receiver analytics
        AccountAnalyticsResponse recvAnalytics = analyticsService.getAccountAnalytics(receiver.getId());
        assertEquals(1, recvAnalytics.getTransactionCount());
        assertEquals(BigDecimal.ZERO, recvAnalytics.getTotalDeposits());
        assertEquals(BigDecimal.ZERO, recvAnalytics.getTotalWithdrawals());
        assertEquals(BigDecimal.ZERO, recvAnalytics.getTotalTransfersSent());
        assertEquals(new BigDecimal("1200.00"), recvAnalytics.getTotalTransfersReceived());
        assertEquals(new BigDecimal("1200.00"), recvAnalytics.getTotalTransfers());
        assertEquals(new BigDecimal("1200.00"), recvAnalytics.getTotalMoneyReceived());
        assertEquals(BigDecimal.ZERO, recvAnalytics.getTotalMoneySpent());
        assertEquals(new BigDecimal("1200.00"), recvAnalytics.getNetSavings());
    }

    @Test
    void shouldThrowExceptionWhenAccountNotFound() {
        assertThrows(AccountNotFoundException.class, () -> analyticsService.getAccountAnalytics(99999L));
    }
}
