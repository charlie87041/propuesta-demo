---
name: springboot-security
description: Spring Security best practices for authn/authz, validation, CSRF, secrets, headers, rate limiting, and dependency security in Java Spring Boot services.
---

# Spring Boot Security Review

Use when adding auth, handling input, creating endpoints, or dealing with secrets.

## Authentication

- Prefer stateless JWT or opaque tokens with revocation list
- Use `httpOnly`, `Secure`, `SameSite=Strict` cookies for sessions
- Validate tokens with `OncePerRequestFilter` or resource server

```java
@Component
public class JwtAuthFilter extends OncePerRequestFilter {
  private final JwtService jwtService;

  public JwtAuthFilter(JwtService jwtService) {
    this.jwtService = jwtService;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain) throws ServletException, IOException {
    String header = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (header != null && header.startsWith("Bearer ")) {
      String token = header.substring(7);
      Authentication auth = jwtService.authenticate(token);
      SecurityContextHolder.getContext().setAuthentication(auth);
    }
    chain.doFilter(request, response);
  }
}
```

## Authorization (Domain-Ability-Permission Pattern)

Use the hierarchical **Domain → Ability → Permission** model for flexible, multi-tenant authorization.

**See**: [domain-ability-authorization.md](domain-ability-authorization.md) for complete implementation.

**Quick Reference**:
- **Domain**: Tenant/context (e.g., `main-store`)
- **Ability**: Permission bundle (e.g., `ManageInventory`)
- **Permission**: Atomic action (e.g., `products:list`)

```java
// Enable method security
@EnableMethodSecurity

// Use custom annotations
@RequiresPermission("products:list")
public Page<Product> listProducts(@PathVariable String domainCode) { }

@RequiresAbility("manage-inventory")
public void updateStock(@PathVariable String domainCode, ...) { }

// Or SpEL directly
@PreAuthorize("@domainAuthz.check(#domainCode, 'products:create')")
public Product createProduct(@PathVariable String domainCode, ...) { }
```

**Key Rules**:
- Deny by default; no access unless ability grants permission
- Include domain in API paths: `/api/domains/{domainCode}/products`
- Explicit denials override ability grants
- Combine with ownership checks for resource-level security

## Input Validation

- Use Bean Validation with `@Valid` on controllers
- Apply constraints on DTOs: `@NotBlank`, `@Email`, `@Size`, custom validators
- Sanitize any HTML with a whitelist before rendering

## SQL Injection Prevention

- Use Spring Data repositories or parameterized queries
- For native queries, use `:param` bindings; never concatenate strings

## CSRF Protection

- For browser session apps, keep CSRF enabled; include token in forms/headers
- For pure APIs with Bearer tokens, disable CSRF and rely on stateless auth

```java
http
  .csrf(csrf -> csrf.disable())
  .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
```

## Secrets Management

- No secrets in source; load from env or vault
- Keep `application.yml` free of credentials; use placeholders
- Rotate tokens and DB credentials regularly

## Security Headers

```java
http
  .headers(headers -> headers
    .contentSecurityPolicy(csp -> csp
      .policyDirectives("default-src 'self'"))
    .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
    .xssProtection(Customizer.withDefaults())
    .referrerPolicy(rp -> rp.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER)));
```

## Rate Limiting

- Apply Bucket4j or gateway-level limits on expensive endpoints
- Log and alert on bursts; return 429 with retry hints

## Dependency Security

- Run OWASP Dependency Check / Snyk in CI
- Keep Spring Boot and Spring Security on supported versions
- Fail builds on known CVEs

## Logging and PII

- Never log secrets, tokens, passwords, or full PAN data
- Redact sensitive fields; use structured JSON logging

## File Uploads

- Validate size, content type, and extension
- Store outside web root; scan if required

## Checklist Before Release

- [ ] Auth tokens validated and expired correctly
- [ ] Authorization guards on every sensitive path
- [ ] All inputs validated and sanitized
- [ ] No string-concatenated SQL
- [ ] CSRF posture correct for app type
- [ ] Secrets externalized; none committed
- [ ] Security headers configured
- [ ] Rate limiting on APIs
- [ ] Dependencies scanned and up to date
- [ ] Logs free of sensitive data

**Remember**: Deny by default, validate inputs, least privilege, and secure-by-configuration first.

# Skill: springboot-security

## Purpose
Apply security best practices in Spring Boot: authn/authz, CSRF, headers, secrets, and rate limiting.

## When to Use
- When implementing authentication/authorization.
- When creating endpoints and validating inputs.
- When handling secrets or sensitive data.

## Usage
- Use auth filters and `@PreAuthorize`.
- Enable validation with Bean Validation.
- Configure security headers and appropriate CSRF policies.

## Examples
- Implement `JwtAuthFilter` with `OncePerRequestFilter`.
- Configure CSP and `SameSite` in headers.

## Related Skills
- springboot-patterns
- security-review
- springboot-verification
