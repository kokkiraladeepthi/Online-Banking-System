package com.bank.mvp.service;

import com.bank.mvp.dto.AccountResponse;
import com.bank.mvp.dto.CreateAccountRequest;
import com.bank.mvp.dto.MoneyRequest;
import com.bank.mvp.dto.TransactionResponse;
import com.bank.mvp.exception.InsufficientBalanceException;
import com.bank.mvp.exception.InvalidAmountException;
import com.bank.mvp.exception.ResourceNotFoundException;
import com.bank.mvp.model.Account;
import com.bank.mvp.model.Transaction;
import com.bank.mvp.repository.AccountRepository;
import com.bank.mvp.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public AccountService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    public AccountResponse createAccount(CreateAccountRequest request) {
        if (request == null) {
            throw new InvalidAmountException("Request body is required");
        }

        String name = request.getName() == null ? null : request.getName().trim();
        String email = request.getEmail() == null ? null : request.getEmail().trim();
        BigDecimal initialBalance = request.getInitialBalance();

        if (name == null || name.isEmpty()) {
            throw new InvalidAmountException("Name is required");
        }
        if (email == null || email.isEmpty()) {
            throw new InvalidAmountException("Email is required");
        }
        if (initialBalance == null) {
            throw new InvalidAmountException("Initial balance is required");
        }
        if (initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidAmountException("Initial balance cannot be negative");
        }
        if (accountRepository.findByEmail(email).isPresent()) {
            throw new InvalidAmountException("Account with this email already exists");
        }

        String accountNumber;
        if (request.getAccountNumber() != null && !request.getAccountNumber().trim().isEmpty()) {
            accountNumber = request.getAccountNumber().trim();
            if (accountRepository.findByAccountNumber(accountNumber).isPresent()) {
                throw new InvalidAmountException("Account with this account number already exists");
            }
        } else {
            accountNumber = "ACC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }

        Account account = new Account(accountNumber, name, email, initialBalance);
        Account saved = accountRepository.save(account);
        return mapToAccountResponse(saved);
    }

    public AccountResponse getAccountById(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + id));
        return mapToAccountResponse(account);
    }

    @Transactional
    public AccountResponse deposit(Long accountId, MoneyRequest request) {
        Account account = getAccountEntity(accountId);
        BigDecimal amount = validatePositiveAmount(request);

        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);
        saveTransaction(account, "DEPOSIT", amount, "SUCCESS");

        return mapToAccountResponse(account);
    }

    @Transactional
    public AccountResponse withdraw(Long accountId, MoneyRequest request) {
        Account account = getAccountEntity(accountId);
        BigDecimal amount = validatePositiveAmount(request);

        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance for withdrawal");
        }

        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);
        saveTransaction(account, "WITHDRAW", amount, "SUCCESS");

        return mapToAccountResponse(account);
    }

    public List<TransactionResponse> getTransactionHistory(Long accountId) {
        Account account = getAccountEntity(accountId);

        return transactionRepository.findByAccountIdOrderByCreatedAtDesc(account.getId())
                .stream()
                .map(this::mapToTransactionResponse)
                .toList();
    }

    private Account getAccountEntity(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + accountId));
    }

    private BigDecimal validatePositiveAmount(MoneyRequest request) {
        if (request == null || request.getAmount() == null) {
            throw new InvalidAmountException("Amount is required");
        }

        BigDecimal amount = request.getAmount();
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Amount must be greater than zero");
        }

        return amount;
    }

    private void saveTransaction(Account account, String type, BigDecimal amount, String status) {
        Transaction transaction = new Transaction(account, type, amount, status);
        transactionRepository.save(transaction);
    }

    private AccountResponse mapToAccountResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getName(),
                account.getEmail(),
                account.getBalance()
        );
    }

    private TransactionResponse mapToTransactionResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getAccount().getId(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getStatus(),
                transaction.getCreatedAt()
        );
    }
}
