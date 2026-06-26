# Migrating Legacy MySQL Data To PostgreSQL

Use this only when you have existing data in the former MySQL schemas and need
to copy it into the monolith PostgreSQL database.

1. Start the monolith once against PostgreSQL so Hibernate creates the target
   tables in the `giavico` database.
2. Use `pgloader` or an equivalent ETL process to copy the old MySQL data into
   PostgreSQL.
3. Verify row counts and sample records before switching production traffic.

Example `pgloader` command for a single legacy schema:

```bash
pgloader mysql://giavico:<mysql-password>@<mysql-host>/giavico_formula \
  postgresql://giavico:<postgres-password>@<postgres-host>/giavico
```

Repeat for the former schemas that contain data:

- `giavico_formula`
- `giavico_inventory`
- `giavico_chat`
- `giavico_rnd_documents`

Review conflicts before running multiple imports into the same PostgreSQL
database, especially if UUIDs or table names overlap.
