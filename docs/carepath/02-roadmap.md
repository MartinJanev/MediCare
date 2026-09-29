# CarePath: Roadmap (about 9 weeks)

Each phase ends with: a merged PR, green CI, tests for new behavior, and one ADR. Adjust timing to the user's availability. Track status in `05-progress-log.md`.

---
## Phase 1: Foundations (week 1)
**Goal:** a repo where every later change is safe to make.

Tasks
- Monorepo layout: `/backend` (Spring Boot), `/frontend` (Angular), `/docs` (ADRs, diagrams), `/infra` (Compose).
- Docker Compose with PostgreSQL; one command starts the database.
- Generate Spring Boot 4.1 project (Java, Gradle) and Angular 22 project (standalone, zoneless, Vitest).
- GitHub Actions: build + test for backend and frontend on every push and PR.
- Formatter/linter for both sides (Spotless or Checkstyle; ESLint + Prettier).
- Conventional commits, branch + PR workflow even though the user works alone; review your own PRs.
- OpenAPI skeleton and a health endpoint; generate the TypeScript client from the spec.
- ADR-001: repository layout and build tool. ADR-002: architecture style (hexagonal, feature-based packages).

Learning objectives: build automation, reproducible environments, CI, trunk-based workflow, documenting decisions.

Done when: fresh clone, one command to run everything, CI green, empty app shows a health check end to end.

---
## Phase 2: Domain core (weeks 2-3)
**Goal:** a correct, well-tested domain model behind clean boundaries.

Tasks
- Package by feature, hexagonal: `domain`, `application` (use cases), `adapter.in.web`, `adapter.out.persistence`.
- Entities/value objects: Patient, Referral, Urgency, Modality. Keep domain free of Spring/JPA annotations where practical, or record the trade-off in an ADR.
- Referral state machine driven by data: `PathwayDefinition` and `PathwayTransition` tables, role checks, guard conditions, invalid transition rejected with a clear error.
- Flyway migrations for all schema changes (never edit an applied migration).
- REST endpoints for referrals with DTOs separate from entities, bean validation at the boundary.
- RFC 9457 problem-detail error responses via a global exception handler.
- Pagination, sorting and filtering on the triage queue endpoint.
- Unit tests for domain rules (table-driven for transitions). Integration tests for persistence with Testcontainers.
- ADR-003: state machine as data vs code. ADR-004: DTO/entity mapping approach.

Learning objectives: SRP, dependency inversion, State pattern, value objects, validation, error handling, test design.

Done when: every legal and illegal transition is covered by tests; API contract documented in OpenAPI.

---
## Phase 3: Scheduling (weeks 4-5)
**Goal:** booking that stays correct under concurrency.

Tasks
- Resource, Slot, Appointment model; slot generation from templates (admin-defined).
- Booking use case in a single transaction; optimistic locking with `@Version`; unique constraint as the final safety net.
- Idempotency key on booking requests so retries do not create duplicates.
- Conflict returns 409 with a problem-detail body; reschedule and cancel rules tied to referral status.
- Concurrency test: N threads booking the same slot against real Postgres; exactly one wins.
- Domain events (for example `AppointmentBooked`) published inside the application layer, consumed by the audit trail later.
- ADR-005: optimistic vs pessimistic locking. ADR-006: idempotency approach.

Learning objectives: ACID, isolation, race conditions, locking strategies, idempotency, transaction boundaries.

Done when: the concurrency test is reliable in CI and the user can whiteboard why it works.

---
## Phase 4: Angular app (weeks 6-7)
**Goal:** a usable, accessible, well-structured frontend.

Tasks
- App shell with lazy-loaded feature routes (referrals, triage, scheduling, admin), functional route guards and HTTP interceptors (auth, error mapping).
- Signals for state (`signal`, `computed`, `effect`); RxJS only where a real stream exists. Zoneless change detection.
- Referral form with **Signal Forms** (stable in Angular 22): validation, error display, dirty/touched handling.
- Triage queue: table with server-side pagination, filters, urgency sorting; accept/reject/request-info actions.
- Booking view: calendar/slot picker, optimistic UI with rollback on 409.
- Accessibility: keyboard navigation, focus management, labelled controls, live regions for status changes; use Angular ARIA / CDK where they fit. Run axe checks.
- Use the generated TypeScript client; no hand-written duplicate DTO types.
- Tests with Vitest for services, components and guards; test behavior, not implementation.
- ADR-007: state management approach. ADR-008: component structure and folder layout.

Learning objectives: modern Angular architecture, reactive state, form design, accessibility, client-side error handling, testing UI.

Done when: the core flow works end to end in the browser using only the keyboard.

---
## Phase 5: Hardening (week 8)
**Goal:** the things that make it feel like real healthcare software.

Tasks
- Authentication (JWT/OIDC, for example a Keycloak container in Compose) and backend RBAC using method or request security; frontend hides what the backend forbids, never the reverse.
- Audit trail: append-only `AuditEntry` written from domain events; view for admins.
- FHIR facade: read endpoints for Patient, ServiceRequest, Appointment with mapper classes and mapper tests. ADR-009: FHIR version and scope.
- Playwright end-to-end tests for the main flow and a permissions check.
- Observability: structured logging (no PHI), Actuator health/metrics, correlation id per request.
- Security review pass: input validation, CORS, secrets handling, dependency scanning in CI.
- ADR-010: authentication and authorization design.

Learning objectives: security, auditability, interoperability, end-to-end testing, observability.

Done when: a user without the right role provably cannot perform a restricted action, at API level and in tests.

---
## Phase 6: Polish (week 9)
**Goal:** something a reviewer understands in five minutes.

Tasks
- README: what it is, screenshot/GIF, architecture diagram, one-command run, tech choices with links to ADRs, what you would do next.
- Seed realistic synthetic data (for example from Synthea) with a script.
- Deploy a demo or record a short walkthrough.
- Cleanup pass: dead code, TODOs, naming, dependency updates to latest stable.
- Write a short "interview notes" doc: the three hard features explained in your own words, trade-offs you rejected, what you learned.
- Final phase review with Claude.

Done when: a stranger can clone, run and understand the project without asking you anything.

---
## Stretch goals (only after Phase 6)
Patient self-booking link, email/SMS notification adapter, waitlist and no-show handling, reporting dashboard, HL7-style event export.
