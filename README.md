# Giavico Beverage Microservices

Spring Boot microservice workspace for Giavico beverage R&D, formula management, inventory management, and Ollama-backed AI chat/generation.

## Services

| Service | Port | Responsibility |
| --- | ---: | --- |
| `gateway-service` | `8080` | Single Angular entrypoint and reverse proxy |
| `formula-service` | `8081` | Formula generation, formula CRUD, product variants |
| `inventory-service` | `8082` | Inventory item master, stock movements, low-stock alerts |
| `chat-ai-service` | `8083` | Chat APIs, Ollama chat streaming, chat history |
| `rnd-document-service` | `8084` | Controlled R&D templates, workflow, revisions, and printable exports |

Angular should call the gateway:

```text
http://localhost:8080
```

## Gateway Routes

```text
/api/formulas/**   -> formula-service
/api/inventory/**  -> inventory-service
/api/chat/**       -> chat-ai-service
/api/rnd-documents/** -> rnd-document-service
```

The gateway is implemented with Spring WebFlux and `WebClient` so the project does not need a new Spring Cloud dependency.

## Run Locally

Run all modules through Maven:

```bash
mvn -q -Dmaven.repo.local=.m2/repository test
```

Run one service:

```bash
mvn -pl gateway-service spring-boot:run
mvn -pl formula-service spring-boot:run
mvn -pl inventory-service spring-boot:run
mvn -pl chat-ai-service spring-boot:run
mvn -pl rnd-document-service spring-boot:run
```

Direct service runs use MySQL by default. Start the MySQL container first, or run against another MySQL instance with the same schemas and credentials.

To run one service against a different MySQL schema or user, pass datasource environment variables:

```bash
SPRING_DATASOURCE_URL='jdbc:mysql://localhost:3306/giavico_formula?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC' \
SPRING_DATASOURCE_USERNAME=giavico \
SPRING_DATASOURCE_PASSWORD=giavico \
SPRING_DATASOURCE_DRIVER_CLASS_NAME=com.mysql.cj.jdbc.Driver \
mvn -pl formula-service spring-boot:run
```

## Docker Compose

```bash
docker compose up --build
```

Compose starts:

- one MySQL container
- separate schemas: `giavico_formula`, `giavico_inventory`, `giavico_chat`
- R&D document schema: `giavico_rnd_documents`
- `gateway-service`
- `formula-service`
- `inventory-service`
- `chat-ai-service`
- `rnd-document-service`

Ollama is expected on the host:

```text
http://host.docker.internal:11434
```

## CI/CD

GitHub Actions runs the pipeline in `.github/workflows/ci-cd.yml`.

On pull requests and pushes to `main`, `master`, or `develop`, CI:

- sets up Java 21
- runs `mvn -B -ntp verify` for all Maven modules
- builds Docker images for `gateway-service`, `formula-service`, `inventory-service`, `chat-ai-service`, and `rnd-document-service`

On pushes to `main`, `master`, or version tags like `v1.0.0`, CD publishes each service image to GitHub Container Registry:

```text
ghcr.io/<github-owner>/gateway-service
ghcr.io/<github-owner>/formula-service
ghcr.io/<github-owner>/inventory-service
ghcr.io/<github-owner>/chat-ai-service
ghcr.io/<github-owner>/rnd-document-service
```

The workflow uses the repository `GITHUB_TOKEN`, so no extra registry secret is required for GitHub Container Registry.

## Formula APIs

```text
POST   /api/formulas
GET    /api/formulas?page=0&size=20
GET    /api/formulas/{uuid}
PUT    /api/formulas/{uuid}
DELETE /api/formulas/{uuid}
POST   /api/formulas/generate
POST   /api/formulas/generate/stream
GET    /api/formulas/generate/stream
```

## Inventory APIs

```text
GET    /api/inventory/items?search=yuzu&status=ACTIVE&page=0&size=20
POST   /api/inventory/items
GET    /api/inventory/items/{uuid}
GET    /api/inventory/items/by-raw-material-key/{rawMaterialKey}
PUT    /api/inventory/items/{uuid}
DELETE /api/inventory/items/{uuid}
POST   /api/inventory/items/{uuid}/movements
GET    /api/inventory/items/{uuid}/movements?page=0&size=50
GET    /api/inventory/alerts/low-stock
```

Movement types are `RECEIPT`, `ISSUE`, and `ADJUSTMENT`.

## Chat APIs

```text
POST   /api/chat
POST   /api/chat/stream
GET    /api/chat/messages?page=0&size=100
POST   /api/chat/messages
DELETE /api/chat/messages
```

## R&D Document APIs

```text
GET    /api/rnd-documents/templates
GET    /api/rnd-documents/templates/{type}
GET    /api/rnd-documents/templates/{type}/source
POST   /api/rnd-documents
GET    /api/rnd-documents?page=0&size=20&status=DRAFT&search=mango
GET    /api/rnd-documents/{uuid}
PUT    /api/rnd-documents/{uuid}
DELETE /api/rnd-documents/{uuid}
GET    /api/rnd-documents/{uuid}/print
POST   /api/rnd-documents/{uuid}/{workflow-action}
```

Workflow actions are `submit`, `start-review`, `approve`, `request-changes`, `issue`, and `acknowledge`. The template catalog includes the official blank PDF, trilingual names and fields, required-field metadata, table-column definitions, and approval roles.

## Service Data Ownership

- Formula service owns formula/product tables.
- Inventory service owns inventory item and movement tables.
- Chat AI service owns chat message tables.
- R&D document service owns controlled document, approval, and revision tables.
- Services do not share JPA entities or cross-database foreign keys.
- `rawMaterialKey` is the integration contract between formulas and inventory.
