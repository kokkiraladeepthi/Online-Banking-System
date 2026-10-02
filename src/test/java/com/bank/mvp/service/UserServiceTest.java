package com.bank.mvp.service;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import com.bank.mvp.dto.LoginRequest;
import com.bank.mvp.dto.LoginResponse;
import com.bank.mvp.dto.RegisterUserRequest;
import com.bank.mvp.dto.UserResponse;
import com.bank.mvp.exception.InvalidCredentialsException;
import com.bank.mvp.exception.UserAlreadyExistsException;
import com.bank.mvp.model.User;
import com.bank.mvp.repository.UserRepository;

@DataJpaTest
@Import({UserService.class, AccountService.class})
@ActiveProfiles("test")
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldRegisterUserSuccessfully() {
        RegisterUserRequest request = new RegisterUserRequest("John Smith", "john.smith@example.com", "secret123", "9876543210");
        UserResponse response = userService.registerUser(request);

        assertNotNull(response.getId());
        assertEquals("John Smith", response.getName());
        assertEquals("john.smith@example.com", response.getEmail());
        assertEquals("9876543210", response.getPhone());
        assertNotNull(response.getCreatedAt());

        User entity = userRepository.findById(response.getId()).orElseThrow();
        assertFalse(entity.getPassword().equals("secret123"), "Password must be encrypted");
        assertTrue(entity.getPassword().startsWith("$2a$"), "Password should be BCrypt hashed");
    }

    @Test
    void shouldRejectDuplicateEmailRegistration() {
        RegisterUserRequest request1 = new RegisterUserRequest("User One", "duplicate@example.com", "pass123", "1111111111");
        userService.registerUser(request1);

        RegisterUserRequest request2 = new RegisterUserRequest("User Two", "duplicate@example.com", "pass456", "2222222222");
        assertThrows(UserAlreadyExistsException.class, () -> userService.registerUser(request2));
    }

    @Test
    void shouldLoginSuccessfullyWithCorrectCredentials() {
        RegisterUserRequest registerReq = new RegisterUserRequest("Alice", "alice.login@example.com", "password123", "3333333333");
        userService.registerUser(registerReq);

        LoginRequest loginReq = new LoginRequest("alice.login@example.com", "password123");
        LoginResponse loginResp = userService.loginUser(loginReq);

        assertEquals("Login successful", loginResp.getMessage());
        assertNotNull(loginResp.getUser());
        assertEquals("Alice", loginResp.getUser().getName());
    }

    @Test
    void shouldRejectLoginWithWrongPassword() {
        RegisterUserRequest registerReq = new RegisterUserRequest("Bob", "bob.login@example.com", "correctPassword", "4444444444");
        userService.registerUser(registerReq);

        LoginRequest loginReq = new LoginRequest("bob.login@example.com", "wrongPassword");
        assertThrows(InvalidCredentialsException.class, () -> userService.loginUser(loginReq));
    }

    @Test
    void shouldRejectLoginForNonexistentUser() {
        LoginRequest loginReq = new LoginRequest("nonexistent@example.com", "anyPass");
        assertThrows(InvalidCredentialsException.class, () -> userService.loginUser(loginReq));
    }

    @Test
    void shouldLinkAccountToUserAndRetrieveUserAccounts() {
        RegisterUserRequest registerReq = new RegisterUserRequest("Charlie", "charlie@example.com", "password123", "5555555555");
        UserResponse user = userService.registerUser(registerReq);

        CreateAccountRequest accReq = new CreateAccountRequest();
        accReq.setName("Charlie Savings");
        accReq.setEmail("charlie.acc@example.com");
        accReq.setInitialBalance(new BigDecimal("3000.00"));
        accReq.setUserId(user.getId());

        AccountResponse createdAccount = accountService.createAccount(accReq);
        assertEquals(user.getId(), createdAccount.getUserId());

        List<AccountResponse> userAccounts = userService.getUserAccounts(user.getId());
        assertEquals(1, userAccounts.size());
        assertEquals(createdAccount.getId(), userAccounts.get(0).getId());
        assertEquals(new BigDecimal("3000.00"), userAccounts.get(0).getBalance());
    }
}
