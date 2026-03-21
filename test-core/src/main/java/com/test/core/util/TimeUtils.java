package com.test.core.util;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * 时间工具类
 */
public final class TimeUtils {

    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter COMPACT_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private TimeUtils() {}

    /**
     * 格式化日期时间
     */
    public static String format(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DEFAULT_FORMATTER) : "";
    }

    /**
     * 格式化日期时间（自定义格式）
     */
    public static String format(LocalDateTime dateTime, String pattern) {
        return dateTime != null ? dateTime.format(DateTimeFormatter.ofPattern(pattern)) : "";
    }

    /**
     * 格式化为紧凑格式
     */
    public static String formatCompact(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(COMPACT_FORMATTER) : "";
    }

    /**
     * 格式化为 ISO 格式
     */
    public static String formatIso(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(ISO_FORMATTER) : "";
    }

    /**
     * 解析日期时间
     */
    public static LocalDateTime parse(String dateTimeStr) {
        return LocalDateTime.parse(dateTimeStr, DEFAULT_FORMATTER);
    }

    /**
     * 解析日期时间（自定义格式）
     */
    public static LocalDateTime parse(String dateTimeStr, String pattern) {
        return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 当前时间
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    /**
     * 当前时间戳（毫秒）
     */
    public static long currentTimestamp() {
        return System.currentTimeMillis();
    }

    /**
     * 当前时间戳（秒）
     */
    public static long currentTimestampSeconds() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * 时间戳转 LocalDateTime
     */
    public static LocalDateTime fromTimestamp(long timestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    }

    /**
     * LocalDateTime 转时间戳
     */
    public static long toTimestamp(LocalDateTime dateTime) {
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * 计算两个时间之间的毫秒数
     */
    public static long betweenMillis(LocalDateTime start, LocalDateTime end) {
        return Duration.between(start, end).toMillis();
    }

    /**
     * 计算两个时间之间的秒数
     */
    public static long betweenSeconds(LocalDateTime start, LocalDateTime end) {
        return Duration.between(start, end).getSeconds();
    }

    /**
     * 格式化持续时间（人类可读）
     */
    public static String formatDuration(long millis) {
        if (millis < 1000) {
            return millis + "ms";
        }
        if (millis < 60000) {
            return String.format("%.2fs", millis / 1000.0);
        }

        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;

        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, seconds);
        }
        return String.format("%dm %ds", minutes, seconds);
    }

    /**
     * 格式化持续时间（简洁格式）
     */
    public static String formatDurationShort(long millis) {
        if (millis < 1000) {
            return millis + "ms";
        }
        if (millis < 60000) {
            return String.format("%.1fs", millis / 1000.0);
        }
        if (millis < 3600000) {
            return String.format("%.1fm", millis / 60000.0);
        }
        return String.format("%.1fh", millis / 3600000.0);
    }

    /**
     * 今天开始时间
     */
    public static LocalDateTime todayStart() {
        return LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
    }

    /**
     * 今天结束时间
     */
    public static LocalDateTime todayEnd() {
        return LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999999999);
    }

    /**
     * N 天前
     */
    public static LocalDateTime daysAgo(int days) {
        return LocalDateTime.now().minusDays(days);
    }

    /**
     * N 天后
     */
    public static LocalDateTime daysLater(int days) {
        return LocalDateTime.now().plusDays(days);
    }

    /**
     * N 小时前
     */
    public static LocalDateTime hoursAgo(int hours) {
        return LocalDateTime.now().minusHours(hours);
    }

    /**
     * N 小时后
     */
    public static LocalDateTime hoursLater(int hours) {
        return LocalDateTime.now().plusHours(hours);
    }
}