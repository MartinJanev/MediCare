# MediCare: Stack and Conventions

Versions below were verified on 2026-09-29. Pin exact versions in `pom.xml` and `package.json`, and re-check for newer stable releases when starting each phase. Do not rely on memory for version numbers; search when in doubt.

## Stack
| Area | Choice | Notes |
|---|---|---|
| Backend | Java 25 (LTS) + Spring Boot 4.1.1 | Boot 4 is built on Spring Framework 7 and Jakarta EE 11 |
| Frontend | Angular 22.1.8 | Standalone components, signals, zoneless (default since 21), Vitest (default since 21), Signal Forms and Angular ARIA stable in 22 |
| Database | PostgreSQL 18 | Flyway migrations |
| Build | **Maven**, one `pom.xml` at the repo root | `frontend-maven-plugin` builds Angular into the same jar |
| API docs | springdoc | Reads the controllers, serves `/swagger-ui.html`. TypeScript types are hand-written to match. |
| Errors | RFC 9457 problem details | A standard JSON error shape; Spring Boot supports it natively |
| Testing | JUnit 5, AssertJ, Mockito, Testcontainers, Vitest, Playwright | Playwright only in Phase 6, two tests |
| Infra | Docker Compose, GitHub Actions | One CI job: `mvn verify` |
| Auth | Spring Security, users table, self-issued JWT | Phase 5. No external identity server. |

## Backend architecture: plain layers, packaged by feature
Group files by **what they are about**, not by what kind of file they are. One package per feature:

```
com.medicare.referral/
  ReferralController.java     HTTP in, HTTP out. No business rules.
  ReferralService.java        The business rules. Owns @Transactional.
  ReferralRepository.java     Talks to the database.
  Referral.java               The entity (a row in a table).
  dto/                        Request and response records.
```

Rules
- **The arrow points one way:** controller -> service -> repository. A repository never calls a service; a service never calls a controller.
- A controller never returns an entity. It returns a DTO. (A DTO is a small record shaped for the API, so changing a database column does not silently change your public API.)
- A DTO never reaches the database. The service converts.
- Business rules live in the service or the entity, never in the controller.
- Constructor injection only — no `@Autowired` on fields. It makes dependencies visible and the class testable without Spring.
- Prefer immutability. Use `record` for DTOs and value objects.
- Validate at the edge with bean validation (`@NotNull`, `@Size`) and enforce real invariants inside the domain.
- Throw domain-specific exceptions. One `@RestControllerAdvice` turns them all into problem-detail responses.

## Frontend architecture
- Feature folders: `features/referrals`, `features/triage`, `features/scheduling`, `features/admin`, plus `core` (auth, interceptors, guards) and `shared` (UI primitives).
- Standalone components only; lazy-loaded routes per feature.
- Signals for local and shared state; services expose read-only signals. RxJS for genuine streams only.
- Smart/container components fetch and coordinate; presentational components take inputs and emit outputs.
- One hand-written `*.model.ts` per feature holding its TypeScript interfaces. When a backend DTO changes, update the interface in the same commit — nothing enforces this automatically.
- Accessibility is a definition-of-done item for every component.

## Git and process
- Trunk-based: short-lived branches, PR into `main`, squash merge.
- Conventional commits: `feat:`, `fix:`, `refactor:`, `test:`, `docs:`, `chore:`.
- CI must be green before merging. No skipped tests without a linked issue.
- Delete a feature branch only after its PR is merged.

## Testing pyramid
- Many fast **unit tests** (domain rules, mappers, pure functions).
- Fewer **integration tests** (persistence with Testcontainers, web layer slices, security rules).
- **Two** end-to-end tests (Playwright), in Phase 6 only.
- The booking concurrency test is mandatory and must not be flaky.
- Test names describe behavior. Prefer real objects over mocks inside the domain.

## Security and privacy defaults
- Synthetic data only. No PHI in logs, error messages or URLs.
- Secrets via environment variables, never committed.
- Least privilege: enforce roles on the backend for every endpoint.
- Audit every state-changing action on referrals and appointments.
- Keep dependencies current; enable dependency scanning in CI.

## Code review checklist (Claude uses this when reviewing)
1. Does it do what the task says, including edge cases and failure paths?
2. Layering respected? Any business logic in a controller? Any entity returned from one?
3. Names reveal intent? Functions do one thing?
4. Tests: behavior covered, failure paths covered, not brittle?
5. Error handling consistent (problem details, no swallowed exceptions)?
6. Security/privacy: authorization checked, no PHI leaked, input validated?
7. Concurrency and transactions: boundaries correct?
8. Angular: signals used sensibly, no leaked subscriptions, accessible markup, model types still matching the backend?
9. Is there a pattern here the user could not justify out loud in an interview? If so, simplify it.
