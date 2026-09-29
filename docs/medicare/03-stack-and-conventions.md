# MediCare: Stack and Conventions

Versions below were current as of late September 2026. Pin exact versions in the repo and re-check for newer stable releases when starting each phase. Do not rely on memory for version numbers; search when in doubt.

## Stack
| Area | Choice | Notes |
|---|---|---|
| Frontend | Angular 22 (released June 3, 2026) | Standalone components, signals, zoneless (default since 21), Vitest (default since 21), Signal Forms and Angular ARIA stable in 22 |
| Backend | Java (current LTS, 25) + Spring Boot 4.1.x | Boot 4 is built on Spring Framework 7 and Jakarta EE 11; 4.2 is expected around November 2026 |
| Database | PostgreSQL (current stable) | Flyway migrations |
| Build | Gradle for backend, npm/pnpm for frontend | Record the choice in ADR-001 |
| API | OpenAPI as source of truth | Generated TypeScript client; problem-detail (RFC 9457) errors |
| Testing | JUnit 5, Mockito/AssertJ, Testcontainers, Vitest, Playwright | |
| Infra | Docker Compose, GitHub Actions | |
| Auth | JWT/OIDC (Keycloak in Compose) | Phase 5 |

## Backend architecture: hexagonal, package by feature
```
com.medicare.<feature>/
  domain/            entities, value objects, domain services, domain events (no web/persistence details)
  application/       use cases (one class per use case), ports (interfaces)
  adapter/in/web/    controllers, request/response DTOs, mappers
  adapter/out/persistence/   JPA entities, repositories, mappers implementing ports
  adapter/out/...    other outbound adapters (notifications, FHIR)
```
Rules
- Dependencies point inward: adapters depend on application, application depends on domain, never the reverse.
- Controllers contain no business logic. Use cases own transaction boundaries.
- DTOs never leak into the domain; JPA entities never returned from controllers.
- Constructor injection only. Prefer immutability, records for DTOs and value objects.
- Validate at the boundary (bean validation) and enforce invariants in the domain.
- Exceptions: domain-specific, mapped to problem-detail responses in one global handler.

## Frontend architecture
- Feature folders: `features/referrals`, `features/triage`, `features/scheduling`, `features/admin`, plus `core` (auth, interceptors, guards) and `shared` (UI primitives).
- Standalone components only; lazy-loaded routes per feature.
- Signals for local and shared state; services expose read-only signals. RxJS for genuine streams only.
- Smart/container components fetch and coordinate; presentational components take inputs and emit outputs.
- Typed API client generated from OpenAPI; do not hand-write duplicate types.
- Accessibility is a definition-of-done item for every component.

## Git and process
- Trunk-based: short-lived branches, PR into `main`, squash merge.
- Conventional commits: `feat:`, `fix:`, `refactor:`, `test:`, `docs:`, `chore:`.
- CI must be green before merging. No skipped tests without a linked issue.
- One ADR per significant decision, numbered, in `/docs/adr`.

## Testing pyramid
- Many fast **unit tests** (domain rules, mappers, pure functions).
- Fewer **integration tests** (persistence with Testcontainers, web layer slices, security rules).
- A few **end-to-end tests** (Playwright) covering the critical flow and permissions.
- Concurrency test for booking is mandatory.
- Test names describe behavior. Prefer real objects over mocks inside the domain.

## Security and privacy defaults
- Synthetic data only. No PHI in logs, error messages or URLs.
- Secrets via environment variables, never committed.
- Least privilege: enforce roles on the backend for every endpoint.
- Audit every state-changing action on referrals and appointments.
- Keep dependencies current; enable dependency scanning in CI.

## Code review checklist (Claude uses this when reviewing)
1. Does it do what the task says, including edge cases and failure paths?
2. Layering respected? Any business logic in a controller or adapter?
3. Names reveal intent? Functions do one thing?
4. Tests: behavior covered, failure paths covered, not brittle?
5. Error handling consistent (problem details, no swallowed exceptions)?
6. Security/privacy: authorization checked, no PHI leaked, input validated?
7. Concurrency and transactions: boundaries correct?
8. Angular: signals used sensibly, no unnecessary subscriptions, accessible markup?
9. Is there an ADR-worthy decision that is not recorded?
