CREATE TABLE admin_source_currency_denominations (
    id BIGSERIAL PRIMARY KEY,
    currency_code CHAR(3) NOT NULL REFERENCES currencies(code),
    value_minor BIGINT NOT NULL,
    label VARCHAR(40) NOT NULL,
    is_bill BOOLEAN NOT NULL DEFAULT TRUE,
    display_order INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_admin_source_currency_denomination UNIQUE (currency_code, value_minor)
);

CREATE INDEX idx_admin_source_currency_denominations_currency
    ON admin_source_currency_denominations(currency_code, active, display_order);

INSERT INTO admin_source_currency_denominations (
    currency_code,
    value_minor,
    label,
    is_bill,
    display_order,
    active
) VALUES
    ('USD', 100, '$1', TRUE, 10, TRUE),
    ('USD', 200, '$2', TRUE, 20, TRUE),
    ('USD', 500, '$5', TRUE, 30, TRUE),
    ('USD', 1000, '$10', TRUE, 40, TRUE),
    ('USD', 2000, '$20', TRUE, 50, TRUE),
    ('USD', 5000, '$50', TRUE, 60, TRUE),
    ('USD', 10000, '$100', TRUE, 70, TRUE)
ON CONFLICT (currency_code, value_minor) DO NOTHING;

