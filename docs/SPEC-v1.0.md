# <img src="https://wearemobi.com/icon-light.svg" width="24" height="24" valign="middle"> M.O.B.I.™ · SPEC or DOCS change request v1.0

## Context & Goal
The objective is to initialize the `mobi-auth` repository with a minimalist, high-performance architecture. This service will eventually transition to the latest Spring Boot standards (Spring Boot 4 target), utilizing Java 21's Virtual Threads for maximum throughput. We are establishing a "clean-code-only" environment by enforcing formatting and license headers via Spotless from day one.

## Implementation Plan
- [x] This issue is the trigger for a **New SPEC**.
- [x] Target Directory: `docs/`
- [x] Target Filename: `docs/SPEC-v1.0.md`

### Project Infrastructure
- **Build Tool:** Gradle (Groovy DSL) for traditional, stable build management.
- **Runtime:** Java 21 (LTS).
- **Virtual Threads:** Enabled via `application.yml` to handle high-concurrency OIDC handshakes.
- **Code Style:** **Spotless** with `google-java-format`.

#### Build Configuration (`build.gradle`)
The build file must remain "lean". Key plugins include `spring-boot`, `dependency-management`, and `spotless`.

#### Database & Migrations
- **Engine:** H2 (In-memory) for the R&D phase; PostgreSQL-ready.
- **Migrations:** **Flyway**. All schema changes must be versioned in `src/main/resources/db/migration`.
- **Initial Schema:** A `mobi_users` table with support for `tenant_id`.

#### Domain Model (SDD Core)
Implementation of the `MobiUser` as a native Java Record to ensure immutability and zero boilerplate.

### Technical Artifacts

#### Spotless Configuration Snippet
```groovy
spotless {
    java {
        googleJavaFormat()
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
        licenseHeader '/* Copyright © 2026 M.O.B.I.™ (Machine Oriented Brilliant Ideas™) */\n'
    }
}
```
#### Initial Migration (`V1__init_schema.sql`)
```sql
CREATE TABLE mobi_users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    tenant_id VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```
#### The Record (`MobiUser.java`)
```java
package com.wearemobi.auth.domain;

import java.util.UUID;

public record MobiUser(
    UUID id,
    String email,
    String password,
    String role,
    String tenantId
) {}
```

## Acceptance Criteria (Definition of Done)
- [x] All stakeholders (or R&D) have reviewed the draft.
- [x] The document is merged into the `main` branch under the `docs/` folder.
- [x] Project compiles with `./gradlew build`.
- [x] `./gradlew spotlessCheck` passes without errors.
- [x] Application starts and Flyway applies V1 successfully.
- [x] MobiUser Record is utilized for initial data loading or tests.

---
Copyright © 2026 **M.O.B.I.™** (Machine Oriented Brilliant Ideas™)  
Transforming ideas into high-impact digital solutions. 🚀  
[wearemobi.com](https://wearemobi.com) · contact@wearemobi.com
