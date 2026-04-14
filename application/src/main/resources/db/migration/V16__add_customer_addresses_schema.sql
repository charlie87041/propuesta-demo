CREATE TABLE IF NOT EXISTS customer_addresses (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    address_type VARCHAR(20) NOT NULL DEFAULT 'SHIPPING',
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    first_name VARCHAR(120) NOT NULL,
    last_name VARCHAR(120) NOT NULL,
    gender VARCHAR(40),
    company_name VARCHAR(180),
    address1 VARCHAR(255) NOT NULL,
    address2 VARCHAR(255),
    city VARCHAR(140) NOT NULL,
    state VARCHAR(140),
    country VARCHAR(140),
    postcode VARCHAR(32),
    email VARCHAR(255),
    phone VARCHAR(32),
    vat_id VARCHAR(64),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT chk_customer_addresses_address_type CHECK (address_type IN ('SHIPPING', 'BILLING'))
);

CREATE INDEX IF NOT EXISTS idx_customer_addresses_customer_id
    ON customer_addresses (customer_id);

CREATE INDEX IF NOT EXISTS idx_customer_addresses_customer_type
    ON customer_addresses (customer_id, address_type);

CREATE INDEX IF NOT EXISTS idx_customer_addresses_customer_default
    ON customer_addresses (customer_id, is_default);

