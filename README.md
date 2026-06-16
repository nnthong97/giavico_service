# Beverage Formulation & R&D Engine

Production-oriented Spring Boot REST microservice for beverage formulation workflows, recipe matrix tracking, mass-balance validation, and local Ollama-assisted generation.

## Runtime

- Java 21, compatible with Java 17+ source patterns
- Spring Boot 3.3.5
- MySQL 8.4
- Local Ollama endpoint: `http://localhost:11434/api/generate`
- Default model: `gemma4`

## Endpoints

### Store formula

`POST /api/formulas`

Stores a formula from the UI or another service without calling Ollama.

### List saved formulas

`GET /api/formulas?page=0&size=20`

Returns a paginated list of saved formula sessions.

### Formula detail

`GET /api/formulas/{uuid}`

Returns the full saved formula, including ingredients, alerts, restrictions, and variance analysis.

### Update formula

`PUT /api/formulas/{uuid}`

Replaces the saved formula payload using the same body shape as `POST /api/formulas`.

### Delete formula

`DELETE /api/formulas/{uuid}`

Deletes the saved formula and its variant tracking relation.

### Complete generation

`POST /api/formulas/generate`

Returns a validated and persisted formula response.

### Streaming generation

`POST /api/formulas/generate/stream`

Returns `text/event-stream` fragments from Ollama's newline-delimited stream.

`GET /api/formulas/generate/stream`

Supports query-param driven SSE generation for simple Angular EventSource flows.

## Docker

```bash
docker compose up --build
```

For direct local runs, the API defaults to `http://localhost:18181` to avoid common port `8080` and Docker Desktop `18080` conflicts:

```bash
mvn spring-boot:run
```

To force another port:

```bash
SERVER_PORT=8081 mvn spring-boot:run
```

Docker Compose still exposes the API on `http://localhost:8080`. The compose file maps the service to a host Ollama instance with `host.docker.internal:11434`.

If `mvn spring-boot:run` reports that a port is already in use, override it with `SERVER_PORT=8081 mvn spring-boot:run`. Docker keeps `8080` by setting `SERVER_PORT=8080` inside `docker-compose.yml`.

## Request Contract

```json
{
  "drinkName": "Yuzu Honey Green Tea",
  "marketDestination": "APAC - Japan Region",
  "targetBrix": 9.5,
  "isAcidified": true,
  "regionalRestrictions": ["No artificial colors", "Japan compliant labeling"],
  "productionArea": "Factory Line 4B - High-shear mixing tank",
  "customerSpecification": "Bright yuzu aroma, light tea bitterness, shelf-stable ambient product.",
  "baselineBOM": "BOM-YUZU-GT-001"
}
```
