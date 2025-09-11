@echo off
echo ========================================
echo Reliable Carriers - Quick Start Script
echo ========================================
echo.

echo Checking Java installation...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java 17 or later
    pause
    exit /b 1
)

echo Checking Maven installation...
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Maven is not installed or not in PATH
    echo Please install Maven 3.6 or later
    pause
    exit /b 1
)

echo Checking MySQL connection...
mysql -u root -p -e "SELECT 1;" >nul 2>&1
if %errorlevel% neq 0 (
    echo WARNING: MySQL connection failed
    echo Please ensure MySQL is running and accessible
    echo You may need to set up the database first
    echo.
    echo To set up the database:
    echo 1. Run: mysql -u root -p < database-setup.sql
    echo 2. Or follow the MYSQL_SETUP_GUIDE.md
    echo.
    set /p continue="Continue anyway? (y/n): "
    if /i not "%continue%"=="y" exit /b 1
)

echo.
echo Building the application...
call mvn clean install -DskipTests
if %errorlevel% neq 0 (
    echo ERROR: Build failed
    pause
    exit /b 1
)

echo.
echo Starting the application...
echo Application will be available at: http://localhost:8080
echo.
echo Default login credentials:
echo Admin: admin@reliablecarriers.com / admin123
echo Driver: driver@reliablecarriers.com / admin123
echo Customer: customer@example.com / admin123
echo.
echo Press Ctrl+C to stop the application
echo.

call mvn spring-boot:run

pause
