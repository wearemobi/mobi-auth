# <img src="https://wearemobi.com/icon-light.svg" width="24" height="24" valign="middle"> M.O.B.I.™ · SPEC or DOCS change request v1.0

## Context & Goal
The objective of **SPEC 1.6 (The Tenant Fortress)** is to evolve the `mobi-auth` service from a basic authentication module into a robust, Enterprise-grade Multi-tenant Identity Provider. This SPEC prepares the architecture for the upcoming `agentic-java` integration (AI Support via Oracle Database 23ai). It addresses data isolation using immutable UUIDs alongside human-readable slugs, expands the RBAC (Role-Based Access Control) to support Machine-to-Machine (M2M) internal communications, and implements continuity mechanisms (Refresh Tokens) to ensure a seamless UI/UX on the M.O.B.I. Dashboard.

Target Milestone: [Milestone 4: Multi-tenancy & DB](https://github.com/wearemobi/mobi-auth/milestone/4)

## Implementation Plan
- [x] This issue is the trigger for a **New SPEC**.
- [x] Target Directory: `docs/`
- [x] Target Filename: `docs/SPEC-v1.6.md`

## Implementation Plan Details

### 1. Extended RBAC (Roles Structure)
Update the `UserEntity` and Spring Security configuration to enforce the following hierarchy:
* `mobi-core-admin`: Global system access for M.O.B.I. Corp operators.
* `mobi-tenant-owner`: The administrative owner of the tenant (Billing, Dashboard access, RAG management).
* `mobi-tenant-user`: Tenant staff (Limited tool access).
* `mobi-tenant-client`: End-users interacting with the tenant's AI Agents.
* `mobi-system-agent`: Internal service role for M2M communication (allowing `agentic-java` to securely interact with the DB and internal APIs without a human credential).

### 2. JWT Payload (Claims) Redesign
Modify `JwtService` to generate tokens that explicitly map both the technical identity and the display identity for multi-tenant isolation.
* Include `orgId` (UUID) as the immutable primary relational key for Oracle DB.
* Include `tenantId` (String/Slug) for routing and aesthetics.
* Include `orgName` (String) for UI contextualization.

*Example Payload:*
```json
{
    "sub": "user@mobi.com",
    "roles": ["mobi-tenant-owner"],
    "tenantId": "logan",
    "orgId": "550e8400-e29b-41d4-a716-446655440000",
    "orgName": "LOGAN CARNICERIA",
    "iat": 1712419200,
    "exp": 1712422800
}
```

### 3. Session Continuity (Refresh Token Flow)
Implement a robust token refresh mechanism to prevent session timeouts while users (e.g., `mobi-tenant-owner`) perform long-running tasks on the Dashboard (like uploading RAG documents).
* Create endpoint `POST /api/v1/auth/refresh`.
* Establish validation logic for long-lived refresh tokens.

### 4. Dynamic CORS Configuration
Adjust `CorsConfigurationSource` to handle multi-tenant origins securely.
* Enable `allowCredentials(true)`.
* Prepare the configuration to accept requests from the central dashboard (`account.wearemobi.com`) and future embedded widgets dynamically.

### 5. OCI Deployment Readiness (Docker Blueprint)
* Standardize the `Dockerfile` using the lightweight multi-stage approach previously validated in `agentic-java` (Eclipse Temurin 21 JRE Alpine).
* Externalize all cryptographic secrets (JWT Secret, expirations) to be dynamically injected via environment variables in the OCI compute instance.

## Acceptance Criteria (Definition of Done)
- [x] All stakeholders (or R&D) have reviewed the draft.
- [x] The document is merged into the `main` branch under the `docs/` folder.
- [x] `UserEntity` successfully persists the new roles and UUIDs to the database.
- [x] JWT validates successfully in postman containing `orgId`, `tenantId`, and `orgName`.
- [x] The `POST /api/v1/auth/refresh` endpoint returns a valid new access token.
- [x] M2M authentication for `mobi-system-agent` is successfully mocked and validated.
- [x] Docker image builds successfully and runs locally exposing the designated port.

---
Copyright © 2026 **M.O.B.I.™** (Machine Oriented Brilliant Ideas™)  
Transforming ideas into high-impact digital solutions. 🚀  
[wearemobi.com](https://wearemobi.com) · contact@wearemobi.com
