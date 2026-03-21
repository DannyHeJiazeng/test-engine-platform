package com.test.core.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * IdGenerator 单元测试
 */
@DisplayName("IdGenerator 测试")
class IdGeneratorTest {

    @Test
    @DisplayName("生成执行 ID")
    void testGenerateExecutionId() {
        String id = IdGenerator.generateExecutionId();

        assertThat(id).startsWith("EXEC-");
        assertThat(id.length()).isGreaterThan(10);
    }

    @Test
    @DisplayName("生成测试 ID")
    void testGenerateTestId() {
        String id = IdGenerator.generateTestId();

        assertThat(id).startsWith("TEST-");
    }

    @Test
    @DisplayName("生成性能测试 ID")
    void testGeneratePerformanceId() {
        String id = IdGenerator.generatePerformanceId();

        assertThat(id).startsWith("PERF-");
    }

    @Test
    @DisplayName("生成 UUID")
    void testGenerateUuid() {
        String uuid = IdGenerator.uuid();

        assertThat(uuid).hasSize(32);
        assertThat(uuid).doesNotContain("-");
    }

    @Test
    @DisplayName("生成短 UUID")
    void testGenerateShortUuid() {
        String uuid = IdGenerator.shortUuid();

        assertThat(uuid).hasSize(8);
    }

    @Test
    @DisplayName("生成唯一 ID")
    void testGenerateUniqueIds() {
        String id1 = IdGenerator.generateExecutionId();
        String id2 = IdGenerator.generateExecutionId();

        assertThat(id1).isNotEqualTo(id2);
    }
}