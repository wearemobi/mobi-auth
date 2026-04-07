# <img src="https://wearemobi.com/icon-light.svg" width="24" height="24" valign="middle"> M.O.B.I.™ · SPEC v1.7

## Context & Goal
The objective of **SPEC 1.7 (The Identity & Gateway Protocol)** is to bridge the gap between the `mobi-auth` backend and the user-facing Dashboard. This specification enables secure cross-domain communication (CORS), implements **Just-In-Time (JIT) Provisioning** for Oracle-authenticated users, and provides a dedicated endpoint to resolve identity for a personalized UI experience.

## Implementation Plan
- [x] This issue is the trigger for a **New SPEC**.
- [x] Target Directory: `docs/`
- [x] Target Filename: `docs/SPEC-v1.7.md`

## Implementation Details

### 1. Dynamic CORS Configuration
To allow the Dashboard (running on a separate domain/port) to interact with the Auth Server:
* **Origin Mapping:** Support for `http://localhost:5500` (Local Dev) and future `*.mobi.local` domains (via `/etc/hosts`).
* **Allowed Headers:** Include `Authorization`, `Content-Type`, and `X-Requested-With`.
* **Credentials:** Enable `allowCredentials(true)` to support contextual sessions.

### 2. JIT (Just-In-Time) Provisioning & Event Hook
Instead of a separate registration flow, we will "hook" into the successful OCI authentication:
* **Interception Logic:** Within `OciAuthenticationProvider`, if a user is authenticated via Oracle but missing in the local `mobi_user` table, they will be created automatically.
* **Automatic Forging:** Generate an immutable `orgId` (UUID) and a unique `tenantId` (slug) upon first login.
* **Domain Events:** Use `ApplicationEventPublisher` to fire a `UserRegisteredEvent`.
* **Onboarding Listener:** A dedicated `@EventListener` will catch this event to prepare for future downstream provisioning (e.g., Cloudflare KV for Signature 2.0).

### 3. The Identity Endpoint (`GET /api/v1/auth/me`)
A secure resource that returns the "DNA" of the authenticated user:
* **Input:** Valid Bearer JWT in the Authorization header.
* **Logic:** Decode JWT -> Extract Email -> Fetch `UserEntity` -> Map to `MobiUserDTO`.
* **Payload:** Includes `email`, `roles`, `tenantId`, `orgId`, and `orgName`.

### 4. Frontend Integration (`profile.html`)
* **Vanilla JS Fetch:** Call `/api/v1/auth/me` on page load using the stored JWT.
* **Security Interceptor:** If the response is `401 Unauthorized`, clear local storage and redirect to `login.html`.
* **DOM Update:** Dynamically populate user profile fields with the returned JSON data.

## Acceptance Criteria (Definition of Done)
- [x] All stakeholders (or R&D) have reviewed the draft.
- [ ] The document is merged into the `main` branch under the `docs/` folder.
- [ ] CORS Pre-flight (`OPTIONS`) requests return `200 OK` from a different local origin.
- [ ] JIT Logic: New OCI users are automatically persisted in the `mobi_user` table upon first login.
- [ ] Event Hook: System logs confirm the `UserRegisteredEvent` is fired and caught.
- [ ] `/api/v1/auth/me` returns the correct JSON payload.
- [ ] `profile.html` successfully displays dynamic tenant data.

---
Copyright © 2026 **M.O.B.I.™** (Machine Oriented Brilliant Ideas™)  
Transforming ideas into high-impact digital solutions. 🚀  
[wearemobi.com](https://wearemobi.com) · contact@wearemobi.com
