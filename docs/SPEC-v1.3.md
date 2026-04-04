# <img src="https://wearemobi.com/icon-light.svg" width="24" height="24" valign="middle"> M.O.B.I.™ · SPEC v1.3: Token Capture & Session Management

## Context & Goal
Now that **M.O.B.I.™ Auth** successfully authenticates against OCI IAM (Hito M3), the next goal is to capture the **Access Token** and **ID Token** returned by the OIDC provider. This allows the application to maintain a rich session and prepares the architecture for downstream authorization (Resource Server role).

## Implementation Plan
- [x] This issue is the trigger for a **New SPEC**.
- [x] Target Directory: `docs/`
- [x] Target Filename: `docs/SPEC-v1.3.md`

### Technical Requirements

#### 1. Token Model (The "Gold Coin")
- Create a DTO (Data Transfer Object) `OciTokenResponse` to map the JSON returned by OCI:
    - `access_token`
    - `id_token`
    - `expires_in`
    - `token_type`
    - `refresh_token` (Optional, but recommended)

#### 2. Provider Refactoring (The "Extraction")
- Update `OciAuthenticationProvider` to:
    - Parse the `ResponseEntity<String>` using Jackson/ObjectMapper.
    - Extract the `access_token` and `id_token`.
    - Store the token inside the `Authentication` object as a detail or create a custom `OciAuthenticationToken`.

#### 3. Session Persistence (Security Context)
- Ensure the captured token is accessible via `SecurityContextHolder`.
- **Debug Route:** Create a protected endpoint `/api/v1/auth/me` that returns the current token details (for testing purposes only in `dev/oci` profiles).

#### 4. Logging & Security
- **Sensitive Data:** Ensure tokens are NEVER printed in logs (use `log.debug` only for "Token captured successfully" without printing the actual string).
- **Error Handling:** Map specific OCI errors (token expired, insufficient scopes) to clear log messages using the existing `HttpClientErrorException` logic.

## Acceptance Criteria (Definition of Done)
- [x] All stakeholders (or R&D) have reviewed the draft.
- [x] The document is merged into the `main` branch under the `docs/` folder.
- [ ] `OciAuthenticationProvider` no longer just returns a boolean success, but returns a Principal containing the OCI Access Token.
- [ ] Successful login redirects to a landing page (e.g., `/home`) instead of the current 404/Whitelabel.
- [ ] A protected controller can retrieve the "Access Token" from the session for verification.
- [ ] No hardcoded logic for token parsing (use Jackson/Lombok).

---
Copyright © 2026 **M.O.B.I.™** (Machine Oriented Brilliant Ideas™)  
Transforming ideas into high-impact digital solutions. 🚀  
[wearemobi.com](https://wearemobi.com) · contact@wearemobi.com
