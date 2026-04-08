# Source-Level Product Management — Implementation Plan

## Context

Today, the system tracks **what** a product's stock is per source (`ProductSource.stockQuantity = 47`), but not **how** it got there. There is no record of whether those 47 units came from an initial allocation of 100, minus 60 sales, plus 7 returns. This plan transforms the source-product relationship from a static snapshot into a living, auditable system that supports operational decisions.

The plan is organized in five phases. Each phase builds on the previous one, and each is independently deployable and valuable.

---

## Phase 1 — Inventory Movement Ledger

### Why this phase first

Every downstream feature (alerts, metrics, transfers, purchase orders) needs to answer the question "what happened to the stock?". Without a movement ledger, we would have to reconstruct history from scattered order records, manual adjustments, and guesswork. The ledger is the single source of truth that makes everything else possible.

### Milestone 1.1 — Schema & Entity Design

**What:** Design and create the `stock_movements` table and its corresponding JPA entity.

**Explanation:** A stock movement is an immutable event that records a single change to a product's inventory at a specific source. Each row says: "On this date, this many units were added/removed from this product-source, for this reason, by this person." The `balance_after` column captures a running balance so we can reconstruct stock at any historical point without re-summing every prior movement.

Movement types to support:

| Type | Direction | Description |
|------|-----------|-------------|
| `INITIAL_STOCK` | + | First allocation when a product is assigned to a source |
| `SALE` | - | Units sold through an order |
| `RETURN` | + | Units returned by a customer |
| `RESTOCK` | + | Units received from a purchase order |
| `ADJUSTMENT` | +/- | Manual correction (inventory count found 3 extra units) |
| `TRANSFER_IN` | + | Units received from another source |
| `TRANSFER_OUT` | - | Units sent to another source |
| `DAMAGE` | - | Units removed due to damage |
| `SHRINKAGE` | - | Units lost to theft, spoilage, or unknown causes |

Each movement also carries a `reference_type` and `reference_id` to trace it back to its origin — an order, a purchase order, a transfer, or a manual action.

**Deliverables:**
- Flyway migration creating `stock_movements` table with CHECK constraints (`balance_after >= 0`)
- `StockMovement` entity (immutable — no setters, built via constructor/builder)
- `StockMovementType` enum
- `StockMovementRepository` with queries for audit history and balance reconstruction

### Milestone 1.2 — Movement Service Layer

**What:** Build the service that creates movements and updates the cached stock quantity atomically.

**Explanation:** When a movement is recorded, two things must happen in the same transaction: (1) the movement row is inserted, and (2) `ProductSource.stockQuantity` is updated to reflect the new balance. If either fails, both roll back. This prevents the cached quantity from drifting out of sync with the ledger.

The service also ensures `balance_after` is calculated correctly by reading the current stock, applying the delta, and storing the result. This must be done under a row-level lock (`SELECT ... FOR UPDATE` on the `ProductSource` row) to prevent race conditions when two concurrent sales try to reduce stock simultaneously.

**Deliverables:**
- `StockMovementService` with methods:
  - `recordMovement(productSourceId, type, quantity, referenceType, referenceId, notes, userId)` — core method
  - `recordSale(productSourceId, quantity, orderId, userId)` — convenience wrapper
  - `recordReturn(productSourceId, quantity, orderId, userId)` — convenience wrapper
  - `recordAdjustment(productSourceId, quantity, notes, userId)` — for manual corrections
- Pessimistic locking on `ProductSource` during movement recording
- Validation: movement cannot result in negative stock (unless configured to allow backorders)

### Milestone 1.3 — Retrofit Existing Stock Operations

**What:** Modify the existing product creation and update flows to generate movements instead of directly setting `stockQuantity`.

**Explanation:** Currently, when a product is created with `stockQuantity = 100`, that value is written directly to `ProductSource`. After this milestone, that same action will instead call `recordMovement(INITIAL_STOCK, 100)`, which writes the movement and updates the cached quantity. The external behavior is identical — the admin still enters "100" in the stock field — but now there is an audit trail.

Similarly, when an admin edits stock from 47 to 50 via the product update form, the system calculates the delta (+3) and records an `ADJUSTMENT` movement. The admin never interacts with movements directly at this point; the system generates them transparently.

**Deliverables:**
- Modify `ProductSourceService.upsertProductSources()` to use `StockMovementService`
- On create: generate `INITIAL_STOCK` movement
- On update: calculate delta from current stock, generate `ADJUSTMENT` movement
- Backfill migration: for every existing `ProductSource` with `stockQuantity > 0`, generate a synthetic `INITIAL_STOCK` movement dated at the `ProductSource.createdAt` timestamp

### Milestone 1.4 — Movement History API & UI

**What:** Expose the movement history for a given product-source and display it in the admin panel.

**Explanation:** An admin managing "Chocolate Chip Cookies at Warehouse North" should be able to see a chronological log:

```
2026-04-01  INITIAL_STOCK    +100   Balance: 100   "Initial allocation"
2026-04-02  SALE              -3    Balance:  97   Order #1042
2026-04-03  SALE              -5    Balance:  92   Order #1048
2026-04-05  RETURN            +1    Balance:  93   Order #1042
2026-04-07  ADJUSTMENT        -2    Balance:  91   "Inventory count correction"
```

This visibility is the core payoff of Phase 1. It turns "stock = 91" from an opaque number into a comprehensible story.

**Deliverables:**
- REST endpoint: `GET /admin/products/{id}/sources/{sourceId}/movements?page=0&size=20`
- Thymeleaf partial rendering the movement timeline
- Filters by movement type and date range
- Pagination (movements can accumulate fast for high-volume products)
- Unit and integration tests

### Milestone 1.5 — Manual Adjustment UI

**What:** Add a dedicated form for admins to record manual stock adjustments with a reason.

**Explanation:** Sometimes stock needs to be corrected outside of sales and restocks: a physical inventory count reveals discrepancies, items are found damaged, or stock was miscounted during receiving. This form allows an admin to say "add 5 units" or "remove 3 units" and attach a note explaining why. The movement is recorded with full attribution (who, when, why).

This replaces the implicit "edit the stock number in the product form" with an explicit action that requires a reason, improving accountability.

**Deliverables:**
- Form component: quantity delta (positive or negative), movement type dropdown (ADJUSTMENT, DAMAGE, SHRINKAGE), notes (required)
- Controller endpoint: `POST /admin/products/{id}/sources/{sourceId}/movements`
- Validation: notes required for manual movements, quantity != 0
- Success feedback with updated stock display

---

## Phase 2 — Alerts System

### Why this phase second

With the movement ledger in place, the system now knows not just current stock but velocity (how fast it is changing). Alerts transform this data into actionable notifications so the operations team does not have to manually monitor every product-source combination.

### Milestone 2.1 — Alert Schema & Entity

**What:** Create the `source_product_alerts` table and entity for persisting alerts.

**Explanation:** An alert is a time-stamped notification tied to a specific product-source combination. It has a type (what happened), a severity (how urgent), a human-readable message, and optional structured context in JSON (e.g., `{"current_stock": 5, "threshold": 10, "daily_velocity": 3.2}`). Alerts can be acknowledged by an admin, marking them as "seen and handled" without deleting the historical record.

Alerts are not notifications (no emails or push at this stage) — they are records that appear on dashboards and product detail pages.

**Deliverables:**
- Flyway migration for `source_product_alerts`
- `SourceProductAlert` entity
- `AlertType` enum: `LOW_STOCK`, `OUT_OF_STOCK`, `OVERSTOCK`, `RESTOCK_NEEDED`, `DEMAND_SPIKE`, `SLOW_MOVING`, `PRICE_CHANGE`
- `AlertSeverity` enum: `INFO`, `WARNING`, `CRITICAL`
- `SourceProductAlertRepository`

### Milestone 2.2 — Alert Generation Engine

**What:** Build the service that evaluates conditions and creates alerts.

**Explanation:** The alert engine runs in two modes:

1. **Reactive (event-driven):** After every stock movement, evaluate the new balance against thresholds. If `stockQuantity` just dropped below `lowStockThreshold`, generate a `LOW_STOCK` alert. If it hit zero, generate `OUT_OF_STOCK` (severity: CRITICAL). This gives instant feedback.

2. **Proactive (scheduled):** A daily (or configurable) batch job scans all active product-sources and computes velocity-based alerts. For example, "Product X at Source A has 15 units and sells 5/day — at this rate, stock runs out in 3 days and the supplier lead time is 7 days. Generate `RESTOCK_NEEDED` with severity WARNING."

The engine must also deduplicate — if a `LOW_STOCK` alert already exists and is unacknowledged for this product-source, do not create another one. This prevents alert fatigue.

**Deliverables:**
- `AlertEvaluationService` with:
  - `evaluateAfterMovement(StockMovement)` — reactive hook
  - `evaluateAllProductSources()` — batch evaluation
- Deduplication logic
- `@Scheduled` job for daily proactive evaluation (configurable via `application.yml`)
- Integration with `StockMovementService` (called after each movement)

### Milestone 2.3 — Enhanced ProductSource Thresholds

**What:** Add new configurable fields to `ProductSource` that drive alert logic.

**Explanation:** The existing `lowStockThreshold` is a good start, but the alert engine needs more inputs to generate meaningful alerts:

- `restockPoint`: The stock level at which a restock should be triggered. Different from `lowStockThreshold` — the low stock alert is informational ("heads up"), while the restock point is operational ("place an order now").
- `overstockThreshold`: Upper bound. If stock exceeds this after a restock, alert that we may have over-ordered at this source.
- `leadTimeDays`: How many days it takes the supplier to deliver. Used to calculate "days until stockout" and generate `RESTOCK_NEEDED` alerts early enough.
- `restockQuantity`: The suggested order quantity when restocking. Informational — used later by purchase orders.

These fields are all optional and nullable. If not set, the alert engine skips velocity-based alerts for that product-source and only evaluates simple threshold alerts.

**Deliverables:**
- Flyway migration adding columns to `product_sources`
- Update `ProductSource` entity
- Update product creation/update forms and DTOs to expose these fields
- Update `ProductSourceService` to persist the new fields

### Milestone 2.4 — Alert Dashboard & Acknowledgment UI

**What:** Build an admin view that lists all active alerts with filtering, sorting, and bulk acknowledgment.

**Explanation:** The alert dashboard is the operations team's daily starting point. It answers: "What needs my attention right now?" The view shows:

- A summary bar: X critical, Y warnings, Z info alerts
- A filterable/sortable table of unacknowledged alerts
- Each row shows: product name, source name, alert type, severity, message, age (how long ago it was created)
- Click to expand: structured context details (current stock, threshold, velocity, etc.)
- Acknowledge button (single or bulk): marks as handled, records who and when

Acknowledged alerts move to a "history" tab and are no longer shown on the main dashboard.

**Deliverables:**
- `GET /admin/alerts` — dashboard page
- `POST /admin/alerts/{id}/acknowledge` — single acknowledgment
- `POST /admin/alerts/acknowledge` — bulk acknowledgment (list of IDs)
- Filter by: alert type, severity, source, product, date range
- Sort by: severity (default), created date, product name
- Pagination
- Alert count badge in the admin navigation sidebar

---

## Phase 3 — Purchase Orders & Restock Flow

### Why this phase third

With movements tracking history and alerts telling us when to restock, the natural next step is a structured way to manage incoming inventory. Purchase orders formalize the "we need more stock" workflow into a trackable process with states, quantities, and costs.

### Milestone 3.1 — Purchase Order Schema

**What:** Create the `purchase_orders` and `purchase_order_items` tables.

**Explanation:** A purchase order (PO) represents a request to replenish stock at a specific source. It starts as a draft, gets submitted to the supplier, and progresses through states until the goods are received and stock is updated.

Each PO targets a single source (the receiving location), but contains multiple line items — one per product being ordered. Each line item tracks `quantity_ordered` vs. `quantity_received`, enabling partial receiving (the supplier sends 150 of 200 ordered).

Critically, each line item also records `unit_cost` and `currency`. This captures the purchase price, enabling gross margin analysis later: "We buy for $2.50 and sell for $4.99 at this source."

**Deliverables:**
- Flyway migration for `purchase_orders` and `purchase_order_items`
- `PurchaseOrder` entity with state machine (DRAFT → SUBMITTED → CONFIRMED → PARTIALLY_RECEIVED → RECEIVED → CANCELLED)
- `PurchaseOrderItem` entity
- `PurchaseOrderStatus` enum
- Repositories for both entities

### Milestone 3.2 — Purchase Order Service & State Machine

**What:** Build the business logic for creating, updating, and transitioning purchase orders through their lifecycle.

**Explanation:** The PO lifecycle has strict rules:

- **DRAFT:** Can be freely edited (add/remove items, change quantities). Not yet communicated to the supplier.
- **SUBMITTED:** Locked for editing. The supplier has been notified. Can be cancelled (→ CANCELLED) or confirmed by the supplier (→ CONFIRMED).
- **CONFIRMED:** Supplier has acknowledged the order. Waiting for delivery. Expected delivery date is set.
- **PARTIALLY_RECEIVED:** At least one item has been received, but not all. Each receive action increments `quantity_received` on the relevant items and generates `RESTOCK` movements.
- **RECEIVED:** All items fully received. Final state.
- **CANCELLED:** Order was cancelled. If stock was partially received, those movements remain.

Invalid transitions (e.g., RECEIVED → DRAFT) are rejected with a domain exception.

**Deliverables:**
- `PurchaseOrderService` with:
  - `createDraft(sourceId, items)` — create new PO in DRAFT state
  - `updateDraft(poId, items)` — modify items while still in DRAFT
  - `submit(poId)` — transition to SUBMITTED
  - `confirm(poId, expectedDate)` — transition to CONFIRMED
  - `receiveItems(poId, receivedItems)` — process partial/full receiving
  - `cancel(poId, reason)` — cancel with reason
- State transition validation
- On receive: automatically creates `RESTOCK` stock movements for each received item
- On receive: updates `ProductSource.lastRestockAt` timestamp

### Milestone 3.3 — Cost Tracking on ProductSource

**What:** Add cost-tracking fields to `ProductSource` and update them automatically when receiving PO items.

**Explanation:** Knowing the cost price is essential for margin analysis. When a PO item is received, the system updates:

- `lastCostPrice`: the unit cost from the most recently received PO
- `costCurrency`: the currency of that cost

This is a simple last-cost model. For businesses needing weighted average cost (WAC) or FIFO costing, this can be extended later, but last-cost covers 80% of use cases and is trivial to maintain.

**Deliverables:**
- Flyway migration adding `last_cost_price`, `cost_currency` to `product_sources`
- Update entity and DTOs
- Auto-update on PO receiving
- Display cost and margin on product-source detail view

### Milestone 3.4 — Purchase Order Management UI

**What:** Full CRUD interface for managing purchase orders in the admin panel.

**Explanation:** The PO management UI has three main views:

1. **PO List:** All purchase orders with status filter tabs (Draft / Active / Completed / Cancelled). Shows order number, source, item count, total cost, status, expected date.

2. **PO Detail/Edit (Draft):** Add line items by searching products. Set quantities and unit costs. Save as draft or submit.

3. **PO Receiving:** For CONFIRMED orders, a receiving interface where the admin enters `quantity_received` per item. Partial receiving is supported — receive 50 of 100 today, the rest next week. Each receive generates movements automatically.

The PO list should also be accessible from the product-source detail page, filtered to that specific product-source combination ("show me all POs that include this product for this source").

**Deliverables:**
- `GET /admin/purchase-orders` — list with status filter
- `GET /admin/purchase-orders/new?sourceId=X` — create form
- `GET /admin/purchase-orders/{id}` — detail view
- `POST /admin/purchase-orders/{id}/submit` — submit action
- `POST /admin/purchase-orders/{id}/receive` — receiving form
- Product search autocomplete for adding items
- Validation: cannot submit PO with zero items, cannot receive more than ordered

### Milestone 3.5 — Auto-Generate PO Suggestions

**What:** Use stock levels, velocity, and lead times to suggest purchase orders automatically.

**Explanation:** When a `RESTOCK_NEEDED` alert fires, the system already knows the product, source, and urgency. This milestone connects that to a pre-filled PO draft. An admin reviewing alerts can click "Create Purchase Order" and get a draft PO pre-populated with:

- The target source
- The product(s) that need restocking
- A suggested quantity based on `restockQuantity` (from ProductSource) or calculated from velocity × lead time × safety factor

The admin reviews, adjusts, and submits. This turns alerts into action with minimal friction.

**Deliverables:**
- "Create PO" action button on `RESTOCK_NEEDED` alerts
- Service method to generate PO drafts from alert context
- Batch PO suggestion: "Generate PO drafts for all critical restock alerts at Source X"
- Suggested quantity calculation: `max(restockQuantity, dailyVelocity * leadTimeDays * 1.5)`

---

## Phase 4 — Stock Transfers Between Sources

### Why this phase fourth

With movements, alerts, and purchase orders in place, the next operational need is rebalancing inventory. Source A has 200 units and slow sales; Source B has 5 units and high demand. A transfer solves this without buying more from the supplier.

### Milestone 4.1 — Transfer Schema

**What:** Create the `stock_transfers` and `stock_transfer_items` tables.

**Explanation:** A stock transfer moves inventory from one source to another. It is modeled similarly to a purchase order but involves two internal sources rather than an external supplier. The transfer goes through states:

- **REQUESTED:** Someone requested moving stock from Source A to Source B.
- **APPROVED:** A manager approved the transfer (optional, can be skipped via configuration).
- **IN_TRANSIT:** Items are physically moving between locations.
- **RECEIVED:** Items arrived at the destination. Stock movements generated.
- **CANCELLED:** Transfer was cancelled before receiving.

Each transfer contains items specifying which products and how many units to move.

**Deliverables:**
- Flyway migration for `stock_transfers` and `stock_transfer_items`
- `StockTransfer` entity with status enum
- `StockTransferItem` entity
- Repositories

### Milestone 4.2 — Transfer Service

**What:** Build the transfer lifecycle management service.

**Explanation:** The critical moment is when a transfer is received — it must atomically:

1. Create a `TRANSFER_OUT` movement on the origin source (decreasing stock)
2. Create a `TRANSFER_IN` movement on the destination source (increasing stock)
3. Update `stockQuantity` on both `ProductSource` rows
4. Transition the transfer to RECEIVED

All four operations happen in a single transaction. If the origin source does not have enough stock at the time of receiving (stock may have changed since the transfer was requested), the transfer fails with a clear error.

**Deliverables:**
- `StockTransferService` with lifecycle methods:
  - `requestTransfer(fromSourceId, toSourceId, items)` — create transfer request
  - `approve(transferId)` — optional approval step
  - `markInTransit(transferId)` — update status
  - `receive(transferId)` — atomic stock movement generation
  - `cancel(transferId, reason)` — cancellation
- Stock validation on receive (sufficient stock at origin)
- Cross-reference: movements link back to transfer via `reference_type = TRANSFER`

### Milestone 4.3 — Transfer Suggestions

**What:** Suggest transfers based on stock imbalances across sources.

**Explanation:** The system can identify opportunities for rebalancing by comparing the same product across sources:

- Source A: 200 units, sells 2/day (100 days of stock)
- Source B: 10 units, sells 8/day (1.25 days of stock)

Clearly, Source A can spare units for Source B. The suggestion engine calculates a recommended transfer quantity that equalizes "days of stock" across sources, accounting for lead time between locations.

An admin sees: "Suggested: Transfer 80 units of SKU-123 from Warehouse North to Warehouse South. This gives both locations ~15 days of stock."

**Deliverables:**
- `TransferSuggestionService` with imbalance detection algorithm
- Configurable: minimum imbalance ratio to trigger suggestion (e.g., 3:1)
- Dashboard widget showing suggested transfers
- One-click "Create Transfer Request" from suggestion

### Milestone 4.4 — Transfer Management UI

**What:** Admin interface for viewing, creating, approving, and receiving stock transfers.

**Explanation:** Similar structure to the PO interface:

- **Transfer List:** Filterable by status, origin/destination source
- **Create Transfer:** Select origin source, destination source, and products with quantities
- **Approve/Receive:** Actions on pending transfers
- **History:** Completed and cancelled transfers with movement links

**Deliverables:**
- `GET /admin/transfers` — list page
- `GET /admin/transfers/new` — create form
- `GET /admin/transfers/{id}` — detail with receive action
- Transfer history per product-source on the product detail page
- Validation: cannot transfer to the same source, quantity > 0, origin has stock

---

## Phase 5 — Metrics, Analytics & Dashboards

### Why this phase last

All prior phases generate the data. This phase makes it legible and actionable at scale. Without movements, alerts, POs, and transfers, there is nothing to analyze.

### Milestone 5.1 — Metrics Materialization

**What:** Build a daily job that aggregates movement data into summary metrics per product-source.

**Explanation:** Querying raw movements for reporting is expensive — a product with 1,000 sales per day generates 365,000 movement rows per year. The metrics table stores pre-computed daily summaries:

- Units sold, restocked, returned, transferred in/out
- Revenue (from sales × sell price), cost (from PO cost prices), gross margin
- Average daily sales velocity (rolling 30-day window)
- Days of stock remaining (current stock / velocity)
- Inventory turn rate (annual: cost of goods sold / average inventory value)

The materialization job runs once daily (typically off-peak hours) and inserts one row per active product-source per day. For real-time needs, the movement ledger can be queried directly.

**Deliverables:**
- Flyway migration for `source_product_metrics`
- `SourceProductMetric` entity
- `MetricsMaterializationService` — daily aggregation job
- `@Scheduled` configuration (time, timezone)
- Idempotent: re-running for the same date overwrites, does not duplicate
- Backfill capability: can compute metrics for historical dates from existing movements

### Milestone 5.2 — Source Performance Dashboard

**What:** A high-level dashboard showing how each source is performing.

**Explanation:** This is the "source manager's view" — answering questions like:

- "Which source has the highest revenue this month?"
- "Which source has the most stockouts?"
- "What is the average margin across all products at Source A?"

The dashboard shows:
- Source-level KPIs: total revenue, total margin, active products count, stockout count, average days-of-stock
- Ranking: sources ordered by revenue or margin
- Trend sparklines: 30-day revenue and margin trend per source
- Alert summary: open alerts by severity per source

**Deliverables:**
- `GET /admin/analytics/sources` — source performance dashboard
- Aggregation queries on `source_product_metrics`
- Top/bottom N sources by revenue, margin, stockouts
- Date range selector (7d, 30d, 90d, custom)

### Milestone 5.3 — Product-Source Detail Analytics

**What:** Deep-dive analytics for a specific product at a specific source.

**Explanation:** When an admin clicks into "Chocolate Chip Cookies at Warehouse North," they see a comprehensive page combining:

- **Current state:** Stock level, last restock date, days of stock remaining, sell price, cost price, margin %
- **Sales chart:** Daily units sold over the selected period
- **Stock level chart:** Stock level over time (reconstructed from movements)
- **Price history:** Timeline of price changes
- **Movement log:** Paginated table of all movements (from Phase 1)
- **PO history:** Purchase orders involving this product at this source (from Phase 3)
- **Transfer history:** Transfers in/out (from Phase 4)
- **Active alerts:** Unacknowledged alerts for this product-source

This page becomes the single source of truth for "everything about this product at this source."

**Deliverables:**
- `GET /admin/products/{id}/sources/{sourceId}/analytics` — detail analytics page
- Chart data endpoints (JSON for frontend rendering)
- Stock reconstruction query: `SELECT created_at, balance_after FROM stock_movements WHERE product_source_id = ? ORDER BY created_at`
- Integration with all prior phase data

### Milestone 5.4 — Comparative Analysis

**What:** Compare a product's performance across sources, or compare products within a source.

**Explanation:** Decision-supporting views:

1. **Product across sources:** "How does SKU-123 perform at each source?" Table showing revenue, velocity, margin, days-of-stock side by side for each source. Helps decide where to allocate more stock.

2. **Source product ranking:** "What are the top 20 products at Warehouse North by margin?" Supports assortment decisions — maybe a low-margin product should be discontinued at this source.

3. **Heatmap:** A matrix of products × sources, color-coded by a selected metric (velocity, margin, days-of-stock). Instantly shows outliers and imbalances.

**Deliverables:**
- `GET /admin/analytics/products/{id}/compare-sources` — cross-source comparison
- `GET /admin/analytics/sources/{id}/product-ranking` — product ranking within source
- Sortable tables with export capability (CSV)
- Visual heatmap component (optional, can be Phase 5+)

### Milestone 5.5 — Forecasting & Recommendations (Stretch)

**What:** Use historical velocity data to forecast future stock needs and generate automated recommendations.

**Explanation:** With enough historical data (ideally 60+ days), the system can project:

- "At current velocity, Source B runs out of SKU-456 in 8 days. Given lead time of 5 days, a PO should be placed within 3 days."
- "SKU-789 at Source C has not sold in 45 days. Consider transferring remaining stock to Source A where velocity is 3/day."
- "Demand for SKU-123 is seasonal — velocity doubles in December. Plan restocking accordingly."

This is a stretch milestone because accurate forecasting requires sufficient data and tuning. The initial implementation can be a simple linear projection; more sophisticated models (moving averages, seasonal decomposition) can be added iteratively.

**Deliverables:**
- `ForecastingService` with linear projection based on rolling velocity
- "Stockout forecast" column in dashboards (estimated date)
- Automated weekly "restock recommendation" report per source
- Slow-moving product detection and transfer suggestions

---

## Summary — Phase Dependencies

```
Phase 1: Inventory Ledger
    │
    ├──► Phase 2: Alerts (reads movements for velocity-based alerts)
    │       │
    │       └──► Phase 3: Purchase Orders (alerts trigger PO suggestions)
    │               │
    │               └──► Phase 5: Metrics & Analytics (needs PO cost data for margins)
    │
    └──► Phase 4: Transfers (generates paired movements)
                │
                └──► Phase 5: Metrics & Analytics (includes transfer data)
```

Phases 2, 3, and 4 can run in parallel after Phase 1 is complete. Phase 5 benefits from having all prior phases done but can start with Phase 1 data alone and incrementally incorporate data from later phases.

## Estimated Scope Per Phase

| Phase | New Entities | New Tables | API Endpoints | UI Pages |
|-------|-------------|-----------|---------------|----------|
| 1 — Ledger | 1 | 1 | 2-3 | 2 (history, adjustment form) |
| 2 — Alerts | 1 | 1 | 3-4 | 1 (dashboard + acknowledge) |
| 3 — Purchase Orders | 2 | 2 | 6-8 | 4 (list, create, detail, receive) |
| 4 — Transfers | 2 | 2 | 5-6 | 3 (list, create, detail) |
| 5 — Analytics | 1 | 1 | 5-7 | 4 (source dashboard, detail, compare, ranking) |
