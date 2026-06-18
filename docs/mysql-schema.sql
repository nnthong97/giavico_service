-- giavico_formula.formula_ingredients
CREATE TABLE `formula_ingredients` (
  `id` binary(16) NOT NULL,
  `cost_projection` decimal(14,4) NOT NULL,
  `mass_percentage` double NOT NULL,
  `raw_material_key` varchar(160) NOT NULL,
  `formula_session_id` binary(16) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK2cfe27li3qb5o8n0ewcsv8w9m` (`formula_session_id`),
  CONSTRAINT `FK2cfe27li3qb5o8n0ewcsv8w9m` FOREIGN KEY (`formula_session_id`) REFERENCES `formula_sessions` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- giavico_formula.formula_regional_restrictions
CREATE TABLE `formula_regional_restrictions` (
  `formula_session_id` binary(16) NOT NULL,
  `restriction_text` varchar(500) DEFAULT NULL,
  KEY `FK941jx78aoni2oic3d9vp7yg6s` (`formula_session_id`),
  CONSTRAINT `FK941jx78aoni2oic3d9vp7yg6s` FOREIGN KEY (`formula_session_id`) REFERENCES `formula_sessions` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- giavico_formula.formula_sessions
CREATE TABLE `formula_sessions` (
  `id` binary(16) NOT NULL,
  `acidified` bit(1) NOT NULL,
  `baselinebom` varchar(160) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `customer_specification` tinytext NOT NULL,
  `market_destination` varchar(240) NOT NULL,
  `name` varchar(160) NOT NULL,
  `production_area` varchar(240) NOT NULL,
  `summary` varchar(500) NOT NULL,
  `target_brix` double NOT NULL,
  `variance_analysis` longtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- giavico_formula.formula_stability_alerts
CREATE TABLE `formula_stability_alerts` (
  `formula_session_id` binary(16) NOT NULL,
  `alert_text` varchar(1000) DEFAULT NULL,
  KEY `FKl1uurumfjebuwjglns44ej9oy` (`formula_session_id`),
  CONSTRAINT `FKl1uurumfjebuwjglns44ej9oy` FOREIGN KEY (`formula_session_id`) REFERENCES `formula_sessions` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- giavico_formula.product_variants
CREATE TABLE `product_variants` (
  `id` binary(16) NOT NULL,
  `customer_specification` tinytext NOT NULL,
  `effective_from` datetime(6) NOT NULL,
  `effective_to` datetime(6) DEFAULT NULL,
  `market_destination` varchar(240) NOT NULL,
  `production_area` varchar(240) NOT NULL,
  `regulatory_snapshot` longtext,
  `formula_session_id` binary(16) DEFAULT NULL,
  `product_id` binary(16) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKr4dt1v4bgk01pnp8jl5jimfq` (`formula_session_id`),
  KEY `FKosqitn4s405cynmhb87lkvuau` (`product_id`),
  CONSTRAINT `FKosqitn4s405cynmhb87lkvuau` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`),
  CONSTRAINT `FKr4dt1v4bgk01pnp8jl5jimfq` FOREIGN KEY (`formula_session_id`) REFERENCES `formula_sessions` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- giavico_formula.products
CREATE TABLE `products` (
  `id` binary(16) NOT NULL,
  `drink_name` varchar(160) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- giavico_inventory.inventory_items
CREATE TABLE `inventory_items` (
  `id` binary(16) NOT NULL,
  `category` varchar(120) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `expiration_date` date DEFAULT NULL,
  `lot_number` varchar(120) DEFAULT NULL,
  `material_name` varchar(240) NOT NULL,
  `quantity_on_hand` decimal(18,4) NOT NULL,
  `raw_material_key` varchar(160) NOT NULL,
  `reorder_point` decimal(18,4) NOT NULL,
  `status` varchar(32) NOT NULL,
  `supplier_name` varchar(180) DEFAULT NULL,
  `unit_cost` decimal(18,4) NOT NULL,
  `unit_of_measure` varchar(32) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `warehouse_location` varchar(160) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_inventory_raw_material_key` (`raw_material_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- giavico_inventory.inventory_movements
CREATE TABLE `inventory_movements` (
  `id` binary(16) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `movement_type` varchar(32) NOT NULL,
  `performed_by` varchar(120) DEFAULT NULL,
  `quantity_delta` decimal(18,4) NOT NULL,
  `reason` varchar(1000) NOT NULL,
  `reference_id` varchar(160) DEFAULT NULL,
  `reference_type` varchar(80) DEFAULT NULL,
  `resulting_quantity` decimal(18,4) NOT NULL,
  `inventory_item_id` binary(16) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK5g2urco9ofj37nh4x8v01fdxv` (`inventory_item_id`),
  CONSTRAINT `FK5g2urco9ofj37nh4x8v01fdxv` FOREIGN KEY (`inventory_item_id`) REFERENCES `inventory_items` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- giavico_chat.chat_messages
CREATE TABLE `chat_messages` (
  `id` binary(16) NOT NULL,
  `content` longtext NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `role` varchar(24) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- giavico_formulation.chat_messages
CREATE TABLE `chat_messages` (
  `id` binary(16) NOT NULL,
  `content` longtext NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `role` varchar(24) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- giavico_formulation.formula_ingredients
CREATE TABLE `formula_ingredients` (
  `id` binary(16) NOT NULL,
  `cost_projection` decimal(14,4) NOT NULL,
  `mass_percentage` double NOT NULL,
  `raw_material_key` varchar(160) NOT NULL,
  `formula_session_id` binary(16) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK2cfe27li3qb5o8n0ewcsv8w9m` (`formula_session_id`),
  CONSTRAINT `FK2cfe27li3qb5o8n0ewcsv8w9m` FOREIGN KEY (`formula_session_id`) REFERENCES `formula_sessions` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- giavico_formulation.formula_regional_restrictions
CREATE TABLE `formula_regional_restrictions` (
  `formula_session_id` binary(16) NOT NULL,
  `restriction_text` varchar(500) DEFAULT NULL,
  KEY `FK941jx78aoni2oic3d9vp7yg6s` (`formula_session_id`),
  CONSTRAINT `FK941jx78aoni2oic3d9vp7yg6s` FOREIGN KEY (`formula_session_id`) REFERENCES `formula_sessions` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- giavico_formulation.formula_sessions
CREATE TABLE `formula_sessions` (
  `id` binary(16) NOT NULL,
  `acidified` bit(1) NOT NULL,
  `baselinebom` varchar(160) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `customer_specification` tinytext NOT NULL,
  `market_destination` varchar(240) NOT NULL,
  `name` varchar(160) NOT NULL,
  `production_area` varchar(240) NOT NULL,
  `summary` varchar(500) NOT NULL,
  `target_brix` double NOT NULL,
  `variance_analysis` longtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- giavico_formulation.formula_stability_alerts
CREATE TABLE `formula_stability_alerts` (
  `formula_session_id` binary(16) NOT NULL,
  `alert_text` varchar(1000) DEFAULT NULL,
  KEY `FKl1uurumfjebuwjglns44ej9oy` (`formula_session_id`),
  CONSTRAINT `FKl1uurumfjebuwjglns44ej9oy` FOREIGN KEY (`formula_session_id`) REFERENCES `formula_sessions` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- giavico_formulation.product_variants
CREATE TABLE `product_variants` (
  `id` binary(16) NOT NULL,
  `customer_specification` tinytext NOT NULL,
  `effective_from` datetime(6) NOT NULL,
  `effective_to` datetime(6) DEFAULT NULL,
  `market_destination` varchar(240) NOT NULL,
  `production_area` varchar(240) NOT NULL,
  `regulatory_snapshot` longtext,
  `formula_session_id` binary(16) DEFAULT NULL,
  `product_id` binary(16) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKr4dt1v4bgk01pnp8jl5jimfq` (`formula_session_id`),
  KEY `FKosqitn4s405cynmhb87lkvuau` (`product_id`),
  CONSTRAINT `FKosqitn4s405cynmhb87lkvuau` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`),
  CONSTRAINT `FKr4dt1v4bgk01pnp8jl5jimfq` FOREIGN KEY (`formula_session_id`) REFERENCES `formula_sessions` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- giavico_formulation.products
CREATE TABLE `products` (
  `id` binary(16) NOT NULL,
  `drink_name` varchar(160) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

