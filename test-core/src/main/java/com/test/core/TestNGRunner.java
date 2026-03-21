package com.test.core;

import com.test.annotation.TestType;
import lombok.extern.slf4j.Slf4j;
import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * TestNG 执行器
 * 基于反射机制动态加载并执行 TestNG 测试用例
 */
@Slf4j
public class TestNGRunner {

    private final AnnotationParser annotationParser;
    private TestResultListener resultListener;

    /**
     * 默认构造函数
     */
    public TestNGRunner() {
        this.annotationParser = new AnnotationParser();
    }

    /**
     * 构造函数 - 指定测试输出目录
     */
    public TestNGRunner(String outputDir) {
        this.annotationParser = new AnnotationParser();
    }

    /**
     * 执行单个测试方法
     *
     * @param testClass  测试类
     * @param testMethod 测试方法
     * @return 测试结果
     */
    public TestResult runTest(Class<?> testClass, java.lang.reflect.Method testMethod) {
        log.info("Running test: {}.{}", testClass.getName(), testMethod.getName());

        try {
            // 解析注解获取测试类型和 group
            TestType testType = annotationParser.parseType(testMethod);
            String group = annotationParser.parseGroup(testMethod);

            // 创建新的 TestNG 实例
            TestNG testNG = new TestNG();
            testNG.setUseDefaultListeners(false);

            // 配置 TestNG
            testNG.setTestClasses(new Class[]{testClass});
            testNG.setGroups(group);

            // 创建并添加结果监听器
            resultListener = new TestResultListener();
            testNG.addListener(resultListener);

            // 执行测试
            testNG.run();

            // 获取结果
            List<TestResult> results = resultListener.getResults();
            if (results.isEmpty()) {
                return TestResult.error(
                        testClass.getName(),
                        testMethod.getName(),
                        new RuntimeException("No test results generated"),
                        0
                );
            }

            return results.get(0);

        } catch (Exception e) {
            log.error("Failed to run test", e);
            return TestResult.error(
                    testClass.getName(),
                    testMethod.getName(),
                    e,
                    0
            );
        }
    }

    /**
     * 执行测试类中的所有测试方法
     *
     * @param testClass 测试类
     * @return 测试结果列表
     */
    public List<TestResult> runTestClass(Class<?> testClass) {
        log.info("Running all tests in class: {}", testClass.getName());

        try {
            // 创建新的 TestNG 实例
            TestNG testNG = new TestNG();
            testNG.setUseDefaultListeners(false);

            // 配置 TestNG
            testNG.setTestClasses(new Class[]{testClass});

            // 创建并添加结果监听器
            resultListener = new TestResultListener();
            testNG.addListener(resultListener);

            // 执行测试
            testNG.run();

            return resultListener.getResults();

        } catch (Exception e) {
            log.error("Failed to run test class", e);
            return Collections.singletonList(
                    TestResult.error(
                            testClass.getName(),
                            "*",
                            e,
                            0
                    )
            );
        }
    }

    /**
     * 批量执行测试套件
     *
     * @param packagePath 包路径
     * @param groups      TestNG groups
     * @return 测试结果列表
     */
    public List<TestResult> runSuite(String packagePath, String[] groups) {
        log.info("Running test suite: package={}, groups={}", packagePath, java.util.Arrays.toString(groups));

        try {
            // 扫描测试类
            List<Class<?>> testClasses = scanClasses(packagePath);
            if (testClasses.isEmpty()) {
                log.warn("No test classes found in package: {}", packagePath);
                return Collections.emptyList();
            }

            // 创建新的 TestNG 实例
            TestNG testNG = new TestNG();
            testNG.setUseDefaultListeners(false);

            // 配置 TestNG 使用 XML 方式以获得更好的控制
            XmlSuite suite = new XmlSuite();
            suite.setName("Test Suite");

            XmlTest xmlTest = new XmlTest(suite);
            xmlTest.setName("Test Execution");

            if (groups != null && groups.length > 0) {
                xmlTest.setIncludedGroups(java.util.Arrays.asList(groups));
            }

            // 设置并发
            xmlTest.setParallel(XmlSuite.ParallelMode.METHODS);
            xmlTest.setThreadCount(10);

            // 添加测试类
            List<XmlClass> xmlClasses = new ArrayList<>();
            for (Class<?> testClass : testClasses) {
                xmlClasses.add(new XmlClass(testClass));
            }
            xmlTest.setXmlClasses(xmlClasses);

            List<XmlSuite> suites = new ArrayList<>();
            suites.add(suite);
            testNG.setXmlSuites(suites);

            // 创建并添加结果监听器
            resultListener = new TestResultListener();
            testNG.addListener(resultListener);

            // 执行测试
            testNG.run();

            return resultListener.getResults();

        } catch (Exception e) {
            log.error("Failed to run test suite", e);
            return Collections.emptyList();
        }
    }

    /**
     * 按类型执行测试
     *
     * @param packagePath 包路径
     * @param testType    测试类型
     * @return 测试结果列表
     */
    public List<TestResult> runByType(String packagePath, TestType testType) {
        return runSuite(packagePath, new String[]{testType.getGroupName()});
    }

    /**
     * 执行功能测试
     */
    public List<TestResult> runFunctionalTests(String packagePath) {
        return runByType(packagePath, TestType.FUNCTIONAL);
    }

    /**
     * 执行性能测试
     */
    public List<TestResult> runPerformanceTests(String packagePath) {
        return runByType(packagePath, TestType.PERFORMANCE);
    }

    /**
     * 执行巡检测试
     */
    public List<TestResult> runInspectionTests(String packagePath) {
        return runByType(packagePath, TestType.INSPECTION);
    }

    /**
     * 从 XML 配置文件执行测试
     *
     * @param xmlFile XML 配置文件
     * @return 测试结果列表
     */
    public List<TestResult> runFromXml(String xmlFile) {
        log.info("Running tests from XML: {}", xmlFile);

        try {
            TestNG testNG = new TestNG();
            testNG.setTestSuites(Collections.singletonList(xmlFile));
            testNG.setUseDefaultListeners(false);

            resultListener = new TestResultListener();
            testNG.addListener(resultListener);

            testNG.run();

            return resultListener.getResults();

        } catch (Exception e) {
            log.error("Failed to run tests from XML", e);
            return Collections.emptyList();
        }
    }

    /**
     * 扫描指定包路径下的所有测试类
     */
    private List<Class<?>> scanClasses(String packagePath) {
        try {
            org.reflections.Reflections reflections = new org.reflections.Reflections(packagePath);
            return new ArrayList<>(reflections.getTypesAnnotatedWith(org.testng.annotations.Test.class));
        } catch (Exception e) {
            log.error("Failed to scan classes in package: {}", packagePath, e);
            return Collections.emptyList();
        }
    }

    /**
     * 动态加载并扫描测试类
     */
    public List<Class<?>> scanClassesWithClassLoader(TestClassLoader classLoader, String packagePath) {
        try {
            org.reflections.Reflections reflections = new org.reflections.Reflections(
                    packagePath,
                    classLoader
            );
            return new ArrayList<>(reflections.getTypesAnnotatedWith(org.testng.annotations.Test.class));
        } catch (Exception e) {
            log.error("Failed to scan classes with custom classloader", e);
            return Collections.emptyList();
        }
    }

    /**
     * 配置 TestNG 执行器
     */
    public void configure(Runnable config) {
        config.run();
    }

    /**
     * 获取注解解析器
     */
    public AnnotationParser getAnnotationParser() {
        return annotationParser;
    }

    /**
     * 执行测试并生成统计报告
     */
    public TestReport executeAndGenerateReport(String packagePath, String[] groups) {
        List<TestResult> results = runSuite(packagePath, groups);
        return generateReport(results);
    }

    /**
     * 生成测试报告
     */
    private TestReport generateReport(List<TestResult> results) {
        int total = results.size();
        int success = (int) results.stream()
                .filter(r -> r.getStatus() == TestResult.TestStatus.SUCCESS)
                .count();
        int failure = (int) results.stream()
                .filter(r -> r.getStatus() == TestResult.TestStatus.FAILURE)
                .count();

        return TestReport.builder()
                .totalTests(total)
                .passed(success)
                .failed(failure)
                .skipped(total - success - failure)
                .passRate(total > 0 ? (double) success / total : 0.0)
                .results(results)
                .build();
    }

    /**
     * 测试报告
     */
    @lombok.Data
    @lombok.Builder
    public static class TestReport {
        private int totalTests;
        private int passed;
        private int failed;
        private int skipped;
        private double passRate;
        private List<TestResult> results;

        public String getSummary() {
            return String.format(
                    "Test Report: Total=%d, Passed=%d, Failed=%d, Skipped=%d, PassRate=%.2f%%",
                    totalTests, passed, failed, skipped, passRate * 100
            );
        }
    }
}