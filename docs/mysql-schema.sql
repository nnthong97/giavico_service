-- Giavico modular-monolith schema
--
-- The application uses the `giavico` database and Hibernate currently creates
-- or updates these tables at startup. This file documents the owned table set;
-- it is not used as an initialization migration.

CREATE DATABASE IF NOT EXISTS giavico
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

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

-- See docs/migrate-legacy-schemas.sql for the optional, non-destructive copy
-- from the former per-service schemas.
