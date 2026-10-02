package com.bank.mvp.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bank.mvp.dto.AccountResponse;
import com.bank.mvp.dto.CreateAccountRequest;
import com.bank.mvp.dto.MoneyRequest;
import com.bank.mvp.dto.TransactionResponse;
import com.bank.mvp.dto.TransferRequest;
import com.bank.mvp.dto.TransferResponse;
import com.bank.mvp.exception.AccountNotFoundException;
import com.bank.mvp.exception.InsufficientBalanceException;
import com.bank.mvp.exception.InvalidAccountException;
import com.bank.mvp.exception.InvalidAmountException;
import com.bank.mvp.exception.ResourceNotFoundException;
import com.bank.mvp.exception.UserNotFoundException;
import com.bank.mvp.model.Account;
import com.bank.mvp.model.Transaction;
import com.bank.mvp.model.User;
import com.bank.mvp.repository.AccountRepository;
import com.bank.mvp.repository.TransactionRepository;
import com.bank.mvp.repository.UserRepository;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Autowired
    public AccountService(AccountRepository accountRepository, TransactionRepository transactionRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    public AccountService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this(accountRepository, transactionRepository, null);
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
            throw new InvalidAccountException("Account with this email already exists");
        }

        String accountNumber;
        if (request.getAccountNumber() != null && !request.getAccountNumber().trim().isEmpty()) {
            accountNumber = request.getAccountNumber().trim();
            if (accountRepository.findByAccountNumber(accountNumber).isPresent()) {
                throw new InvalidAccountException("Account with this account number already exists");
            }
        } else {
            accountNumber = "ACC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }

        Account account = new Account(accountNumber, name, email, initialBalance);
        if (userRepository != null && request.getUserId() != null) {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + request.getUserId()));
            account.setUser(user);
        }
        Account saved = accountRepository.save(account);
        return mapToAccountResponse(saved);
    }

    public AccountResponse getAccountById(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with id: " + id));
        return mapToAccountResponse(account);
    }

    @Transactional
    public AccountResponse deposit(Long accountId, MoneyRequest request) {
        Account account = getAccountEntity(accountId);
        BigDecimal amount = validatePositiveAmount(request);

        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);
        saveTransaction(account, "DEPOSIT", amount, "SUCCESS", null, account.getAccountNumber(), "Deposit");

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
        saveTransaction(account, "WITHDRAW", amount, "SUCCESS", account.getAccountNumber(), null, "Withdrawal");

        return mapToAccountResponse(account);
    }

    @Transactional
    public TransferResponse transferMoney(TransferRequest request) {
        if (request == null) {
            throw new InvalidAmountException("Request body is required");
        }

        BigDecimal amount = request.getAmount();
        if (amount == null) {
            throw new InvalidAmountException("Amount is required");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Transfer amount must be greater than zero");
        }

        Account sender = resolveAccount(request.getFromAccountId(), request.getFromAccountNumber(), "Sender");
        Account receiver = resolveAccount(request.getToAccountId(), request.getToAccountNumber(), "Receiver");

        if (sender.getId().equals(receiver.getId())) {
            throw new InvalidAccountException("Cannot transfer money to the same account");
        }

        if (sender.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance for fund transfer");
        }

        // Deduct money from sender
        sender.setBalance(sender.getBalance().subtract(amount));
        accountRepository.save(sender);

        // Add money to receiver
        receiver.setBalance(receiver.getBalance().add(amount));
        accountRepository.save(receiver);

        // Description
        String desc = request.getDescription();
        String senderDesc = (desc != null && !desc.trim().isEmpty())
                ? desc.trim()
                : "Transfer to " + receiver.getAccountNumber();
        String receiverDesc = (desc != null && !desc.trim().isEmpty())
                ? desc.trim()
                : "Transfer from " + sender.getAccountNumber();

        // Create transaction records
        Transaction senderTx = new Transaction(
                sender,
                "TRANSFER_OUT",
                amount,
                "SUCCESS",
                sender.getAccountNumber(),
                receiver.getAccountNumber(),
                senderDesc
        );
        senderTx = transactionRepository.save(senderTx);

        Transaction receiverTx = new Transaction(
                receiver,
                "TRANSFER_IN",
                amount,
                "SUCCESS",
                sender.getAccountNumber(),
                receiver.getAccountNumber(),
                receiverDesc
        );
        transactionRepository.save(receiverTx);

        LocalDateTime timestamp = senderTx.getCreatedAt() != null ? senderTx.getCreatedAt() : LocalDateTime.now();

        return new TransferResponse(
                "Transfer successful",
                senderTx.getId(),
                sender.getId(),
                sender.getAccountNumber(),
                receiver.getId(),
                receiver.getAccountNumber(),
                amount,
                sender.getBalance(),
                "SUCCESS",
                timestamp
        );
    }

    private Account resolveAccount(Long id, String accountNumber, String role) {
        if (id != null) {
            var found = accountRepository.findById(id);
            if (found.isPresent()) {
                return found.get();
            }
            if (accountNumber != null && !accountNumber.trim().isEmpty()) {
                return accountRepository.findByAccountNumber(accountNumber.trim())
                        .orElseThrow(() -> new AccountNotFoundException(role + " account not found with id: " + id + " or account number: " + accountNumber.trim()));
            }
            throw new AccountNotFoundException(role + " account not found with id: " + id);
        }
        if (accountNumber != null && !accountNumber.trim().isEmpty()) {
            return accountRepository.findByAccountNumber(accountNumber.trim())
                    .orElseThrow(() -> new AccountNotFoundException(role + " account not found with account number: " + accountNumber.trim()));
        }
        throw new InvalidAccountException(role + " account identifier (ID or account number) is required");
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
                .orElseThrow(() -> new AccountNotFoundException("Account not found with id: " + accountId));
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
        saveTransaction(account, type, amount, status, null, null, null);
    }

    private void saveTransaction(Account account, String type, BigDecimal amount, String status, String senderAccount, String receiverAccount, String description) {
        Transaction transaction = new Transaction(account, type, amount, status, senderAccount, receiverAccount, description);
        transactionRepository.save(transaction);
    }

    private AccountResponse mapToAccountResponse(Account account) {
        Long userId = account.getUser() != null ? account.getUser().getId() : null;
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getName(),
                account.getEmail(),
                account.getBalance(),
                userId
        );
    }

    private TransactionResponse mapToTransactionResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getAccount().getId(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getStatus(),
                transaction.getCreatedAt(),
                transaction.getSenderAccount(),
                transaction.getReceiverAccount(),
                transaction.getDescription()
        );
    }
}
