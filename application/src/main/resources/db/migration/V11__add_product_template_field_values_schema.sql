CREATE TABLE IF NOT EXISTS product_template_field_values (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    template_field_id BIGINT NOT NULL REFERENCES product_template_fields(id) ON DELETE CASCADE,
    field_value TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_product_template_field_values_product_field UNIQUE (product_id, template_field_id)
);

CREATE INDEX IF NOT EXISTS idx_product_template_field_values_product
    ON product_template_field_values (product_id);

CREATE INDEX IF NOT EXISTS idx_product_template_field_values_template_field
    ON product_template_field_values (template_field_id);
