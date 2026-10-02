package com.bank.mvp.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.bank.mvp.model.AdminLog;
import com.bank.mvp.model.User;
import com.bank.mvp.repository.AdminLogRepository;
import com.bank.mvp.repository.UserRepository;

@Component
public class AdminDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AdminLogRepository adminLogRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AdminDataInitializer(UserRepository userRepository, AdminLogRepository adminLogRepository) {
        this.userRepository = userRepository;
        this.adminLogRepository = adminLogRepository;
    }

    @Override
    public void run(String... args) {
        String adminEmail = "admin@bank.com";
        if (!userRepository.existsByEmail(adminEmail)) {
            User admin = new User(
                    "System Administrator",
                    adminEmail,
                    passwordEncoder.encode("admin123"),
                    "9999999999",
                    "ADMIN"
            );
            userRepository.save(admin);
            adminLogRepository.save(new AdminLog(
                    "SYSTEM_INIT",
                    "SYSTEM",
                    "Default administrator account initialized (admin@bank.com)"
            ));
        }
    }
}

