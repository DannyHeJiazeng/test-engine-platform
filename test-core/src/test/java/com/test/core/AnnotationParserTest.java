package com.test.core;

import com.test.annotation.TestType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AnnotationParser 单元测试
 */
@DisplayName("AnnotationParser 测试")
class AnnotationParserTest {

    private AnnotationParser parser;

    @BeforeEach
    void setUp() {
        parser = new AnnotationParser();
    }

    @Test
    @DisplayName("解析功能测试注解")
    void testParseFunctionalTestAnnotation() throws NoSuchMethodException {
        // Given
        Method method = TestClass.class.getMethod("functionalTestMethod");

        // When
        TestType type = parser.parseType(method);
        String group = parser.parseGroup(method);
        String description = parser.parseDescription(method);

        // Then
        assertThat(type).isEqualTo(TestType.FUNCTIONAL);
        assertThat(group).isEqualTo("functional");
        assertThat(description).isEqualTo("功能测试描述");
    }

    @Test
    @DisplayName("解析性能测试注解")
    void testParsePerformanceTestAnnotation() throws NoSuchMethodException {
        // Given
        Method method = TestClass.class.getMethod("performanceTestMethod");

        // When
        TestType type = parser.parseType(method);
        String group = parser.parseGroup(method);

        // Then
        assertThat(type).isEqualTo(TestType.PERFORMANCE);
        assertThat(group).isEqualTo("performance");
    }

    @Test
    @DisplayName("解析巡检注解")
    void testParseInspectionAnnotation() throws NoSuchMethodException {
        // Given
        Method method = TestClass.class.getMethod("inspectionTestMethod");

        // When
        TestType type = parser.parseType(method);
        String group = parser.parseGroup(method);

        // Then
        assertThat(type).isEqualTo(TestType.INSPECTION);
        assertThat(group).isEqualTo("inspection");
    }

    @Test
    @DisplayName("解析无注解方法")
    void testParseNoAnnotation() throws NoSuchMethodException {
        // Given
        Method method = TestClass.class.getMethod("noAnnotationMethod");

        // When
        TestType type = parser.parseType(method);
        String group = parser.parseGroup(method);

        // Then
        assertThat(type).isEqualTo(TestType.UNKNOWN);
        assertThat(group).isEqualTo("default");
    }

    @Test
    @DisplayName("检查是否有测试注解")
    void testHasTestAnnotation() throws NoSuchMethodException {
        Method functionalMethod = TestClass.class.getMethod("functionalTestMethod");
        Method noAnnotationMethod = TestClass.class.getMethod("noAnnotationMethod");

        assertThat(parser.hasTestAnnotation(functionalMethod)).isTrue();
        assertThat(parser.hasTestAnnotation(noAnnotationMethod)).isFalse();
    }

    @Test
    @DisplayName("解析测试套件注解")
    void testParseTestSuite() {
        // When
        var info = parser.parseTestSuite(TestClass.class);

        // Then
        assertThat(info).isPresent();
        assertThat(info.get().getName()).isEqualTo("测试套件名称");
        assertThat(info.get().getCategory()).isEqualTo("FUNCTIONAL");
    }

    @Test
    @DisplayName("解析性能测试配置")
    void testParsePerformanceConfig() throws NoSuchMethodException {
        // Given
        Method method = TestClass.class.getMethod("performanceTestMethod");

        // When
        var config = parser.parsePerformanceConfig(method);

        // Then
        assertThat(config).isPresent();
        assertThat(config.get().getMaxDurationMs()).isEqualTo(3000);
        assertThat(config.get().getConcurrency()).isEqualTo(5);
    }

    @Test
    @DisplayName("解析巡检配置")
    void testParseInspectionConfig() throws NoSuchMethodException {
        // Given
        Method method = TestClass.class.getMethod("inspectionTestMethod");

        // When
        var config = parser.parseInspectionConfig(method);

        // Then
        assertThat(config).isPresent();
        assertThat(config.get().getInterval()).isEqualTo("10m");
        assertThat(config.get().isAlertOnFailure()).isTrue();
    }

    // 测试类
    @com.test.annotation.TestSuite(name = "测试套件名称", category = com.test.annotation.TestSuite.TestCategory.FUNCTIONAL)
    static class TestClass {

        @com.test.annotation.FunctionalTest(description = "功能测试描述", priority = "P0")
        public void functionalTestMethod() {}

        @com.test.annotation.PerformanceTest(description = "性能测试", maxDurationMs = 3000, concurrency = 5)
        public void performanceTestMethod() {}

        @com.test.annotation.Inspection(description = "巡检测试", interval = "10m", alertOnFailure = true)
        public void inspectionTestMethod() {}

        public void noAnnotationMethod() {}
    }
}