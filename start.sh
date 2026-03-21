#!/bin/bash

# Test Engine Platform - 启动脚本

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 打印带颜色的消息
print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查依赖
check_dependencies() {
    print_info "检查依赖..."

    # 检查 Java
    if ! command -v java &> /dev/null; then
        print_error "Java 未安装，请安装 JDK 17+"
        exit 1
    fi

    # 检查 Maven
    if ! command -v mvn &> /dev/null; then
        print_error "Maven 未安装，请安装 Maven 3.8+"
        exit 1
    fi

    # 检查 Docker
    if ! command -v docker &> /dev/null; then
        print_warn "Docker 未安装，将使用本地模式"
        return 1
    fi

    return 0
}

# 构建项目
build_project() {
    print_info "构建项目..."
    mvn clean package -DskipTests -B

    if [ $? -ne 0 ]; then
        print_error "构建失败"
        exit 1
    fi

    print_info "构建成功"
}

# 创建必要的目录
create_directories() {
    print_info "创建必要的目录..."

    mkdir -p test-workspace
    mkdir -p test-results
    mkdir -p logs
    mkdir -p jmx-files
    mkdir -p prometheus
}

# 启动 Docker 服务
start_docker() {
    print_info "启动 Docker 服务..."

    if [ -f "docker-compose.yml" ]; then
        docker-compose up -d
    else
        print_warn "docker-compose.yml 不存在，跳过 Docker 启动"
    fi
}

# 启动应用
start_application() {
    print_info "启动应用..."

    if command -v java &> /dev/null && [ -f "test-web/target/test-web-*.jar" ]; then
        java -jar test-web/target/test-web-*.jar &
        APP_PID=$!

        print_info "应用已启动，PID: $APP_PID"
        print_info "访问地址: http://localhost:8080"

        # 保存 PID
        echo $APP_PID > .app.pid

        # 等待应用启动
        sleep 5

        # 检查应用状态
        if curl -f http://localhost:8080/actuator/health &> /dev/null; then
            print_info "应用启动成功"
        else
            print_warn "应用可能未完全启动，请检查日志"
        fi
    else
        print_error "找不到应用 JAR 文件"
        exit 1
    fi
}

# 停止应用
stop_application() {
    print_info "停止应用..."

    if [ -f ".app.pid" ]; then
        PID=$(cat .app.pid)
        kill $PID 2>/dev/null || true
        rm .app.pid
        print_info "应用已停止"
    else
        print_warn "找不到应用 PID 文件"
    fi
}

# 查看日志
view_logs() {
    print_info "查看日志..."
    tail -f logs/test-engine.log
}

# 清理
cleanup() {
    print_info "清理..."

    # 停止 Docker 容器
    if [ -f "docker-compose.yml" ]; then
        docker-compose down
    fi

    # 清理临时文件
    rm -f .app.pid

    # 保留用户数据
    print_warn "保留 test-workspace 和 test-results 目录"
}

# 显示帮助信息
show_help() {
    echo "Test Engine Platform - 启动脚本"
    echo ""
    echo "用法: $0 [命令]"
    echo ""
    echo "命令:"
    echo "  build     构建项目"
    echo "  start     启动应用"
    echo "  stop      停止应用"
    echo "  restart   重启应用"
    echo "  logs      查看日志"
    echo "  status    查看状态"
    echo "  clean     清理资源"
    echo "  help      显示帮助信息"
    echo ""
    echo "示例:"
    echo "  $0 build          # 构建项目"
    echo "  $0 start          # 启动应用"
    echo "  $0 logs           # 查看日志"
}

# 查看状态
show_status() {
    print_info "应用状态:"

    # 检查应用进程
    if [ -f ".app.pid" ]; then
        PID=$(cat .app.pid)
        if ps -p $PID > /dev/null 2>&1; then
            echo "  应用运行中 (PID: $PID)"
        else
            echo "  应用未运行"
        fi
    else
        echo "  应用未运行"
    fi

    # 检查 Docker 容器
    if command -v docker &> /dev/null; then
        echo ""
        echo "Docker 容器状态:"
        docker ps --filter "name=test-engine" --format "  {{.Names}}: {{.Status}}"
    fi
}

# 主函数
main() {
    case "${1:-start}" in
        build)
            check_dependencies
            build_project
            ;;
        start)
            check_dependencies || true
            create_directories
            build_project
            start_docker || true
            start_application
            ;;
        stop)
            stop_application
            ;;
        restart)
            stop_application
            sleep 2
            start_application
            ;;
        logs)
            view_logs
            ;;
        status)
            show_status
            ;;
        clean)
            cleanup
            ;;
        help|--help|-h)
            show_help
            ;;
        *)
            print_error "未知命令: $1"
            show_help
            exit 1
            ;;
    esac
}

# 运行主函数
main "$@"