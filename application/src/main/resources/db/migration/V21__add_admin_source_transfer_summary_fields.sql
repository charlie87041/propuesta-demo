ALTER TABLE admin_source_transfers
    ADD COLUMN IF NOT EXISTS estimated_transit_minutes INTEGER,
    ADD COLUMN IF NOT EXISTS weight_class VARCHAR(40),
    ADD COLUMN IF NOT EXISTS weight_kg INTEGER,
    ADD COLUMN IF NOT EXISTS high_priority BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS notify_recipient_hub BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS require_dual_verification BOOLEAN NOT NULL DEFAULT FALSE;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_admin_source_transfers_weight_non_negative'
    ) THEN
        ALTER TABLE admin_source_transfers
            ADD CONSTRAINT chk_admin_source_transfers_weight_non_negative
                CHECK (weight_kg IS NULL OR weight_kg >= 0);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_admin_source_transfers_estimated_transit_non_negative'
    ) THEN
        ALTER TABLE admin_source_transfers
            ADD CONSTRAINT chk_admin_source_transfers_estimated_transit_non_negative
                CHECK (estimated_transit_minutes IS NULL OR estimated_transit_minutes >= 0);
    END IF;
END $$;
