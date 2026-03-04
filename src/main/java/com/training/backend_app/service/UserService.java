package com.training.backend_app.service;

import com.training.backend_app.dto.BulkUploadResponse;
import com.training.backend_app.entity.User;
import com.training.backend_app.repository.UserRepository;
import com.training.backend_app.repository.EnrollmentRepository;
import com.training.backend_app.repository.SubmissionRepository;
import com.training.backend_app.repository.AssignmentRepository;
import com.training.backend_app.repository.MaterialRepository;
import com.training.backend_app.repository.NotificationRepository;
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
    private final EnrollmentRepository enrollmentRepository;
    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final MaterialRepository materialRepository;
    private final NotificationRepository notificationRepository;

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

    /**
     * Deletes a user and any dependent records that would violate foreign keys.
     * Students: remove enrollments & submissions. Trainers: remove their resources.
     */
    @Transactional
    public void deleteUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

        if (user.getRole() == User.Role.STUDENT) {
            // remove all enrollments, submissions, notifications tied to this student
            enrollmentRepository.findByStudentId(id).forEach(enrollmentRepository::delete);
            submissionRepository.findByStudentId(id).forEach(submissionRepository::delete);
            // notifications repo already provides ordered methods, just use one of them
            notificationRepository.findByUserIdOrderByCreatedAtDesc(id).forEach(notificationRepository::delete);
        } else if (user.getRole() == User.Role.TRAINER) {
            // remove any assignments/materials created by this trainer
            assignmentRepository.findByTrainerId(id).forEach(assignmentRepository::delete);
            materialRepository.findByTrainerId(id).forEach(materialRepository::delete);
            notificationRepository.findByUserIdOrderByCreatedAtDesc(id).forEach(notificationRepository::delete);
        }

        // other roles (admin) also may have notifications
        notificationRepository.findByUserIdOrderByCreatedAtDesc(id).forEach(notificationRepository::delete);
        userRepository.delete(user);
    }
}
