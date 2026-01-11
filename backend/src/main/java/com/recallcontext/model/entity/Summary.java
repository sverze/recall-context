package com.recallcontext.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "summaries")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Summary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false, unique = true)
    private Meeting meeting;

    @Column(name = "key_points", nullable = false, columnDefinition = "TEXT[]")
    @JdbcTypeCode(SqlTypes.ARRAY)
    private List<String> keyPoints;

    @Column(name = "decisions", nullable = false, columnDefinition = "TEXT[]")
    @JdbcTypeCode(SqlTypes.ARRAY)
    private List<String> decisions;

    @Column(name = "summary_text", nullable = false, columnDefinition = "TEXT")
    private String summaryText;

    @Column(name = "sentiment", length = 50)
    private String sentiment;

    @Column(name = "tone", length = 50)
    private String tone;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ai_metadata", columnDefinition = "jsonb")
    private Map<String, Object> aiMetadata;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
