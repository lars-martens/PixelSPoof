@echo off
REM PixelSPoof Test Runner Script for Windows
REM This script runs the comprehensive test suite for PixelSPoof

echo ==========================================
echo PixelSPoof Test Suite Runner
echo ==========================================

REM Colors for output (Windows CMD)
set "RED=[91m"
set "GREEN=[92m"
set "YELLOW=[93m"
set "NC=[0m"

REM Function to print colored output
:print_status
echo [INFO] %~1
goto :eof

:print_warning
echo [WARN] %~1
goto :eof

:print_error
echo [ERROR] %~1
goto :eof

REM Check if we're in the right directory
if not exist "gradlew.bat" (
    call :print_error "gradlew.bat not found. Please run this script from the project root directory."
    exit /b 1
)

call :print_status "Setting up test environment..."

REM Clean previous builds
call :print_status "Cleaning previous builds..."
call gradlew.bat clean

REM Build the project
call :print_status "Building project..."
call gradlew.bat assembleDebug
if %errorlevel% neq 0 (
    call :print_error "Failed to build project"
    exit /b 1
)
call :print_status "Project built successfully"

REM Run unit tests
call :print_status "Running unit tests..."
call gradlew.bat testDebugUnitTest
if %errorlevel% neq 0 (
    call :print_error "Unit tests failed"
    exit /b 1
)
call :print_status "Unit tests completed successfully"

REM Check for connected devices
call :print_status "Checking for connected devices..."
adb devices > temp_devices.txt 2>nul
findstr /v "List" temp_devices.txt > temp_devices_clean.txt
for /f %%i in ('find /c "device" ^< temp_devices_clean.txt') do set DEVICES=%%i
del temp_devices.txt temp_devices_clean.txt 2>nul

if %DEVICES% gtr 0 (
    call :print_status "Found %DEVICES% device(s), running instrumented tests..."
    call gradlew.bat connectedDebugAndroidTest
    if %errorlevel% neq 0 (
        call :print_warning "Instrumented tests failed or no tests were run"
    ) else (
        call :print_status "Instrumented tests completed successfully"
    )
) else (
    call :print_warning "No devices connected, skipping instrumented tests"
)

REM Generate test reports
call :print_status "Generating test reports..."
call gradlew.bat jacocoTestReport

REM Check test results
if exist "app\build\reports\tests\testDebugUnitTest" (
    call :print_status "Test results available in: app\build\reports\tests\testDebugUnitTest"
) else (
    call :print_warning "Test results directory not found"
)

REM Coverage report
if exist "app\build\reports\jacoco\jacocoTestReport" (
    call :print_status "Coverage report available in: app\build\reports\jacoco\jacocoTestReport"
    if exist "app\build\reports\jacoco\jacocoTestReport\html\index.html" (
        call :print_status "Open app\build\reports\jacoco\jacocoTestReport\html\index.html in your browser for detailed coverage"
    )
)

echo ==========================================
call :print_status "Test suite execution completed!"
echo ==========================================

call :print_status "All tests passed! ✅"
exit /b 0
