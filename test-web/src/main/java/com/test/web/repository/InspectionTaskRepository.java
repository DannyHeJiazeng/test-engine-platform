package com.test.web.repository;

import com.test.web.entity.InspectionTaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 巡检任务 Repository
 */
@Repository
public interface InspectionTaskRepository extends JpaRepository<InspectionTaskEntity, Long> {

    Optional<InspectionTaskEntity> findByTaskId(String taskId);

    List<InspectionTaskEntity> findByEnabled(Boolean enabled);

    List<InspectionTaskEntity> findByAlertOnFailure(Boolean alertOnFailure);

    @Query("SELECT t FROM InspectionTaskEntity t WHERE t.lastRunTime < :threshold")
    List<InspectionTaskEntity> findTasksNotRunSince(LocalDateTime threshold);

    @Modifying
    @Query("UPDATE InspectionTaskEntity t SET t.enabled = :enabled WHERE t.taskId = :taskId")
    int updateEnabled(String taskId, Boolean enabled);

    @Modifying
    @Query("UPDATE InspectionTaskEntity t SET t.lastRunTime = :lastRunTime, " +
           "t.lastRunStatus = :status, t.totalRuns = t.totalRuns + 1 WHERE t.taskId = :taskId")
    int updateRunStatus(String taskId, LocalDateTime lastRunTime, String status);

    void deleteByTaskId(String taskId);

    boolean existsByTaskId(String taskId);
}