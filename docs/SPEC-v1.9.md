# <img src="https://wearemobi.com/icon-light.svg" width="24" height="24" valign="middle"> M.O.B.I.™ · SPEC or DOCS change request v1.0

## Context & Goal

The objective of **SPEC 1.9 (The Edge Bridge Protocol)** is to establish an automated synchronization between the `mobi-auth` backend (OCI) and **Cloudflare Workers/KV**. This enables **Signature 2.0**, allowing user profiles and authorization DNA to exist at the edge for sub-millisecond validation and global scalability.

## Implementation Plan

- [x] This issue is the trigger for a **New SPEC**.
- [x] Target Directory: `docs/`
- [x] Target Filename: `docs/SPEC-v1.9.md`

## Implementation Details

### 1. Cloudflare Sync Service & Mapper

Implement a dedicated Java service (`CloudflareSyncService`) to orchestrate the edge synchronization:

* **MobiProfileMapper:** A core engineering component to transform `UserEntity` and `BioEntity` data into the finalized "Robin-style" JSON format.
* **Event Listener:** Intercept the `UserRegisteredEvent` (JIT) and future `BioUpdatedEvent` triggers.
* **Asynchronous Push:** Utilize Spring's `WebClient` or the Cloudflare API to update KV stores without blocking the main authentication flow.

### 2. Signature 2.0 Schema (The Poneglyph)

The `profile.json` object stored in Cloudflare KV using the **Master Slug** as the key:

* **Root:** `slug` (Unique identity anchor for URLs).
* **Node `auth`:** `uid` (Immutable UUID), `username`, `name` (Registration name), `email`, `tenantId`, `orgId`, `orgName`, `roles`, `status`.
* **Node `bio`:**
    * **`signature`**: `name` (Public display name), `headline`, `avatarUrl`, `about`, `content`.
    * **`urls`**: Dynamic map of social/professional links (GitHub, Website, LinkedIn, etc.).
* **Node `metadata`:** Extensible block for system configuration, subscription tiers, and Agentic Directives.

### 3. Digital Identity Assets (The Lighthouse)

Establish **`cdn.wearemobi.com`** as the central repository for identity resources:

* Host official **JSON Schemas** for M2M/Agentic validation.
* Serve static assets and technical documentation for the M.O.B.I.™ Identity Provider ecosystem.

### 4. Unified Edge Validation

Enable Cloudflare Workers to perform decentralized identity resolution:

* **Integrity Handshake:** Compare the JWT `sub` claim against the `auth.uid` or `auth.email` stored in the KV.
* **RBAC Enforcement:** Validate `auth.roles` at the edge before granting access to protected dashboard resources.

## Acceptance Criteria (Definition of Done)
- [x] All stakeholders (or R&D) have reviewed the draft.
- [x] The document is merged into the `main` branch under the `docs/` folder.
- [x] **Data Integrity:** The JSON stored in the KV strictly adheres to the official Mobi-Schema.
- [x] **Sync Speed:** OCI to Cloudflare KV synchronization completes in \< 2 seconds after registration.

---
Copyright © 2026 **M.O.B.I.™** (Machine Oriented Brilliant Ideas™)  
Transforming ideas into high-impact digital solutions. 🚀  
[wearemobi.com](https://wearemobi.com) · contact@wearemobi.com
