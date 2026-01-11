package com.recallcontext.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionItemResponse {
    private Long id;
    private Long meetingId;
    private String meetingType;
    private LocalDateTime meetingDate;
    private String description;
    private String assignee;
    private LocalDate dueDate;
    private String status;
    private String priority;
    private String notes;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
