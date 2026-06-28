-- Giavico modular-monolith PostgreSQL database
--
-- The application uses the `giavico` database and Hibernate currently creates
-- or updates these tables at startup. This file documents the owned table set;
-- it is not used as an initialization migration.

-- Create the database with docker-compose via POSTGRES_DB=giavico, or manually:
-- CREATE DATABASE giavico;

-- Formula domain
-- products
-- product_variants
-- formula_sessions
-- formula_ingredients
-- formula_regional_restrictions
-- formula_stability_alerts

-- Inventory domain
-- inventory_items
-- inventory_movements

-- Chat domain
-- chat_messages

-- R&D document domain
-- rnd_documents
-- rnd_document_approvals
-- rnd_document_revisions

-- Process management domain
-- process_runs

-- See docs/migrate-legacy-schemas.md for optional legacy data migration guidance
-- from the former per-service schemas.
