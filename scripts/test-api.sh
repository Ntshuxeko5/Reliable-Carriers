#!/bin/bash

# API Testing Script for Reliable Carriers
# This script tests all critical API endpoints

BASE_URL="${BASE_URL:-http://localhost:8080}"
TEST_EMAIL="test@example.com"
TEST_PASSWORD="password123"

echo "=========================================="
echo "Reliable Carriers API Testing"
echo "=========================================="
echo "Base URL: $BASE_URL"
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test counter
PASSED=0
FAILED=0

# Function to test endpoint
test_endpoint() {
    local method=$1
    local endpoint=$2
    local expected_status=$3
    local description=$4
    local data=$5
    local token=$6
    
    echo -n "Testing: $description... "
    
    if [ -z "$token" ]; then
        if [ "$method" = "GET" ]; then
            response=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL$endpoint")
        else
            response=$(curl -s -w "\n%{http_code}" -X $method "$BASE_URL$endpoint" \
                -H "Content-Type: application/json" \
                -d "$data")
        fi
    else
        if [ "$method" = "GET" ]; then
            response=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL$endpoint" \
                -H "Authorization: Bearer $token")
        else
            response=$(curl -s -w "\n%{http_code}" -X $method "$BASE_URL$endpoint" \
                -H "Authorization: Bearer $token" \
                -H "Content-Type: application/json" \
                -d "$data")
        fi
    fi
    
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" = "$expected_status" ]; then
        echo -e "${GREEN}✓ PASSED${NC} (Status: $http_code)"
        ((PASSED++))
        return 0
    else
        echo -e "${RED}✗ FAILED${NC} (Expected: $expected_status, Got: $http_code)"
        echo "Response: $body"
        ((FAILED++))
        return 1
    fi
}

# 1. Health Check
echo "1. Health Check"
test_endpoint "GET" "/actuator/health" "200" "Health endpoint"

# 2. Public Endpoints
echo ""
echo "2. Public Endpoints"
test_endpoint "GET" "/api/public/track/RC12345678" "200" "Public tracking (may return 404 if tracking number doesn't exist)"
test_endpoint "POST" "/api/customer/quote" "200" "Quote generation" \
    '{"pickupAddress":"123 Test St","deliveryAddress":"456 Test Ave","weight":5.0}'

# 3. Authentication
echo ""
echo "3. Authentication"
echo -n "Testing: User registration... "
register_response=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/auth/register" \
    -H "Content-Type: application/json" \
    -d "{\"email\":\"$TEST_EMAIL\",\"password\":\"$TEST_PASSWORD\",\"firstName\":\"Test\",\"lastName\":\"User\",\"role\":\"CUSTOMER\"}")

register_code=$(echo "$register_response" | tail -n1)
if [ "$register_code" = "200" ] || [ "$register_code" = "201" ]; then
    echo -e "${GREEN}✓ PASSED${NC} (Status: $register_code)"
    ((PASSED++))
else
    echo -e "${YELLOW}⚠ SKIPPED${NC} (User may already exist, Status: $register_code)"
fi

# Login
echo -n "Testing: User login... "
login_response=$(curl -s -X POST "$BASE_URL/api/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"email\":\"$TEST_EMAIL\",\"password\":\"$TEST_PASSWORD\"}")

TOKEN=$(echo "$login_response" | grep -o '"token":"[^"]*' | cut -d'"' -f4)

if [ -n "$TOKEN" ]; then
    echo -e "${GREEN}✓ PASSED${NC} (Token received)"
    ((PASSED++))
else
    echo -e "${RED}✗ FAILED${NC} (No token received)"
    echo "Response: $login_response"
    ((FAILED++))
    TOKEN=""
fi

# 4. Authenticated Endpoints (if token available)
if [ -n "$TOKEN" ]; then
    echo ""
    echo "4. Authenticated Endpoints"
    test_endpoint "GET" "/api/customer/shipments" "200" "Get customer shipments" "" "$TOKEN"
    test_endpoint "GET" "/api/customer/profile" "200" "Get customer profile" "" "$TOKEN"
fi

# 5. Admin Endpoints (if admin token available)
if [ -n "$ADMIN_TOKEN" ]; then
    echo ""
    echo "5. Admin Endpoints"
    test_endpoint "GET" "/api/admin/users" "200" "Get all users" "" "$ADMIN_TOKEN"
    test_endpoint "GET" "/api/admin/verification/pending" "200" "Get pending verifications" "" "$ADMIN_TOKEN"
fi

# Summary
echo ""
echo "=========================================="
echo "Test Summary"
echo "=========================================="
echo -e "${GREEN}Passed: $PASSED${NC}"
echo -e "${RED}Failed: $FAILED${NC}"
echo "Total: $((PASSED + FAILED))"

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}All tests passed!${NC}"
    exit 0
else
    echo -e "${RED}Some tests failed.${NC}"
    exit 1
fi

