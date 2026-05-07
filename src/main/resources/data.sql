-- ============================================================
--  V2__seed_data.sql
--  Datos iniciales de ejemplo para desarrollo y pruebas.
-- ============================================================

-- Franquicias
INSERT INTO franchise (name) VALUES
    ('McDonald''s Colombia'),
    ('Burger King Colombia');

-- Sucursales de McDonald's Colombia (id=1)
INSERT INTO branch (franchise_id, name) VALUES
    (1, 'Sucursal Bogotá Centro'),
    (1, 'Sucursal Medellín El Poblado'),
    (1, 'Sucursal Cali Norte');

-- Sucursales de Burger King Colombia (id=2)
INSERT INTO branch (franchise_id, name) VALUES
    (2, 'Sucursal Bogotá Chapinero'),
    (2, 'Sucursal Barranquilla Centro');

-- Productos — Sucursal Bogotá Centro (branch_id=1)
INSERT INTO product (branch_id, name, stock) VALUES
    (1, 'Big Mac',         150),
    (1, 'McPollo',         200),
    (1, 'Papas Medianas',  300);

-- Productos — Sucursal Medellín El Poblado (branch_id=2)
INSERT INTO product (branch_id, name, stock) VALUES
    (2, 'Big Mac',         80),
    (2, 'McFlurry Oreo',   120),
    (2, 'Nuggets 10 Pzas', 95);

-- Productos — Sucursal Cali Norte (branch_id=3)
INSERT INTO product (branch_id, name, stock) VALUES
    (3, 'Cuarto de Libra', 60),
    (3, 'McPollo',         175),
    (3, 'Papas Grandes',   220);

-- Productos — Sucursal Bogotá Chapinero (branch_id=4)
INSERT INTO product (branch_id, name, stock) VALUES
    (4, 'Whopper',         130),
    (4, 'Double Whopper',  70),
    (4, 'Onion Rings',     180);

-- Productos — Sucursal Barranquilla Centro (branch_id=5)
INSERT INTO product (branch_id, name, stock) VALUES
    (5, 'Whopper Junior',  90),
    (5, 'Chicken Royale',  140),
    (5, 'Papas Medianas',  250);