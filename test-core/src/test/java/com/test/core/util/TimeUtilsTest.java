package com.test.core.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TimeUtils 单元测试
 */
@DisplayName("TimeUtils 测试")
class TimeUtilsTest {

    @Test
    @DisplayName("格式化日期时间")
    void testFormat() {
        LocalDateTime dateTime = LocalDateTime.of(2024, 1, 15, 10, 30, 45);

        String result = TimeUtils.format(dateTime);

        assertThat(result).isEqualTo("2024-01-15 10:30:45");
    }

    @Test
    @DisplayName("格式化紧凑格式")
    void testFormatCompact() {
        LocalDateTime dateTime = LocalDateTime.of(2024, 1, 15, 10, 30, 45);

        String result = TimeUtils.formatCompact(dateTime);

        assertThat(result).isEqualTo("20240115103045");
    }

    @Test
    @DisplayName("格式化空日期时间")
    void testFormatNull() {
        String result = TimeUtils.format(null);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("计算时间差（毫秒）")
    void testBetweenMillis() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 15, 10, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 15, 10, 0, 5);

        long millis = TimeUtils.betweenMillis(start, end);

        assertThat(millis).isEqualTo(5000);
    }

    @Test
    @DisplayName("格式化持续时间 - 毫秒")
    void testFormatDurationMillis() {
        String result = TimeUtils.formatDuration(500);

        assertThat(result).isEqualTo("500ms");
    }

    @Test
    @DisplayName("格式化持续时间 - 秒")
    void testFormatDurationSeconds() {
        String result = TimeUtils.formatDuration(5500);

        assertThat(result).isEqualTo("5.50s");
    }

    @Test
    @DisplayName("格式化持续时间 - 分钟")
    void testFormatDurationMinutes() {
        String result = TimeUtils.formatDuration(125000); // 2m 5s

        assertThat(result).isEqualTo("2m 5s");
    }

    @Test
    @DisplayName("格式化持续时间 - 小时")
    void testFormatDurationHours() {
        String result = TimeUtils.formatDuration(3725000); // 1h 2m 5s

        assertThat(result).isEqualTo("1h 2m 5s");
    }

    @Test
    @DisplayName("获取 N 天前")
    void testDaysAgo() {
        LocalDateTime result = TimeUtils.daysAgo(7);

        assertThat(result).isBefore(LocalDateTime.now());
    }

    @Test
    @DisplayName("获取今天开始时间")
    void testTodayStart() {
        LocalDateTime result = TimeUtils.todayStart();

        assertThat(result.getHour()).isEqualTo(0);
        assertThat(result.getMinute()).isEqualTo(0);
        assertThat(result.getSecond()).isEqualTo(0);
    }

    @Test
    @DisplayName("时间戳转换")
    void testTimestampConversion() {
        LocalDateTime dateTime = LocalDateTime.of(2024, 1, 15, 10, 30, 45);

        long timestamp = TimeUtils.toTimestamp(dateTime);
        LocalDateTime converted = TimeUtils.fromTimestamp(timestamp);

        assertThat(converted).isEqualTo(dateTime);
    }
}