package com.test.core.util;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 文件工具类
 */
public final class FileUtils {

    private FileUtils() {}

    /**
     * 确保目录存在
     */
    public static Path ensureDirectory(String dirPath) throws IOException {
        Path path = Paths.get(dirPath);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
        return path;
    }

    /**
     * 确保目录存在
     */
    public static Path ensureDirectory(Path path) throws IOException {
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
        return path;
    }

    /**
     * 删除目录及其内容
     */
    public static void deleteDirectory(Path path) throws IOException {
        if (!Files.exists(path)) {
            return;
        }

        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * 列出目录下的所有文件
     */
    public static List<Path> listFiles(Path dir, String extension) throws IOException {
        if (!Files.exists(dir)) {
            return new ArrayList<>();
        }

        try (Stream<Path> stream = Files.list(dir)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(p -> extension == null || p.toString().endsWith(extension))
                    .collect(Collectors.toList());
        }
    }

    /**
     * 递归列出目录下的所有文件
     */
    public static List<Path> listFilesRecursively(Path dir, String extension) throws IOException {
        List<Path> result = new ArrayList<>();
        if (!Files.exists(dir)) {
            return result;
        }

        Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (extension == null || file.toString().endsWith(extension)) {
                    result.add(file);
                }
                return FileVisitResult.CONTINUE;
            }
        });

        return result;
    }

    /**
     * 读取文件内容为字符串
     */
    public static String readFileToString(Path path) throws IOException {
        return Files.readString(path);
    }

    /**
     * 读取文件内容为行列表
     */
    public static List<String> readLines(Path path) throws IOException {
        return Files.readAllLines(path);
    }

    /**
     * 写入字符串到文件
     */
    public static void writeStringToFile(Path path, String content) throws IOException {
        ensureDirectory(path.getParent());
        Files.writeString(path, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    /**
     * 追加内容到文件
     */
    public static void appendToFile(Path path, String content) throws IOException {
        ensureDirectory(path.getParent());
        Files.writeString(path, content, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    /**
     * 复制文件
     */
    public static void copyFile(Path source, Path target) throws IOException {
        ensureDirectory(target.getParent());
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * 移动文件
     */
    public static void moveFile(Path source, Path target) throws IOException {
        ensureDirectory(target.getParent());
        Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * 获取文件扩展名
     */
    public static String getExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex > 0 ? filename.substring(dotIndex + 1) : "";
    }

    /**
     * 获取不带扩展名的文件名
     */
    public static String getBaseName(String filename) {
        if (filename == null) {
            return "";
        }
        int dotIndex = filename.lastIndexOf('.');
        int sepIndex = filename.lastIndexOf('/');
        sepIndex = Math.max(sepIndex, filename.lastIndexOf('\\'));
        int start = sepIndex > 0 ? sepIndex + 1 : 0;
        return dotIndex > start ? filename.substring(start, dotIndex) : filename.substring(start);
    }

    /**
     * 获取人类可读的文件大小
     */
    public static String humanReadableSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    /**
     * 创建临时目录
     */
    public static Path createTempDirectory(String prefix) throws IOException {
        return Files.createTempDirectory(prefix);
    }

    /**
     * 创建临时文件
     */
    public static Path createTempFile(String prefix, String suffix) throws IOException {
        return Files.createTempFile(prefix, suffix);
    }
}