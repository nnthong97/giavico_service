# Giavico monolith data relationships

All feature areas use the single `giavico` MySQL schema. Package and table
boundaries still separate the internal domains.

```mermaid
erDiagram
    PRODUCTS ||--o{ PRODUCT_VARIANTS : has
    FORMULA_SESSIONS ||--o{ PRODUCT_VARIANTS : powers
    FORMULA_SESSIONS ||--o{ FORMULA_INGREDIENTS : contains
    FORMULA_SESSIONS ||--o{ FORMULA_REGIONAL_RESTRICTIONS : records
    FORMULA_SESSIONS ||--o{ FORMULA_STABILITY_ALERTS : records

    INVENTORY_ITEMS ||--o{ INVENTORY_MOVEMENTS : receives

    RND_DOCUMENTS ||--o{ RND_DOCUMENT_APPROVALS : records
    RND_DOCUMENTS ||--o{ RND_DOCUMENT_REVISIONS : records
```

Formula ingredients and inventory items continue to integrate through the
`rawMaterialKey` application contract. No database foreign key is introduced
between those domains, so inventory records can be managed independently.

R&D documents may store a formula UUID as an optional reference. It is likewise
an application-level link rather than a cross-domain foreign key.
