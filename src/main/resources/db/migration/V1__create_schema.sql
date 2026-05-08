-- ============================================================
--  V1__create_schema.sql
--  Creación del esquema inicial de la base de datos.
--  Tablas: franchise, branch, product
-- ============================================================

-- Tabla: franchise
CREATE TABLE IF NOT EXISTS franchise (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    name       VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_franchise PRIMARY KEY (id),
    CONSTRAINT uq_franchise_name UNIQUE (name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- Tabla: branch
CREATE TABLE IF NOT EXISTS branch (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    franchise_id BIGINT       NOT NULL,
    name         VARCHAR(255) NOT NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_branch PRIMARY KEY (id),
    CONSTRAINT uq_branch_franchise_name UNIQUE (franchise_id, name),
    CONSTRAINT fk_branch_franchise FOREIGN KEY (franchise_id)
        REFERENCES franchise (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- Tabla: product
CREATE TABLE IF NOT EXISTS product (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    branch_id  BIGINT       NOT NULL,
    name       VARCHAR(255) NOT NULL,
    stock      INT          NOT NULL DEFAULT 0,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_product PRIMARY KEY (id),
    CONSTRAINT uq_product_branch_name UNIQUE (branch_id, name),
    CONSTRAINT chk_product_stock CHECK (stock >= 0),
    CONSTRAINT fk_product_branch FOREIGN KEY (branch_id)
        REFERENCES branch (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- Índices de búsqueda frecuente
-- CREATE INDEX IF NOT EXISTS no está soportado en MySQL 8,
-- por eso usamos un bloque condicional vía stored procedure desechable.
-- El continue-on-error del inicializador de Spring maneja el caso
-- en que los índices ya existan (error 1061 — Duplicate key name).
CREATE INDEX idx_branch_franchise_id ON branch (franchise_id);
CREATE INDEX idx_product_branch_id   ON product (branch_id);
CREATE INDEX idx_product_stock       ON product (branch_id, stock DESC);