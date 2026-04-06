INSERT INTO product_template_fields (
    template_id,
    field_key,
    label,
    field_type,
    required,
    default_value,
    validation_rules,
    sort_order,
    created_at,
    updated_at
)
SELECT
    pt.id,
    field_data.field_key,
    field_data.label,
    field_data.field_type,
    field_data.required,
    field_data.default_value,
    field_data.validation_rules,
    field_data.sort_order,
    NOW(),
    NOW()
FROM (
    VALUES
        ('CUSTOM_CAKE_TEMPLATE', 'ingredients', 'Ingredients', 'LONG_TEXT', FALSE, NULL, 'max:5000', 60),
        ('CUSTOM_CAKE_TEMPLATE', 'allergen_info', 'Allergen Info', 'LONG_TEXT', FALSE, NULL, 'max:5000', 70),
        ('CUSTOM_CAKE_TEMPLATE', 'nutrition_facts', 'Nutrition Facts', 'LONG_TEXT', FALSE, NULL, 'max:5000', 80)
) AS field_data(template_code, field_key, label, field_type, required, default_value, validation_rules, sort_order)
JOIN product_templates pt
    ON pt.code = field_data.template_code
ON CONFLICT (template_id, field_key) DO UPDATE
SET
    label = EXCLUDED.label,
    field_type = EXCLUDED.field_type,
    required = EXCLUDED.required,
    default_value = EXCLUDED.default_value,
    validation_rules = EXCLUDED.validation_rules,
    sort_order = EXCLUDED.sort_order,
    updated_at = NOW();

INSERT INTO product_template_field_values (
    product_id,
    template_field_id,
    field_value,
    created_at,
    updated_at
)
SELECT
    p.id,
    tf.id,
    field_data.field_value,
    NOW(),
    NOW()
FROM products p
JOIN product_templates pt
    ON pt.id = p.template_id
    AND pt.code = 'CUSTOM_CAKE_TEMPLATE'
JOIN LATERAL (
    VALUES
        ('ingredients', p.ingredients),
        ('allergen_info', p.allergen_info),
        ('nutrition_facts', p.nutrition_facts)
) AS field_data(field_key, field_value)
    ON TRUE
JOIN product_template_fields tf
    ON tf.template_id = pt.id
    AND tf.field_key = field_data.field_key
WHERE field_data.field_value IS NOT NULL
  AND BTRIM(field_data.field_value) <> ''
ON CONFLICT (product_id, template_field_id) DO UPDATE
SET
    field_value = EXCLUDED.field_value,
    updated_at = NOW();

ALTER TABLE products
    DROP COLUMN IF EXISTS ingredients,
    DROP COLUMN IF EXISTS allergen_info,
    DROP COLUMN IF EXISTS nutrition_facts;
