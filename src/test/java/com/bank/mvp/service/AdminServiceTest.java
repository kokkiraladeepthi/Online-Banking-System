package com.bank.mvp.service;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.bank.mvp.dto.AccountResponse;
import com.bank.mvp.dto.AdminDashboardResponse;
import com.bank.mvp.dto.CreateAccountRequest;
import com.bank.mvp.dto.MoneyRequest;
import com.bank.mvp.dto.RegisterUserRequest;
import com.bank.mvp.dto.SavingsGoalRequest;
import com.bank.mvp.dto.TransactionResponse;
import com.bank.mvp.dto.TransferRequest;
import com.bank.mvp.dto.UserResponse;
import com.bank.mvp.model.AdminLog;
import com.bank.mvp.repository.AccountRepository;
import com.bank.mvp.repository.AdminLogRepository;
import com.bank.mvp.repository.SavingsGoalRepository;
import com.bank.mvp.repository.TransactionRepository;
import com.bank.mvp.repository.UserRepository;

@DataJpaTest
@Import({AdminService.class, UserService.class, AccountService.class, SavingsGoalService.class})
@ActiveProfiles("test")
class AdminServiceTest {

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserService userService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private SavingsGoalService savingsGoalService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private SavingsGoalRepository savingsGoalRepository;

    @Autowired
    private AdminLogRepository adminLogRepository;

    @BeforeEach
    void setUp() {
        adminLogRepository.deleteAll();
        savingsGoalRepository.deleteAll();
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldGetAllUsersWithoutExposingPasswords() {
        userService.registerUser(new RegisterUserRequest("Admin One", "admin1@bank.com", "pass123", "1111111111", "ADMIN"));
        userService.registerUser(new RegisterUserRequest("Cust One", "cust1@bank.com", "pass456", "2222222222", "CUSTOMER"));

        List<UserResponse> users = adminService.getAllUsers();

        assertEquals(2, users.size());
        for (UserResponse u : users) {
            assertNotNull(u.getId());
            assertNotNull(u.getEmail());
            assertNotNull(u.getRole());
        }

        // Verify admin log was recorded
        List<AdminLog> logs = adminService.getAdminLogs();
        assertTrue(logs.stream().anyMatch(l -> "VIEW_USERS".equals(l.getAction())));
    }

    @Test
    void shouldGetAllAccounts() {
        CreateAccountRequest req1 = new CreateAccountRequest();
        req1.setName("Alice");
        req1.setEmail("alice@test.com");
        req1.setInitialBalance(new BigDecimal("1000.00"));
        accountService.createAccount(req1);

        CreateAccountRequest req2 = new CreateAccountRequest();
        req2.setName("Bob");
        req2.setEmail("bob@test.com");
        req2.setInitialBalance(new BigDecimal("2000.00"));
        accountService.createAccount(req2);

        List<AccountResponse> accounts = adminService.getAllAccounts();

        assertEquals(2, accounts.size());
    }

    @Test
    void shouldGetAllTransactionsAcrossAccounts() {
        CreateAccountRequest req1 = new CreateAccountRequest();
        req1.setName("User A");
        req1.setEmail("usera@test.com");
        req1.setInitialBalance(new BigDecimal("5000.00"));
        AccountResponse acc1 = accountService.createAccount(req1);

        CreateAccountRequest req2 = new CreateAccountRequest();
        req2.setName("User B");
        req2.setEmail("userb@test.com");
        req2.setInitialBalance(new BigDecimal("2000.00"));
        AccountResponse acc2 = accountService.createAccount(req2);

        // Deposit
        MoneyRequest dep = new MoneyRequest();
        dep.setAmount(new BigDecimal("500.00"));
        accountService.deposit(acc1.getId(), dep);

        // Withdraw
        MoneyRequest wth = new MoneyRequest();
        wth.setAmount(new BigDecimal("200.00"));
        accountService.withdraw(acc2.getId(), wth);

        // Transfer
        TransferRequest trf = new TransferRequest();
        trf.setFromAccountId(acc1.getId());
        trf.setToAccountId(acc2.getId());
        trf.setAmount(new BigDecimal("300.00"));
        accountService.transferMoney(trf);

        List<TransactionResponse> txList = adminService.getAllTransactions();

        // 1 deposit + 1 withdraw + 2 transfer legs (TRANSFER_OUT + TRANSFER_IN) = 4 transactions
        assertEquals(4, txList.size());
    }

    @Test
    void shouldComputeDashboardStatisticsAccurately() {
        // Create user
        UserResponse u = userService.registerUser(new RegisterUserRequest("Client", "client@bank.com", "pass123", "5555555555"));

        // Create 2 accounts
        CreateAccountRequest req1 = new CreateAccountRequest();
        req1.setName("Client Acc 1");
        req1.setEmail("client1@bank.com");
        req1.setInitialBalance(new BigDecimal("1000.00"));
        req1.setUserId(u.getId());
        AccountResponse acc1 = accountService.createAccount(req1);

        CreateAccountRequest req2 = new CreateAccountRequest();
        req2.setName("Client Acc 2");
        req2.setEmail("client2@bank.com");
        req2.setInitialBalance(new BigDecimal("2000.00"));
        req2.setUserId(u.getId());
        AccountResponse acc2 = accountService.createAccount(req2);

        // Deposit 500
        MoneyRequest dep = new MoneyRequest();
        dep.setAmount(new BigDecimal("500.00"));
        accountService.deposit(acc1.getId(), dep);

        // Withdraw 200
        MoneyRequest wth = new MoneyRequest();
        wth.setAmount(new BigDecimal("200.00"));
        accountService.withdraw(acc2.getId(), wth);

        // Transfer 300
        TransferRequest trf = new TransferRequest();
        trf.setFromAccountId(acc1.getId());
        trf.setToAccountId(acc2.getId());
        trf.setAmount(new BigDecimal("300.00"));
        accountService.transferMoney(trf);

        // Create savings goal
        savingsGoalService.createGoal(new SavingsGoalRequest(acc1.getId(), "Vacation", new BigDecimal("5000.00"), BigDecimal.ZERO, null));

        AdminDashboardResponse stats = adminService.getDashboardStats();

        assertEquals(1, stats.getTotalUsers());
        assertEquals(2, stats.getTotalAccounts());
        assertEquals(4, stats.getTotalTransactions());
        assertEquals(new BigDecimal("500.00"), stats.getTotalDeposits());
        assertEquals(new BigDecimal("200.00"), stats.getTotalWithdrawals());
        assertEquals(new BigDecimal("300.00"), stats.getTotalTransfers());
        assertEquals(new BigDecimal("3300.00"), stats.getTotalSystemBalance());
        assertEquals(1, stats.getTotalSavingsGoals());
    }

    @Test
    void shouldRecordAndRetrieveAdminLogs() {
        adminService.recordActivity("TEST_ACTION", "ADMIN_USER", "Testing activity log persistence");

        List<AdminLog> logs = adminService.getAdminLogs();

        assertFalse(logs.isEmpty());
        assertTrue(logs.stream().anyMatch(l -> "TEST_ACTION".equals(l.getAction())));
    }
}

