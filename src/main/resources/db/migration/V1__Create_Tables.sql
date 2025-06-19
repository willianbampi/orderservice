CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE tb_partner (
    id UUID NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    credit_limit NUMERIC(18, 2) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE tb_order (
    id UUID NOT NULL PRIMARY KEY,
    partner_id UUID NOT NULL REFERENCES tb_partner(id),
    total_amount NUMERIC(18, 2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE tb_order_item (
    id UUID NOT NULL PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES tb_order(id) ON DELETE CASCADE,
    product_id UUID NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(18, 2) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_order_partner_id ON tb_order(partner_id);
CREATE INDEX idx_order_status ON tb_order(status);
CREATE INDEX idx_order_created_at ON tb_order(created_at);