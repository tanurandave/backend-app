package com.training.backend_app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "submissions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(nullable = false)
    private String status; // PENDING, SUBMITTED, GRADED

    @Column(length = 2000)
    private String comment;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] fileData;

    private String fileName;
    private String contentType;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime submittedAt;
}
