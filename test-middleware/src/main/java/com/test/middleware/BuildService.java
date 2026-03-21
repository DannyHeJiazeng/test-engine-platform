package com.test.middleware;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * 构建服务
 * 使用 ProcessBuilder 调用 Maven 进行项目打包
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BuildService {

    private final ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * Maven 构建命令
     */
    private static final String MAVEN_CMD = isWindows() ? "mvn.cmd" : "mvn";

    /**
     * 执行 Maven 构建
     *
     * @param projectDir 项目目录
     * @param goals      Maven 目标（如：clean, compile, package）
     * @param options    Maven 选项（如：-DskipTests, -Pprofile）
     * @return 构建结果
     */
    public BuildResult build(String projectDir, List<String> goals, List<String> options) {
        log.info("Starting Maven build in: {}", projectDir);
        log.info("Goals: {}, Options: {}", goals, options);

        long startTime = System.currentTimeMillis();
        BuildResult result = new BuildResult();
        result.setProjectDir(projectDir);
        result.setStartTime(startTime);

        try {
            // 构建命令
            List<String> command = new ArrayList<>();
            command.add(MAVEN_CMD);
            if (options != null) {
                command.addAll(options);
            }
            if (goals != null) {
                command.addAll(goals);
            }

            log.debug("Executing command: {}", String.join(" ", command));

            // 创建 ProcessBuilder
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.directory(new File(projectDir));
            processBuilder.redirectErrorStream(true);

            // 执行进程
            Process process = processBuilder.start();

            // 读取输出
            List<String> output = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.add(line);
                    log.debug(line);
                }
            }

            // 等待完成
            int exitCode = process.waitFor();
            long endTime = System.currentTimeMillis();

            result.setSuccess(exitCode == 0);
            result.setExitCode(exitCode);
            result.setDurationMs(endTime - startTime);
            result.setEndTime(endTime);
            result.setOutput(output);

            if (exitCode == 0) {
                log.info("Build completed successfully in {}ms", result.getDurationMs());
            } else {
                log.error("Build failed with exit code: {}", exitCode);
            }

        } catch (IOException e) {
            log.error("Failed to execute Maven build", e);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
            result.setDurationMs(System.currentTimeMillis() - startTime);

        } catch (InterruptedException e) {
            log.error("Build interrupted", e);
            Thread.currentThread().interrupt();
            result.setSuccess(false);
            result.setErrorMessage("Build interrupted: " + e.getMessage());
            result.setDurationMs(System.currentTimeMillis() - startTime);
        }

        return result;
    }

    /**
     * 执行 Maven 构建（异步）
     */
    public CompletableFuture<BuildResult> buildAsync(String projectDir, List<String> goals,
                                                      List<String> options, Consumer<String> outputConsumer) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Starting async Maven build in: {}", projectDir);

            try {
                // 构建命令
                List<String> command = new ArrayList<>();
                command.add(MAVEN_CMD);
                if (options != null) {
                    command.addAll(options);
                }
                if (goals != null) {
                    command.addAll(goals);
                }

                ProcessBuilder processBuilder = new ProcessBuilder(command);
                processBuilder.directory(new File(projectDir));
                processBuilder.redirectErrorStream(true);

                Process process = processBuilder.start();

                // 读取输出并回调
                List<String> output = new ArrayList<>();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.add(line);
                        if (outputConsumer != null) {
                            outputConsumer.accept(line);
                        }
                    }
                }

                int exitCode = process.waitFor();

                BuildResult result = new BuildResult();
                result.setSuccess(exitCode == 0);
                result.setExitCode(exitCode);
                result.setOutput(output);
                return result;

            } catch (IOException e) {
                log.error("Failed to execute Maven build", e);
                BuildResult result = new BuildResult();
                result.setSuccess(false);
                result.setErrorMessage(e.getMessage());
                return result;

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Build interrupted", e);
                BuildResult result = new BuildResult();
                result.setSuccess(false);
                result.setErrorMessage("Build interrupted");
                return result;
            }
        }, executor);
    }

    /**
     * Maven 编译
     */
    public BuildResult compile(String projectDir) {
        return build(projectDir, List.of("compile"), null);
    }

    /**
     * Maven 打包（跳过测试）
     */
    public BuildResult packageSkipTests(String projectDir) {
        return build(projectDir, List.of("package"), List.of("-DskipTests"));
    }

    /**
     * Maven 打包（包含测试）
     */
    public BuildResult packageWithTests(String projectDir) {
        return build(projectDir, List.of("package"), null);
    }

    /**
     * Maven 安装
     */
    public BuildResult install(String projectDir) {
        return build(projectDir, List.of("install"), null);
    }

    /**
     * Maven 清理
     */
    public BuildResult clean(String projectDir) {
        return build(projectDir, List.of("clean"), null);
    }

    /**
     * Maven 清理并打包
     */
    public BuildResult cleanPackage(String projectDir) {
        return build(projectDir, List.of("clean", "package"), null);
    }

    /**
     * Maven 清理、打包并跳过测试
     */
    public BuildResult cleanPackageSkipTests(String projectDir) {
        return build(projectDir, List.of("clean", "package"), List.of("-DskipTests"));
    }

    /**
     * 检查 Maven 是否可用
     */
    public boolean isMavenAvailable() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(MAVEN_CMD, "--version");
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            log.error("Maven not available: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取 Maven 版本
     */
    public String getMavenVersion() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(MAVEN_CMD, "--version");
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                return reader.readLine();
            }
        } catch (Exception e) {
            return "Unknown";
        }
    }

    /**
     * 获取构建输出目录
     */
    public String getTargetDir(String projectDir) {
        return Paths.get(projectDir, "target").toString();
    }

    /**
     * 获取 JAR 文件路径
     */
    public String getJarPath(String projectDir, String artifactId, String version) {
        return Paths.get(projectDir, "target", artifactId + "-" + version + ".jar").toString();
    }

    /**
     * 检查 JAR 文件是否存在
     */
    public boolean jarExists(String projectDir, String artifactId, String version) {
        String jarPath = getJarPath(projectDir, artifactId, version);
        return Files.exists(Paths.get(jarPath));
    }

    /**
     * 构建结果
     */
    @Data
    public static class BuildResult {
        private boolean success;
        private int exitCode;
        private String projectDir;
        private String errorMessage;
        private long startTime;
        private long endTime;
        private long durationMs;
        private List<String> output = new ArrayList<>();

        public String getOutputSummary() {
            if (output.isEmpty()) {
                return "";
            }
            // 返回最后 50 行输出
            int size = output.size();
            int start = Math.max(0, size - 50);
            return String.join("\n", output.subList(start, size));
        }
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }
}
