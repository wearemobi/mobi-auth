# <img src="https://wearemobi.com/icon-light.svg" width="24" height="24" valign="middle"> M.O.B.I.™ QA Runbook v1.0

**SPEC:** v1.6
**Component:** `mobi-auth`  
**Engineer Level:** L2/L3 Technical QA  
**Objective:** To validate the resilience of the Machine-to-Machine (M2M) authentication flow, ensuring proper tenant isolation, credential hashing, and JWT claim integrity in a "Scorched Earth" (clean-slate) environment.

---

## Prerequisites & Environment Setup

Before execution, ensure the following technical requirements are met:

* **Repository Access:** Ensure you have permissions to clone `wearemobi/mobi-auth`.
* **Tooling:** * Docker Engine & Docker Compose (v2.20+ recommended).
    * Java OpenJDK 21.
    * Gradle 8.x+ (optional, wrapper provided).
* **Port Availability:** * `9090` (Auth Service).
    * `5432` (PostgreSQL).
* Clean repository on `main` branch
    ```sh
    git clone [https://github.com/wearemobi/mobi-auth.git](https://github.com/wearemobi/mobi-auth.git)
    cd mobi-auth
    git checkout main
    ```

---

## Integration Tests

### Step 1: Branch Purge

```sh
# Purge local Gradle build artifacts
./gradlew clean
```

### Step 2. Build & Run Tests

```sh
# Compile the Spring Boot executable (shadow JAR)
./gradlew bootJar

# Run tests
./gradlew test
```
#### Expected Results
**Verification:** Test suite successful once the build completes.

## Machine to Machine (M2M) Authentication Flow Manual Test

### Step 1. Environment Purge ("Scorched Earth" Protocol)

To eliminate false positives from residual data or orphaned volumes, a full purge is mandatory.

```sh
# Shutdown services and remove legacy volumes
docker-compose down -v

# Purge local Gradle build artifacts
./gradlew clean
```

### Step 2. Build & Deployment Sequence

```sh
# Compile the Spring Boot executable (shadow JAR)
./gradlew bootJar

# Spin up infrastructure and force image rebuild
docker-compose up -d --build
```

#### Expected Results
**Verification:** Run `docker logs -f mobi-auth`. The deployment is successful once the `Started MobiAuthApplication` log entry is visible.

### Step 3. Data Seeding (Agent Infiltration)

Manually register a synthetic AI Agent for the `acme` tenant. This simulates the internal provisioning of a M2M client.

```sh
docker exec -it mobi-db psql -U mobi_user -d mobi_auth -c "INSERT INTO mobi_client (id, client_id, client_secret_hash, org_id, tenant_id, app_name) VALUES (gen_random_uuid(), 'agentic-java-001', '\$2a\$10\$qKCifkCZkjV5mHTBZcjfeObyvk9sLfmvCTskNvKKsjQToN.aD0Y2a', gen_random_uuid(), 'acme', 'AGENTIC_SUPPORT');"
```

#### Expected Results
**Expected Output:** `INSERT 0 1`.

### Step 4. E2E Execution (The Handshake)

Request the system-level Access Token using the seeded credentials.
```sh
curl -X POST http://localhost:9090/api/v1/auth/token \
     -H "Content-Type: application/json" \
     -d '{"clientId": "agentic-java-001", "clientSecret": "mobi-secret"}'
```

#### Expected Results

1. **HTTP Status:** `200 OK`.
2. **Payload Structure:** Must return a JSON object containing `accessToken`, `tokenType: Bearer`, and `expiresIn`.
3. **Identity Verification:** Decoded JWT (via jwt.io) must contain:
    * `sub`: `AGENTIC_SUPPORT@mobi.systems`
    * `roles`: `MOBI_SYSTEM_AGENT`
    * `tenantId`: `acme`
    * `type`: `ACCESS`

## Acceptance Criteria
1. All preceding execution test steps must be completed successfully without regression.
2. Test results and the execution checklist must be persisted in the designated artifacts directory.


---
Copyright © 2026 **M.O.B.I.™** (Machine Oriented Brilliant Ideas™)  
Transforming ideas into high-impact digital solutions. 🚀  
[wearemobi.com](https://wearemobi.com) · contact@wearemobi.com
