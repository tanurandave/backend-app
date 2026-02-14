package com.training.backend_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class BackendAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendAppApplication.class, args);
        System.out.println("Training Institute Backend is running on port 8080");
        // use BCryptPasswordEncoder from Spring Security
BCryptPasswordEncoder enc = new BCryptPasswordEncoder();
System.out.println(enc.encode("StrongPass123!"));
    }
}
