# MediCare: Roadmap (about 9 weeks)

Each phase ends with: a merged PR, green CI, and tests for the new behavior. Adjust timing to the user's availability. Track status in `04-progress-log.md`.

The repo is **one Maven project**. There is no `backend/` folder:

```
MediCare/
├── pom.xml                 the whole app
├── src/main/java/mk/ukim/finki/medicare/...
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/       Flyway migrations
├── src/test/java/
├── frontend/               Angular, built into the jar by Maven
├── docs/medicare/
├── infra/docker-compose.yml
└── .github/workflows/
```

Two commands to remember:

- `./mvnw spring-boot:run` — the whole app on `:8080`
- `./mvnw package` — one jar with Angular inside it

---
## Phase 1: Foundations (week 1)
**Goal:** a repo where every later change is safe to make.

Tasks
- Root `pom.xml`: Spring Boot 4.1.x, Java 25. Package `mk.ukim.finki.medicare`.
- Angular 22 project in `frontend/` (standalone components, zoneless, Vitest).
- Wire the two together with `frontend-maven-plugin`. It downloads Node, runs `npm ci` and `npm run build`, and writes the result into `target/classes/static/` — which is where Spring Boot serves static files from. Result: `./mvnw package` produces one jar containing both.
- **Development mode is different from production mode, and this trips people up.** While developing you run two processes: `./mvnw spring-boot:run` on `:8080` and `npm start` on `:4200`. You open `:4200` so you keep Angular's instant reload. A `frontend/proxy.conf.json` forwards anything starting with `/api` from `:4200` to `:8080`. In production there is only one process and no proxy, because the browser loads everything from `:8080`.
- **The deep-link trap.** Once Angular is served by Spring Boot, opening `http://localhost:8080/triage` directly returns 404. Spring looks for a file called `triage` and finds none. Angular routing only works after `index.html` has loaded. Fix it by telling Spring to serve `index.html` for any path that is not a real file and not under `/api`. Write this in Phase 1 or you will hit it in Phase 4 and lose an evening.
- Docker Compose with PostgreSQL 18; one command starts the database. (Already written, in `infra/`.)
- GitHub Actions: **one** job that runs `./mvnw verify`. Because Maven builds the frontend too, that single command tests both sides.
- Formatting: Spotless for Java, ESLint + Prettier for TypeScript.
- springdoc dependency, so `/swagger-ui.html` documents the API automatically.
- `.gitignore` and `.editorconfig` already target Maven (`target/`, `mvnw`); extend them if the scaffold adds anything new.
- Conventional commits, branch + PR workflow even though the user works alone; review your own PRs.
- An `/api/health` endpoint and an Angular page that calls it and shows the result.

Learning objectives: build automation, how a single deployable is assembled, reproducible environments, CI, a develop/main branch workflow.

Done when: fresh clone, database up, `./mvnw spring-boot:run`, and the browser shows a health status fetched from the backend. CI green.

---
## Phase 2: Domain core (weeks 2-3)
**Goal:** a correct, well-tested domain model behind clean boundaries.

Tasks
- Package by feature, plain layers. One package per feature (`referral`, `patient`), each holding its controller, service, repository, entity and a `dto/` folder. Layout and rules are in `03-stack-and-conventions.md`.
- Entities and value objects: Patient, Referral, Urgency, Modality. A value object is a small immutable type defined by its value, not an id — use Java `record`s.
- Referral state machine driven by data: `PathwayDefinition` and `PathwayTransition` tables, role checks, guard conditions. An invalid transition is rejected with a clear error.
- Flyway migrations for all schema changes. **Never edit a migration that has already run** — add a new one instead.
- REST endpoints for referrals. DTOs are separate from entities, and bean validation (`@NotNull`, `@Size`) runs at the controller boundary.
- RFC 9457 problem-detail error responses from one global `@RestControllerAdvice`.
- Pagination, sorting and filtering on the triage queue endpoint.
- Unit tests for domain rules — table-driven for transitions, so one test method covers every legal and illegal move. Integration tests for persistence with Testcontainers (it starts a real Postgres in Docker for the test).

Learning objectives: single responsibility, depending on abstractions, the State pattern, value objects, validation, error handling, test design.

Done when: every legal and illegal transition is covered by tests, and the Swagger UI shows the referral API.

---
## Phase 3: Scheduling (weeks 4-5)
**Goal:** booking that stays correct under concurrency.

Tasks
- Resource, Slot, Appointment model; slot generation from admin-defined templates.
- Booking in a single transaction. Optimistic locking with `@Version`: each slot row carries a version number, and an update using a stale version fails instead of overwriting. A unique constraint on the database is the final safety net.
- Idempotency key on booking requests, so a retried request does not create a second appointment.
- A conflict returns HTTP 409 with a problem-detail body. Reschedule and cancel rules tied to referral status.
- **The test that matters:** N threads book the same slot against a real Postgres, and exactly one succeeds. Must be reliable in CI, not flaky.

Learning objectives: ACID, isolation levels, race conditions, locking strategies, idempotency, transaction boundaries.

Done when: the concurrency test passes reliably in CI and the user can whiteboard why it works.

---
## Phase 4: Angular app (weeks 6-7)
**Goal:** a usable, accessible, well-structured frontend.

Tasks
- App shell with lazy-loaded feature routes (referrals, triage, scheduling, admin), functional route guards and HTTP interceptors (auth header, error mapping).
- Signals for state (`signal`, `computed`, `effect`); RxJS only where there is a genuine stream. Zoneless change detection.
- Referral form with **Signal Forms** (stable in Angular 22): validation, error display, dirty/touched handling.
- Triage queue: table with server-side pagination, filters, urgency sorting; accept/reject/request-info actions.
- Booking view: calendar/slot picker, optimistic UI that rolls back on a 409.
- Accessibility: keyboard navigation, focus management, labelled controls, live regions for status changes. Use Angular ARIA / CDK where they fit. Run axe checks.
- **TypeScript types are hand-written.** Keep one small `*.model.ts` per feature. Nothing checks that they still match the backend, so whenever you change a controller or a DTO, open `/swagger-ui.html` and update the interface in the same commit. This is the price of dropping the code generator; the discipline is the whole point.
- Tests with Vitest for services, components and guards. Test behavior, not implementation.

Learning objectives: modern Angular architecture, reactive state, form design, accessibility, client-side error handling, testing UI.

Done when: the core flow works end to end in the browser using only the keyboard.

---
## Phase 5: Hardening (week 8)
**Goal:** the things that make it feel like real healthcare software.

Tasks
- Authentication with Spring Security: a `users` table with hashed passwords, a login endpoint, and a JWT the app signs itself. No external identity server — you should be able to explain every step of the login flow.
- Role-based access control enforced on the backend for every endpoint. The frontend may hide what the backend forbids; never the reverse.
- Audit trail: an append-only `AuditEntry` written directly by the service method that changes something, plus an admin view. Writing it inline keeps the whole action readable in one place.
- FHIR facade: read-only endpoints for Patient, ServiceRequest and Appointment, with mapper classes and mapper tests. Pick a FHIR version and be able to say why.
- Observability: structured logging with no PHI, Actuator health and metrics, a correlation id per request.
- Security review pass: input validation, CORS, secrets from environment variables, dependency scanning in CI.

Learning objectives: authentication vs authorization, auditability, interoperability, observability.

Done when: a user without the right role provably cannot perform a restricted action — proven at API level, in a test.

---
## Phase 6: Polish (week 9)
**Goal:** something a reviewer understands in five minutes.

Tasks
- README: what it is, screenshot/GIF, architecture diagram, run instructions, tech choices explained in your own words, what you would do next.
- Seed realistic synthetic data (for example from Synthea) with a script.
- **Two Playwright tests**, no more. Playwright drives a real browser: it logs in, fills the referral form, submits, and checks the result. Write one for the happy path (referral created, triaged, booked) and one permission check (a GP cannot reach the scheduler screen). These catch the bugs that only appear when frontend and backend run together.
- Deploy a demo or record a short walkthrough.
- Cleanup pass: dead code, TODOs, naming, dependency updates to latest stable.
- Write a short "interview notes" doc: the three hard features explained in your own words, trade-offs you rejected, what you learned.
- Final phase review with Claude.

Done when: a stranger can clone, run and understand the project without asking you anything.

---
## Stretch goals (only after Phase 6)
Patient self-booking link, email/SMS notification adapter, waitlist and no-show handling, reporting dashboard, HL7-style event export.
