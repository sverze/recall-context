package com.recallcontext.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionUpdateRequest {
    private String status;
    private String assignee;
    private LocalDate dueDate;
    private String priority;
    private String notes;
}
