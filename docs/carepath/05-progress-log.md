# CarePath: Progress Log

Update this after each work session. Claude reads it to know where you are and what to review next.

## Current phase
Phase 1: Foundations (not started)

## Phase checklist
### Phase 1: Foundations
- [ ] Monorepo layout and Docker Compose with PostgreSQL
- [ ] Spring Boot 4.1 (Java) and Angular 22 projects generated
- [ ] CI for backend and frontend
- [ ] Formatters/linters configured
- [ ] OpenAPI skeleton + generated TypeScript client + health check end to end
- [ ] ADR-001, ADR-002
- [ ] Phase review with Claude

### Phase 2: Domain core
- [ ] Patient, Referral, value objects
- [ ] Data-driven referral state machine
- [ ] Flyway migrations
- [ ] Referral REST API with DTOs, validation, problem-detail errors
- [ ] Triage queue pagination/filtering
- [ ] Unit + Testcontainers integration tests
- [ ] ADR-003, ADR-004
- [ ] Phase review with Claude

### Phase 3: Scheduling
- [ ] Resource, Slot, Appointment, slot templates
- [ ] Booking with optimistic locking and unique constraint
- [ ] Idempotency key
- [ ] Concurrency test in CI
- [ ] Domain events
- [ ] ADR-005, ADR-006
- [ ] Phase review with Claude

### Phase 4: Angular app
- [ ] App shell, lazy routes, guards, interceptors
- [ ] Referral form with Signal Forms
- [ ] Triage queue UI
- [ ] Booking view with conflict handling
- [ ] Accessibility pass (keyboard, screen reader, axe)
- [ ] Vitest tests
- [ ] ADR-007, ADR-008
- [ ] Phase review with Claude

### Phase 5: Hardening
- [ ] Authentication and backend RBAC
- [ ] Audit trail
- [ ] FHIR facade with mapper tests
- [ ] Playwright end-to-end tests
- [ ] Observability and security review pass
- [ ] ADR-009, ADR-010
- [ ] Phase review with Claude

### Phase 6: Polish
- [ ] README with architecture diagram
- [ ] Seed data script
- [ ] Demo deployed or recorded
- [ ] Cleanup and dependency update
- [ ] Interview notes
- [ ] Final review with Claude

## Session notes
(Date, what you did, what confused you, what to ask Claude next.)
