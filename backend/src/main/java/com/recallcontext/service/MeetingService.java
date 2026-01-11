package com.recallcontext.service;

import com.recallcontext.exception.TranscriptProcessingException;
import com.recallcontext.model.dto.MeetingResponse;
import com.recallcontext.model.dto.ProcessingStatusResponse;
import com.recallcontext.model.entity.*;
import com.recallcontext.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final MeetingSeriesRepository meetingSeriesRepository;
    private final SummaryRepository summaryRepository;
    private final ParticipantRepository participantRepository;
    private final ActionItemRepository actionItemRepository;
    private final TranscriptParserService parserService;
    private final SummaryService summaryService;
    private final SettingsService settingsService;

    /**
     * Upload and process a meeting transcript
     */
    @Transactional
    public MeetingResponse uploadTranscript(String filename, String content) {
        log.info("Uploading transcript: {}", filename);

        try {
            // Parse filename to extract metadata
            TranscriptParserService.ParsedMetadata metadata = parserService.parseFilename(filename);

            // Find or create meeting series
            MeetingSeries series = findOrCreateSeries(metadata.getSeriesName(), metadata.getMeetingType());

            // Create meeting entity
            Meeting meeting = Meeting.builder()
                    .series(series)
                    .meetingDate(metadata.getMeetingDate())
                    .meetingType(metadata.getMeetingType())
                    .seriesName(metadata.getSeriesName())
                    .originalFilename(filename)
                    .transcriptContent(content)
                    .processingStatus("PROCESSING")
                    .build();

            meeting = meetingRepository.save(meeting);
            log.info("Created meeting entity with ID: {}", meeting.getId());

            // Process with AI (synchronous for MVP)
            try {
                String apiKey = settingsService.getApiKey();
                summaryService.analyzeAndStoreMeeting(meeting, apiKey);

                // Update status to COMPLETED
                meeting.setProcessingStatus("COMPLETED");
                meetingRepository.save(meeting);

                log.info("Successfully processed meeting {}", meeting.getId());

            } catch (Exception e) {
                log.error("Error processing meeting {}", meeting.getId(), e);
                meeting.setProcessingStatus("FAILED");
                meeting.setProcessingError(e.getMessage());
                meetingRepository.save(meeting);

                throw new TranscriptProcessingException(
                        "Failed to process transcript: " + e.getMessage(), e
                );
            }

            return convertToResponse(meeting, false);

        } catch (Exception e) {
            log.error("Error uploading transcript", e);
            throw e;
        }
    }

    /**
     * Get meeting by ID
     */
    public MeetingResponse getMeetingById(Long id) {
        Meeting meeting = meetingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Meeting not found with ID: " + id));

        return convertToResponse(meeting, true);
    }

    /**
     * Get all meetings (paginated)
     */
    public Page<MeetingResponse> getAllMeetings(Pageable pageable) {
        return meetingRepository.findAllByOrderByMeetingDateDesc(pageable)
                .map(meeting -> convertToResponse(meeting, false));
    }

    /**
     * Get processing status
     */
    public ProcessingStatusResponse getProcessingStatus(Long id) {
        Meeting meeting = meetingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Meeting not found with ID: " + id));

        return ProcessingStatusResponse.builder()
                .meetingId(meeting.getId())
                .status(meeting.getProcessingStatus())
                .error(meeting.getProcessingError())
                .build();
    }

    /**
     * Delete meeting
     */
    @Transactional
    public void deleteMeeting(Long id) {
        log.info("Deleting meeting {}", id);
        Meeting meeting = meetingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Meeting not found with ID: " + id));

        meetingRepository.delete(meeting);
        log.info("Deleted meeting {}", id);
    }

    /**
     * Find or create meeting series
     */
    private MeetingSeries findOrCreateSeries(String seriesName, String meetingType) {
        return meetingSeriesRepository
                .findBySeriesNameAndMeetingType(seriesName, meetingType)
                .orElseGet(() -> {
                    MeetingSeries series = MeetingSeries.builder()
                            .seriesName(seriesName)
                            .meetingType(meetingType)
                            .build();
                    return meetingSeriesRepository.save(series);
                });
    }

    /**
     * Convert Meeting entity to MeetingResponse DTO
     */
    private MeetingResponse convertToResponse(Meeting meeting, boolean includeTranscript) {
        MeetingResponse.MeetingResponseBuilder builder = MeetingResponse.builder()
                .id(meeting.getId())
                .meetingDate(meeting.getMeetingDate())
                .meetingType(meeting.getMeetingType())
                .seriesName(meeting.getSeriesName())
                .originalFilename(meeting.getOriginalFilename())
                .processingStatus(meeting.getProcessingStatus())
                .processingError(meeting.getProcessingError())
                .createdAt(meeting.getCreatedAt());

        // Include transcript if requested (for detail view)
        if (includeTranscript) {
            builder.transcriptContent(meeting.getTranscriptContent());
        }

        // Include summary if exists
        summaryRepository.findByMeetingId(meeting.getId())
                .ifPresent(summary -> builder.summary(convertSummaryToDto(summary)));

        // Include participants
        List<Participant> participants = participantRepository.findByMeetingId(meeting.getId());
        if (!participants.isEmpty()) {
            builder.participants(participants.stream()
                    .map(this::convertParticipantToDto)
                    .collect(Collectors.toList()));
        }

        // Include action items
        List<ActionItem> actionItems = actionItemRepository.findByMeetingId(meeting.getId());
        if (!actionItems.isEmpty()) {
            builder.actionItems(actionItems.stream()
                    .map(this::convertActionItemToDto)
                    .collect(Collectors.toList()));
        }

        return builder.build();
    }

    private MeetingResponse.SummaryDto convertSummaryToDto(Summary summary) {
        return MeetingResponse.SummaryDto.builder()
                .keyPoints(summary.getKeyPoints())
                .decisions(summary.getDecisions())
                .summaryText(summary.getSummaryText())
                .sentiment(summary.getSentiment())
                .tone(summary.getTone())
                .build();
    }

    private MeetingResponse.ParticipantDto convertParticipantToDto(Participant participant) {
        return MeetingResponse.ParticipantDto.builder()
                .id(participant.getId())
                .name(participant.getName())
                .role(participant.getRole())
                .build();
    }

    private MeetingResponse.ActionItemDto convertActionItemToDto(ActionItem action) {
        return MeetingResponse.ActionItemDto.builder()
                .id(action.getId())
                .description(action.getDescription())
                .assignee(action.getAssignee())
                .dueDate(action.getDueDate() != null ? action.getDueDate().toString() : null)
                .status(action.getStatus())
                .priority(action.getPriority())
                .build();
    }
}
