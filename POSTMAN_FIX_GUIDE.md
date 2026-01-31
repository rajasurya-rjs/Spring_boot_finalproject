# Quick Fix for Postman Errors

## The Problem
Your APIs are returning errors because the base URL is incorrect.

## The Solution
All your API endpoints need to use: `http://localhost:8080/api/`

The `/api` prefix is required because it's set as the `context-path` in application.yaml.

## Fix Your Existing Postman Collection

1. **Open Postman**
2. **Click on your "Task Management System" collection**
3. **Click the three dots (...) next to the collection name**
4. **Select "Edit"**
5. **Go to "Variables" tab**
6. **Find the `base_url` variable**
7. **Change INITIAL VALUE to:** `http://localhost:8080/api`
8. **Change CURRENT VALUE to:** `http://localhost:8080/api`
9. **Click "Save"**

## Correct API URLs

All endpoints should be:
- Auth: `http://localhost:8080/api/auth/register`
- Projects: `http://localhost:8080/api/projects`
- Tasks: `http://localhost:8080/api/tasks`
- Files: `http://localhost:8080/api/files`
- Analytics: `http://localhost:8080/api/analytics`
- Integrations: `http://localhost:8080/api/integrations/currency`

## Test It Works

Try this in your terminal:
```bash
curl http://localhost:8080/api/swagger-ui/index.html
```

If that works, your server is running correctly!
