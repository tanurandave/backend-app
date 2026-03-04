package com.training.backend_app;

import com.training.backend_app.entity.User;
import com.training.backend_app.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@SpringBootApplication
public class BackendAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendAppApplication.class, args);
        System.out.println("Training Institute Backend is running on port 8080");
    }

    @Bean
    public CommandLineRunner initData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String adminEmail = "admin@example.com";
            Optional<User> admin = userRepository.findByEmail(adminEmail);

            if (admin.isEmpty()) {
                User newAdmin = User.builder()
                        .name("Administrator")
                        .email(adminEmail)
                        .password(passwordEncoder.encode("StrongPass123!"))
                        .role(User.Role.ADMIN)
                        .build();
                userRepository.save(newAdmin);
                System.out.println("Default admin created: " + adminEmail);
            } else {
                // Ensure password is correct and hashed properly
                User existingAdmin = admin.get();
                existingAdmin.setPassword(passwordEncoder.encode("StrongPass123!"));
                existingAdmin.setRole(User.Role.ADMIN); // Ensure role is ADMIN
                userRepository.save(existingAdmin);
                System.out.println("Default admin password reset for: " + adminEmail);
            }
        };
    }
}
