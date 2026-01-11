package com.recallcontext.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "action_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "assignee")
    private String assignee;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "priority", length = 20)
    private String priority;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = "NOT_STARTED";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if ("COMPLETED".equals(status) && completedAt == null) {
            completedAt = LocalDateTime.now();
        }
    }
}
