# <img src="https://wearemobi.com/icon-light.svg" width="24" height="24" valign="middle"> M.O.B.I.™ · SPEC or DOCS change request v1.0

## Context & Goal
Now that the project foundation (M1) is stable on Spring Boot 4 and Java 21, the objective of **M2** is to implement the core **Spring Authorization Server** engine. This will transform the service into a functional OIDC provider capable of issuing tokens and managing authentication flows (Authorization Code + PKCE) for the rest of the M.O.B.I.™ ecosystem.

### Security Configuration
- Configure the `SecurityFilterChain` to handle OAuth2/OIDC protocols.
- Define `RegisteredClientRepository` to manage internal clients (Signature, AI Support).
- Set up `JWKSource` for token signing using a secure, rotating key strategy.

### Identity Flows
- Implement **Authorization Code Flow with PKCE** for secure client-agent communication.
- Configure standard endpoints: `/oauth2/authorize`, `/oauth2/token`, and `/.well-known/openid-configuration`.

### Minimalist Login UI
- Create a lightweight, Mobi-branded login page using **Thymeleaf**.
- Design CSS following the established brand aesthetics (minimalist, high-contrast).
- Ensure the UI is mobile-friendly and accessible.

### User Authentication Integration
- Connect the `MobiUser` domain entity with the Spring Security `UserDetailsService`.
- Implement initial login logic against the H2 database populated in M1.

## Implementation Plan
- [x] This issue is the trigger for a **New SPEC**.
- [x] Target Directory: `docs/`
- [x] Target Filename: `docs/SPEC-v1.1.md`

## Acceptance Criteria (Definition of Done)
- [x] All stakeholders (or R&D) have reviewed the draft.
- [x] The document is merged into the `main` branch under the `docs/` folder.
- [x] Spring Authorization Server dependency configured.
- [x] Default OIDC configuration bean implemented.
- [x] Login form customized with M.O.B.I.™ branding.
- [x] Authorization Code flow verified with a test client (e.g., Postman or Curl).
- [x] PKCE enforcement validated.
- [x] JWKS endpoint returning public keys correctly.

---
Copyright © 2026 **M.O.B.I.™** (Machine Oriented Brilliant Ideas™)  
Transforming ideas into high-impact digital solutions. 🚀  
[wearemobi.com](https://wearemobi.com) · contact@wearemobi.com
