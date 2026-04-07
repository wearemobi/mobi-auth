# <img src="https://wearemobi.com/icon-light.svg" width="24" height="24" valign="middle"> M.O.B.I.™ · Auth (Identity & Authorization Server)

**Mobi Auth** is a cloud-native Identity Provider (IdP) and OAuth2 Authorization Server. Built with Java 21, Spring Boot 3.4, and Spring Security.

>🛡 Mobi Auth is one of the core components of the M.O.B.I. ecosystem, designed specifically with the AI platform in mind. Evolved from a multi-tenant Identity Provider into a robust **Identity Gateway**, it provides unified access for human operators, autonomous AI Agents (M2M), and seamless Dashboard integration.

## 🚀 Key Features

* **Multi-tenant Fortress:** Strict data isolation using immutable UUIDs and human-readable slugs for tenant organization and real users.
* **Identity Gateway & API:** New `/api/v1/auth/me` endpoint to resolve user "DNA" (roles, tenant, org) for personalized frontend experiences.
* **JIT (Just-In-Time) Provisioning:** Automatic user forking and organization creation upon successful OCI authentication.
* **M2M Ready:** Secure Client Credentials flow allowing internal clients (AI agents) to authenticate autonomously via machine-specific roles.
* **Session Continuity:** Robust Refresh Token implementation to ensure seamless UI/UX for long-running operations.
* **Enterprise Security:** BCrypt hashing, JWT-based contextual claims, and dynamic CORS/Security configuration via `application.properties`.

## 🛠️ Tech Stack

* **Language:** Java 21 (Virtual Threads ready)
* **Framework:** Spring Boot 3.4 / Spring Security
* **Database:** PostgreSQL 15 (Primary) / H2 (In-memory testing)
* **Migrations:** Flyway
* **Security Standards:** JWT, OIDC, OAuth 2.1, RBAC, CORS

## 📖 Documentation & Testing

* **Architecture Specs:** See `docs/SPEC-v1.7.md` for details on the Identity & Gateway Protocol.
* **QA Runbooks:** For End-to-End validation procedures, refer to `docs/QA-v1.7-RUNBOOK.md`.

---
Copyright © 2026 **M.O.B.I.™** (Machine Oriented Brilliant Ideas™)  
Transforming ideas into high-impact digital solutions. 🚀  
[wearemobi.com](https://wearemobi.com) · contact@wearemobi.com