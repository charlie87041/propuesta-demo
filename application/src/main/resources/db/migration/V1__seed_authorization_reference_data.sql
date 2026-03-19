-- Domains
INSERT INTO domains (code, name, description, active, created_at, updated_at)
VALUES
  ('main-store', 'Main Cookie Store', 'Primary e-commerce storefront', true, NOW(), NOW()),
  ('example.test', 'Example Test Domain', 'Temporary domain for admin user-management tests', true, NOW(), NOW())
ON CONFLICT (code) DO NOTHING;

-- Permissions
INSERT INTO permissions (code, name, description, resource, action, created_at)
VALUES
  ('products:list', 'products:list', NULL, 'products', 'list', NOW()),
  ('products:read', 'products:read', NULL, 'products', 'read', NOW()),
  ('products:create', 'products:create', NULL, 'products', 'create', NOW()),
  ('products:update', 'products:update', NULL, 'products', 'update', NOW()),
  ('products:delete', 'products:delete', NULL, 'products', 'delete', NOW()),
  ('products:toggle-status', 'products:toggle-status', NULL, 'products', 'toggle-status', NOW()),
  ('categories:list', 'categories:list', NULL, 'categories', 'list', NOW()),
  ('categories:read', 'categories:read', NULL, 'categories', 'read', NOW()),
  ('categories:create', 'categories:create', NULL, 'categories', 'create', NOW()),
  ('categories:update', 'categories:update', NULL, 'categories', 'update', NOW()),
  ('categories:delete', 'categories:delete', NULL, 'categories', 'delete', NOW()),
  ('inventory:view-stock', 'inventory:view-stock', NULL, 'inventory', 'view-stock', NOW()),
  ('inventory:update-stock', 'inventory:update-stock', NULL, 'inventory', 'update-stock', NOW()),
  ('inventory:view-alerts', 'inventory:view-alerts', NULL, 'inventory', 'view-alerts', NOW()),
  ('inventory:configure-alerts', 'inventory:configure-alerts', NULL, 'inventory', 'configure-alerts', NOW()),
  ('cart:view', 'cart:view', NULL, 'cart', 'view', NOW()),
  ('cart:add-item', 'cart:add-item', NULL, 'cart', 'add-item', NOW()),
  ('cart:update-item', 'cart:update-item', NULL, 'cart', 'update-item', NOW()),
  ('cart:remove-item', 'cart:remove-item', NULL, 'cart', 'remove-item', NOW()),
  ('cart:clear', 'cart:clear', NULL, 'cart', 'clear', NOW()),
  ('profile:read', 'profile:read', NULL, 'profile', 'read', NOW()),
  ('profile:update', 'profile:update', NULL, 'profile', 'update', NOW()),
  ('addresses:list', 'addresses:list', NULL, 'addresses', 'list', NOW()),
  ('addresses:create', 'addresses:create', NULL, 'addresses', 'create', NOW()),
  ('addresses:update', 'addresses:update', NULL, 'addresses', 'update', NOW()),
  ('addresses:delete', 'addresses:delete', NULL, 'addresses', 'delete', NOW()),
  ('customers:list', 'customers:list', NULL, 'customers', 'list', NOW()),
  ('customers:read', 'customers:read', NULL, 'customers', 'read', NOW()),
  ('customers:update', 'customers:update', NULL, 'customers', 'update', NOW()),
  ('customers:disable', 'customers:disable', NULL, 'customers', 'disable', NOW()),
  ('users:list', 'users:list', NULL, 'users', 'list', NOW()),
  ('users:read', 'users:read', NULL, 'users', 'read', NOW()),
  ('users:create', 'users:create', NULL, 'users', 'create', NOW()),
  ('users:update', 'users:update', NULL, 'users', 'update', NOW()),
  ('users:delete', 'users:delete', NULL, 'users', 'delete', NOW()),
  ('users:assign-ability', 'users:assign-ability', NULL, 'users', 'assign-ability', NOW()),
  ('users:revoke-ability', 'users:revoke-ability', NULL, 'users', 'revoke-ability', NOW()),
  ('users:override-permission', 'users:override-permission', NULL, 'users', 'override-permission', NOW()),
  ('orders:list-own', 'orders:list-own', NULL, 'orders', 'list-own', NOW()),
  ('orders:read-own', 'orders:read-own', NULL, 'orders', 'read-own', NOW()),
  ('orders:cancel-own', 'orders:cancel-own', NULL, 'orders', 'cancel-own', NOW()),
  ('orders:list', 'orders:list', NULL, 'orders', 'list', NOW()),
  ('orders:read', 'orders:read', NULL, 'orders', 'read', NOW()),
  ('orders:update-status', 'orders:update-status', NULL, 'orders', 'update-status', NOW()),
  ('orders:add-tracking', 'orders:add-tracking', NULL, 'orders', 'add-tracking', NOW()),
  ('orders:cancel', 'orders:cancel', NULL, 'orders', 'cancel', NOW()),
  ('orders:refund', 'orders:refund', NULL, 'orders', 'refund', NOW()),
  ('checkout:initiate', 'checkout:initiate', NULL, 'checkout', 'initiate', NOW()),
  ('checkout:complete', 'checkout:complete', NULL, 'checkout', 'complete', NOW()),
  ('payments:create-intent', 'payments:create-intent', NULL, 'payments', 'create-intent', NOW()),
  ('payments:confirm', 'payments:confirm', NULL, 'payments', 'confirm', NOW()),
  ('payments:view', 'payments:view', NULL, 'payments', 'view', NOW()),
  ('payments:refund', 'payments:refund', NULL, 'payments', 'refund', NOW()),
  ('reports:sales', 'reports:sales', NULL, 'reports', 'sales', NOW()),
  ('reports:inventory', 'reports:inventory', NULL, 'reports', 'inventory', NOW()),
  ('reports:customers', 'reports:customers', NULL, 'reports', 'customers', NOW()),
  ('reports:export', 'reports:export', NULL, 'reports', 'export', NOW()),
  ('settings:view', 'settings:view', NULL, 'settings', 'view', NOW()),
  ('settings:update', 'settings:update', NULL, 'settings', 'update', NOW()),
  ('audit:view', 'audit:view', NULL, 'audit', 'view', NOW()),
  ('*', '*', NULL, '*', '*', NOW())
ON CONFLICT (code) DO NOTHING;

-- Abilities
INSERT INTO abilities (code, name, description, created_at)
VALUES
  ('browse-catalog', 'browse-catalog', NULL, NOW()),
  ('manage-cart', 'manage-cart', NULL, NOW()),
  ('checkout', 'checkout', NULL, NOW()),
  ('manage-profile', 'manage-profile', NULL, NOW()),
  ('view-orders', 'view-orders', NULL, NOW()),
  ('view-inventory', 'view-inventory', NULL, NOW()),
  ('manage-inventory', 'manage-inventory', NULL, NOW()),
  ('process-orders', 'process-orders', NULL, NOW()),
  ('manage-orders', 'manage-orders', NULL, NOW()),
  ('manage-customers', 'manage-customers', NULL, NOW()),
  ('manage-users', 'manage-users', NULL, NOW()),
  ('view-reports', 'view-reports', NULL, NOW()),
  ('manage-settings', 'manage-settings', NULL, NOW()),
  ('super-admin', 'super-admin', NULL, NOW())
ON CONFLICT (code) DO NOTHING;

-- Ability to permission mapping
INSERT INTO ability_permissions (ability_id, permission_id)
SELECT a.id, p.id
FROM abilities a
JOIN permissions p ON (
  (a.code = 'browse-catalog' AND p.code IN ('products:list','products:read','categories:list','categories:read')) OR
  (a.code = 'manage-cart' AND p.code IN ('cart:view','cart:add-item','cart:update-item','cart:remove-item','cart:clear')) OR
  (a.code = 'checkout' AND p.code IN ('checkout:initiate','checkout:complete','payments:create-intent','payments:confirm')) OR
  (a.code = 'manage-profile' AND p.code IN ('profile:read','profile:update','addresses:list','addresses:create','addresses:update','addresses:delete')) OR
  (a.code = 'view-orders' AND p.code IN ('orders:list-own','orders:read-own','orders:cancel-own')) OR
  (a.code = 'view-inventory' AND p.code IN ('products:list','products:read','categories:list','categories:read','inventory:view-stock','inventory:view-alerts')) OR
  (a.code = 'manage-inventory' AND p.code IN ('products:list','products:read','products:create','products:update','products:delete','products:toggle-status','categories:list','categories:read','categories:create','categories:update','categories:delete','inventory:view-stock','inventory:update-stock','inventory:view-alerts','inventory:configure-alerts')) OR
  (a.code = 'process-orders' AND p.code IN ('orders:list','orders:read','orders:update-status','orders:add-tracking')) OR
  (a.code = 'manage-orders' AND p.code IN ('orders:list','orders:read','orders:update-status','orders:add-tracking','orders:cancel','orders:refund','payments:view','payments:refund')) OR
  (a.code = 'manage-customers' AND p.code IN ('customers:list','customers:read','customers:update','customers:disable','orders:list','orders:read')) OR
  (a.code = 'manage-users' AND p.code IN ('users:list','users:read','users:create','users:update','users:delete','users:assign-ability','users:revoke-ability','users:override-permission')) OR
  (a.code = 'view-reports' AND p.code IN ('reports:sales','reports:inventory','reports:customers','reports:export')) OR
  (a.code = 'manage-settings' AND p.code IN ('settings:view','settings:update','audit:view')) OR
  (a.code = 'super-admin' AND p.code = '*')
)
ON CONFLICT DO NOTHING;
