INSERT INTO product_templates (
    code,
    name,
    description,
    active,
    created_at,
    updated_at
)
VALUES
    (
        'CUSTOM_CAKE_TEMPLATE',
        'Custom Cake Template',
        'Template for personalized cake orders.',
        TRUE,
        NOW(),
        NOW()
    ),
    (
        'GIFT_BOX_TEMPLATE',
        'Gift Box Template',
        'Template for curated gift box products.',
        TRUE,
        NOW(),
        NOW()
    ),
    (
        'CATERING_TRAY_TEMPLATE',
        'Catering Tray Template',
        'Template for event catering tray requests.',
        TRUE,
        NOW(),
        NOW()
    )
ON CONFLICT (code) DO UPDATE
SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_at = NOW();

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
        ('CUSTOM_CAKE_TEMPLATE', 'occasion', 'Occasion', 'TEXT', TRUE, NULL, 'max:120', 10),
        ('CUSTOM_CAKE_TEMPLATE', 'servings', 'Servings', 'NUMBER', TRUE, NULL, 'min:1|max:500', 20),
        ('CUSTOM_CAKE_TEMPLATE', 'flavor', 'Flavor', 'SELECT', TRUE, NULL, 'choices:vanilla,chocolate,red_velvet,carrot', 30),
        ('CUSTOM_CAKE_TEMPLATE', 'message_on_cake', 'Message on Cake', 'TEXT', FALSE, NULL, 'max:140', 40),
        ('CUSTOM_CAKE_TEMPLATE', 'delivery_date', 'Delivery Date', 'DATE', TRUE, NULL, NULL, 50),

        ('GIFT_BOX_TEMPLATE', 'recipient_name', 'Recipient Name', 'TEXT', TRUE, NULL, 'max:120', 10),
        ('GIFT_BOX_TEMPLATE', 'theme', 'Theme', 'SELECT', FALSE, 'classic', 'choices:classic,birthday,romantic,corporate', 20),
        ('GIFT_BOX_TEMPLATE', 'include_card', 'Include Card', 'BOOLEAN', FALSE, 'false', NULL, 30),
        ('GIFT_BOX_TEMPLATE', 'card_message', 'Card Message', 'LONG_TEXT', FALSE, NULL, 'max:300', 40),

        ('CATERING_TRAY_TEMPLATE', 'event_type', 'Event Type', 'SELECT', TRUE, NULL, 'choices:office,birthday,wedding,private', 10),
        ('CATERING_TRAY_TEMPLATE', 'guest_count', 'Guest Count', 'NUMBER', TRUE, NULL, 'min:5|max:1000', 20),
        ('CATERING_TRAY_TEMPLATE', 'include_utensils', 'Include Utensils', 'BOOLEAN', FALSE, 'true', NULL, 30),
        ('CATERING_TRAY_TEMPLATE', 'special_notes', 'Special Notes', 'LONG_TEXT', FALSE, NULL, 'max:500', 40),
        ('CATERING_TRAY_TEMPLATE', 'service_datetime', 'Service Datetime', 'DATETIME', TRUE, NULL, NULL, 50)
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

INSERT INTO categories (
    code,
    name,
    slug,
    description,
    active,
    sort_order,
    default_template_id,
    created_at,
    updated_at
)
SELECT
    'CUSTOM_ORDERS',
    'Custom Orders',
    'custom-orders',
    'Category for products that require customer-provided configuration data.',
    TRUE,
    40,
    pt.id,
    NOW(),
    NOW()
FROM product_templates pt
WHERE pt.code = 'CUSTOM_CAKE_TEMPLATE'
ON CONFLICT (code) DO UPDATE
SET
    name = EXCLUDED.name,
    slug = EXCLUDED.slug,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    sort_order = EXCLUDED.sort_order,
    default_template_id = EXCLUDED.default_template_id,
    updated_at = NOW();
