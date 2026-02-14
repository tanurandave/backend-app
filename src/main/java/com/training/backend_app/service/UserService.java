package com.training.backend_app.service;

import com.training.backend_app.dto.BulkUploadResponse;
import com.training.backend_app.entity.User;
import com.training.backend_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public BulkUploadResponse bulkUploadStudents(MultipartFile file) {
        int totalRecords = 0;
        int successfulRecords = 0;
        int failedRecords = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue; // Skip header line
                }

                totalRecords++;

                try {
                    String[] data = line.split(",");
                    if (data.length < 3) {
                        failedRecords++;
                        continue;
                    }

                    String name = data[0].trim();
                    String email = data[1].trim();
                    String password = data[2].trim();

                    // Check if email already exists
                    if (userRepository.existsByEmail(email)) {
                        failedRecords++;
                        continue;
                    }

                    User student = User.builder()
                            .name(name)
                            .email(email)
                            .password(passwordEncoder.encode(password))
                            .role(User.Role.STUDENT)
                            .build();

                    userRepository.save(student);
                    successfulRecords++;

                } catch (Exception e) {
                    failedRecords++;
                }
            }

        } catch (Exception e) {
            return BulkUploadResponse.builder()
                    .totalRecords(0)
                    .successfulRecords(0)
                    .failedRecords(0)
                    .message("Error reading file: " + e.getMessage())
                    .build();
        }

        return BulkUploadResponse.builder()
                .totalRecords(totalRecords)
                .successfulRecords(successfulRecords)
                .failedRecords(failedRecords)
                .message("Bulk upload completed. " + successfulRecords + " students uploaded successfully.")
                .build();
    }
}
