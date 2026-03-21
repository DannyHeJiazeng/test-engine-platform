package com.test.web.repository;

import com.test.web.entity.TestResultDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 测试结果详情 Repository
 */
@Repository
public interface TestResultDetailRepository extends JpaRepository<TestResultDetailEntity, Long> {

    List<TestResultDetailEntity> findByExecutionId(String executionId);

    List<TestResultDetailEntity> findByStatus(String status);

    List<TestResultDetailEntity> findByClassName(String className);

    List<TestResultDetailEntity> findByPriority(String priority);

    @Query("SELECT d FROM TestResultDetailEntity d WHERE d.executionId = :executionId AND d.status = 'FAILURE'")
    List<TestResultDetailEntity> findFailuresByExecutionId(String executionId);

    @Query("SELECT COUNT(d) FROM TestResultDetailEntity d WHERE d.executionId = :executionId AND d.status = :status")
    long countByExecutionIdAndStatus(String executionId, String status);

    void deleteByExecutionId(String executionId);
}