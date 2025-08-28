#!/bin/bash
# PixelSPoof Test Runner Script
# This script runs the comprehensive test suite for PixelSPoof

echo "=========================================="
echo "PixelSPoof Test Suite Runner"
echo "=========================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if we're in the right directory
if [ ! -f "gradlew" ]; then
    print_error "gradlew not found. Please run this script from the project root directory."
    exit 1
fi

print_status "Setting up test environment..."

# Clean previous builds
print_status "Cleaning previous builds..."
./gradlew clean

# Build the project
print_status "Building project..."
if ./gradlew assembleDebug; then
    print_status "Project built successfully"
else
    print_error "Failed to build project"
    exit 1
fi

# Run unit tests
print_status "Running unit tests..."
if ./gradlew testDebugUnitTest; then
    print_status "Unit tests completed successfully"
else
    print_error "Unit tests failed"
    exit 1
fi

# Run Android tests (if emulator/device is available)
print_status "Checking for connected devices..."
DEVICES=$(adb devices | grep -v "List" | grep -v "^$" | wc -l)
if [ "$DEVICES" -gt 0 ]; then
    print_status "Found $DEVICES device(s), running instrumented tests..."
    if ./gradlew connectedDebugAndroidTest; then
        print_status "Instrumented tests completed successfully"
    else
        print_warning "Instrumented tests failed or no tests were run"
    fi
else
    print_warning "No devices connected, skipping instrumented tests"
fi

# Generate test reports
print_status "Generating test reports..."
./gradlew jacocoTestReport

# Check test results
TEST_RESULTS_DIR="app/build/reports/tests/testDebugUnitTest"
if [ -d "$TEST_RESULTS_DIR" ]; then
    print_status "Test results available in: $TEST_RESULTS_DIR"
    
    # Count test results
    PASSED=$(find "$TEST_RESULTS_DIR" -name "*.xml" -exec grep -l "testsuite" {} \; | xargs grep -o "tests=\"[0-9]*\"" | grep -o "[0-9]*" | awk '{sum += $1} END {print sum}')
    FAILED=$(find "$TEST_RESULTS_DIR" -name "*.xml" -exec grep -l "testsuite" {} \; | xargs grep -o "failures=\"[0-9]*\"" | grep -o "[0-9]*" | awk '{sum += $1} END {print sum}')
    
    if [ -n "$PASSED" ] && [ -n "$FAILED" ]; then
        print_status "Test Summary: $PASSED passed, $FAILED failed"
    fi
else
    print_warning "Test results directory not found"
fi

# Coverage report
COVERAGE_DIR="app/build/reports/jacoco/jacocoTestReport"
if [ -d "$COVERAGE_DIR" ]; then
    print_status "Coverage report available in: $COVERAGE_DIR"
    if [ -f "$COVERAGE_DIR/html/index.html" ]; then
        print_status "Open $COVERAGE_DIR/html/index.html in your browser for detailed coverage"
    fi
fi

print_status "=========================================="
print_status "Test suite execution completed!"
print_status "=========================================="

# Final status
if [ "$FAILED" -eq 0 ] 2>/dev/null; then
    print_status "All tests passed! ✅"
    exit 0
else
    print_error "Some tests failed. Please check the test reports for details."
    exit 1
fi
