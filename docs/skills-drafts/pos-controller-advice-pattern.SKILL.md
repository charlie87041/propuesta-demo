---
name: pos-controller-advice-pattern
description: Enforce POS/controller architecture with paired ModelAdvice and ExceptionAdvice, both extending BaseAdviceSupport when applicable, including secure currentUserId resolution from SecurityContext.
---

# POS Controller + Advice Pattern

Use this skill whenever you create or modify a Spring MVC controller in this repository, especially in `pos-application` and `admin-module`.

## Goal

Standardize the web layer so each controller has:
1. A `ModelAdvice` that populates shared view context.
2. An `ExceptionAdvice` that handles domain/runtime failures with redirect + flash message.
3. Shared support through `BaseAdviceSupport` for security-context derived user identity.

## Required Pattern

### 1) Controller rules

- Controllers should focus on orchestration only.
- Do not resolve identity from request payload.
- For write operations, use `@ModelAttribute("currentUserId")` provided by advice.
- Keep controller methods thin; move business logic to services.

### 2) ModelAdvice rules

- Create `@ControllerAdvice(assignableTypes = <Controller>.class)`.
- Class must extend `BaseAdviceSupport`.
- In `@ModelAttribute` method, populate:
  - `currentUserId` from `currentUserId()`
  - route context fields needed by layout (`sourceId`, `activeNav`, etc.)
  - default form object if absent (`if (!model.containsAttribute("form")) ...`)
- If authentication principal is missing/invalid, rely on `BaseAdviceSupport` to fail with access denied.

### 3) ExceptionAdvice rules

- Create `@ControllerAdvice(assignableTypes = <Controller>.class)`.
- Handle domain/runtime exceptions used by that controller.
- Use `FlashMap` + redirect to return UX-friendly error messages.
- Keep redirect target deterministic and derived from request path/ids.
- If the advice needs `currentUserId` or security helpers, extend `BaseAdviceSupport`.

### 4) BaseAdviceSupport rules

- `BaseAdviceSupport` is the single source for extracting authenticated user id from `SecurityContext`.
- No controller/advice should parse auth principal ad-hoc.
- If module-specific principal type differs, keep one module-local `BaseAdviceSupport` with same intent and method names.

## Implementation Checklist (Mandatory)

When adding a new controller:

1. Create/update `ModelAdvice` for that controller.
2. Create/update `ExceptionAdvice` for that controller.
3. Ensure `ModelAdvice` extends `BaseAdviceSupport`.
4. Ensure write endpoints consume `currentUserId` from model advice, not from client payload.
5. Ensure view forms are wired to controller mappings (`th:action`, proper method, CSRF if enabled).
6. Ensure invalid binding returns/redirects with usable feedback.
7. Ensure route names and model attributes match existing layout conventions.

## Anti-Patterns to Avoid

- Passing `userId` in form body for identity-sensitive actions.
- Duplicating `enrichModel()` logic in controllers when advice can provide it.
- Throwing raw exceptions to end user without flash error handling.
- Creating advice classes without `assignableTypes`, causing cross-controller leakage.

## Suggested File Locations

- Controller: `.../web/.../*Controller.java`
- ModelAdvice: `.../web/advice/model/...`
- ExceptionAdvice: `.../web/advice/exception/...`
- Base support: `.../web/advice/support/BaseAdviceSupport.java`

## Optional Enhancements (Recommended)

If the controller manages forms and redirects:
- Add a small mapper/helper for converting domain exceptions to message keys.
- Centralize route-name constants to reduce string drift.
- Add integration tests for:
  - auth principal propagation to `currentUserId`
  - validation failure path
  - exception advice redirect + flash message path
