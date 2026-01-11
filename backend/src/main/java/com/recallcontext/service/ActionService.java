package com.recallcontext.service;

import com.recallcontext.model.dto.ActionItemResponse;
import com.recallcontext.model.dto.ActionUpdateRequest;
import com.recallcontext.model.entity.ActionItem;
import com.recallcontext.repository.ActionItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActionService {

    private final ActionItemRepository actionItemRepository;

    /**
     * Get all action items (paginated)
     */
    public Page<ActionItemResponse> getAllActions(Pageable pageable) {
        return actionItemRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::convertToResponse);
    }

    /**
     * Get action by ID
     */
    public ActionItemResponse getActionById(Long id) {
        ActionItem action = actionItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Action not found with ID: " + id));

        return convertToResponse(action);
    }

    /**
     * Update action item
     */
    @Transactional
    public ActionItemResponse updateAction(Long id, ActionUpdateRequest request) {
        log.info("Updating action {}", id);

        ActionItem action = actionItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Action not found with ID: " + id));

        // Update fields if provided
        if (request.getStatus() != null) {
            action.setStatus(request.getStatus());
        }
        if (request.getAssignee() != null) {
            action.setAssignee(request.getAssignee());
        }
        if (request.getDueDate() != null) {
            action.setDueDate(request.getDueDate());
        }
        if (request.getPriority() != null) {
            action.setPriority(request.getPriority());
        }
        if (request.getNotes() != null) {
            action.setNotes(request.getNotes());
        }

        action = actionItemRepository.save(action);
        log.info("Updated action {}", id);

        return convertToResponse(action);
    }

    /**
     * Convert ActionItem entity to ActionItemResponse DTO
     */
    private ActionItemResponse convertToResponse(ActionItem action) {
        return ActionItemResponse.builder()
                .id(action.getId())
                .meetingId(action.getMeeting().getId())
                .meetingType(action.getMeeting().getMeetingType())
                .meetingDate(action.getMeeting().getMeetingDate())
                .description(action.getDescription())
                .assignee(action.getAssignee())
                .dueDate(action.getDueDate())
                .status(action.getStatus())
                .priority(action.getPriority())
                .notes(action.getNotes())
                .completedAt(action.getCompletedAt())
                .createdAt(action.getCreatedAt())
                .updatedAt(action.getUpdatedAt())
                .build();
    }
}
