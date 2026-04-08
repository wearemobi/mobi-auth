# <img src="https://wearemobi.com/icon-light.svg" width="24" height="24" valign="middle"> M.O.B.I.™ · SPEC or DOCS change request v1.0

## Context & Goal
The objective of SPEC 1.8 (The Command Center Protocol) is to transition from a single-tier authentication system to a granular Role-Based Access Control (RBAC) architecture. This allows the M.O.B.I. ecosystem to differentiate between Human Operators (Owners/Admins) and Autonomous AI Agents (M2M), ensuring data isolation and securing sensitive administrative endpoints for the upcoming Dashboard.

## Implementation Plan
- [x] This issue is the trigger for a **New SPEC**.
- [x] Target Directory: `docs/`
- [x] Target Filename: `docs/SPEC-v1.8.md`

## Implementation Details

### 1. The Role Hierarchy (Enum Expansion)

Refactor the `Role` domain to support professional-grade permissions:

* `MOBI_ADMIN`: Global cross-tenant visibility (for internal M.O.B.I. operations).
* `MOBI_TENANT_OWNER`: Full control over a specific Organization/Tenant (Human user).
* `MOBI_SYSTEM_AGENT`: Restricted access for M2M services (Read-only or specific task-oriented scopes).

### 2\. Method-Level Security

Enable `@EnableMethodSecurity` in `OciSecurityConfig` to move authorization closer to the business logic:

* Implement `@PreAuthorize("hasRole('ROLE_TENANT_OWNER')")` on sensitive tenant-management endpoints.
* Secure the `/api/v1/auth/token` endpoint to strictly handle `M2M` scopes.

### 3. JWT Claim Enrichment

Ensure the "DNA" of the user is transportable:

* **Roles Mapping:** The JWT `roles` claim must be accurately mapped from the `UserEntity`.
* **Scope Filtering:** Ensure that `SYSTEM_AGENT` tokens do not carry unnecessary human-oriented claims.

### 4. Controller Refactoring

Update `AuthController` and the future `DashboardController` (Command Center) to handle identity resolution based on these roles, providing different JSON responses for Agents vs. Owners.

## Acceptance Criteria (Definition of Done)
- [x] All stakeholders (or R&D) have reviewed the draft.
- [x] The document is merged into the `main` branch under the `docs/` folder.
- [x] **Security Validation:** An AI Agent token is blocked when trying to access `/api/v1/auth/me`.
- [x] **Owner Validation:** A `TENANT_OWNER` can successfully resolve identity and access dashboard resources.
- [x] **Identity Integrity:** Decoded JWTs show correct role strings under the `roles` claim.

---
Copyright © 2026 **M.O.B.I.™** (Machine Oriented Brilliant Ideas™)  
Transforming ideas into high-impact digital solutions. 🚀  
[wearemobi.com](https://wearemobi.com) · contact@wearemobi.com
