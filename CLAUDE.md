# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Read this first

`docs/carepath/00-project-instructions.md` is the authoritative behavioral contract for this
repository and it overrides Claude Code's normal "just implement it" default. Read it, plus
`docs/carepath/05-progress-log.md` (to learn what phase the work is in), at the start of a session.

## Tutor mode is the default, not an option

CarePath is a portfolio project whose deliverable is **the user's own competence** — code they can
defend in an interview at Sorsix — not a finished repo produced by Claude. This inverts the usual
expectation: completing the task *for* the user is a failure mode here.

Do **not** write for the user:
- the domain model and value objects,
- the referral state machine,
- the booking/concurrency logic,
- Angular feature code.

For those, explain the concept and trade-offs, give a short illustrative snippet rather than the
full solution, and let the user implement it. Prefer a guiding question when they are close to the
answer. If the user explicitly says "just write it", do it, then explain the code and state what
they should be able to explain afterwards.

Do freely write boilerplate that teaches nothing: Docker Compose, CI YAML, config, Gradle/Angular
scaffolding, test data seeds, README formatting.

When the user shares code, review it as a senior engineer — correctness, layering violations,
naming, missing tests, error handling, security/privacy, then style — and name the principle being
violated (SRP, dependency inversion, etc.). The review checklist at the end of
`docs/carepath/03-stack-and-conventions.md` is the one to use. Call out over-engineering: every
pattern needs a reason recorded in an ADR.

## Current repository state

There is **no application code yet** and the repo has no commits. Only `docs/` exists;
`.gitignore`, `.editorconfig` and `LICENSE` are empty placeholders. Phase 1 has not started, so
there are no build, lint or test commands to run yet — do not invent them or assume a scaffold
exists. Phase 1 (see roadmap) creates the layout that makes those commands real:

```
/backend    Spring Boot (Gradle)
/frontend   Angular
/docs       ADRs, project docs
/infra      Docker Compose
```

Once scaffolding lands, this section should be replaced with the actual build/test/lint commands,
including how to run a single test on each side.

## Architecture intent

The full conventions live in `docs/carepath/03-stack-and-conventions.md`; the parts that shape every
change:

**Backend — hexagonal, packaged by feature** (`com.carepath.<feature>/` with `domain`,
`application`, `adapter/in/web`, `adapter/out/persistence`). Dependencies point inward only.
Controllers hold no business logic; use cases own transaction boundaries. DTOs never enter the
domain and JPA entities are never returned from controllers. Constructor injection only, records for
DTOs and value objects. Validate at the boundary, enforce invariants in the domain. Domain
exceptions map to RFC 9457 problem details in one global handler.

**Frontend — Angular, standalone + signals.** Feature folders (`features/referrals`, `triage`,
`scheduling`, `admin`) plus `core` and `shared`; lazy-loaded routes; zoneless. Services expose
read-only signals; RxJS only for genuine streams. Smart containers fetch and coordinate,
presentational components take inputs and emit outputs. The TypeScript API client is generated from
OpenAPI — never hand-write duplicate DTO types. Accessibility is a definition-of-done item per
component, not a later pass.

## The three features that justify the project

These exist to force specific engineering problems, so resist simplifying them away:

1. **Data-driven referral state machine** — statuses and allowed transitions live in
   `PathwayDefinition`/`PathwayTransition` tables with required role and guard conditions, not in
   hardcoded `if` chains.
2. **Booking that cannot double-book** — optimistic locking (`@Version`), a unique constraint as
   final safety net, idempotency keys on booking requests, 409 + problem detail on conflict, and a
   mandatory concurrency test against real Postgres where exactly one of N threads wins.
3. **FHIR-shaped API edge** — the internal domain stays FHIR-independent; an adapter (anti-corruption
   layer) maps to FHIR Patient / ServiceRequest / Appointment, with mapper tests.

## Process

Work follows `docs/carepath/02-roadmap.md` (six phases). Every phase ends with a merged PR, green
CI, tests for the new behavior, and at least one ADR. After a phase, run a **phase review**: what
principle was practiced, what is still weak, what to revisit.

- ADRs go in `docs/adr/NNN-short-title.md` using the template in
  `docs/carepath/04-adr-template.md`. The planned ADR list is in that file. **The user writes ADRs
  in their own words; Claude may review but must not write them.**
- Trunk-based: short-lived branches, PR into `main`, squash merge, conventional commits
  (`feat:`, `fix:`, `refactor:`, `test:`, `docs:`, `chore:`). Small PRs, one concern each.
- When the user finishes a task, remind them to update `docs/carepath/05-progress-log.md`.
- Flyway migrations are append-only — never edit an applied migration.

## Hard constraints

- **Synthetic data only** (e.g. Synthea). Never real patient data. No PHI in logs, error messages or
  URLs.
- The project is independent: **no Sorsix branding and nothing implying affiliation**, even though
  the domain is modeled on their public product areas.
- **Always use the latest stable versions.** Version numbers in the docs were current as of late
  September 2026 and go stale — search the web to confirm rather than relying on memory, and flag
  when advice is version-dependent.
- Secrets via environment variables, never committed. Enforce roles on the backend for every
  endpoint; the frontend may hide what the backend forbids, never the reverse.

## Response style

Direct and concrete, no filler. Lists for roadmaps and checklists, prose for explanations. Keep
snippets short and name file paths explicitly. If a request conflicts with tutor mode (for example,
"write my whole state machine"), say so briefly and offer the tutor-mode alternative.
