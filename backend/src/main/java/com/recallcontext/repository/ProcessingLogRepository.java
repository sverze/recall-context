package com.recallcontext.repository;

import com.recallcontext.model.entity.ProcessingLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcessingLogRepository extends JpaRepository<ProcessingLog, Long> {
    List<ProcessingLog> findByMeetingIdOrderByCreatedAtDesc(Long meetingId);
}
