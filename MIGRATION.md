# Migration to Spring Boot

## What was migrated

The project started as a console Java application with manual JDBC usage.
It was migrated in phases to Spring Boot with layered architecture:

- Spring Boot application entrypoint (`FoodDeliveryApplication`).
- REST controllers for auth, restaurants, cart, and orders.
- Service layer as Spring beans with constructor injection.
- Repository layer with `JdbcTemplate`.
- Unified API response and error handling.
- Validation for request DTOs.
- Security + password hashing.
- Flyway migrations for database bootstrap.

## Runtime modes

### Main mode: Spring Boot REST API

Default runtime path used for current development and execution.

Run:

```bash
mvn spring-boot:run
```

Optional explicit main class:

```bash
mvn spring-boot:run -Dspring-boot.run.main-class=com.team7.FoodDeliveryApplication
```

### Legacy mode: Console (blocked by default)

Legacy console path is kept temporarily for compatibility and migration safety.

- Default config: `legacy.console.enabled=false`.
- Legacy entrypoints are guarded and require explicit opt-in.

Run legacy mode explicitly:

```bash
mvn spring-boot:run -Dspring-boot.run.main-class=com.team7.Main -Dspring-boot.run.jvmArguments="-Dlegacy.console.enabled=true"
```

## Default legacy flag

The project uses:

- `legacy.console.enabled=false` in `application.yml`.

This means legacy console (`Main`, `userstory/*`) does not run unless enabled explicitly.

## Remaining legacy (to remove in Wave 3)

- Console entrypoint: `Main`.
- Console scenarios: `userstory/*`.
- Legacy DB bootstrap/helpers kept for transition.
- Deprecated fallback constructors kept only where legacy path still needs them.

## Wave 3 target

Planned cleanup (separate step):

- Remove legacy console entrypoints and scenarios.
- Remove legacy DB bootstrap/config classes.
- Remove remaining fallback constructors after legacy path is retired.

