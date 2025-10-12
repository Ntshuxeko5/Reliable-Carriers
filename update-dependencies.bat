@echo off
echo ===============================================
echo Updating Reliable Carriers Dependencies
echo ===============================================

echo.
echo Step 1: Cleaning previous build...
call mvnw.cmd clean

echo.
echo Step 2: Updating dependencies...
call mvnw.cmd versions:use-latest-releases

echo.
echo Step 3: Compiling with updated dependencies...
call mvnw.cmd compile

echo.
echo Step 4: Running tests...
call mvnw.cmd test

echo.
echo Step 5: Building the application...
call mvnw.cmd package -DskipTests

echo.
echo ===============================================
echo Dependency update completed!
echo ===============================================
echo.
echo To start the application, run:
echo mvnw.cmd spring-boot:run
echo.
pause
