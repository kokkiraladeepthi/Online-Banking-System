package com.bank.mvp.service;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.bank.mvp.dto.AccountResponse;
import com.bank.mvp.dto.CreateAccountRequest;
import com.bank.mvp.dto.MoneyRequest;
import com.bank.mvp.dto.TransferRequest;
import com.bank.mvp.dto.TransferResponse;
import com.bank.mvp.exception.InsufficientBalanceException;
import com.bank.mvp.exception.InvalidAmountException;
import com.bank.mvp.exception.ResourceNotFoundException;
import com.bank.mvp.model.Account;
import com.bank.mvp.model.Transaction;
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

    @Test
    void shouldTransferMoneySuccessfully() {
        Account sender = accountRepository.save(new Account("ACC-SEND-1", "Alice", "alice.send@example.com", new BigDecimal("500.00")));
        Account receiver = accountRepository.save(new Account("ACC-RECV-1", "Bob", "bob.recv@example.com", new BigDecimal("200.00")));

        TransferRequest request = new TransferRequest();
        request.setFromAccountId(sender.getId());
        request.setToAccountId(receiver.getId());
        request.setAmount(new BigDecimal("150.00"));
        request.setDescription("Rent payment");

        TransferResponse response = accountService.transferMoney(request);

        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertEquals(new BigDecimal("150.00"), response.getAmount());
        assertEquals(new BigDecimal("350.00"), response.getSenderBalance());
        assertEquals("ACC-SEND-1", response.getFromAccountNumber());
        assertEquals("ACC-RECV-1", response.getToAccountNumber());

        Account updatedSender = accountRepository.findById(sender.getId()).orElseThrow();
        Account updatedReceiver = accountRepository.findById(receiver.getId()).orElseThrow();
        assertEquals(new BigDecimal("350.00"), updatedSender.getBalance());
        assertEquals(new BigDecimal("350.00"), updatedReceiver.getBalance());

        var senderTxs = transactionRepository.findByAccountIdOrderByCreatedAtDesc(sender.getId());
        assertEquals(1, senderTxs.size());
        assertEquals("TRANSFER_OUT", senderTxs.get(0).getType());
        assertEquals("ACC-SEND-1", senderTxs.get(0).getSenderAccount());
        assertEquals("ACC-RECV-1", senderTxs.get(0).getReceiverAccount());
        assertEquals("Rent payment", senderTxs.get(0).getDescription());

        var receiverTxs = transactionRepository.findByAccountIdOrderByCreatedAtDesc(receiver.getId());
        assertEquals(1, receiverTxs.size());
        assertEquals("TRANSFER_IN", receiverTxs.get(0).getType());
        assertEquals("ACC-SEND-1", receiverTxs.get(0).getSenderAccount());
        assertEquals("ACC-RECV-1", receiverTxs.get(0).getReceiverAccount());
        assertEquals("Rent payment", receiverTxs.get(0).getDescription());
    }

    @Test
    void shouldTransferMoneyUsingAccountNumbers() {
        accountRepository.save(new Account("ACC-NUM-S", "Charlie", "charlie@example.com", new BigDecimal("300.00")));
        accountRepository.save(new Account("ACC-NUM-R", "David", "david@example.com", new BigDecimal("100.00")));

        TransferRequest request = new TransferRequest();
        request.setFromAccountNumber("ACC-NUM-S");
        request.setToAccountNumber("ACC-NUM-R");
        request.setAmount(new BigDecimal("75.00"));

        TransferResponse response = accountService.transferMoney(request);

        assertEquals("SUCCESS", response.getStatus());
        assertEquals(new BigDecimal("225.00"), response.getSenderBalance());
    }

    @Test
    void shouldRejectTransferWithInsufficientBalance() {
        Account sender = accountRepository.save(new Account("ACC-LOW", "Eve", "eve@example.com", new BigDecimal("50.00")));
        Account receiver = accountRepository.save(new Account("ACC-RECV-2", "Frank", "frank@example.com", new BigDecimal("100.00")));

        TransferRequest request = new TransferRequest();
        request.setFromAccountId(sender.getId());
        request.setToAccountId(receiver.getId());
        request.setAmount(new BigDecimal("100.00"));

        assertThrows(InsufficientBalanceException.class, () -> accountService.transferMoney(request));

        Account unchangedSender = accountRepository.findById(sender.getId()).orElseThrow();
        Account unchangedReceiver = accountRepository.findById(receiver.getId()).orElseThrow();
        assertEquals(new BigDecimal("50.00"), unchangedSender.getBalance());
        assertEquals(new BigDecimal("100.00"), unchangedReceiver.getBalance());
    }

    @Test
    void shouldRejectTransferWithInvalidSender() {
        Account receiver = accountRepository.save(new Account("ACC-RECV-3", "Grace", "grace@example.com", new BigDecimal("100.00")));

        TransferRequest request = new TransferRequest();
        request.setFromAccountId(99999L);
        request.setToAccountId(receiver.getId());
        request.setAmount(new BigDecimal("50.00"));

        assertThrows(ResourceNotFoundException.class, () -> accountService.transferMoney(request));
    }

    @Test
    void shouldRejectTransferWithInvalidReceiver() {
        Account sender = accountRepository.save(new Account("ACC-SEND-3", "Heidi", "heidi@example.com", new BigDecimal("200.00")));

        TransferRequest request = new TransferRequest();
        request.setFromAccountId(sender.getId());
        request.setToAccountId(99999L);
        request.setAmount(new BigDecimal("50.00"));

        assertThrows(ResourceNotFoundException.class, () -> accountService.transferMoney(request));
    }

    @Test
    void shouldRejectTransferToSameAccount() {
        Account sender = accountRepository.save(new Account("ACC-SAME", "Ivan", "ivan@example.com", new BigDecimal("200.00")));

        TransferRequest request = new TransferRequest();
        request.setFromAccountId(sender.getId());
        request.setToAccountId(sender.getId());
        request.setAmount(new BigDecimal("50.00"));

        assertThrows(InvalidAmountException.class, () -> accountService.transferMoney(request));
    }

    @Test
    void shouldRejectZeroOrNegativeTransferAmount() {
        Account sender = accountRepository.save(new Account("ACC-S-NEG", "Judy", "judy@example.com", new BigDecimal("200.00")));
        Account receiver = accountRepository.save(new Account("ACC-R-NEG", "Ken", "ken@example.com", new BigDecimal("100.00")));

        TransferRequest zeroReq = new TransferRequest();
        zeroReq.setFromAccountId(sender.getId());
        zeroReq.setToAccountId(receiver.getId());
        zeroReq.setAmount(BigDecimal.ZERO);
        assertThrows(InvalidAmountException.class, () -> accountService.transferMoney(zeroReq));

        TransferRequest negReq = new TransferRequest();
        negReq.setFromAccountId(sender.getId());
        negReq.setToAccountId(receiver.getId());
        negReq.setAmount(new BigDecimal("-50.00"));
        assertThrows(InvalidAmountException.class, () -> accountService.transferMoney(negReq));
    }
}
