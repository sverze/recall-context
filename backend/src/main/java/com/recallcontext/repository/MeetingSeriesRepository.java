package com.recallcontext.repository;

import com.recallcontext.model.entity.MeetingSeries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MeetingSeriesRepository extends JpaRepository<MeetingSeries, Long> {
    Optional<MeetingSeries> findBySeriesNameAndMeetingType(String seriesName, String meetingType);
}
