package com.test.web.repository;

import com.test.web.entity.AlertRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 告警记录 Repository
 */
@Repository
public interface AlertRecordRepository extends JpaRepository<AlertRecordEntity, Long> {

    List<AlertRecordEntity> findByLevel(String level);

    List<AlertRecordEntity> findByAlertType(String alertType);

    List<AlertRecordEntity> findBySent(Boolean sent);

    List<AlertRecordEntity> findByAcknowledged(Boolean acknowledged);

    List<AlertRecordEntity> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT a FROM AlertRecordEntity a WHERE a.level IN :levels AND a.acknowledged = false")
    List<AlertRecordEntity> findUnacknowledgedByLevels(List<String> levels);

    @Modifying
    @Query("UPDATE AlertRecordEntity a SET a.acknowledged = true, a.acknowledgedAt = :acknowledgedAt, " +
           "a.acknowledgedBy = :acknowledgedBy WHERE a.id = :id")
    int acknowledge(Long id, LocalDateTime acknowledgedAt, String acknowledgedBy);

    @Query("SELECT COUNT(a) FROM AlertRecordEntity a WHERE a.createdAt BETWEEN :start AND :end")
    long countBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT a.level, COUNT(a) FROM AlertRecordEntity a GROUP BY a.level")
    List<Object[]> countByLevel();
}