# Authorization Architecture: Cookies Store

## Overview

This document defines the Domain-Ability-Permission authorization model for the Cookies Store e-commerce platform.

## Domains

For the MVP, a single domain is used. The architecture supports multi-tenancy for future franchise expansion.

| Domain Code | Name | Description |
|-------------|------|-------------|
| `main-store` | Main Cookie Store | Primary e-commerce storefront |
| `franchise-*` | Franchise Stores | Future: regional franchise operations |

### Domain Hierarchy

```
                    ┌─────────────────┐
                    │   Super Admin   │
                    │  (all domains)  │
                    └────────┬────────┘
                             │
           ┌─────────────────┼─────────────────┐
           ▼                 ▼                 ▼
    ┌─────────────┐   ┌─────────────┐   ┌─────────────┐
    │ main-store  │   │franchise-nyc│   │franchise-la │
    └─────────────┘   └─────────────┘   └─────────────┘
```

## Abilities

### Customer-Facing Abilities

| Ability Code | Name | Description | Target User |
|--------------|------|-------------|-------------|
| `browse-catalog` | Browse Catalog | View products and categories | Guest, Customer |
| `manage-cart` | Manage Cart | Add/remove items from cart | Guest, Customer |
| `checkout` | Checkout | Complete purchases | Customer |
| `manage-profile` | Manage Profile | Update personal info and addresses | Customer |
| `view-orders` | View Orders | View own order history | Customer |

### Admin Abilities

| Ability Code | Name | Description | Target User |
|--------------|------|-------------|-------------|
| `manage-inventory` | Manage Inventory | Full product and stock control | Inventory Manager, Admin |
| `view-inventory` | View Inventory | Read-only product/stock access | Staff |
| `process-orders` | Process Orders | Handle order fulfillment | Fulfillment Staff |
| `manage-orders` | Manage Orders | Full order control including cancellation/refunds | Order Manager, Admin |
| `manage-customers` | Manage Customers | Customer account management | Customer Support, Admin |
| `view-reports` | View Reports | Access analytics and reports | Manager, Admin |
| `manage-settings` | Manage Settings | System configuration | Admin |
| `super-admin` | Super Admin | All permissions, all domains | Super Admin |

## Permissions

### Catalog Permissions

| Permission Code | Name | Description |
|-----------------|------|-------------|
| `products:list` | List Products | View product catalog |
| `products:read` | Read Product | View product details |
| `products:create` | Create Product | Add new products |
| `products:update` | Update Product | Modify product details |
| `products:delete` | Delete Product | Remove products |
| `products:toggle-status` | Toggle Product Status | Enable/disable products |
| `categories:list` | List Categories | View categories |
| `categories:read` | Read Category | View category details |
| `categories:create` | Create Category | Add new categories |
| `categories:update` | Update Category | Modify categories |
| `categories:delete` | Delete Category | Remove categories |

### Inventory Permissions

| Permission Code | Name | Description |
|-----------------|------|-------------|
| `inventory:view-stock` | View Stock | See stock levels |
| `inventory:update-stock` | Update Stock | Adjust stock quantities |
| `inventory:view-alerts` | View Alerts | See low stock alerts |
| `inventory:configure-alerts` | Configure Alerts | Set alert thresholds |

### Cart Permissions

| Permission Code | Name | Description |
|-----------------|------|-------------|
| `cart:view` | View Cart | See cart contents |
| `cart:add-item` | Add to Cart | Add items to cart |
| `cart:update-item` | Update Cart Item | Change quantities |
| `cart:remove-item` | Remove from Cart | Remove items |
| `cart:clear` | Clear Cart | Empty entire cart |

### Customer Permissions

| Permission Code | Name | Description |
|-----------------|------|-------------|
| `profile:read` | Read Profile | View own profile |
| `profile:update` | Update Profile | Modify own profile |
| `addresses:list` | List Addresses | View own addresses |
| `addresses:create` | Create Address | Add new address |
| `addresses:update` | Update Address | Modify address |
| `addresses:delete` | Delete Address | Remove address |
| `customers:list` | List Customers | View all customers (admin) |
| `customers:read` | Read Customer | View customer details (admin) |
| `customers:update` | Update Customer | Modify customer (admin) |
| `customers:disable` | Disable Customer | Deactivate account (admin) |

### Order Permissions

| Permission Code | Name | Description |
|-----------------|------|-------------|
| `orders:list-own` | List Own Orders | View own order history |
| `orders:read-own` | Read Own Order | View own order details |
| `orders:cancel-own` | Cancel Own Order | Cancel own pending/paid orders |
| `orders:list` | List Orders | View all orders (admin) |
| `orders:read` | Read Order | View any order details (admin) |
| `orders:update-status` | Update Order Status | Change order status |
| `orders:add-tracking` | Add Tracking | Add shipping tracking info |
| `orders:cancel` | Cancel Order | Cancel any order (admin) |
| `orders:refund` | Refund Order | Process refunds |

### Checkout Permissions

| Permission Code | Name | Description |
|-----------------|------|-------------|
| `checkout:initiate` | Initiate Checkout | Start checkout process |
| `checkout:complete` | Complete Checkout | Finalize order |

### Payment Permissions

| Permission Code | Name | Description |
|-----------------|------|-------------|
| `payments:create-intent` | Create Payment Intent | Initialize payment |
| `payments:confirm` | Confirm Payment | Complete payment |
| `payments:view` | View Payments | View payment records (admin) |
| `payments:refund` | Refund Payment | Process refunds (admin) |

### Report Permissions

| Permission Code | Name | Description |
|-----------------|------|-------------|
| `reports:sales` | Sales Reports | View sales analytics |
| `reports:inventory` | Inventory Reports | View stock reports |
| `reports:customers` | Customer Reports | View customer analytics |
| `reports:export` | Export Reports | Export data to CSV/Excel |

### Settings Permissions

| Permission Code | Name | Description |
|-----------------|------|-------------|
| `settings:view` | View Settings | See system configuration |
| `settings:update` | Update Settings | Modify configuration |
| `audit:view` | View Audit Log | Access audit trail |

## Ability-Permission Matrix

### browse-catalog
```yaml
permissions:
  - products:list
  - products:read
  - categories:list
  - categories:read
```

### manage-cart
```yaml
permissions:
  - cart:view
  - cart:add-item
  - cart:update-item
  - cart:remove-item
  - cart:clear
```

### checkout
```yaml
permissions:
  - checkout:initiate
  - checkout:complete
  - payments:create-intent
  - payments:confirm
```

### manage-profile
```yaml
permissions:
  - profile:read
  - profile:update
  - addresses:list
  - addresses:create
  - addresses:update
  - addresses:delete
```

### view-orders
```yaml
permissions:
  - orders:list-own
  - orders:read-own
  - orders:cancel-own
```

### view-inventory
```yaml
permissions:
  - products:list
  - products:read
  - categories:list
  - categories:read
  - inventory:view-stock
  - inventory:view-alerts
```

### manage-inventory
```yaml
permissions:
  - products:list
  - products:read
  - products:create
  - products:update
  - products:delete
  - products:toggle-status
  - categories:list
  - categories:read
  - categories:create
  - categories:update
  - categories:delete
  - inventory:view-stock
  - inventory:update-stock
  - inventory:view-alerts
  - inventory:configure-alerts
```

### process-orders
```yaml
permissions:
  - orders:list
  - orders:read
  - orders:update-status
  - orders:add-tracking
```

### manage-orders
```yaml
permissions:
  - orders:list
  - orders:read
  - orders:update-status
  - orders:add-tracking
  - orders:cancel
  - orders:refund
  - payments:view
  - payments:refund
```

### manage-customers
```yaml
permissions:
  - customers:list
  - customers:read
  - customers:update
  - customers:disable
  - orders:list
  - orders:read
```

### view-reports
```yaml
permissions:
  - reports:sales
  - reports:inventory
  - reports:customers
  - reports:export
```

### manage-settings
```yaml
permissions:
  - settings:view
  - settings:update
  - audit:view
```

### super-admin
```yaml
permissions:
  - "*"  # All permissions in all domains
```

## User Roles to Abilities Mapping

| Role | Domain | Abilities |
|------|--------|-----------|
| Guest | `main-store` | `browse-catalog`, `manage-cart` |
| Customer | `main-store` | `browse-catalog`, `manage-cart`, `checkout`, `manage-profile`, `view-orders` |
| Staff | `main-store` | `view-inventory`, `process-orders` |
| Inventory Manager | `main-store` | `manage-inventory` |
| Order Manager | `main-store` | `manage-orders`, `view-reports` |
| Customer Support | `main-store` | `manage-customers` |
| Store Admin | `main-store` | `manage-inventory`, `manage-orders`, `manage-customers`, `view-reports`, `manage-settings` |
| Super Admin | `*` (all) | `super-admin` |

## API Endpoint Authorization

### Public Endpoints (No Auth Required)

| Endpoint | Method | Permission |
|----------|--------|------------|
| `/api/products` | GET | None (public) |
| `/api/products/{slug}` | GET | None (public) |
| `/api/categories` | GET | None (public) |
| `/api/auth/register` | POST | None (public) |
| `/api/auth/login` | POST | None (public) |
| `/api/auth/forgot-password` | POST | None (public) |

### Customer Endpoints

| Endpoint | Method | Required Permission |
|----------|--------|---------------------|
| `/api/cart` | GET | `cart:view` |
| `/api/cart/items` | POST | `cart:add-item` |
| `/api/cart/items/{id}` | PUT | `cart:update-item` |
| `/api/cart/items/{id}` | DELETE | `cart:remove-item` |
| `/api/customers/me` | GET | `profile:read` |
| `/api/customers/me` | PUT | `profile:update` |
| `/api/customers/me/addresses` | GET | `addresses:list` |
| `/api/customers/me/addresses` | POST | `addresses:create` |
| `/api/customers/me/addresses/{id}` | PUT | `addresses:update` |
| `/api/customers/me/addresses/{id}` | DELETE | `addresses:delete` |
| `/api/checkout` | POST | `checkout:complete` |
| `/api/orders` | GET | `orders:list-own` |
| `/api/orders/{orderNumber}` | GET | `orders:read-own` |
| `/api/orders/{orderNumber}/cancel` | POST | `orders:cancel-own` |
| `/api/payments/intent` | POST | `payments:create-intent` |
| `/api/payments/confirm` | POST | `payments:confirm` |

### Admin Endpoints

| Endpoint | Method | Required Permission |
|----------|--------|---------------------|
| `/api/domains/{domainCode}/admin/products` | GET | `products:list` |
| `/api/domains/{domainCode}/admin/products` | POST | `products:create` |
| `/api/domains/{domainCode}/admin/products/{id}` | PUT | `products:update` |
| `/api/domains/{domainCode}/admin/products/{id}` | DELETE | `products:delete` |
| `/api/domains/{domainCode}/admin/products/{id}/stock` | PUT | `inventory:update-stock` |
| `/api/domains/{domainCode}/admin/categories` | POST | `categories:create` |
| `/api/domains/{domainCode}/admin/categories/{id}` | PUT | `categories:update` |
| `/api/domains/{domainCode}/admin/categories/{id}` | DELETE | `categories:delete` |
| `/api/domains/{domainCode}/admin/orders` | GET | `orders:list` |
| `/api/domains/{domainCode}/admin/orders/{orderNumber}` | GET | `orders:read` |
| `/api/domains/{domainCode}/admin/orders/{orderNumber}/status` | PUT | `orders:update-status` |
| `/api/domains/{domainCode}/admin/orders/{orderNumber}/tracking` | PUT | `orders:add-tracking` |
| `/api/domains/{domainCode}/admin/orders/{orderNumber}/cancel` | POST | `orders:cancel` |
| `/api/domains/{domainCode}/admin/orders/{orderNumber}/refund` | POST | `orders:refund` |
| `/api/domains/{domainCode}/admin/customers` | GET | `customers:list` |
| `/api/domains/{domainCode}/admin/customers/{id}` | GET | `customers:read` |
| `/api/domains/{domainCode}/admin/customers/{id}` | PUT | `customers:update` |
| `/api/domains/{domainCode}/admin/customers/{id}/disable` | POST | `customers:disable` |
| `/api/domains/{domainCode}/admin/reports/sales` | GET | `reports:sales` |
| `/api/domains/{domainCode}/admin/reports/inventory` | GET | `reports:inventory` |
| `/api/domains/{domainCode}/admin/reports/customers` | GET | `reports:customers` |
| `/api/domains/{domainCode}/admin/settings` | GET | `settings:view` |
| `/api/domains/{domainCode}/admin/settings` | PUT | `settings:update` |
| `/api/domains/{domainCode}/admin/audit` | GET | `audit:view` |

## Ownership Rules

In addition to permission checks, these ownership validations apply:

| Resource | Ownership Rule |
|----------|----------------|
| Cart | Customer can only access their own cart (by `customerId` or `sessionId`) |
| Order | Customer can only access their own orders (by `customerId`) |
| Address | Customer can only access their own addresses (by `customerId`) |
| Profile | Customer can only access their own profile (by `customerId`) |

### Combined Check Example

```java
// For customer viewing their order:
// 1. Check permission: orders:read-own
// 2. Check ownership: order.customerId == currentUser.id

@GetMapping("/orders/{orderNumber}")
@RequiresPermission("orders:read-own")
public OrderDTO getOrder(@PathVariable String orderNumber) {
    Order order = orderService.findByOrderNumber(orderNumber);
    
    // Ownership check
    if (!order.getCustomerId().equals(getCurrentUserId())) {
        throw new AccessDeniedException("Not your order");
    }
    
    return orderMapper.toDTO(order);
}
```

## Database Schema

```sql
-- Domains
CREATE TABLE domains (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Abilities
CREATE TABLE abilities (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Permissions
CREATE TABLE permissions (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    resource VARCHAR(50) NOT NULL,
    action VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Ability-Permission mapping
CREATE TABLE ability_permissions (
    ability_id BIGINT REFERENCES abilities(id) ON DELETE CASCADE,
    permission_id BIGINT REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (ability_id, permission_id)
);

-- User-Domain-Ability grants
CREATE TABLE user_domain_abilities (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    domain_id BIGINT NOT NULL REFERENCES domains(id) ON DELETE CASCADE,
    ability_id BIGINT NOT NULL REFERENCES abilities(id) ON DELETE CASCADE,
    granted BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    granted_by BIGINT REFERENCES customers(id),
    UNIQUE (user_id, domain_id, ability_id)
);

-- Permission overrides (fine-grained)
CREATE TABLE user_domain_permission_overrides (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    domain_id BIGINT NOT NULL REFERENCES domains(id) ON DELETE CASCADE,
    permission_id BIGINT NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    granted BOOLEAN NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    granted_by BIGINT REFERENCES customers(id),
    UNIQUE (user_id, domain_id, permission_id)
);

-- Indexes for fast lookups
CREATE INDEX idx_user_domain_abilities_user ON user_domain_abilities(user_id);
CREATE INDEX idx_user_domain_abilities_domain ON user_domain_abilities(domain_id);
CREATE INDEX idx_user_domain_abilities_lookup ON user_domain_abilities(user_id, domain_id, granted);
CREATE INDEX idx_permission_overrides_lookup ON user_domain_permission_overrides(user_id, domain_id);
```

## Seed Data

```sql
-- Insert default domain
INSERT INTO domains (code, name, description) VALUES
('main-store', 'Main Cookie Store', 'Primary e-commerce storefront');

-- Insert abilities
INSERT INTO abilities (code, name, description) VALUES
('browse-catalog', 'Browse Catalog', 'View products and categories'),
('manage-cart', 'Manage Cart', 'Add/remove items from cart'),
('checkout', 'Checkout', 'Complete purchases'),
('manage-profile', 'Manage Profile', 'Update personal info and addresses'),
('view-orders', 'View Orders', 'View own order history'),
('view-inventory', 'View Inventory', 'Read-only product/stock access'),
('manage-inventory', 'Manage Inventory', 'Full product and stock control'),
('process-orders', 'Process Orders', 'Handle order fulfillment'),
('manage-orders', 'Manage Orders', 'Full order control'),
('manage-customers', 'Manage Customers', 'Customer account management'),
('view-reports', 'View Reports', 'Access analytics and reports'),
('manage-settings', 'Manage Settings', 'System configuration'),
('super-admin', 'Super Admin', 'All permissions, all domains');

-- Insert permissions (abbreviated, full list in migration)
INSERT INTO permissions (code, name, resource, action) VALUES
('products:list', 'List Products', 'products', 'list'),
('products:read', 'Read Product', 'products', 'read'),
('products:create', 'Create Product', 'products', 'create'),
('products:update', 'Update Product', 'products', 'update'),
('products:delete', 'Delete Product', 'products', 'delete'),
-- ... (full list in actual migration)
('*', 'All Permissions', '*', '*');

-- Map abilities to permissions
INSERT INTO ability_permissions (ability_id, permission_id)
SELECT a.id, p.id FROM abilities a, permissions p
WHERE a.code = 'browse-catalog' AND p.code IN ('products:list', 'products:read', 'categories:list', 'categories:read');

-- ... (full mapping in actual migration)
```
