# Deploy With Supabase Postgres And Render

This project is a Spring Boot API. Supabase hosts the PostgreSQL database, and
Render hosts the API container from `Dockerfile`.

## 1. Create Supabase Database

1. Create a Supabase project.
2. Open **Project Settings > Database > Connection string**.
3. Use the **Session pooler** connection string for Render if direct database
   connections are not available from your Render region.
4. Convert the connection string to JDBC format:

```text
jdbc:postgresql://<pooler-host>:5432/postgres?sslmode=require
```

Keep the database password outside git.

## 2. Configure Render Environment

The checked-in `render.yaml` prompts for secret values with `sync: false`.
Set these in the Render dashboard or during Blueprint creation:

```text
SPRING_DATASOURCE_URL=jdbc:postgresql://<pooler-host>:5432/postgres?sslmode=require
SPRING_DATASOURCE_USERNAME=<pooler-user>
SPRING_DATASOURCE_PASSWORD=<database-password>
SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.postgresql.Driver
SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE=5
SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE=1
SPRING_JPA_HIBERNATE_DDL_AUTO=update
GIAVICO_CORS_ALLOWED_ORIGINS=https://<frontend-domain>
OLLAMA_BASE_URL=https://<hosted-ollama-endpoint>
```

Do not paste Supabase's full `postgresql://user:password@host:port/database`
string into `SPRING_DATASOURCE_URL`. Spring's JDBC URL should contain only the
host, port, database, and query parameters. Put the user and password in
`SPRING_DATASOURCE_USERNAME` and `SPRING_DATASOURCE_PASSWORD`.

For local shell testing, do not leave placeholder values such as
`https://<frontend-domain>` in `.env.supabase.local`. Shells like zsh can treat
angle brackets as redirection syntax when the file is sourced.

Supabase pooler usernames often include the project reference, for example:

```text
postgres.<project-ref>
```

Use the exact username shown by Supabase.

## 3. Deploy API To Render

1. Push this repository to GitHub.
2. In Render, create a Blueprint from this repository.
3. Enter the prompted secret environment variables.
4. Deploy the `giavico-service` web service.
5. Confirm the service is healthy:

```text
https://<render-service-domain>/actuator/health
```

Before deploying, test the same Supabase values locally:

```bash
zsh scripts/check-supabase-db.sh .env.supabase.local
```

If this command returns `FATAL: password authentication failed`, the issue is
with the Supabase database password or username, not with Spring Boot or Render.
Reset the database password in Supabase, update `.env.supabase.local`, then use
the same values in Render.

## 4. First Database Boot

For the current project stage, Hibernate creates and updates tables because
`SPRING_JPA_HIBERNATE_DDL_AUTO=update`.

After the schema stabilizes, switch production to SQL migrations and change:

```text
SPRING_JPA_HIBERNATE_DDL_AUTO=validate
```

## Notes

- Do not commit `.env` files or database passwords.
- Render cannot connect to an Ollama server running on your laptop. Use a hosted
  Ollama-compatible service for production formula generation and chat.
- If the app cannot connect, verify `sslmode=require`, the pooler host, the
  pooler username, and whether the database password contains special characters.
