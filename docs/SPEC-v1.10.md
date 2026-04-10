# <img src="https://wearemobi.com/icon-light.svg" width="24" height="24" valign="middle"> M.O.B.I.™ · SPEC or DOCS change request v1.0

## Context & Goal
Establish the production infrastructure for the M.O.B.I.™ identity ecosystem. Use a **Hybrid Architecture** where **Oracle DB** acts as the primary Authority Store (Users/Roles) and **Cloudflare KV** acts as the Edge Cache (Profiles).

## Implementation Plan
- [x] This issue is the trigger for a **New SPEC**.
- [x] Target Directory: `docs/`
- [x] Target Filename: `docs/SPEC-v1.10.md`

## Implementation Plan

### 1\. Production Infrastructure (The Oracle Core)

Unlike previous expeditions, this phase assumes a "Greenfield" environment:

* **Data Policy:** No data migration will be performed. All production profiles will be new, ensuring every KV entry is born under the **Signature 2.0** standard.
* **Database Engine:** Full transition from PostgreSQL to **Oracle DB**.
    * **Dev:** Oracle Free (formerly Oracle Express) (replacing PGSQL in docker-compose).
    * **Prod:** Oracle Autonomous Database in OCI.
* **Environment Injection:** Configuration of secrets in Cloudflare (Properties/Secrets) to inject `CLOUDFLARE_API_TOKEN`, `OCI_CREDENTIALS`, and the Oracle `DB_URL`.

### 2\. Edge Connectivity (v1.9 Legacy)

* **Profile Store:** Maintain the **v1.9 Hook** that writes `profile.json` to Cloudflare KV for high-speed global access to user signatures.

### 3\. The Redirection Handshake

* **Navigation:** Implementation of `redirect_uri` for seamless return flows (Postman, Test Apps, or `slug.mobi.bio` in the future).
* **Security:** Strict domain whitelist validation.

## Acceptance Criteria (Definition of Done)
- [x] All stakeholders (or R&D) have reviewed the draft.
- [x] The document is merged into the `main` branch under the `docs/` folder.
- [x] Production Live: The account.wearemobi.com domain is publicly accessible with a valid certificate.
- [x] Handshake Success: A registration flow initiated with ?redirect_uri=https://test.mobi.bio successfully returns the user to said URL after profile creation.
- [x] Edge Confirmation: Users created in production appear instantaneously in the production KV Namespace.

---
Copyright © 2026 **M.O.B.I.™** (Machine Oriented Brilliant Ideas™)  
Transforming ideas into high-impact digital solutions. 🚀  
[wearemobi.com](https://wearemobi.com) · contact@wearemobi.com
