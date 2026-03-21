package com.test.core;

import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 自定义类加载器
 * 用于隔离测试用例的依赖，避免与中台依赖冲突
 */
@Slf4j
public class TestClassLoader extends URLClassLoader {

    private final List<String> parentFirstPackages;

    /**
     * 构造函数
     *
     * @param urls      URL 数组
     * @param parent    父类加载器
     */
    public TestClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
        this.parentFirstPackages = new ArrayList<>();
        // 默认某些包由父类加载器加载
        parentFirstPackages.add("java.");
        parentFirstPackages.add("javax.");
        parentFirstPackages.add("sun.");
        parentFirstPackages.add("com.test.annotation.");
    }

    /**
     * 构造函数 - 从目录路径创建
     *
     * @param classPaths 类路径列表
     * @param parent     父类加载器
     */
    public TestClassLoader(List<String> classPaths, ClassLoader parent) {
        super(toUrls(classPaths), parent);
        this.parentFirstPackages = new ArrayList<>();
        parentFirstPackages.add("java.");
        parentFirstPackages.add("javax.");
        parentFirstPackages.add("sun.");
        parentFirstPackages.add("com.test.annotation.");
    }

    /**
     * 构造函数 - 从单个目录创建
     *
     * @param directory 类目录
     * @param parent    父类加载器
     */
    public TestClassLoader(String directory, ClassLoader parent) {
        super(new URL[]{pathToUrl(directory)}, parent);
        this.parentFirstPackages = new ArrayList<>();
        parentFirstPackages.add("java.");
        parentFirstPackages.add("javax.");
        parentFirstPackages.add("sun.");
        parentFirstPackages.add("com.test.annotation.");
    }

    /**
     * 添加父类优先的包
     */
    public void addParentFirstPackage(String pkg) {
        if (!parentFirstPackages.contains(pkg)) {
            parentFirstPackages.add(pkg);
        }
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            // 首先检查是否已经加载过
            Class<?> loadedClass = findLoadedClass(name);
            if (loadedClass != null) {
                return loadedClass;
            }

            // 检查是否属于父类优先的包
            boolean parentFirst = isParentFirstPackage(name);

            if (parentFirst) {
                // 父类优先加载
                try {
                    loadedClass = getParent().loadClass(name);
                } catch (ClassNotFoundException e) {
                    // 父类无法加载，尝试自己加载
                    loadedClass = findClass(name);
                }
            } else {
                // 自己优先加载
                try {
                    loadedClass = findClass(name);
                } catch (ClassNotFoundException e) {
                    // 自己无法加载，委托给父类
                    loadedClass = getParent().loadClass(name);
                }
            }

            if (resolve) {
                resolveClass(loadedClass);
            }

            return loadedClass;
        }
    }

    /**
     * 判断是否属于父类优先的包
     */
    private boolean isParentFirstPackage(String className) {
        for (String pkg : parentFirstPackages) {
            if (className.startsWith(pkg)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 关闭类加载器，释放资源
     */
    public void close() {
        try {
            super.close();
            log.debug("TestClassLoader closed");
        } catch (Exception e) {
            log.error("Failed to close TestClassLoader", e);
        }
    }

    /**
     * 将路径列表转换为 URL 数组
     */
    private static URL[] toUrls(List<String> classPaths) {
        List<URL> urls = new ArrayList<>();
        for (String path : classPaths) {
            urls.add(pathToUrl(path));
        }
        return urls.toArray(new URL[0]);
    }

    /**
     * 将字符串路径转换为 URL
     */
    private static URL pathToUrl(String path) {
        try {
            Path absolutePath = Paths.get(path).toAbsolutePath();
            return absolutePath.toUri().toURL();
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert path to URL: " + path, e);
        }
    }

    /**
     * 创建测试类加载器
     */
    public static TestClassLoader createForTestClass(String classpath) {
        return new TestClassLoader(classpath, TestClassLoader.class.getClassLoader().getParent());
    }

    /**
     * 创建测试类加载器
     */
    public static TestClassLoader createForTestClasses(List<String> classpaths) {
        return new TestClassLoader(classpaths, TestClassLoader.class.getClassLoader().getParent());
    }
}
