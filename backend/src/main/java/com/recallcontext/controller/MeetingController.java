package com.recallcontext.controller;

import com.recallcontext.model.dto.MeetingResponse;
import com.recallcontext.model.dto.MeetingUploadRequest;
import com.recallcontext.model.dto.ProcessingStatusResponse;
import com.recallcontext.service.MeetingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/meetings")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class MeetingController {

    private final MeetingService meetingService;

    /**
     * Upload a new meeting transcript
     */
    @PostMapping
    public ResponseEntity<MeetingResponse> uploadTranscript(@Valid @RequestBody MeetingUploadRequest request) {
        log.info("Received transcript upload request: {}", request.getFilename());

        MeetingResponse response = meetingService.uploadTranscript(
                request.getFilename(),
                request.getContent()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all meetings (paginated)
     */
    @GetMapping
    public ResponseEntity<Page<MeetingResponse>> getAllMeetings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<MeetingResponse> meetings = meetingService.getAllMeetings(pageable);

        return ResponseEntity.ok(meetings);
    }

    /**
     * Get meeting by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<MeetingResponse> getMeetingById(@PathVariable Long id) {
        MeetingResponse meeting = meetingService.getMeetingById(id);
        return ResponseEntity.ok(meeting);
    }

    /**
     * Get processing status
     */
    @GetMapping("/{id}/processing-status")
    public ResponseEntity<ProcessingStatusResponse> getProcessingStatus(@PathVariable Long id) {
        ProcessingStatusResponse status = meetingService.getProcessingStatus(id);
        return ResponseEntity.ok(status);
    }

    /**
     * Delete meeting
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMeeting(@PathVariable Long id) {
        meetingService.deleteMeeting(id);
        return ResponseEntity.noContent().build();
    }
}
