@echo off
REM Test Engine Platform - 启动脚本 (Windows)

setlocal enabledelayedexpansion

:: 颜色定义
set "GREEN=[INFO]"
set "YELLOW=[WARN]"
set "RED=[ERROR]"

:: 打印消息
:print_info
echo %GREEN% %~1
goto :eof

:print_warn
echo %YELLOW% %~1
goto :eof

:print_error
echo %RED% %~1
goto :eof

:: 检查依赖
:check_dependencies
call :print_info "检查依赖..."

:: 检查 Java
where java >nul 2>&1
if %errorlevel% neq 0 (
    call :print_error "Java 未安装，请安装 JDK 17+"
    exit /b 1
)

:: 检查 Maven
where mvn >nul 2>&1
if %errorlevel% neq 0 (
    call :print_error "Maven 未安装，请安装 Maven 3.8+"
    exit /b 1
)

exit /b 0

:: 构建项目
:build_project
call :print_info "构建项目..."
call mvn clean package -DskipTests -B

if %errorlevel% neq 0 (
    call :print_error "构建失败"
    exit /b 1
)

call :print_info "构建成功"
exit /b 0

:: 创建必要的目录
:create_directories
call :print_info "创建必要的目录..."

if not exist "test-workspace" mkdir test-workspace
if not exist "test-results" mkdir test-results
if not exist "logs" mkdir logs
if not exist "jmx-files" mkdir jmx-files
if not exist "prometheus" mkdir prometheus
exit /b 0

:: 启动应用
:start_application
call :print_info "启动应用..."

if exist "test-web\target\test-web-*.jar" (
    start java -jar test-web\target\test-web-*.jar
    call :print_info "应用已启动"
    call :print_info "访问地址: http://localhost:8080"

    timeout /t 5 /nobreak >nul

    call :print_info "应用启动成功"
) else (
    call :print_error "找不到应用 JAR 文件"
    exit /b 1
)
exit /b 0

:: 停止应用
:stop_application
call :print_info "停止应用..."
taskkill /F /FI "WINDOWTITLE eq test-engine*" 2>nul
call :print_info "应用已停止"
exit /b 0

:: 查看日志
:view_logs
call :print_info "查看日志..."
if exist "logs\test-engine.log" (
    type logs\test-engine.log
) else (
    call :print_error "日志文件不存在"
)
exit /b 0

:: 清理
:cleanup
call :print_info "清理..."

:: 停止 Docker 容器
if exist "docker-compose.yml" (
    docker-compose down
)

call :print_info "清理完成"
exit /b 0

:: 显示帮助信息
:show_help
echo Test Engine Platform - 启动脚本
echo.
echo 用法: %~nx0 [命令]
echo.
echo 命令:
echo   build     构建项目
echo   start     启动应用
echo   stop      停止应用
echo   restart   重启应用
echo   logs      查看日志
echo   status    查看状态
echo   clean     清理资源
echo   help      显示帮助信息
echo.
echo 示例:
echo   %~nx0 build          # 构建项目
echo   %~nx0 start          # 启动应用
echo   %~nx0 logs           # 查看日志
exit /b 0

:: 查看状态
:show_status
call :print_info "应用状态:"

:: 检查应用进程
tasklist | findstr java.exe >nul 2>&1
if %errorlevel% equ 0 (
    echo   应用运行中
) else (
    echo   应用未运行
)

exit /b 0

:: 主函数
:main
set "command=%~1"
if "%command%"=="" set "command=start"

if "%command%"=="build" (
    call :check_dependencies
    if %errorlevel% equ 0 call :build_project
) else if "%command%"=="start" (
    call :check_dependencies
    if %errorlevel% equ 0 (
        call :create_directories
        call :build_project
        call :start_application
    )
) else if "%command%"=="stop" (
    call :stop_application
) else if "%command%"=="restart" (
    call :stop_application
    timeout /t 2 /nobreak >nul
    call :start_application
) else if "%command%"=="logs" (
    call :view_logs
) else if "%command%"=="status" (
    call :show_status
) else if "%command%"=="clean" (
    call :cleanup
) else if "%command%"=="help" (
    call :show_help
) else (
    call :print_error "未知命令: %command%"
    call :show_help
    exit /b 1
)

exit /b 0

call :main %*