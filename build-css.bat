@echo off
REM Tailwind CSS Build Script for Reliable Carriers (Windows)
REM This script builds the production CSS from the Tailwind configuration

echo Building Tailwind CSS for Reliable Carriers...

REM Check if npx is available
npx --version >nul 2>&1
if errorlevel 1 (
    echo Error: npx is not installed. Please install Node.js and npm first.
    pause
    exit /b 1
)

REM Build the CSS
echo Building CSS from Tailwind configuration...
npx tailwindcss -i ./src/main/resources/static/css/tailwind.css -o ./src/main/resources/static/css/tailwind-built.css --minify

if errorlevel 1 (
    echo CSS build failed!
    pause
    exit /b 1
) else (
    echo CSS build successful!
    echo Built file: src/main/resources/static/css/tailwind-built.css
    echo.
    echo To use the built CSS in production:
    echo 1. Update templates to reference '/css/tailwind-built.css' instead of '/css/tailwind.css'
    echo 2. The built file is minified and optimized for production
)

pause
