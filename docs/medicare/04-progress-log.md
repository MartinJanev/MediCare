# MediCare: Progress Log

Update this after each work session. Claude reads it to know where you are and what to review next.

## Current phase
Phase 1: Foundations (not started)

## Phase checklist
### Phase 1: Foundations
- [ ] Root `pom.xml` — Spring Boot 4.1.x, Java 25
- [ ] Angular 22 project in `frontend/`
- [ ] `frontend-maven-plugin` builds Angular into the jar (`mvn package` produces one artifact)
- [ ] `proxy.conf.json` so `npm start` on :4200 reaches the backend on :8080
- [ ] Deep-link fallback: unknown non-`/api` paths serve `index.html`
- [ ] Docker Compose with PostgreSQL (done, in `infra/`)
- [ ] CI: one GitHub Actions job running `mvn verify`
- [ ] Spotless, ESLint, Prettier
- [ ] springdoc Swagger UI reachable
- [ ] `/api/health` endpoint displayed by an Angular page
- [ ] Phase review with Claude

### Phase 2: Domain core
- [ ] Patient, Referral, value objects
- [ ] Data-driven referral state machine (`PathwayDefinition`, `PathwayTransition`)
- [ ] Flyway migrations
- [ ] Referral REST API with DTOs, validation, problem-detail errors
- [ ] Triage queue pagination/filtering
- [ ] Unit tests (table-driven transitions) + Testcontainers integration tests
- [ ] Phase review with Claude

### Phase 3: Scheduling
- [ ] Resource, Slot, Appointment, slot templates
- [ ] Booking with optimistic locking and a unique constraint
- [ ] Idempotency key on booking requests
- [ ] 409 + problem detail on conflict
- [ ] Concurrency test green and non-flaky in CI
- [ ] Phase review with Claude

### Phase 4: Angular app
- [ ] App shell, lazy routes, guards, interceptors
- [ ] Referral form with Signal Forms
- [ ] Triage queue UI
- [ ] Booking view with conflict handling
- [ ] Hand-written model types per feature, checked against Swagger UI
- [ ] Accessibility pass (keyboard, screen reader, axe)
- [ ] Vitest tests
- [ ] Phase review with Claude

### Phase 5: Hardening
- [ ] Users table, login endpoint, self-issued JWT
- [ ] Role checks on every endpoint
- [ ] Audit trail written directly by the service, plus admin view
- [ ] FHIR read endpoints with mapper tests
- [ ] Structured logging (no PHI), Actuator, correlation id
- [ ] Security review pass
- [ ] Phase review with Claude

### Phase 6: Polish
- [ ] README with architecture diagram
- [ ] Seed data script
- [ ] Two Playwright tests (happy path + permission check)
- [ ] Demo deployed or recorded
- [ ] Cleanup and dependency update
- [ ] Interview notes
- [ ] Final review with Claude

## Session notes
(Date, what you did, what confused you, what to ask Claude next.)
