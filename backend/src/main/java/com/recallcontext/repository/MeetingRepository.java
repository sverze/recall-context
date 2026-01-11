package com.recallcontext.repository;

import com.recallcontext.model.entity.Meeting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    Page<Meeting> findAllByOrderByMeetingDateDesc(Pageable pageable);

    Page<Meeting> findByMeetingTypeOrderByMeetingDateDesc(String meetingType, Pageable pageable);

    Page<Meeting> findBySeriesNameOrderByMeetingDateDesc(String seriesName, Pageable pageable);

    List<Meeting> findTop5ByOrderByCreatedAtDesc();

    List<Meeting> findByProcessingStatus(String processingStatus);

    @Query("SELECT m FROM Meeting m WHERE m.meetingDate >= :startDate AND m.meetingDate <= :endDate ORDER BY m.meetingDate DESC")
    Page<Meeting> findByMeetingDateBetween(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate,
                                           Pageable pageable);

    long countByMeetingDateBetween(LocalDateTime startDate, LocalDateTime endDate);
}
