-- Run this script once after the monolith has started and Hibernate has created
-- the target tables in the `giavico` schema.
--
-- It copies data without deleting or changing the legacy schemas. Existing rows
-- in the target schema are kept through INSERT IGNORE.

DELIMITER //

DROP PROCEDURE IF EXISTS migrate_legacy_table//
CREATE PROCEDURE migrate_legacy_table(IN p_source_schema VARCHAR(64), IN p_table_name VARCHAR(64))
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = p_source_schema
          AND table_name = p_table_name
    ) AND EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'giavico'
          AND table_name = p_table_name
    ) THEN
        SET @copy_sql = CONCAT(
            'INSERT IGNORE INTO `giavico`.`', p_table_name,
            '` SELECT * FROM `', p_source_schema, '`.`', p_table_name, '`'
        );
        PREPARE copy_statement FROM @copy_sql;
        EXECUTE copy_statement;
        DEALLOCATE PREPARE copy_statement;
    END IF;
END//

DELIMITER ;

CALL migrate_legacy_table('giavico_formula', 'products');
CALL migrate_legacy_table('giavico_formula', 'formula_sessions');
CALL migrate_legacy_table('giavico_formula', 'formula_regional_restrictions');
CALL migrate_legacy_table('giavico_formula', 'formula_stability_alerts');
CALL migrate_legacy_table('giavico_formula', 'formula_ingredients');
CALL migrate_legacy_table('giavico_formula', 'product_variants');

CALL migrate_legacy_table('giavico_inventory', 'inventory_items');
CALL migrate_legacy_table('giavico_inventory', 'inventory_movements');

CALL migrate_legacy_table('giavico_chat', 'chat_messages');

CALL migrate_legacy_table('giavico_rnd_documents', 'rnd_documents');
CALL migrate_legacy_table('giavico_rnd_documents', 'rnd_document_revisions');
CALL migrate_legacy_table('giavico_rnd_documents', 'rnd_document_approvals');

DROP PROCEDURE migrate_legacy_table;
