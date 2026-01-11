package com.recallcontext.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingUploadRequest {

    @NotBlank(message = "Filename is required")
    private String filename;

    @NotBlank(message = "Content is required")
    @Size(max = 5_000_000, message = "Content size must not exceed 5MB")
    private String content;
}
