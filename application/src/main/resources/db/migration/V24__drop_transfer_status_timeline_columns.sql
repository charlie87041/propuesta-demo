ALTER TABLE admin_source_transfers
    DROP COLUMN IF EXISTS requested_at,
    DROP COLUMN IF EXISTS shipped_at,
    DROP COLUMN IF EXISTS received_at,
    DROP COLUMN IF EXISTS created_by_admin_user_id,
    DROP COLUMN IF EXISTS updated_by_admin_user_id;
