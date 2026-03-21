package com.test.web.repository;

import com.test.web.entity.TestExecutionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 测试执行记录 Repository
 */
@Repository
public interface TestExecutionRepository extends JpaRepository<TestExecutionEntity, Long> {

    Optional<TestExecutionEntity> findByExecutionId(String executionId);

    List<TestExecutionEntity> findByStatus(String status);

    List<TestExecutionEntity> findByTestType(String testType);

    Page<TestExecutionEntity> findByTestType(String testType, Pageable pageable);

    List<TestExecutionEntity> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT e FROM TestExecutionEntity e WHERE e.passRate >= :minPassRate")
    List<TestExecutionEntity> findByPassRateGreaterThanEqual(Double minPassRate);

    @Query("SELECT COUNT(e) FROM TestExecutionEntity e WHERE e.status = :status")
    long countByStatus(String status);

    @Query("SELECT AVG(e.passRate) FROM TestExecutionEntity e WHERE e.testType = :testType")
    Double getAveragePassRate(String testType);

    @Query("SELECT e.testType, COUNT(e) FROM TestExecutionEntity e GROUP BY e.testType")
    List<Object[]> countByTestType();

    @Query("SELECT DATE(e.createdAt), COUNT(e) FROM TestExecutionEntity e " +
           "WHERE e.createdAt BETWEEN :start AND :end GROUP BY DATE(e.createdAt)")
    List<Object[]> countByDate(LocalDateTime start, LocalDateTime end);

    void deleteByExecutionId(String executionId);

    boolean existsByExecutionId(String executionId);
}