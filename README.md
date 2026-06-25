# Giavico Modular Monolith

Giavico's backend is a single Spring Boot application containing four internal
feature areas:

- formula management and Ollama-backed generation
- inventory management and stock movements
- AI chat and chat history
- controlled R&D documents, workflows, HTML, and PDF exports

Java 17 or newer is required.

All APIs are served from:

```text
http://localhost:8080
```

The existing API paths remain stable:

```text
/api/formulas/**
/api/inventory/**
/api/chat/**
/api/rnd-documents/**
```

## Run locally

Start MySQL:

```bash
docker compose up mysql phpmyadmin
```

Run the application:

```bash
mvn spring-boot:run
```

Or start the complete stack:

```bash
docker compose up --build
```

Ollama is expected at `http://localhost:11434` for local Maven runs and at
`http://host.docker.internal:11434` from Docker.

## Verify

```bash
mvn verify
```

Tests use an in-memory H2 database. Live Ollama is not required for the test
suite.

## Deploy to Render

The repository includes `render.yaml` and a root `Dockerfile`.

Before deploying, prepare:

1. A MySQL 8 database reachable from Render.
2. A hosted Ollama-compatible endpoint if formula generation and chat should
   work in production. Render cannot connect to Ollama running on your laptop.
3. The deployed frontend origin for CORS, such as
   `https://your-frontend.example.com`.

Create a Render Blueprint from this repository and enter these prompted values:

```text
SPRING_DATASOURCE_URL=jdbc:mysql://<host>:3306/giavico?useSSL=true&serverTimezone=UTC
SPRING_DATASOURCE_USERNAME=<database-user>
SPRING_DATASOURCE_PASSWORD=<database-password>
GIAVICO_CORS_ALLOWED_ORIGINS=https://<frontend-domain>
OLLAMA_BASE_URL=https://<hosted-ollama-domain>
```

The service uses Render's `PORT` automatically and exposes
`/actuator/health` as its health check.

## Database

The monolith uses the `giavico` MySQL schema. Hibernate currently manages table
creation with `ddl-auto=update`.

If data exists in the former service schemas, start the monolith once and then
run:

```bash
mysql -ugiavico -p giavico < docs/migrate-legacy-schemas.sql
```

The migration uses `INSERT IGNORE` and does not alter or delete the legacy
schemas.

## Main APIs

Formula:

```text
POST   /api/formulas
GET    /api/formulas
GET    /api/formulas/{uuid}
PUT    /api/formulas/{uuid}
DELETE /api/formulas/{uuid}
POST   /api/formulas/generate
POST   /api/formulas/generate/stream
```

Inventory:

```text
GET    /api/inventory/items
POST   /api/inventory/items
GET    /api/inventory/items/{uuid}
PUT    /api/inventory/items/{uuid}
DELETE /api/inventory/items/{uuid}
POST   /api/inventory/items/{uuid}/movements
GET    /api/inventory/items/{uuid}/movements
GET    /api/inventory/alerts/low-stock
```

Chat:

```text
POST   /api/chat
POST   /api/chat/stream
GET    /api/chat/messages
POST   /api/chat/messages
DELETE /api/chat/messages
GET    /api/chat/account/openai-key/status
```

R&D documents:

```text
GET    /api/rnd-documents/templates
POST   /api/rnd-documents
GET    /api/rnd-documents
GET    /api/rnd-documents/{uuid}
PUT    /api/rnd-documents/{uuid}
DELETE /api/rnd-documents/{uuid}
GET    /api/rnd-documents/{uuid}/print
GET    /api/rnd-documents/{uuid}/pdf
POST   /api/rnd-documents/{uuid}/{workflow-action}
```
