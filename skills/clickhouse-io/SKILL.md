---
name: clickhouse-io
description: ClickHouse database patterns, query optimization, analytics, and data engineering best practices for high-performance analytical workloads.
---

# ClickHouse Analytics Patterns

ClickHouse-specific patterns for high-performance analytics and data engineering.

## Overview

ClickHouse is a column-oriented database management system (DBMS) for online analytical processing (OLAP). It's optimized for fast analytical queries on large datasets.

**Key Features:**
- Column-oriented storage
- Data compression
- Parallel query execution
- Distributed queries
- Real-time analytics

## Table Design Patterns

### MergeTree Engine (Most Common)

```sql
CREATE TABLE markets_analytics (
    date Date,
    market_id String,
    market_name String,
    volume UInt64,
    trades UInt32,
    unique_traders UInt32,
    avg_trade_size Float64,
    created_at DateTime
) ENGINE = MergeTree()
PARTITION BY toYYYYMM(date)
ORDER BY (date, market_id)
SETTINGS index_granularity = 8192;
```

### ReplacingMergeTree (Deduplication)

```sql
-- For data that may have duplicates (e.g., from multiple sources)
CREATE TABLE user_events (
    event_id String,
    user_id String,
    event_type String,
    timestamp DateTime,
    properties String
) ENGINE = ReplacingMergeTree()
PARTITION BY toYYYYMM(timestamp)
ORDER BY (user_id, event_id, timestamp)
PRIMARY KEY (user_id, event_id);
```

### AggregatingMergeTree (Pre-aggregation)

```sql
-- For maintaining aggregated metrics
CREATE TABLE market_stats_hourly (
    hour DateTime,
    market_id String,
    total_volume AggregateFunction(sum, UInt64),
    total_trades AggregateFunction(count, UInt32),
    unique_users AggregateFunction(uniq, String)
) ENGINE = AggregatingMergeTree()
PARTITION BY toYYYYMM(hour)
ORDER BY (hour, market_id);

-- Query aggregated data
SELECT
    hour,
    market_id,
    sumMerge(total_volume) AS volume,
    countMerge(total_trades) AS trades,
    uniqMerge(unique_users) AS users
FROM market_stats_hourly
WHERE hour >= toStartOfHour(now() - INTERVAL 24 HOUR)
GROUP BY hour, market_id
ORDER BY hour DESC;
```

## Query Optimization Patterns

### Efficient Filtering

```sql
-- ✅ GOOD: Use indexed columns first
SELECT *
FROM markets_analytics
WHERE date >= '2025-01-01'
  AND market_id = 'market-123'
  AND volume > 1000
ORDER BY date DESC
LIMIT 100;

-- ❌ BAD: Filter on non-indexed columns first
SELECT *
FROM markets_analytics
WHERE volume > 1000
  AND market_name LIKE '%election%'
  AND date >= '2025-01-01';
```

### Aggregations

```sql
-- ✅ GOOD: Use ClickHouse-specific aggregation functions
SELECT
    toStartOfDay(created_at) AS day,
    market_id,
    sum(volume) AS total_volume,
    count() AS total_trades,
    uniq(trader_id) AS unique_traders,
    avg(trade_size) AS avg_size
FROM trades
WHERE created_at >= today() - INTERVAL 7 DAY
GROUP BY day, market_id
ORDER BY day DESC, total_volume DESC;

-- ✅ Use quantile for percentiles (more efficient than percentile)
SELECT
    quantile(0.50)(trade_size) AS median,
    quantile(0.95)(trade_size) AS p95,
    quantile(0.99)(trade_size) AS p99
FROM trades
WHERE created_at >= now() - INTERVAL 1 HOUR;
```

### Window Functions

```sql
-- Calculate running totals
SELECT
    date,
    market_id,
    volume,
    sum(volume) OVER (
        PARTITION BY market_id
        ORDER BY date
        ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
    ) AS cumulative_volume
FROM markets_analytics
WHERE date >= today() - INTERVAL 30 DAY
ORDER BY market_id, date;
```

## Data Insertion Patterns

### Bulk Insert (Recommended)

```java
ClickHouseDataSource dataSource = new ClickHouseDataSource(
    System.getenv("CLICKHOUSE_URL"),
    new Properties() {{
      put("user", System.getenv("CLICKHOUSE_USER"));
      put("password", System.getenv("CLICKHOUSE_PASSWORD"));
    }}
);

// ✅ Batch insert (efficient)
void bulkInsertTrades(List<Trade> trades) throws SQLException {
  String sql = "INSERT INTO trades (id, market_id, user_id, amount, timestamp) VALUES (?, ?, ?, ?, ?)";

  try (Connection conn = dataSource.getConnection();
       PreparedStatement stmt = conn.prepareStatement(sql)) {
    for (Trade trade : trades) {
      stmt.setString(1, trade.id());
      stmt.setString(2, trade.marketId());
      stmt.setString(3, trade.userId());
      stmt.setBigDecimal(4, trade.amount());
      stmt.setTimestamp(5, Timestamp.from(trade.timestamp()));
      stmt.addBatch();
    }
    stmt.executeBatch();
  }
}

// ❌ Individual inserts (slow)
void insertTrade(Trade trade) throws SQLException {
  // Don't do this in a loop!
  try (Connection conn = dataSource.getConnection();
       PreparedStatement stmt = conn.prepareStatement(
           "INSERT INTO trades (id, market_id, user_id, amount, timestamp) VALUES (?, ?, ?, ?, ?)")) {
    stmt.setString(1, trade.id());
    stmt.setString(2, trade.marketId());
    stmt.setString(3, trade.userId());
    stmt.setBigDecimal(4, trade.amount());
    stmt.setTimestamp(5, Timestamp.from(trade.timestamp()));
    stmt.executeUpdate();
  }
}
```

### Streaming Insert

```java
// For continuous data ingestion
ClickHouseClient client = ClickHouseClient.newInstance(ClickHouseProtocol.HTTP);

void streamInserts(Stream<Trade> tradeStream) throws Exception {
  try (ClickHouseResponse response = client.connect(System.getenv("CLICKHOUSE_URL"))
      .write()
      .format(ClickHouseFormat.JSONEachRow)
      .table("trades")
      .data(output -> {
        tradeStream.forEach(trade -> {
          String json = String.format(
              "{\"id\":\"%s\",\"market_id\":\"%s\",\"user_id\":\"%s\",\"amount\":%s,\"timestamp\":\"%s\"}%n",
              trade.id(), trade.marketId(), trade.userId(), trade.amount(), trade.timestamp()
          );
          try {
            output.write(json.getBytes(StandardCharsets.UTF_8));
          } catch (IOException e) {
            throw new UncheckedIOException(e);
          }
        });
      })
      .executeAndWait()) {
    // handle response if needed
  }
}
```

## Materialized Views

### Real-time Aggregations

```sql
-- Create materialized view for hourly stats
CREATE MATERIALIZED VIEW market_stats_hourly_mv
TO market_stats_hourly
AS SELECT
    toStartOfHour(timestamp) AS hour,
    market_id,
    sumState(amount) AS total_volume,
    countState() AS total_trades,
    uniqState(user_id) AS unique_users
FROM trades
GROUP BY hour, market_id;

-- Query the materialized view
SELECT
    hour,
    market_id,
    sumMerge(total_volume) AS volume,
    countMerge(total_trades) AS trades,
    uniqMerge(unique_users) AS users
FROM market_stats_hourly
WHERE hour >= now() - INTERVAL 24 HOUR
GROUP BY hour, market_id;
```

## Performance Monitoring

### Query Performance

```sql
-- Check slow queries
SELECT
    query_id,
    user,
    query,
    query_duration_ms,
    read_rows,
    read_bytes,
    memory_usage
FROM system.query_log
WHERE type = 'QueryFinish'
  AND query_duration_ms > 1000
  AND event_time >= now() - INTERVAL 1 HOUR
ORDER BY query_duration_ms DESC
LIMIT 10;
```

### Table Statistics

```sql
-- Check table sizes
SELECT
    database,
    table,
    formatReadableSize(sum(bytes)) AS size,
    sum(rows) AS rows,
    max(modification_time) AS latest_modification
FROM system.parts
WHERE active
GROUP BY database, table
ORDER BY sum(bytes) DESC;
```

## Common Analytics Queries

### Time Series Analysis

```sql
-- Daily active users
SELECT
    toDate(timestamp) AS date,
    uniq(user_id) AS daily_active_users
FROM events
WHERE timestamp >= today() - INTERVAL 30 DAY
GROUP BY date
ORDER BY date;

-- Retention analysis
SELECT
    signup_date,
    countIf(days_since_signup = 0) AS day_0,
    countIf(days_since_signup = 1) AS day_1,
    countIf(days_since_signup = 7) AS day_7,
    countIf(days_since_signup = 30) AS day_30
FROM (
    SELECT
        user_id,
        min(toDate(timestamp)) AS signup_date,
        toDate(timestamp) AS activity_date,
        dateDiff('day', signup_date, activity_date) AS days_since_signup
    FROM events
    GROUP BY user_id, activity_date
)
GROUP BY signup_date
ORDER BY signup_date DESC;
```

### Funnel Analysis

```sql
-- Conversion funnel
SELECT
    countIf(step = 'viewed_market') AS viewed,
    countIf(step = 'clicked_trade') AS clicked,
    countIf(step = 'completed_trade') AS completed,
    round(clicked / viewed * 100, 2) AS view_to_click_rate,
    round(completed / clicked * 100, 2) AS click_to_completion_rate
FROM (
    SELECT
        user_id,
        session_id,
        event_type AS step
    FROM events
    WHERE event_date = today()
)
GROUP BY session_id;
```

### Cohort Analysis

```sql
-- User cohorts by signup month
SELECT
    toStartOfMonth(signup_date) AS cohort,
    toStartOfMonth(activity_date) AS month,
    dateDiff('month', cohort, month) AS months_since_signup,
    count(DISTINCT user_id) AS active_users
FROM (
    SELECT
        user_id,
        min(toDate(timestamp)) OVER (PARTITION BY user_id) AS signup_date,
        toDate(timestamp) AS activity_date
    FROM events
)
GROUP BY cohort, month, months_since_signup
ORDER BY cohort, months_since_signup;
```

## Data Pipeline Patterns

### ETL Pattern

```java
// Extract, Transform, Load
@Service
class MarketEtlJob {
  private final PostgresClient postgresClient;
  private final ClickHouseLoader clickHouseLoader;

  MarketEtlJob(PostgresClient postgresClient, ClickHouseLoader clickHouseLoader) {
    this.postgresClient = postgresClient;
    this.clickHouseLoader = clickHouseLoader;
  }

  @Scheduled(fixedRateString = "PT1H")
  public void etlPipeline() {
    List<MarketRow> rawData = postgresClient.fetchMarketRows();

    List<MarketAggregate> transformed = rawData.stream()
        .map(row -> new MarketAggregate(
            row.createdAt().toLocalDate(),
            row.marketSlug(),
            new BigDecimal(row.totalVolume()),
            Integer.parseInt(row.tradeCount())
        ))
        .toList();

    clickHouseLoader.bulkInsert(transformed);
  }
}
```

### Change Data Capture (CDC)

```java
// Listen to PostgreSQL changes and sync to ClickHouse
Connection pgConnection = DriverManager.getConnection(System.getenv("DATABASE_URL"));
PGConnection pg = pgConnection.unwrap(PGConnection.class);
pg.addNotificationListener((processId, channel, payload) -> {
  MarketUpdate update = MarketUpdate.fromJson(payload);
  clickHouseLoader.insertUpdate(new ClickHouseMarketUpdate(
      update.id(),
      update.operation(), // INSERT, UPDATE, DELETE
      Instant.now(),
      update.newDataJson()
  ));
});

try (Statement stmt = pgConnection.createStatement()) {
  stmt.execute("LISTEN market_updates");
}
```

## Best Practices

### 1. Partitioning Strategy
- Partition by time (usually month or day)
- Avoid too many partitions (performance impact)
- Use DATE type for partition key

### 2. Ordering Key
- Put most frequently filtered columns first
- Consider cardinality (high cardinality first)
- Order impacts compression

### 3. Data Types
- Use smallest appropriate type (UInt32 vs UInt64)
- Use LowCardinality for repeated strings
- Use Enum for categorical data

### 4. Avoid
- SELECT * (specify columns)
- FINAL (merge data before query instead)
- Too many JOINs (denormalize for analytics)
- Small frequent inserts (batch instead)

### 5. Monitoring
- Track query performance
- Monitor disk usage
- Check merge operations
- Review slow query log

**Remember**: ClickHouse excels at analytical workloads. Design tables for your query patterns, batch inserts, and leverage materialized views for real-time aggregations.

# Skill: clickhouse-io

## Purpose
Guide table design, queries, and analytical pipelines in ClickHouse.

## When to Use
- When modeling OLAP or high-volume analytics data.
- When optimizing queries, aggregations, and materialized views.
- For batch or streaming ingestion to ClickHouse.

## Usage
- Select the engine (MergeTree/Aggregating/Replacing).
- Define partitioning and ordering keys based on queries.
- Insert in batches and use materialized views for aggregates.

## Examples
- Create a MergeTree table with monthly partitioning.
- Define a materialized view for hourly metrics.
- Query retention with cohorts.

## Related Skills
- postgres-patterns
- backend-patterns
