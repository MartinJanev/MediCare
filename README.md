# MediCare

An independent portfolio project: a small radiology referral and scheduling system,
inspired by the healthtech problem space (eReferrals and triage, digital patient
pathways, scheduling, FHIR interoperability). Not affiliated with, endorsed by, or
derived from any company or commercial product.

## What it does

A referral travels from a GP to a booked exam slot:

1. A GP creates a referral (patient, modality, body region, urgency, clinical notes).
2. The referral enters a triage queue.
3. A triage clinician accepts it, rejects it with a reason, or requests more information.
4. A scheduler books an accepted referral into a free slot on a resource (scanner or room).
5. Exam status progresses: scheduled → arrived → in progress → completed.

## Why it exists

Three features were chosen because each forces a real engineering problem:

- **Data-driven referral state machine** — statuses and allowed transitions live in
  configuration tables with required roles and guard conditions, not in hardcoded `if` chains.
- **Booking that cannot double-book** — optimistic locking, a unique constraint as the final
  safety net, idempotency keys, and a concurrency test against real Postgres where exactly
  one of N concurrent bookings wins.
- **FHIR-shaped API edge** — the internal domain stays FHIR-independent; an anti-corruption
  layer maps to FHIR Patient / ServiceRequest / Appointment.

## Stack

Spring Boot (Gradle) · Angular (standalone components, signals) · PostgreSQL · Flyway ·
Docker Compose. Backend is hexagonal and packaged by feature; the TypeScript API client is
generated from OpenAPI.

## Data and privacy

**Synthetic data only** (e.g. Synthea). No real patient data ever enters this repository or a
running instance. No PHI in logs, error messages, or URLs. Secrets come from environment
variables and are never committed.

## Status

Pre-Phase 1: documentation only, no application code yet. Run instructions land with the
Phase 1 scaffold.

## Docs

- Specification, roadmap, and conventions: [`docs/medicare/`](docs/medicare/)
- Architecture decision records: [`docs/adr/`](docs/adr/)

## Contributing

Trunk-based: short-lived branches, PR into `main` (required by branch rule), squash merge,
conventional commits.

## License

See [`LICENSE`](LICENSE).
