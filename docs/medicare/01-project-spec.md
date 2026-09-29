# MediCare: Project Specification

A small radiology referral and scheduling system. It is an independent portfolio project inspired by the problem space of Sorsix (eReferrals and triage, digital patient pathways, RIS scheduling, FHIR interoperability). It is not a clone of Pinga and uses no Sorsix branding.

## Users and roles
| Role | Can do |
|---|---|
| GP (referrer) | Create/submit referrals, view own referrals and their status, answer information requests |
| Triage clinician | Review the triage queue, accept/reject/request info, set priority |
| Scheduler | Book accepted referrals into slots on a resource (scanner/room), reschedule, cancel |
| Patient (stretch) | Confirm or self-book via a link |
| Admin | Manage resources, slot templates, pathway configuration, users |

## Core flow
1. GP creates a referral (patient, modality such as MRI/CT/Ultrasound/X-ray, body region, urgency, clinical notes).
2. Referral enters the **triage queue**.
3. Triage clinician accepts, rejects (with reason) or requests more information.
4. Scheduler books an accepted referral into an available **slot** on a **resource**.
5. Patient is notified/confirms (stretch: self-booking link).
6. Exam status progresses: scheduled, arrived, in progress, completed. Cancellation possible before completion.

## Three deliberately hard features
Each exists because it forces a real engineering problem. These stay, even as everything around them is kept simple.

1. **Referral lifecycle as a data-driven state machine.** Statuses and allowed transitions (with required role and guard conditions) are stored as rows in the database, not as hardcoded `if` chains. Mirrors Sorsix's configurable "pathway" idea. Practices: State pattern, validation, test tables.
2. **Slot booking that cannot double-book.** Two schedulers booking the same slot at the same moment must result in exactly one success and one clean conflict error. Practices: transactions, optimistic locking (a version column that makes a stale update fail), unique constraints, idempotency keys (a client-supplied id that makes a retry safe), concurrency tests against a real Postgres.
3. **FHIR-shaped API edge.** FHIR is the standard data format healthcare systems exchange. The internal model stays independent of it, and a thin translation layer maps `Patient` -> FHIR Patient, `Referral` -> ServiceRequest, `Appointment` -> Appointment. Practices: anti-corruption layer, mapper tests, contract thinking.

## Cross-cutting requirements
- **Security:** users log in with a username and password; the app issues its own token (JWT) and checks roles on the backend for every endpoint. Least privilege. No secrets in the repo.
- **Audit trail:** who did what to which referral/appointment and when; append-only. The service method writes the audit row directly.
- **Privacy:** synthetic data only (for example Synthea), no PHI in logs, GDPR-aware design notes in the README.
- **Accessibility:** keyboard operable, screen-reader friendly, sensible focus management, contrast. Clinical UIs must be usable by everyone.
- **API quality:** springdoc reads the controllers and publishes a Swagger UI, so the API documents itself. TypeScript types on the frontend are written by hand to match. RFC 9457 problem-detail errors (a standard JSON error shape). Pagination and filtering on list endpoints.

## Domain sketch (starting point, the user should refine it)
- `Patient` (id, name, date of birth, identifier)
- `Referral` (id, patient, referrer, modality, region, urgency, clinical notes, status, pathway, timestamps)
- `PathwayDefinition` + `PathwayTransition` (from status, to status, required role, guard)
- `Resource` (scanner/room, modality supported)
- `Slot` (resource, start, end, status, version)
- `Appointment` (referral, slot, status, idempotency key)
- `AuditEntry` (actor, action, subject, before/after summary, timestamp)

## Non-goals
Real DICOM/PACS integration, real billing/claims, real patient data, mobile apps, multi-tenancy, AI features (the point of this project is fundamentals).

## Definition of "portfolio ready"
- README with an architecture diagram and run instructions: start Postgres with Docker Compose, then one Maven command runs the whole app.
- A short "why I built it this way" section in the README, in the user's own words.
- CI green with unit, integration and end-to-end tests.
- Seeded demo data and a live or recorded demo.
- The user can explain every design decision in an interview, out loud, without notes.
