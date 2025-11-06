@echo off
REM API Testing Script for Reliable Carriers (Windows)
REM This script tests all critical API endpoints

set BASE_URL=http://localhost:8080
set TEST_EMAIL=test@example.com
set TEST_PASSWORD=password123

echo ==========================================
echo Reliable Carriers API Testing
echo ==========================================
echo Base URL: %BASE_URL%
echo.

REM Test Health Check
echo 1. Testing Health Check...
curl -s -o nul -w "%%{http_code}" %BASE_URL%/actuator/health
if errorlevel 1 (
    echo Health check FAILED
) else (
    echo Health check PASSED
)

REM Test Public Tracking
echo.
echo 2. Testing Public Tracking...
curl -s -o nul -w "%%{http_code}" %BASE_URL%/api/public/track/RC12345678
echo Public tracking endpoint tested

REM Test Quote Generation
echo.
echo 3. Testing Quote Generation...
curl -s -X POST %BASE_URL%/api/customer/quote ^
    -H "Content-Type: application/json" ^
    -d "{\"pickupAddress\":\"123 Test St\",\"deliveryAddress\":\"456 Test Ave\",\"weight\":5.0}"

echo.
echo ==========================================
echo API Testing Complete
echo ==========================================
pause

