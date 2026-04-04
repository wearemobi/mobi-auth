# <img src="https://wearemobi.com/icon-light.svg" width="24" height="24" valign="middle"> M.O.B.I.™ · SPEC or DOCS change request v1.0

## Context & Goal
The objective is to transform **M.O.B.I.™ Auth** into an **Identity Broker**. We will leverage **Oracle Cloud Infrastructure (OCI) IAM** as the Source of Truth for user credentials, avoiding local password storage while maintaining the custom M.O.B.I.™ B&W User Interface.

## Implementation Plan
- [x] This issue is the trigger for a **New SPEC**.
- [x] Target Directory: `docs/`
- [x] Target Filename: `docs/SPEC-v1.2.md`

## Technical Requirements

### 0: OCI Infrastructure (Prerequisites)
Before any code is deployed, the following OCI resources MUST be provisioned:

1. **OCI Identity Domain:** A dedicated domain (or the Default one) configured for M.O.B.I.™
2. **Confidential Application:** Registered in OCI IAM to provide:
    - `Client ID`
    - `Client Secret`
    - `OpenID Connect Discovery Endpoint`
3. **Test Users:** At least one user created in OCI with assigned groups for role-mapping tests.

### 1. Identity Brokering (The "M.O.B.I. Vanilla Flow)
- **Primary UI:** The login process MUST remain within the M.O.B.I.™ custom Thymeleaf/CSS pages.
- **Backend Validation:** Spring Security will be configured to validate credentials against **OCI Identity Domains** using the OIDC Resource Owner Password Credentials (ROPC) or a delegated Auth-Code flow.
- **Token Exchange:** Upon OCI validation, M.O.B.I.™ Auth will issue its own signed JWT (the one already tested in Postman).

### 2. Agentic-Java Support (M2M)
- **Client Credentials Flow:** Enable secure access for AI Agents/Widgets (MobiAI Chat).
- **Scope Mapping:** Agents will receive specific scopes (e.g., `mobi:chat:read`) to interact with the ecosystem without a human in the loop.

### 3. Open Source Security & Portability
- **Zero-Secret Policy:** Remove `admin/mobi2026` and all hardcoded secrets.
- **Environment Variables:** All sensitive data (Client IDs, OCI Tenancy details, Secrets) must be injected via:
    - `MOBI_AUTH_UPSTREAM_URL`
    - `MOBI_AUTH_CLIENT_SECRET`
    - `MOBI_OCI_TENANCY_ID`

### 4. Multi-tenancy Prep
- Structure the logic to support different OCI Domains/Tenants based on the login context (preparatory for M4).

## Acceptance Criteria (Definition of Done)
- [x] All stakeholders (or R&D) have reviewed the draft.
- [ ] The document is merged into the `main` branch under the `docs/` folder.
- [ ] M.O.B.I.™ Login form remains the primary entry point (No direct redirect to OCI UI).
- [ ] Users managed in OCI IAM can log in successfully through M.O.B.I.™
- [ ] Postman flow (M2) remains functional and unchanged for external clients.
- [ ] README.md updated with Environment Variable instructions.
- [ ] Hardcoded development credentials removed from `main`.

---
Copyright © 2026 **M.O.B.I.™** (Machine Oriented Brilliant Ideas™)  
Transforming ideas into high-impact digital solutions. 🚀  
[wearemobi.com](https://wearemobi.com) · contact@wearemobi.com
