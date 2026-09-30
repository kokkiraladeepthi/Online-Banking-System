package com.bank.mvp.service;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.bank.mvp.dto.AccountResponse;
import com.bank.mvp.dto.CreateAccountRequest;
import com.bank.mvp.dto.MoneyRequest;
import com.bank.mvp.exception.InsufficientBalanceException;
import com.bank.mvp.exception.InvalidAmountException;
import com.bank.mvp.model.Account;
import com.bank.mvp.repository.AccountRepository;
import com.bank.mvp.repository.TransactionRepository;

@DataJpaTest
@Import(AccountService.class)
@ActiveProfiles("test")
class AccountServiceTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    void shouldCreateAccountAndPersistBalance() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setName("John Doe");
        request.setEmail("john@example.com");
        request.setInitialBalance(new BigDecimal("1000.00"));

        AccountResponse response = accountService.createAccount(request);

        assertNotNull(response.getId());
        assertEquals("John Doe", response.getName());
        assertEquals(new BigDecimal("1000.00"), response.getBalance());
        assertEquals(1, accountRepository.count());
    }

    @Test
    void shouldDepositAndWithdrawBalance() {
        Account account = accountRepository.save(new Account("ACC-TEST", "Alice", "alice@example.com", new BigDecimal("500.00")));

        MoneyRequest depositRequest = new MoneyRequest();
        depositRequest.setAmount(new BigDecimal("250.00"));
        accountService.deposit(account.getId(), depositRequest);

        MoneyRequest withdrawRequest = new MoneyRequest();
        withdrawRequest.setAmount(new BigDecimal("100.00"));
        accountService.withdraw(account.getId(), withdrawRequest);

        Account updated = accountRepository.findById(account.getId()).orElseThrow();
        assertEquals(new BigDecimal("650.00"), updated.getBalance());
        assertEquals(2, transactionRepository.findByAccountIdOrderByCreatedAtDesc(account.getId()).size());
    }

    @Test
    void shouldRejectZeroAmount() {
        Account account = accountRepository.save(new Account("ACC-ZERO", "Bob", "bob@example.com", new BigDecimal("200.00")));

        MoneyRequest request = new MoneyRequest();
        request.setAmount(BigDecimal.ZERO);

        assertThrows(InvalidAmountException.class, () -> accountService.deposit(account.getId(), request));
    }

    @Test
    void shouldRejectWithdrawalWhenBalanceIsInsufficient() {
        Account account = accountRepository.save(new Account("ACC-WITHDRAW", "Cara", "cara@example.com", new BigDecimal("100.00")));

        MoneyRequest request = new MoneyRequest();
        request.setAmount(new BigDecimal("200.00"));

        assertThrows(InsufficientBalanceException.class, () -> accountService.withdraw(account.getId(), request));
    }

    @Test
    void shouldCreateAccountWithCustomAccountNumber() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setName("Jane Doe");
        request.setEmail("jane@example.com");
        request.setAccountNumber("ACC-CUSTOM-1");
        request.setInitialBalance(new BigDecimal("500.00"));

        AccountResponse response = accountService.createAccount(request);

        assertNotNull(response.getId());
        assertEquals("ACC-CUSTOM-1", response.getAccountNumber());
        assertEquals("Jane Doe", response.getName());
    }

    @Test
    void shouldRejectDuplicateAccountNumber() {
        accountRepository.save(new Account("ACC-DUP", "User1", "user1@example.com", new BigDecimal("100.00")));

        CreateAccountRequest request = new CreateAccountRequest();
        request.setName("User2");
        request.setEmail("user2@example.com");
        request.setAccountNumber("ACC-DUP");
        request.setInitialBalance(new BigDecimal("200.00"));

        assertThrows(InvalidAmountException.class, () -> accountService.createAccount(request));
    }
}
