package com.recallcontext.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessingStatusResponse {
    private Long meetingId;
    private String status; // PENDING, PROCESSING, COMPLETED, FAILED
    private String error;
    private Integer progress; // Optional: percentage complete
}
