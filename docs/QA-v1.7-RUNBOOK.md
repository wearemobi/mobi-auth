# <img src="https://wearemobi.com/icon-light.svg" width="24" height="24" valign="middle"> M.O.B.I.™ QA Runbook v1.0

**SPEC:** v1.7 (Identity & Gateway Protocol)  
**Component:** `mobi-auth`  
**Engineer Level:** L2/L3 Technical QA  
**Objective:** To validate the unified identity engine, including **JIT Provisioning**, **M2M Handshake**, and the **Identity Resolution API (`/me`)** in a "Scorched Earth" (clean-slate) environment.

-----

## 🛠 Prerequisites & Environment Setup

Before execution, ensure the following technical requirements are met:

* **Tooling:** Docker Engine & Compose, Java OpenJDK 21, `curl`.
* **Environment Variables:** Ensure OCI credentials are set in your shell or in hand (or ask R&D for them):
    * `MOBI_M2M_CLIENT_ID`
    * `MOBI_M2M_CLIENT_SECRET`
    * `CLIENT_SECRET_HASH` 
* **Clean State Protocol:**
  ```sh
  # Shutdown services and remove legacy volumes
  docker-compose down -v
  # Purge local build artifacts
  ./gradlew clean
  ```

-----

## 🛡 Test Flow 1: Machine to Machine (M2M) Handshake

Validate that AI Agents can authenticate autonomously using the centralized engine.

### Step 1. Agent Provisioning

Manually register a synthetic AI Agent.

> ⚠️ **Security Note:** Use a local-only secret. Never use production credentials.
> ⚠️ **Security Note:** The query below has a `YOUR_CLIENT_SECRET_HASH_HERE` placeholder, replace using a valid secret hash or request R&D for one. 

```sh
# We use 'mobi-secret-dev' as the raw secret for this test
# The hash below corresponds to 'mobi-secret-dev'
docker exec -it mobi-db psql -U mobi_user -d mobi_auth -c "INSERT INTO mobi_client (id, client_id, client_secret_hash, org_id, tenant_id, app_name) VALUES (gen_random_uuid(), 'mobi-qa-agent', 'YOUR_CLIENT_SECRET_HASH_HERE', gen_random_uuid(), 'mobi-system', 'AGENT_QA_TEST');"
```

 

### Step 2. Token Request

Request the system-level Access Token.

```sh
curl -X POST http://localhost:9090/api/v1/auth/token \
     -H "Content-Type: application/json" \
     -d '{"clientId": "mobi-qa-agent", "clientSecret": "mobi-secret-dev"}'
```

#### Expected Results

* **HTTP Status:** `200 OK`.
* **Payload:** Must contain `accessToken`, `tokenType: Bearer`, and `expiresIn: 86400`.

-----

## 👤 Test Flow 2: Web Login & JIT Provisioning

Validate that a human user authenticated via Oracle (OCI) is automatically provisioned in the M.O.B.I. database.

### Step 1. Interactive Login

1.  Open a browser and navigate to `http://localhost:9090/login`.
2.  Authenticate using your OCI credentials.
3.  Upon success, you should be redirected to `/home`.

### Step 2. Log Verification

Check the `mobi-auth` logs for the JIT trigger:

```sh
docker logs mobi-auth | grep "Executing JIT Provisioning"
```

### Step 3. Database Verification

Confirm the user and their organization were created correctly:

```sh
docker exec -it mobi-db psql -U mobi_user -d mobi_auth -c "SELECT email, tenant_id, org_name FROM mobi_user;"
```

#### Expected Results

* **Record Found:** A new row with your email and an automatically generated `tenant_id`.
* **Event Hook:** Verify the `Post-registration hook` log was fired for downstream provisioning.

-----

## 🔍 Test Flow 3: Identity Resolution API (`/me`)

The core of the Gateway Protocol. Validating that the JWT can resolve the user's "DNA".

### Step 1. Execute Request

Copy the `accessToken` from **Test Flow 1** (or from the Dashboard's "View Identity" section) and run:

```sh
export MOBI_TOKEN="PASTE_YOUR_TOKEN_HERE"

curl -i -X GET http://localhost:9090/api/v1/auth/me \
     -H "Authorization: Bearer $MOBI_TOKEN" \
     -H "Content-Type: application/json"
```

#### Expected Results

* **HTTP Status:** `200 OK`.
* **Payload Structure:** A JSON object containing `email`, `tenantId`, `orgId`, and `roles`.
* **Security Check:** Run the same command **without** the header; it must return `401 Unauthorized`.

-----

## 🏺 Acceptance Criteria (Definition of Done)

1.  **Zero Regressions:** Web views (`/home`, `/profile`) load without `ClassCastException`.
2.  **CORS Validation:** `OPTIONS` requests from `localhost:5500` return `200 OK`.
3.  **M2M Integrity:** AI Agents receive tokens with the `MOBI_SYSTEM_AGENT` role.
4.  **JIT Accuracy:** New users receive the `MOBI_TENANT_OWNER` role by default.

-----

Copyright © 2026 **M.O.B.I.™** (Machine Oriented Brilliant Ideas™)  
Transforming ideas into high-impact digital solutions. 🚀  
[wearemobi.com](https://wearemobi.com) · contact@wearemobi.com
