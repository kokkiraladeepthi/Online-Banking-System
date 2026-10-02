package com.bank.mvp.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bank.mvp.dto.AccountResponse;
import com.bank.mvp.dto.AdminDashboardResponse;
import com.bank.mvp.dto.TransactionResponse;
import com.bank.mvp.dto.UserResponse;
import com.bank.mvp.model.Account;
import com.bank.mvp.model.AdminLog;
import com.bank.mvp.model.Transaction;
import com.bank.mvp.model.User;
import com.bank.mvp.repository.AccountRepository;
import com.bank.mvp.repository.AdminLogRepository;
import com.bank.mvp.repository.SavingsGoalRepository;
import com.bank.mvp.repository.TransactionRepository;
import com.bank.mvp.repository.UserRepository;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final SavingsGoalRepository savingsGoalRepository;
    private final AdminLogRepository adminLogRepository;

    public AdminService(UserRepository userRepository,
                        AccountRepository accountRepository,
                        TransactionRepository transactionRepository,
                        SavingsGoalRepository savingsGoalRepository,
                        AdminLogRepository adminLogRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.savingsGoalRepository = savingsGoalRepository;
        this.adminLogRepository = adminLogRepository;
    }

    @Transactional
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAllByOrderByCreatedAtDesc();
        recordActivity("VIEW_USERS", "ADMIN", "Retrieved list of all users (count: " + users.size() + ")");
        return users.stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<AccountResponse> getAllAccounts() {
        List<Account> accounts = accountRepository.findAll();
        recordActivity("VIEW_ACCOUNTS", "ADMIN", "Retrieved list of all accounts (count: " + accounts.size() + ")");
        return accounts.stream()
                .map(this::mapToAccountResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<TransactionResponse> getAllTransactions() {
        List<Transaction> transactions = transactionRepository.findAllByOrderByCreatedAtDesc();
        recordActivity("VIEW_TRANSACTIONS", "ADMIN", "Retrieved all system transactions (count: " + transactions.size() + ")");
        return transactions.stream()
                .map(this::mapToTransactionResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AdminDashboardResponse getDashboardStats() {
        long totalUsers = userRepository.count();
        long totalAccounts = accountRepository.count();
        long totalTransactions = transactionRepository.count();
        long totalGoals = savingsGoalRepository.count();

        BigDecimal totalSystemBalance = accountRepository.findAll().stream()
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Transaction> allTx = transactionRepository.findAll();
        BigDecimal totalDeposits = BigDecimal.ZERO;
        BigDecimal totalWithdrawals = BigDecimal.ZERO;
        BigDecimal totalTransfers = BigDecimal.ZERO;

        for (Transaction tx : allTx) {
            BigDecimal amt = tx.getAmount() != null ? tx.getAmount() : BigDecimal.ZERO;
            if ("DEPOSIT".equalsIgnoreCase(tx.getType())) {
                totalDeposits = totalDeposits.add(amt);
            } else if ("WITHDRAW".equalsIgnoreCase(tx.getType())) {
                totalWithdrawals = totalWithdrawals.add(amt);
            } else if ("TRANSFER_OUT".equalsIgnoreCase(tx.getType())) {
                totalTransfers = totalTransfers.add(amt);
            }
        }

        recordActivity("VIEW_DASHBOARD", "ADMIN", "Viewed system overview dashboard statistics");

        return new AdminDashboardResponse(
                totalUsers,
                totalAccounts,
                totalTransactions,
                totalDeposits,
                totalWithdrawals,
                totalTransfers,
                totalSystemBalance,
                totalGoals
        );
    }

    @Transactional
    public List<AdminLog> getAdminLogs() {
        recordActivity("VIEW_LOGS", "ADMIN", "Retrieved admin activity history logs");
        return adminLogRepository.findAllByOrderByTimestampDesc();
    }

    @Transactional
    public void recordActivity(String action, String performedBy, String details) {
        adminLogRepository.save(new AdminLog(action, performedBy, details));
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
        Long accountId = transaction.getAccount() != null ? transaction.getAccount().getId() : null;
        return new TransactionResponse(
                transaction.getId(),
                accountId,
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
