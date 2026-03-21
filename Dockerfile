# 多阶段构建
# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# 复制 pom 文件
COPY pom.xml .
COPY test-annotation/pom.xml test-annotation/
COPY test-core/pom.xml test-core/
COPY test-performance/pom.xml test-performance/
COPY test-middleware/pom.xml test-middleware/
COPY test-monitor/pom.xml test-monitor/
COPY test-web/pom.xml test-web/
COPY test-examples/pom.xml test-examples/

# 下载依赖
RUN mvn dependency:go-offline -B

# 复制源代码
COPY test-annotation/ test-annotation/
COPY test-core/ test-core/
COPY test-performance/ test-performance/
COPY test-middleware/ test-middleware/
COPY test-monitor/ test-monitor/
COPY test-web/ test-web/
COPY test-examples/ test-examples/

# 构建
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:17-jre

WORKDIR /app

# 创建非 root 用户
RUN groupadd -r testengine && useradd -r -g testengine testengine

# 创建必要的目录
RUN mkdir -p /app/logs /app/test-workspace /app/test-results && \
    chown -R testengine:testengine /app

# 从构建阶段复制 jar 文件
COPY --from=builder /build/test-web/target/test-web-*.jar app.jar

# 切换到非 root 用户
USER testengine

# 暴露端口
EXPOSE 8080

# JVM 参数
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1