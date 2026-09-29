package com.ecommerce.order_management.config;

import com.ecommerce.order_management.entity.Role;
import com.ecommerce.order_management.entity.User;
import com.ecommerce.order_management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${ADMIN_EMAIL}")
    private String adminEmail;

    @Value("${ADMIN_PASSWORD}")
    private String adminPassword;

    @Bean
    CommandLineRunner init() {
        return args -> {

            log.info(">>> INIT USER RUNNING <<<");

            if (userRepository.findByEmail(adminEmail).isEmpty()) {

                log.info(">>> CREATING ADMIN <<<");

                User user = User.builder()
                        .email(adminEmail)
                        .password(passwordEncoder.encode(adminPassword))
                        .firstName("Admin")
                        .lastName("User")
                        .role(Role.ROLE_ADMIN)
                        .enabled(true)
                        .build();

                userRepository.save(user);
            }
        };
    }
}