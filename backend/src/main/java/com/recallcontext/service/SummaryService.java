package com.recallcontext.service;

import com.recallcontext.model.entity.*;
import com.recallcontext.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SummaryService {

    private final AnthropicService anthropicService;
    private final SummaryRepository summaryRepository;
    private final ParticipantRepository participantRepository;
    private final ActionItemRepository actionItemRepository;
    private final ProcessingLogRepository processingLogRepository;

    /**
     * Analyze meeting transcript using AI and store results
     */
    @Transactional
    public void analyzeAndStoreMeeting(Meeting meeting, String apiKey) {
        log.info("Analyzing meeting {} with AI", meeting.getId());

        try {
            // Call Anthropic API
            AnthropicService.MeetingAnalysis analysis =
                    anthropicService.analyzeMeetingTranscript(meeting.getTranscriptContent(), apiKey);

            // Store summary
            storeSummary(meeting, analysis);

            // Store participants
            storeParticipants(meeting, analysis.getParticipants());

            // Store action items
            storeActionItems(meeting, analysis.getActionItems());

            // Log success
            logProcessing(meeting, "AI_ANALYSIS", "SUCCESS", null);

            log.info("Successfully analyzed and stored meeting {}", meeting.getId());

        } catch (Exception e) {
            log.error("Error analyzing meeting {}", meeting.getId(), e);
            logProcessing(meeting, "AI_ANALYSIS", "FAILURE", e.getMessage());
            throw e;
        }
    }

    /**
     * Store summary in database
     */
    private void storeSummary(Meeting meeting, AnthropicService.MeetingAnalysis analysis) {
        Summary summary = Summary.builder()
                .meeting(meeting)
                .keyPoints(analysis.getKeyPoints())
                .decisions(analysis.getDecisions())
                .summaryText(analysis.getSummaryText())
                .sentiment(analysis.getSentiment())
                .tone(analysis.getTone())
                .aiMetadata(analysis.getAiMetadata())
                .build();

        summaryRepository.save(summary);
        log.debug("Stored summary for meeting {}", meeting.getId());
    }

    /**
     * Store participants in database
     */
    private void storeParticipants(Meeting meeting, List<AnthropicService.MeetingAnalysis.Participant> participants) {
        if (participants == null || participants.isEmpty()) {
            log.debug("No participants to store for meeting {}", meeting.getId());
            return;
        }

        List<Participant> participantEntities = new ArrayList<>();
        for (AnthropicService.MeetingAnalysis.Participant p : participants) {
            Participant participant = Participant.builder()
                    .meeting(meeting)
                    .name(p.getName())
                    .role(p.getRole())
                    .build();
            participantEntities.add(participant);
        }

        participantRepository.saveAll(participantEntities);
        log.debug("Stored {} participants for meeting {}", participantEntities.size(), meeting.getId());
    }

    /**
     * Store action items in database
     */
    private void storeActionItems(Meeting meeting, List<AnthropicService.MeetingAnalysis.ActionItemData> actionItems) {
        if (actionItems == null || actionItems.isEmpty()) {
            log.debug("No action items to store for meeting {}", meeting.getId());
            return;
        }

        List<ActionItem> actionEntities = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (AnthropicService.MeetingAnalysis.ActionItemData action : actionItems) {
            LocalDate dueDate = null;
            if (action.getDueDate() != null && !action.getDueDate().isEmpty()) {
                try {
                    dueDate = LocalDate.parse(action.getDueDate(), formatter);
                } catch (Exception e) {
                    log.warn("Invalid due date format: {}", action.getDueDate());
                }
            }

            ActionItem actionItem = ActionItem.builder()
                    .meeting(meeting)
                    .description(action.getDescription())
                    .assignee(action.getAssignee())
                    .dueDate(dueDate)
                    .status("NOT_STARTED")
                    .priority(action.getPriority())
                    .build();
            actionEntities.add(actionItem);
        }

        actionItemRepository.saveAll(actionEntities);
        log.debug("Stored {} action items for meeting {}", actionEntities.size(), meeting.getId());
    }

    /**
     * Log processing operation
     */
    private void logProcessing(Meeting meeting, String operation, String status, String errorMessage) {
        ProcessingLog log = ProcessingLog.builder()
                .meeting(meeting)
                .operation(operation)
                .status(status)
                .errorMessage(errorMessage)
                .build();

        processingLogRepository.save(log);
    }
}
