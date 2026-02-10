---
name: backend-patterns
description: Backend architecture patterns, API design, database optimization, and server-side best practices for Java and Spring Boot services.
---

# Backend Development Patterns

Backend architecture patterns and best practices for scalable server-side applications.

## API Design Patterns

### RESTful API Structure

```java
@RestController
@RequestMapping("/api/markets")
class MarketController {
  @GetMapping
  public ResponseEntity<Page<MarketResponse>> list(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) String status,
      @RequestParam(defaultValue = "volume,desc") String sort
  ) {
    // GET /api/markets?status=active&sort=volume,desc&size=20&page=0
    Page<MarketResponse> results = marketService.list(page, size, status, sort);
    return ResponseEntity.ok(results);
  }

  @GetMapping("/{id}")
  public ResponseEntity<MarketResponse> get(@PathVariable UUID id) {
    return ResponseEntity.ok(marketService.getById(id));
  }

  @PostMapping
  public ResponseEntity<MarketResponse> create(@Valid @RequestBody CreateMarketRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(marketService.create(request));
  }

  @PutMapping("/{id}")
  public ResponseEntity<MarketResponse> replace(
      @PathVariable UUID id,
      @Valid @RequestBody UpdateMarketRequest request
  ) {
    return ResponseEntity.ok(marketService.replace(id, request));
  }

  @PatchMapping("/{id}")
  public ResponseEntity<MarketResponse> update(
      @PathVariable UUID id,
      @Valid @RequestBody PatchMarketRequest request
  ) {
    return ResponseEntity.ok(marketService.update(id, request));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    marketService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
```

### Repository Pattern

```java
public interface MarketRepository {
  List<Market> findAll(MarketFilters filters);
  Optional<Market> findById(UUID id);
  Market save(Market market);
  void deleteById(UUID id);
}

@Repository
class JdbcMarketRepository implements MarketRepository {
  private final NamedParameterJdbcTemplate jdbcTemplate;

  JdbcMarketRepository(NamedParameterJdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public List<Market> findAll(MarketFilters filters) {
    String sql = """
        SELECT id, name, status, volume
        FROM markets
        WHERE (:status IS NULL OR status = :status)
        ORDER BY volume DESC
        LIMIT :limit OFFSET :offset
        """;

    MapSqlParameterSource params = new MapSqlParameterSource()
        .addValue("status", filters.status())
        .addValue("limit", filters.limit())
        .addValue("offset", filters.offset());

    return jdbcTemplate.query(sql, params, new MarketRowMapper());
  }

  @Override
  public Optional<Market> findById(UUID id) {
    String sql = "SELECT id, name, status, volume FROM markets WHERE id = :id";
    List<Market> results = jdbcTemplate.query(sql, Map.of("id", id), new MarketRowMapper());
    return results.stream().findFirst();
  }

  @Override
  public Market save(Market market) {
    // Insert/update logic...
    return market;
  }

  @Override
  public void deleteById(UUID id) {
    jdbcTemplate.update("DELETE FROM markets WHERE id = :id", Map.of("id", id));
  }
}
```

### Service Layer Pattern

```java
@Service
class MarketService {
  private final MarketRepository marketRepository;
  private final VectorSearchClient vectorSearchClient;

  MarketService(MarketRepository marketRepository, VectorSearchClient vectorSearchClient) {
    this.marketRepository = marketRepository;
    this.vectorSearchClient = vectorSearchClient;
  }

  public List<Market> searchMarkets(String query, int limit) {
    List<Double> embedding = vectorSearchClient.embed(query);
    List<VectorMatch> matches = vectorSearchClient.search(embedding, limit);

    List<UUID> ids = matches.stream().map(VectorMatch::id).toList();
    Map<UUID, Market> marketById = marketRepository.findAll(new MarketFilters(ids))
        .stream()
        .collect(Collectors.toMap(Market::id, Function.identity()));

    return matches.stream()
        .map(match -> marketById.get(match.id()))
        .filter(Objects::nonNull)
        .sorted(Comparator.comparingDouble(m -> matches.stream()
            .filter(match -> match.id().equals(m.id()))
            .findFirst()
            .map(VectorMatch::score)
            .orElse(0.0)))
        .toList();
  }
}
```

### Middleware Pattern

```java
@Component
class JwtAuthFilter extends OncePerRequestFilter {
  private final JwtVerifier jwtVerifier;

  JwtAuthFilter(JwtVerifier jwtVerifier) {
    this.jwtVerifier = jwtVerifier;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain
  ) throws ServletException, IOException {
    String header = request.getHeader(HttpHeaders.AUTHORIZATION);

    if (header != null && header.startsWith("Bearer ")) {
      String token = header.substring(7);
      Authentication authentication = jwtVerifier.verify(token);
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    filterChain.doFilter(request, response);
  }
}

@Configuration
class SecurityConfig {
  @Bean
  SecurityFilterChain filterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
    return http.csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
  }
}
```

## Database Patterns

### Query Optimization

```java
// ✅ GOOD: Select only needed columns (projection)
public interface MarketSummary {
  UUID getId();
  String getName();
  String getStatus();
  BigDecimal getVolume();
}

public interface MarketJpaRepository extends JpaRepository<MarketEntity, UUID> {
  List<MarketSummary> findTop10ByStatusOrderByVolumeDesc(String status);
}

// ❌ BAD: SELECT * for everything
List<MarketEntity> markets = marketJpaRepository.findAll();
```

### N+1 Query Prevention

```java
// ❌ BAD: N+1 query problem
List<Market> markets = marketRepository.findAll(filters);
for (Market market : markets) {
  market.setCreator(userRepository.findById(market.getCreatorId()).orElseThrow());
}

// ✅ GOOD: Batch fetch
List<UUID> creatorIds = markets.stream()
    .map(Market::getCreatorId)
    .distinct()
    .toList();
Map<UUID, User> creators = userRepository.findAllById(creatorIds)
    .stream()
    .collect(Collectors.toMap(User::getId, Function.identity()));

markets.forEach(market -> market.setCreator(creators.get(market.getCreatorId())));
```

### Transaction Pattern

```java
@Service
class MarketTransactionService {
  private final MarketRepository marketRepository;
  private final PositionRepository positionRepository;

  MarketTransactionService(MarketRepository marketRepository, PositionRepository positionRepository) {
    this.marketRepository = marketRepository;
    this.positionRepository = positionRepository;
  }

  @Transactional
  public Market createMarketWithPosition(CreateMarketRequest marketRequest, CreatePositionRequest positionRequest) {
    Market market = marketRepository.save(Market.fromRequest(marketRequest));
    positionRepository.save(Position.fromRequest(positionRequest, market.getId()));
    return market;
  }
}
```

## Caching Strategies

### Redis Caching Layer

```java
@Service
class CachedMarketService {
  private final MarketRepository marketRepository;

  CachedMarketService(MarketRepository marketRepository) {
    this.marketRepository = marketRepository;
  }

  @Cacheable(value = "markets", key = "#id")
  public Market getById(UUID id) {
    return marketRepository.findById(id).orElseThrow();
  }

  @CacheEvict(value = "markets", key = "#id")
  public void invalidate(UUID id) {
    // cache eviction triggered
  }
}
```

### Cache-Aside Pattern

```java
@Service
class MarketCacheAsideService {
  private final MarketRepository marketRepository;
  private final RedisTemplate<String, Market> redisTemplate;

  MarketCacheAsideService(MarketRepository marketRepository, RedisTemplate<String, Market> redisTemplate) {
    this.marketRepository = marketRepository;
    this.redisTemplate = redisTemplate;
  }

  public Market getMarket(UUID id) {
    String cacheKey = "market:" + id;
    Market cached = redisTemplate.opsForValue().get(cacheKey);

    if (cached != null) {
      return cached;
    }

    Market market = marketRepository.findById(id).orElseThrow();
    redisTemplate.opsForValue().set(cacheKey, market, Duration.ofMinutes(5));
    return market;
  }
}
```

## Error Handling Patterns

### Centralized Error Handler

```java
@ResponseStatus(HttpStatus.BAD_REQUEST)
class ApiException extends RuntimeException {
  ApiException(String message) {
    super(message);
  }
}

@RestControllerAdvice
class GlobalExceptionHandler {
  @ExceptionHandler(ApiException.class)
  ResponseEntity<Map<String, Object>> handleApiException(ApiException ex) {
    return ResponseEntity.badRequest()
        .body(Map.of("success", false, "error", ex.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
    return ResponseEntity.badRequest()
        .body(Map.of("success", false, "error", "Validation failed", "details", ex.getMessage()));
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<Map<String, Object>> handleUnknown(Exception ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(Map.of("success", false, "error", "Internal server error"));
  }
}
```

### Retry with Exponential Backoff

```java
public <T> T fetchWithRetry(Supplier<T> action, int maxRetries) {
  RuntimeException lastError = null;

  for (int i = 0; i < maxRetries; i++) {
    try {
      return action.get();
    } catch (RuntimeException ex) {
      lastError = ex;
      if (i < maxRetries - 1) {
        try {
          Thread.sleep((long) Math.pow(2, i) * 1000L);
        } catch (InterruptedException ignored) {
          Thread.currentThread().interrupt();
        }
      }
    }
  }

  throw lastError;
}
```

## Authentication & Authorization

### JWT Token Validation

```java
@Component
class JwtVerifier {
  private final JwtParser parser;

  JwtVerifier(@Value("${jwt.secret}") String secret) {
    this.parser = Jwts.parserBuilder().setSigningKey(secret.getBytes(StandardCharsets.UTF_8)).build();
  }

  Authentication verify(String token) {
    Claims claims = parser.parseClaimsJws(token).getBody();
    String userId = claims.getSubject();
    String role = claims.get("role", String.class);
    return new UsernamePasswordAuthenticationToken(userId, token, List.of(new SimpleGrantedAuthority(role)));
  }
}
```

### Role-Based Access Control

```java
enum Permission {
  READ, WRITE, DELETE, ADMIN
}

record UserPermissions(UUID userId, Set<Permission> permissions) {}

@Service
class AuthorizationService {
  boolean hasPermission(UserPermissions user, Permission permission) {
    return user.permissions().contains(permission);
  }
}

@RestController
class AdminController {
  private final AuthorizationService authorizationService;

  AdminController(AuthorizationService authorizationService) {
    this.authorizationService = authorizationService;
  }

  @DeleteMapping("/api/admin/markets/{id}")
  public ResponseEntity<Void> deleteMarket(@PathVariable UUID id, Authentication authentication) {
    UserPermissions user = (UserPermissions) authentication.getPrincipal();

    if (!authorizationService.hasPermission(user, Permission.DELETE)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient permissions");
    }

    return ResponseEntity.noContent().build();
  }
}
```

## Rate Limiting

### Simple In-Memory Rate Limiter

```java
class RateLimiter {
  private final Map<String, Deque<Long>> requests = new ConcurrentHashMap<>();

  boolean checkLimit(String identifier, int maxRequests, Duration window) {
    long now = System.currentTimeMillis();
    Deque<Long> times = requests.computeIfAbsent(identifier, key -> new ArrayDeque<>());

    while (!times.isEmpty() && now - times.peekFirst() > window.toMillis()) {
      times.pollFirst();
    }

    if (times.size() >= maxRequests) {
      return false;
    }

    times.addLast(now);
    return true;
  }
}

@RestController
class RateLimitedController {
  private final RateLimiter limiter = new RateLimiter();

  @GetMapping("/api/markets")
  ResponseEntity<String> list(HttpServletRequest request) {
    String ip = Optional.ofNullable(request.getHeader("X-Forwarded-For"))
        .orElse(request.getRemoteAddr());

    if (!limiter.checkLimit(ip, 100, Duration.ofMinutes(1))) {
      return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Rate limit exceeded");
    }

    return ResponseEntity.ok("ok");
  }
}
```

## Background Jobs & Queues

### Simple Queue Pattern

```java
class JobQueue<T> {
  private final BlockingQueue<T> queue = new LinkedBlockingQueue<>();
  private final ExecutorService executor = Executors.newSingleThreadExecutor();

  JobQueue() {
    executor.submit(this::process);
  }

  void add(T job) {
    queue.add(job);
  }

  private void process() {
    while (!Thread.currentThread().isInterrupted()) {
      try {
        T job = queue.take();
        execute(job);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      } catch (Exception ex) {
        // log error
      }
    }
  }

  private void execute(T job) {
    // Job execution logic
  }
}

record IndexJob(UUID marketId) {}

@RestController
class IndexController {
  private final JobQueue<IndexJob> indexQueue = new JobQueue<>();

  @PostMapping("/api/markets/index")
  ResponseEntity<Map<String, Object>> queue(@RequestBody Map<String, UUID> payload) {
    indexQueue.add(new IndexJob(payload.get("marketId")));
    return ResponseEntity.ok(Map.of("success", true, "message", "Job queued"));
  }
}
```

## Logging & Monitoring

### Structured Logging

```java
@Slf4j
@RestController
class MarketLoggingController {
  @GetMapping("/api/markets/logged")
  ResponseEntity<String> listMarkets() {
    String requestId = UUID.randomUUID().toString();

    MDC.put("requestId", requestId);
    log.info("Fetching markets", Map.of("method", "GET", "path", "/api/markets"));

    try {
      // fetch markets
      return ResponseEntity.ok("ok");
    } catch (Exception ex) {
      log.error("Failed to fetch markets", ex);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal error");
    } finally {
      MDC.clear();
    }
  }
}
```

**Remember**: Backend patterns enable scalable, maintainable server-side applications. Choose patterns that fit your complexity level.

# Skill: backend-patterns

## Purpose
Provide backend patterns for APIs, services, repositories, caching, and observability.

## When to Use
- When designing REST endpoints or middlewares.
- To optimize queries and handle errors.
- When adding authentication, rate limiting, or queues.

## Usage
- Separate layers (controller/service/repo).
- Apply cache-aside and centralized error handling.
- Use structured logging patterns.

## Examples
- Implement a `MarketService` with a repository.
- Add rate limiting by IP.
- Use an error handler with `ApiException`.

## Related Skills
- postgres-patterns
- security-review
- frontend-patterns
