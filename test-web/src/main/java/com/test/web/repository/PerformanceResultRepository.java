package com.test.web.repository;

import com.test.web.entity.PerformanceResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 性能测试结果 Repository
 */
@Repository
public interface PerformanceResultRepository extends JpaRepository<PerformanceResultEntity, Long> {

    Optional<PerformanceResultEntity> findByTestId(String testId);

    List<PerformanceResultEntity> findByTestUrl(String testUrl);

    List<PerformanceResultEntity> findByStatus(String status);

    List<PerformanceResultEntity> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT p FROM PerformanceResultEntity p WHERE p.avgResponseTime > :threshold")
    List<PerformanceResultEntity> findByAvgResponseTimeGreaterThan(Double threshold);

    @Query("SELECT p FROM PerformanceResultEntity p WHERE p.errorRate > :threshold")
    List<PerformanceResultEntity> findByErrorRateGreaterThan(Double threshold);

    @Query("SELECT AVG(p.tps) FROM PerformanceResultEntity p WHERE p.testUrl = :testUrl")
    Double getAverageTps(String testUrl);

    @Query("SELECT p.testUrl, AVG(p.avgResponseTime), AVG(p.tps) FROM PerformanceResultEntity p " +
           "GROUP BY p.testUrl")
    List<Object[]> getPerformanceSummaryByUrl();
}