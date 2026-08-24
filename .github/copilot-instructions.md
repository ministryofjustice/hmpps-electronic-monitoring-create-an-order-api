# GitHub Copilot Instructions — hmpps-electronic-monitoring-create-an-order-api

## 1. Before Starting Any Feature

1. Ask for the Jira ticket ID (`ELM-XXXX`) if it has not been provided.
2. Clarify ambiguous requirements before writing code.
3. Find the nearest analogous controller, service, model, repository, and test,
   then follow the existing pattern.
4. Run the smallest relevant test suite to establish a baseline.

- Make surgical changes only. Do not refactor unrelated code.
- Add or update tests with every behaviour change.
- Do not change test assertions without understanding the behaviour they
  protect.
- Do not add a dependency when an existing library or project pattern can solve
  the problem.
- Keep business rules out of controllers, listeners, repositories, and HTTP
  clients.
- If a JPA entity or database schema changes, add a new Flyway migration. Never
  edit a migration that may already have run.
- Preserve authentication, authorisation, validation, audit, and feature-flag
  behaviour unless the requirement explicitly changes it.

### Checks before completing a task

```bash
./gradlew unitTest     # Unit and slice tests
./gradlew integration  # Integration tests; requires LocalStack
./gradlew check        # CI validation, including tests and linting
```

Use the Gradle wrapper, not a locally installed Gradle version. Prefer a
targeted test selector while developing, then run the relevant suite.

## 2. Project Architecture

This project is a Kotlin 2 / Java 21 Spring Boot REST API. It validates and
persists electronic monitoring orders, stores supporting documents, consumes
Common Platform court hearing events, and submits completed orders to the Field
Monitoring Service (FMS).

The codebase uses a conventional layered Spring architecture:

- `src/main/kotlin/.../resource/` — REST controllers. Handle HTTP routing,
  authentication context, request validation, status codes, and response
  mapping only.
- `src/main/kotlin/.../listener/` — SQS delivery adapters. Parse messages and
  delegate processing to services.
- `src/main/kotlin/.../service/` — application workflows and business rules.
- `src/main/kotlin/.../service/strategy/` — FMS submission strategies for new
  orders, variations, and disabled integrations.
- `src/main/kotlin/.../service/courthearing/` — Common Platform event mapping
  and processing.
- `src/main/kotlin/.../repository/` — Spring Data JPA repositories, projections,
  specifications, and persistence queries.
- `src/main/kotlin/.../client/` — HTTP adapters for FMS, Document Management,
  Manage User, and authentication services.
- `src/main/kotlin/.../models/` — JPA entities and order-domain behaviour.
- `src/main/kotlin/.../models/dto/` — API request and response DTOs.
- `src/main/kotlin/.../models/fms/` — FMS payloads, responses, and mapping logic.
- `src/main/kotlin/.../models/courthearing/` — Common Platform mapping models.
- `src/main/kotlin/.../config/` — security, feature flags, WebClient, OpenAPI,
  exception handling, and application configuration.
- `src/main/resources/db/migration/` — forward-only Flyway migrations.

The normal dependency flow is:

```text
Controller or SQS listener -> Service -> Repository or external client
```

Controllers and listeners must not access repositories directly. Repositories
must not contain business decisions. External clients handle transport details
and error translation, not order workflow decisions.

Do not attempt a repository-wide Clean Architecture rewrite. When changing an
area, improve its boundaries locally without renaming or restructuring unrelated
code.

## 3. Order and Versioning Model

`Order` is the aggregate root. It contains a list of `OrderVersion` entities,
and most order properties delegate to `Order.getCurrentVersion()`.

- Preserve the distinction between:
  - the stable order UUID (`Order.id`);
  - the order-version entity UUID (`OrderVersion.id`);
  - the sequential numeric version (`OrderVersion.versionId`).
- Read and update form sections through the current version unless the
  operation explicitly targets history.
- When creating a version, copy child entities with a new entity UUID and the
  new `OrderVersion.id`.
- Do not reuse JPA child identities between versions.
- Preserve cascade and orphan-removal semantics when changing relationships.
- Draft updates must enforce ownership and `IN_PROGRESS` status using
  `OrderSectionServiceBase` helpers where applicable.
- Save section updates through `updateLastUpdatedByAndSaveOrder` so audit fields
  remain correct.
- Keep invariants that apply to every order operation on the aggregate/model.
  Keep workflow-specific rules in the relevant service.

## 4. Adding or Changing an API Endpoint

1. Define or update request/response DTOs in `models/dto`.
2. Add Jakarta validation annotations or an existing custom validator.
3. Add the route to the nearest matching controller in `resource`.
4. Delegate the operation to a service; do not put business logic in the
   controller.
5. Use `OrderSectionServiceBase` for editable order-section workflows.
6. Update JPA entities and add a new Flyway migration if persistence changes.
7. Add service tests for rules and integration tests for the HTTP contract.

For new endpoints, prefer purpose-specific DTO responses over exposing JPA
entities. Existing entity-returning endpoints may be improved incrementally
when touched.

All order routes normally use `/api/orders/...` and require:

```kotlin
@PreAuthorize("hasRole('ROLE_EM_CEMO__CREATE_ORDER')")
```

Reuse the central `HmppsElectronicMonitoringCreateAnOrderApiExceptionHandler`
and existing custom exceptions. Do not catch broad exceptions or return
success-shaped fallback responses.

## 5. Persistence and Flyway

- Repository interfaces belong in `repository`.
- Use Spring Data method names, JPQL, projections, or specifications following
  the nearest existing example.
- Keep authorisation and business filtering in services unless it is purely a
  persistence query concern.
- Add schema changes as the next numbered migration:

```text
src/main/resources/db/migration/V{next}__Description.sql
```

- Never modify an existing migration to change an already-deployed schema.
- Keep PostgreSQL migrations, JPA annotations, H2 test behaviour, and fixtures
  consistent.
- Add repository tests for custom queries, specifications, projections, or
  relationship mapping changes.

## 6. External Services and Events

- Keep WebClient calls and endpoint paths in `client`.
- Keep FMS payload construction and request-type branching in `models/fms` and
  `service/strategy`.
- Keep orchestration, persistence of submission results, and order status
  transitions in services.
- Continue using the existing synchronous `.block()` model unless deliberately
  refactoring the complete call path.
- Feature-gated behaviour must use `FeatureFlags` or the existing Spring
  property pattern and retain safe defaults.
- Court hearing failures must retain dead-letter and telemetry behaviour.
  Never silently acknowledge malformed or failed SQS messages.
- Never log OAuth tokens, client secrets, document contents, or sensitive
  personal data.

## 7. Kotlin Conventions

Follow `.editorconfig` and nearby code:

| Thing | Convention | Example |
| --- | --- | --- |
| Indentation | 2 spaces | Kotlin and Gradle files |
| Maximum line length | 120 characters | Wrap fluent calls and arguments |
| Classes | `PascalCase` | `AddressService` |
| Functions and variables | `camelCase` | `updateAddress` |
| Constants and enum values | `UPPER_SNAKE_CASE` | `IN_PROGRESS` |
| Controllers | `[Feature]Controller` | `OrderController` |
| Services | `[Feature]Service` | `AddressService` |
| DTOs | Action-oriented `[Name]Dto` | `UpdateAddressDto` |
| Tests | `[ClassUnderTest]Test` | `OrderServiceTest` |

- Use trailing commas and Kotlin official style.
- Prefer constructor injection in new code.
- Reuse existing enums instead of introducing magic strings.
- Preserve null safety; avoid adding `!!` when validation, a guard, or an
  explicit exception can express the invariant.
- Use `java.time` types consistently with surrounding models.
- Add comments only when the intent cannot be expressed clearly in code.

## 8. Testing

This project uses JUnit 5, AssertJ, Mockito/Mockito-Kotlin, Spring Boot Test,
WebTestClient, WireMock, H2, and LocalStack.

- Service tests belong under `src/test/kotlin/.../service`.
- Model and mapping tests belong under `src/test/kotlin/.../model`.
- Repository tests belong under `src/test/kotlin/.../repository`.
- Full HTTP, security, client, SQS, and persistence tests belong under
  `src/test/kotlin/.../integration`.
- Reuse `IntegrationTestBase` for application integration tests.
- Reuse `UpdateOrderIntegrationTestBase` for order-section endpoints.
- Reuse `OrderSectionServiceTestBase` for section service tests where
  applicable.
- Reuse the existing WireMock extensions rather than creating ad-hoc servers.
- Extend scenario JSON fixtures for complete order-to-FMS mapping behaviour.

Each endpoint change should cover:

- the successful response status and body;
- validation failures;
- missing or inaccessible orders;
- ownership/cohort and authentication behaviour where relevant;
- persistence and audit-field effects;
- versioning behaviour when the operation copies or changes an order version.

Each external integration change should cover successful responses, expected
error responses, and the resulting service behaviour.
