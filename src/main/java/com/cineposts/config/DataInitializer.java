package com.cineposts.config;

import com.cineposts.model.User;
import com.cineposts.model.enums.Role;
import com.cineposts.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Bean
    public CommandLineRunner seedAdminUser() {
        return args -> {
            if (!userRepository.existsByUsername("admin")) {
                User admin = User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin123"))
                        .role(Role.ADMIN)
                        .active(true)
                        .build();

                userRepository.save(admin);
                log.info("✅ Default admin user created — username: admin, password: admin123");
                log.warn("⚠️  Change the default admin password immediately in production!");
            } else {
                log.info("Admin user already exists, skipping seed");
            }
        };
    }
}
