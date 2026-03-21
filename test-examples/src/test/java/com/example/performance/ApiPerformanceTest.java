package com.example.performance;

import com.test.annotation.PerformanceTest;
import com.test.annotation.TestSuite;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * API 性能测试示例
 */
@TestSuite(name = "API性能测试套件", category = TestSuite.TestCategory.PERFORMANCE)
public class ApiPerformanceTest {

    @PerformanceTest(description = "用户列表API响应时间测试", maxDurationMs = 1000, concurrency = 1)
    @Test(groups = "performance")
    public void testUserListResponseTime() {
        // 模拟 API 调用
        long startTime = System.currentTimeMillis();

        // 模拟 HTTP 请求
        String response = simulateApiCall("/api/users", "GET");

        long duration = System.currentTimeMillis() - startTime;

        Assert.assertNotNull(response, "响应不应为空");
        Assert.assertTrue(duration < 1000, "响应时间应小于1000ms，实际: " + duration + "ms");
    }

    @PerformanceTest(description = "用户创建API并发测试", maxDurationMs = 2000, concurrency = 10, durationSeconds = 30)
    @Test(groups = "performance")
    public void testCreateUserConcurrency() {
        // 模拟并发创建用户
        long startTime = System.currentTimeMillis();

        String response = simulateApiCall("/api/users", "POST");

        long duration = System.currentTimeMillis() - startTime;

        Assert.assertNotNull(response, "响应不应为空");
        Assert.assertTrue(duration < 2000, "响应时间应小于2000ms");
    }

    @PerformanceTest(description = "登录API性能测试", maxDurationMs = 500, concurrency = 5)
    @Test(groups = "performance")
    public void testLoginPerformance() {
        // 模拟登录请求
        long startTime = System.currentTimeMillis();

        boolean loginSuccess = simulateLogin("testuser", "password123");

        long duration = System.currentTimeMillis() - startTime;

        Assert.assertTrue(loginSuccess, "登录应该成功");
        Assert.assertTrue(duration < 500, "登录响应时间应小于500ms");
    }

    @PerformanceTest(description = "数据库查询性能测试", maxDurationMs = 3000, concurrency = 1)
    @Test(groups = "performance")
    public void testDatabaseQueryPerformance() {
        // 模拟复杂查询
        long startTime = System.currentTimeMillis();

        int resultCount = simulateDatabaseQuery("SELECT * FROM users WHERE status = 'active'");

        long duration = System.currentTimeMillis() - startTime;

        Assert.assertTrue(resultCount >= 0, "结果数量应非负");
        Assert.assertTrue(duration < 3000, "查询时间应小于3000ms");
    }

    @PerformanceTest(description = "批量导入性能测试", maxDurationMs = 10000, concurrency = 1)
    @Test(groups = "performance")
    public void testBatchImportPerformance() {
        // 模拟批量导入
        long startTime = System.currentTimeMillis();

        int importedCount = simulateBatchImport(1000);

        long duration = System.currentTimeMillis() - startTime;

        Assert.assertEquals(importedCount, 1000, "应导入1000条记录");
        Assert.assertTrue(duration < 10000, "导入时间应小于10秒");
    }

    @PerformanceTest(description = "文件上传性能测试", maxDurationMs = 5000, concurrency = 1)
    @Test(groups = "performance")
    public void testFileUploadPerformance() {
        // 模拟文件上传
        long startTime = System.currentTimeMillis();

        boolean uploadSuccess = simulateFileUpload("test-file.pdf", 1024 * 1024); // 1MB

        long duration = System.currentTimeMillis() - startTime;

        Assert.assertTrue(uploadSuccess, "文件上传应该成功");
        Assert.assertTrue(duration < 5000, "上传时间应小于5秒");
    }

    // 模拟方法
    private String simulateApiCall(String endpoint, String method) {
        try {
            // 模拟网络延迟
            Thread.sleep((long) (Math.random() * 200));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "{\"status\": \"ok\", \"data\": []}";
    }

    private boolean simulateLogin(String username, String password) {
        try {
            Thread.sleep((long) (Math.random() * 100));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return true;
    }

    private int simulateDatabaseQuery(String query) {
        try {
            Thread.sleep((long) (Math.random() * 500));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return 100;
    }

    private int simulateBatchImport(int count) {
        try {
            Thread.sleep(count);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return count;
    }

    private boolean simulateFileUpload(String filename, int size) {
        try {
            Thread.sleep(size / 1024);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return true;
    }
}