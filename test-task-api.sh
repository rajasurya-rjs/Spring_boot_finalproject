#!/bin/bash

# Task API Test Script
# This script tests the Task Management API endpoints

BASE_URL="http://localhost:8080/api"

echo "================================"
echo "Testing Task Management API"
echo "================================"

# Step 1: Register a user
echo -e "\n1. Registering user..."
REGISTER_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Test User",
    "username": "testuser123",
    "email": "test123@example.com",
    "password": "Password123!"
  }')

echo "$REGISTER_RESPONSE" | jq '.'
JWT_TOKEN=$(echo "$REGISTER_RESPONSE" | jq -r '.data.token')

if [ "$JWT_TOKEN" = "null" ] || [ -z "$JWT_TOKEN" ]; then
    echo "❌ Failed to get JWT token. Registration may have failed."
    exit 1
fi

echo "✅ JWT Token obtained: ${JWT_TOKEN:0:50}..."

# Step 2: Create a project
echo -e "\n2. Creating project..."
PROJECT_RESPONSE=$(curl -s -X POST "$BASE_URL/projects" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "name": "Test Project",
    "description": "Testing project creation",
    "startDate": "2026-02-01T09:00:00",
    "endDate": "2026-08-31T18:00:00",
    "memberIds": []
  }')

echo "$PROJECT_RESPONSE" | jq '.'
PROJECT_ID=$(echo "$PROJECT_RESPONSE" | jq -r '.data.id')

if [ "$PROJECT_ID" = "null" ] || [ -z "$PROJECT_ID" ]; then
    echo "❌ Failed to get Project ID. Project creation may have failed."
    exit 1
fi

echo "✅ Project created with ID: $PROJECT_ID"

# Step 3: Create a task
echo -e "\n3. Creating task..."
TASK_RESPONSE=$(curl -s -X POST "$BASE_URL/tasks" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d "{
    \"title\": \"Test Task\",
    \"description\": \"Testing task creation\",
    \"projectId\": \"$PROJECT_ID\",
    \"priority\": \"HIGH\",
    \"status\": \"TODO\",
    \"dueDate\": \"2026-02-15T17:00:00\",
    \"estimatedHours\": 8,
    \"tags\": [\"test\", \"api\"]
  }")

echo "$TASK_RESPONSE" | jq '.'
TASK_ID=$(echo "$TASK_RESPONSE" | jq -r '.data.id')

if [ "$TASK_ID" = "null" ] || [ -z "$TASK_ID" ]; then
    echo "❌ Failed to create task"
    exit 1
fi

echo "✅ Task created with ID: $TASK_ID"

# Step 4: Get task by ID
echo -e "\n4. Getting task by ID..."
GET_TASK_RESPONSE=$(curl -s -X GET "$BASE_URL/tasks/$TASK_ID" \
  -H "Authorization: Bearer $JWT_TOKEN")

echo "$GET_TASK_RESPONSE" | jq '.'

# Step 5: Get all tasks
echo -e "\n5. Getting all tasks..."
ALL_TASKS_RESPONSE=$(curl -s -X GET "$BASE_URL/tasks?page=0&size=10" \
  -H "Authorization: Bearer $JWT_TOKEN")

echo "$ALL_TASKS_RESPONSE" | jq '.'

echo -e "\n================================"
echo "✅ All tests completed successfully!"
echo "================================"
