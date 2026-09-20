# VVPD Task Service

Java backend for asynchronous task processing.

## Run locally

```bash
./gradlew bootRun
```

Windows:

```powershell
.\gradlew.bat bootRun
```

The service starts on `http://localhost:8080` by default.

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI JSON:

```text
http://localhost:8080/v3/api-docs
```

## Configuration

Environment variables:

- `SERVER_PORT` - HTTP port, default `8080`
- `TASK_WORKERS` - number of background workers, default `4`
- `TASK_PROCESSING_DURATION` - simulated processing duration, default `2s`

## API

Create a task:

```http
POST /api/v1/tasks
Content-Type: application/json

{
  "title": "Build report",
  "description": "Prepare async report",
  "priority": "HIGH"
}
```

List tasks:

```http
GET /api/v1/tasks?status=PENDING&priority=HIGH&page=0&size=20
```

Get task:

```http
GET /api/v1/tasks/{task_id}
```

Get status:

```http
GET /api/v1/tasks/{task_id}/status
```

Cancel task:

```http
DELETE /api/v1/tasks/{task_id}
```

## Docker

```bash
docker compose up --build
```

## Tests

```bash
./gradlew test
```

Windows:

```powershell
.\gradlew.bat test
```
