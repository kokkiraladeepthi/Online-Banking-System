package com.bank.mvp.service;

import java.util.List;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bank.mvp.dto.AccountResponse;
import com.bank.mvp.dto.LoginRequest;
import com.bank.mvp.dto.LoginResponse;
import com.bank.mvp.dto.RegisterUserRequest;
import com.bank.mvp.dto.UserResponse;
import com.bank.mvp.exception.DuplicateUserException;
import com.bank.mvp.exception.InvalidCredentialsException;
import com.bank.mvp.exception.ResourceNotFoundException;
import com.bank.mvp.exception.UserAlreadyExistsException;
import com.bank.mvp.exception.UserNotFoundException;
import com.bank.mvp.model.Account;
import com.bank.mvp.model.User;
import com.bank.mvp.repository.AccountRepository;
import com.bank.mvp.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, AccountRepository accountRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Transactional
    public UserResponse registerUser(RegisterUserRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Registration request cannot be null");
        }

        String email = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : null;
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateUserException("User with email '" + email + "' already exists");
        }

        String rawPassword = request.getPassword();
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }

        String encodedPassword = passwordEncoder.encode(rawPassword);
        String name = request.getName() != null ? request.getName().trim() : "";
        String phone = request.getPhone() != null ? request.getPhone().trim() : null;
        String role = (request.getRole() != null && !request.getRole().trim().isEmpty())
                ? request.getRole().trim().toUpperCase()
                : "CUSTOMER";

        User user = new User(name, email, encodedPassword, phone, role);
        User savedUser = userRepository.save(user);

        return mapToUserResponse(savedUser);
    }

    public LoginResponse loginUser(LoginRequest request) {
        if (request == null) {
            throw new InvalidCredentialsException("Login request cannot be null");
        }

        String email = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : null;
        String password = request.getPassword();

        if (email == null || password == null) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        return new LoginResponse("Login successful", mapToUserResponse(user));
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        return mapToUserResponse(user);
    }

    public List<AccountResponse> getUserAccounts(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found with id: " + userId);
        }

        return accountRepository.findByUserId(userId)
                .stream()
                .map(this::mapToAccountResponse)
                .toList();
    }

    public UserResponse mapToUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole() != null ? user.getRole() : "CUSTOMER",
                user.getCreatedAt()
        );
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
}
