---
name: spring-web-controllers-architecture
description: Patterns for organizing Spring MVC admin controllers: split API vs view controllers, use ControllerAdvice for model population and exception handling per controller, prefer Bean Validation over manual validation, and keep web package structure consistent. This skill assumes you followed the project structure convention like java/com/mysuperapp/{?app}.
---

# Admin Web Controllers

Use this skill when working on admin web controllers in this repo.

## Structure

- Keep controllers under `/web/controllers/` for each application.
- Keep view-model population and error handling under `/web/interceptos/`.
- Keep web DTOs under `/web/dto/` and subpackages (e.g. `dto/users`).

## Controller Split

- Separate API and view controllers.
- API controllers use `@RestController` and return `ResponseEntity`.
- View controllers use `@Controller` and return view names or redirects.

## Model Population (Views)

- Use `@ControllerAdvice(assignableTypes = ...)` + `@ModelAttribute` to populate common model attributes for a specific controller.
- Avoid repeating `populate*Model(...)` calls inside handlers.
- Add `currentUserId` to the model in the advice when needed by view handlers.
- Prefer routing by handler name when conditions are needed; avoid path checks when the advice is already scoped.

## Route Naming (Views)

- Name view routes with `@RequestMapping(name = "...")` (or `@GetMapping(name = "...")`).
- Use a consistent naming convention, e.g.:
  - `admin.users.list`
  - `admin.users.create`
  - `admin.users.store`
  - `admin.users.edit`
  - `admin.users.update`
  - `admin.users.deactivate`
- In `@ControllerAdvice`, resolve the route name via `HandlerMethod` and branch general data model population by name(pageName, route, handler,...). Controller should populate the specific data like the list of users.

## Exception Handling (Views)

- Use a controller-specific `@ControllerAdvice(assignableTypes = ...)`.
- For GET errors, redirect to list view with flash error.
- For POST errors, return the form view with `errorMessage` in model.
- If a route needs special handling (e.g. `/deactivate`), handle it explicitly in the advice.

## Validation

- Prefer `@Valid` + DTOs over manual `validateForm(...)`.
- Use `@ModelAttribute` for form posts and `@RequestBody` for JSON.
- Keep validation in DTOs; put cross-field or DB-backed validation in the service layer or custom constraints.

## View Models (Serialized Model Data)

Use view models whenever a model attribute needs any kind of “serialization” or derived data.
Do not pass raw domain objects or parallel maps if the view only needs a subset or a derived shape.

Principle: minimum privilege
- The view receives only the fields it needs, nothing more.
- Avoid leaking internal fields or relationships into the model.

Before (raw domain + parallel map):

```java
List<AdminUser> users = adminUserService.listAdminUsersByDomain(domainCode);
Map<Long, String> userRoles = new LinkedHashMap<>();
for (AdminUser user : users) {
    userRoles.put(user.getId(), adminUserService.findPrimaryRoleCode(user.getId(), domainCode));
}

model.addAttribute("users", users);
model.addAttribute("userRoles", userRoles);
```

After (single view model shape):

```java
List<AdminUserViewModel> users = adminUserService.listAdminUsersByDomain(domainCode)
    .stream()
    .map(user -> AdminUserViewModel.from(
        user,
        adminUserService.findPrimaryRoleCode(user.getId(), domainCode)
    ))
    .toList();

model.addAttribute("users", users);
```

- Put view models in `/web/viewmodels/`.
- The template reads from the view model and nothing else.

## Controller Cleanliness

- Keep controller methods focused on HTTP concerns only.
- Move shared logic (general model population and exception flow) to advice classes.
- Avoid duplicating error handling logic across handlers.

## Quick Checks

- View controller methods should not use `@RequestBody`.
- API controllers should not return view names.
- Advice should be scoped with `assignableTypes` to avoid cross-controller side effects.
