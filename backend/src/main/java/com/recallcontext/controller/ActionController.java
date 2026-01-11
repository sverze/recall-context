package com.recallcontext.controller;

import com.recallcontext.model.dto.ActionItemResponse;
import com.recallcontext.model.dto.ActionUpdateRequest;
import com.recallcontext.service.ActionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/actions")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ActionController {

    private final ActionService actionService;

    /**
     * Get all actions (paginated)
     */
    @GetMapping
    public ResponseEntity<Page<ActionItemResponse>> getAllActions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ActionItemResponse> actions = actionService.getAllActions(pageable);

        return ResponseEntity.ok(actions);
    }

    /**
     * Get action by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ActionItemResponse> getActionById(@PathVariable Long id) {
        ActionItemResponse action = actionService.getActionById(id);
        return ResponseEntity.ok(action);
    }

    /**
     * Update action
     */
    @PutMapping("/{id}")
    public ResponseEntity<ActionItemResponse> updateAction(
            @PathVariable Long id,
            @RequestBody ActionUpdateRequest request
    ) {
        ActionItemResponse action = actionService.updateAction(id, request);
        return ResponseEntity.ok(action);
    }

    /**
     * Update action status only
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ActionItemResponse> updateActionStatus(
            @PathVariable Long id,
            @RequestBody ActionUpdateRequest request
    ) {
        ActionItemResponse action = actionService.updateAction(id, request);
        return ResponseEntity.ok(action);
    }
}
