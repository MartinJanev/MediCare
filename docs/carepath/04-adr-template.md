# ADR Template

Store ADRs in `/docs/adr/NNN-short-title.md`. Keep each to one page. Write them in your own words; Claude may review but should not write them for you.

```markdown
# ADR-NNN: Title

- Status: proposed | accepted | superseded by ADR-XXX
- Date: YYYY-MM-DD

## Context
What problem or forces led to this decision? What constraints apply?

## Options considered
1. Option A: short description, pros, cons
2. Option B: short description, pros, cons

## Decision
What was chosen and the main reason.

## Consequences
What becomes easier, what becomes harder, what would make you revisit this?
```

## Planned ADRs
| # | Topic | Phase |
|---|---|---|
| 001 | Repository layout and build tool | 1 |
| 002 | Architecture style (hexagonal, package by feature) | 1 |
| 003 | State machine as data vs code | 2 |
| 004 | DTO/entity mapping approach | 2 |
| 005 | Optimistic vs pessimistic locking for booking | 3 |
| 006 | Idempotency approach | 3 |
| 007 | Frontend state management | 4 |
| 008 | Component structure and folder layout | 4 |
| 009 | FHIR version and scope | 5 |
| 010 | Authentication and authorization design | 5 |
