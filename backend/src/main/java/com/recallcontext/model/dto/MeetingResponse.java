package com.recallcontext.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingResponse {
    private Long id;
    private LocalDateTime meetingDate;
    private String meetingType;
    private String seriesName;
    private String originalFilename;
    private String processingStatus;
    private String processingError;
    private LocalDateTime createdAt;
    private SummaryDto summary;
    private List<ParticipantDto> participants;
    private List<ActionItemDto> actionItems;
    private String transcriptContent; // Optional, only included in detail view

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SummaryDto {
        private List<String> keyPoints;
        private List<String> decisions;
        private String summaryText;
        private String sentiment;
        private String tone;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParticipantDto {
        private Long id;
        private String name;
        private String role;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActionItemDto {
        private Long id;
        private String description;
        private String assignee;
        private String dueDate;
        private String status;
        private String priority;
    }
}
