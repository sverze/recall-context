package com.recallcontext.repository;

import com.recallcontext.model.entity.ActionItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ActionItemRepository extends JpaRepository<ActionItem, Long> {

    List<ActionItem> findByMeetingId(Long meetingId);

    Page<ActionItem> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<ActionItem> findByStatusOrderByDueDateAsc(String status, Pageable pageable);

    Page<ActionItem> findByAssigneeOrderByDueDateAsc(String assignee, Pageable pageable);

    @Query("SELECT a FROM ActionItem a WHERE a.status IN :statuses ORDER BY a.dueDate ASC")
    List<ActionItem> findByStatusIn(@Param("statuses") List<String> statuses);

    long countByStatus(String status);

    long countByAssignee(String assignee);

    @Query("SELECT COUNT(a) FROM ActionItem a WHERE a.dueDate < :today AND a.status != 'COMPLETED'")
    long countOverdueActions(@Param("today") LocalDate today);

    @Query("SELECT COUNT(a) FROM ActionItem a WHERE a.dueDate BETWEEN :startDate AND :endDate AND a.status != 'COMPLETED'")
    long countActionsDueBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT a FROM ActionItem a WHERE a.status != 'COMPLETED' AND (a.dueDate IS NULL OR a.dueDate <= :date) ORDER BY a.dueDate ASC")
    List<ActionItem> findPendingActions(@Param("date") LocalDate date);
}
