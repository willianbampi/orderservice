CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE partner (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    credit_limit NUMERIC(18, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE order (
    id UUID PRIMARY KEY,
    partner_id INTEGER NOT NULL REFERENCES partner(id),
    total_amount NUMERIC(18, 2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE order_item (
    id SERIAL PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES order(id) ON DELETE CASCADE,
    product_id UUID NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(18, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_order_partner_id ON order(partner_id);
CREATE INDEX idx_order_status ON order(status);
CREATE INDEX idx_order_created_at ON order(created_at);