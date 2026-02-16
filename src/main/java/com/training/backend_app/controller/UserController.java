package com.training.backend_app.controller;

import com.training.backend_app.dto.BulkUploadResponse;
import com.training.backend_app.entity.User;
import com.training.backend_app.repository.UserRepository;
import com.training.backend_app.service.NotificationService;
import com.training.backend_app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getUsersByRole(@PathVariable User.Role role) {
        return ResponseEntity.ok(userRepository.findByRole(role));
    }

    @GetMapping("/trainers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllTrainers() {
        return ResponseEntity.ok(userRepository.findByRole(User.Role.TRAINER));
    }

    @GetMapping("/students")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllStudents() {
        return ResponseEntity.ok(userRepository.findByRole(User.Role.STUDENT));
    }

    @PostMapping("/bulk-upload")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BulkUploadResponse> bulkUploadStudents(@RequestParam("file") MultipartFile file) {
        BulkUploadResponse response = userService.bulkUploadStudents(file);
        if (response.getSuccessfulRecords() > 0) {
            notificationService
                    .notifyAdmins("Bulk upload completed: " + response.getSuccessfulRecords() + " students added.");
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable("id") Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);
        notificationService
                .notifyAdmins("New " + savedUser.getRole().name().toLowerCase() + " created: " + savedUser.getName());
        return ResponseEntity.ok(savedUser);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    public ResponseEntity<User> updateUser(@PathVariable("id") Long id, @RequestBody User userDetails) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setName(userDetails.getName());
                    user.setEmail(userDetails.getEmail());
                    user.setPhone(userDetails.getPhone());
                    user.setExperience(userDetails.getExperience());
                    user.setSpecialization(userDetails.getSpecialization());
                    user.setBio(userDetails.getBio());
                    user.setQualification(userDetails.getQualification());

                    if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
                        user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
                    }
                    if (userDetails.getRole() != null) {
                        user.setRole(userDetails.getRole());
                    }
                    User updatedUser = userRepository.save(user);
                    notificationService.notifyAdmins(
                            "User updated: " + updatedUser.getName() + " (" + updatedUser.getRole() + ")");
                    return ResponseEntity.ok(updatedUser);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") Long id) {
        return userRepository.findById(id).map(user -> {
            userRepository.delete(user);
            notificationService.notifyAdmins("User deleted: " + user.getName() + " (" + user.getRole() + ")");
            return ResponseEntity.ok().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }
}
