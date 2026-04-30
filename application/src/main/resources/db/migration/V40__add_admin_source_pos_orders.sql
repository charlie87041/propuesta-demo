CREATE TABLE admin_source_pos_orders (
    id BIGSERIAL PRIMARY KEY,
    source_id BIGINT NOT NULL REFERENCES sources(id),
    order_id BIGINT NOT NULL REFERENCES orders(id),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_admin_source_pos_orders_order UNIQUE (order_id)
);

CREATE INDEX idx_admin_source_pos_orders_source_id
    ON admin_source_pos_orders(source_id);

CREATE INDEX idx_admin_source_pos_orders_created_at
    ON admin_source_pos_orders(created_at DESC);

