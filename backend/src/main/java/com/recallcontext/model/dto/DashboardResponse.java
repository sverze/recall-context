package com.recallcontext.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private List<MeetingSummary> recentMeetings;
    private List<ActionSummary> pendingActions;
    private Stats stats;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MeetingSummary {
        private Long id;
        private String meetingType;
        private String seriesName;
        private String meetingDate;
        private String summaryText;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActionSummary {
        private Long id;
        private String description;
        private String assignee;
        private String dueDate;
        private String status;
        private boolean overdue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Stats {
        private long totalMeetings;
        private long meetingsThisWeek;
        private long totalActions;
        private long actionsPending;
        private long actionsCompleted;
        private long actionsOverdue;
        private Map<String, Long> actionsByStatus;
    }
}
