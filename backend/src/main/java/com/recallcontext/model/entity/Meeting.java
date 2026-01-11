package com.recallcontext.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "meetings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Meeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "series_id")
    private MeetingSeries series;

    @Column(name = "meeting_date", nullable = false)
    private LocalDateTime meetingDate;

    @Column(name = "meeting_type", nullable = false, length = 50)
    private String meetingType;

    @Column(name = "series_name")
    private String seriesName;

    @Column(name = "original_filename", nullable = false, length = 500)
    private String originalFilename;

    @Column(name = "transcript_content", nullable = false, columnDefinition = "TEXT")
    private String transcriptContent;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Column(name = "processing_status", nullable = false, length = 50)
    private String processingStatus;

    @Column(name = "processing_error", columnDefinition = "TEXT")
    private String processingError;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (processingStatus == null) {
            processingStatus = "PENDING";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
