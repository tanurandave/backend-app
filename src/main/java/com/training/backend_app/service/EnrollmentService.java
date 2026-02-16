package com.training.backend_app.service;

import com.opencsv.CSVReader;
import com.training.backend_app.dto.BulkEnrollRequest;
import com.training.backend_app.dto.BulkUploadResponse;
import com.training.backend_app.dto.CourseResponse;
import com.training.backend_app.dto.EnrollmentRequest;
import com.training.backend_app.dto.EnrollmentResponse;
import com.training.backend_app.entity.Course;
import com.training.backend_app.entity.Enrollment;
import com.training.backend_app.entity.User;
import com.training.backend_app.repository.CourseRepository;
import com.training.backend_app.repository.EnrollmentRepository;
import com.training.backend_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

        private final EnrollmentRepository enrollmentRepository;
        private final UserRepository userRepository;
        private final CourseRepository courseRepository;
        private final NotificationService notificationService;

        // Existing methods ...

        public List<EnrollmentResponse> getAllEnrollments() {
                return enrollmentRepository.findAll().stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
        }

        @Transactional
        public EnrollmentResponse enrollStudent(EnrollmentRequest request) {
                User student = userRepository.findById(request.getStudentId())
                                .orElseThrow(() -> new RuntimeException("Student not found"));

                if (student.getRole() != User.Role.STUDENT) {
                        throw new RuntimeException("User is not a student");
                }

                Course course = courseRepository.findById(request.getCourseId())
                                .orElseThrow(() -> new RuntimeException("Course not found"));

                if (enrollmentRepository.existsByStudentIdAndCourseId(request.getStudentId(), request.getCourseId())) {
                        throw new RuntimeException("Student is already enrolled (or request pending) in this course");
                }

                Enrollment enrollment = Enrollment.builder()
                                .student(student)
                                .course(course)
                                .status(Enrollment.EnrollmentStatus.APPROVED) // Admin direct enroll is auto-approved
                                .build();

                enrollmentRepository.save(enrollment);

                // Notify student
                notificationService.createNotification(student.getId(),
                                "You have been manually enrolled in course: " + course.getName());

                return mapToResponse(enrollment);
        }

        public List<EnrollmentResponse> getEnrollmentsByStudentId(Long studentId) {
                return enrollmentRepository.findByStudentId(studentId).stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
        }

        public List<EnrollmentResponse> getEnrollmentsByCourseId(Long courseId) {
                return enrollmentRepository.findByCourseId(courseId).stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
        }

        @Transactional
        public List<EnrollmentResponse> bulkEnrollStudents(BulkEnrollRequest request) {
                Course course = courseRepository.findById(request.getCourseId())
                                .orElseThrow(() -> new RuntimeException("Course not found"));

                List<EnrollmentResponse> enrollmentResponses = new ArrayList<>();

                for (Long studentId : request.getStudentIds()) {
                        try {
                                // Using helper that sets APPROVED
                                enrollSingleStudent(studentId, course, enrollmentResponses);
                        } catch (Exception e) {
                                // Continue with next student
                        }
                }

                return enrollmentResponses;
        }

        private void enrollSingleStudent(Long studentId, Course course, List<EnrollmentResponse> responses) {
                User student = userRepository.findById(studentId)
                                .orElseThrow(() -> new RuntimeException("Student not found"));

                if (student.getRole() != User.Role.STUDENT)
                        return;

                if (enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), course.getId()))
                        return;

                Enrollment enrollment = Enrollment.builder()
                                .student(student)
                                .course(course)
                                .status(Enrollment.EnrollmentStatus.APPROVED) // Auto-approve for bulk
                                .build();

                enrollmentRepository.save(enrollment);

                notificationService.createNotification(student.getId(),
                                "You have been enrolled in course: " + course.getName());

                if (responses != null) {
                        responses.add(mapToResponse(enrollment));
                }
        }

        @Transactional
        public BulkUploadResponse bulkEnrollFromFile(Long courseId, MultipartFile file) {
                // ... (Keep existing implementation logic but reuse processEnrollmentByEmail)
                // For brevity in diff, assume implementation logic is similar but calls updated
                // processEnrollmentByEmail
                Course course = courseRepository.findById(courseId)
                                .orElseThrow(() -> new RuntimeException("Course not found"));

                int total = 0;
                int success = 0;
                int failed = 0;

                try {
                        String filename = file.getOriginalFilename();
                        if (filename == null)
                                throw new RuntimeException("Invalid file");

                        if (filename.endsWith(".csv")) {
                                try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
                                        List<String[]> records = reader.readAll();
                                        boolean firstRow = true;
                                        for (String[] record : records) {
                                                if (firstRow && (record[0].equalsIgnoreCase("email")
                                                                || record[0].equalsIgnoreCase("student email"))) {
                                                        firstRow = false;
                                                        continue;
                                                }
                                                firstRow = false;
                                                total++;
                                                if (record.length > 0 && processEnrollmentByEmail(record[0], course)) {
                                                        success++;
                                                } else {
                                                        failed++;
                                                }
                                        }
                                }
                        } else if (filename.endsWith(".xlsx") || filename.endsWith(".xls")) {
                                // ... (Excel logic same as before)
                                try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
                                        Sheet sheet = workbook.getSheetAt(0);
                                        Iterator<Row> rowIterator = sheet.iterator();
                                        boolean firstRow = true;
                                        while (rowIterator.hasNext()) {
                                                Row row = rowIterator.next();
                                                Cell cell = row.getCell(0);
                                                if (cell == null)
                                                        continue;
                                                String email = cell.getStringCellValue().trim();
                                                if (firstRow) {
                                                        if (email.equalsIgnoreCase("email")
                                                                        || email.equalsIgnoreCase("student email")) {
                                                                firstRow = false;
                                                                continue;
                                                        }
                                                        firstRow = false;
                                                }
                                                total++;
                                                if (!email.isEmpty() && processEnrollmentByEmail(email, course)) {
                                                        success++;
                                                } else {
                                                        failed++;
                                                }
                                        }
                                }
                        } else {
                                throw new RuntimeException("Unsupported file format. Please upload CSV or Excel.");
                        }

                } catch (Exception e) {
                        return BulkUploadResponse.builder()
                                        .totalRecords(total)
                                        .successfulRecords(success)
                                        .failedRecords(failed)
                                        .message("Error processing file: " + e.getMessage())
                                        .build();
                }

                return BulkUploadResponse.builder()
                                .totalRecords(total)
                                .successfulRecords(success)
                                .failedRecords(failed)
                                .message("File processed successfully.")
                                .build();
        }

        private boolean processEnrollmentByEmail(String email, Course course) {
                try {
                        Optional<User> studentOpt = userRepository.findByEmail(email);
                        if (studentOpt.isEmpty())
                                return false;

                        User student = studentOpt.get();
                        if (student.getRole() != User.Role.STUDENT)
                                return false;

                        if (enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), course.getId()))
                                return false;

                        // Direct internal call to save as APPROVED
                        Enrollment enrollment = Enrollment.builder()
                                        .student(student)
                                        .course(course)
                                        .status(Enrollment.EnrollmentStatus.APPROVED)
                                        .build();
                        enrollmentRepository.save(enrollment);
                        notificationService.createNotification(student.getId(),
                                        "You have been enrolled in course: " + course.getName());

                        return true;
                } catch (Exception e) {
                        return false;
                }
        }

        public void deleteEnrollment(Long id) {
                if (!enrollmentRepository.existsById(id)) {
                        throw new RuntimeException("Enrollment not found with id: " + id);
                }
                enrollmentRepository.deleteById(id);
        }

        // New: Request Enrollment (Student)
        @Transactional
        public EnrollmentResponse requestEnrollment(Long studentId, Long courseId) {
                User student = userRepository.findById(studentId)
                                .orElseThrow(() -> new RuntimeException("Student not found"));

                Course course = courseRepository.findById(courseId)
                                .orElseThrow(() -> new RuntimeException("Course not found"));

                if (enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
                        throw new RuntimeException("Already enrolled or request pending");
                }

                Enrollment enrollment = Enrollment.builder()
                                .student(student)
                                .course(course)
                                .status(Enrollment.EnrollmentStatus.PENDING)
                                .build();

                enrollmentRepository.save(enrollment);

                return mapToResponse(enrollment);
        }

        // New: Approve Enrollment (Admin)
        @Transactional
        public EnrollmentResponse approveEnrollment(Long enrollmentId) {
                Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                                .orElseThrow(() -> new RuntimeException("Enrollment not found"));

                enrollment.setStatus(Enrollment.EnrollmentStatus.APPROVED);
                enrollmentRepository.save(enrollment);

                notificationService.createNotification(enrollment.getStudent().getId(),
                                "Your enrollment request for " + enrollment.getCourse().getName()
                                                + " has been APPROVED.");

                return mapToResponse(enrollment);
        }

        // New: Reject Enrollment (Admin)
        @Transactional
        public EnrollmentResponse rejectEnrollment(Long enrollmentId) {
                Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                                .orElseThrow(() -> new RuntimeException("Enrollment not found"));

                enrollment.setStatus(Enrollment.EnrollmentStatus.REJECTED);
                enrollmentRepository.save(enrollment);

                notificationService.createNotification(enrollment.getStudent().getId(),
                                "Your enrollment request for " + enrollment.getCourse().getName()
                                                + " has been REJECTED.");

                return mapToResponse(enrollment);
        }

        public List<CourseResponse> getStudentCourses(Long studentId) {
                if (!userRepository.existsById(studentId)) {
                        throw new RuntimeException("Student not found");
                }

                return enrollmentRepository.findByStudentId(studentId).stream()
                                .filter(e -> e.getStatus() == Enrollment.EnrollmentStatus.APPROVED) // Only show
                                                                                                    // APPROVED courses
                                .map(enrollment -> {
                                        Course course = enrollment.getCourse();
                                        return CourseResponse.builder()
                                                        .id(course.getId())
                                                        .name(course.getName())
                                                        .description(course.getDescription())
                                                        .duration(course.getDuration())
                                                        .build();
                                })
                                .collect(Collectors.toList());
        }

        private EnrollmentResponse mapToResponse(Enrollment enrollment) {
                return EnrollmentResponse.builder()
                                .id(enrollment.getId())
                                .studentId(enrollment.getStudent().getId())
                                .studentName(enrollment.getStudent().getName())
                                .courseId(enrollment.getCourse().getId())
                                .courseName(enrollment.getCourse().getName())
                                .courseDescription(enrollment.getCourse().getDescription())
                                .courseDuration(enrollment.getCourse().getDuration())
                                .enrolledAt(enrollment.getEnrolledAt())
                                .status(enrollment.getStatus() != null ? enrollment.getStatus().name() : "APPROVED") // Handle
                                                                                                                     // legacy
                                                                                                                     // data
                                .build();
        }
}
