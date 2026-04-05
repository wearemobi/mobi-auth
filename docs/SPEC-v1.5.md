# <img src="https://wearemobi.com/icon-light.svg" width="24" height="24" valign="middle"> M.O.B.I.™ · SPEC or DOCS change request v1.0

## Context & Goal
To scale the M.O.B.I.™ fleet, we need an automated way to onboard new users. **SPEC v1.5** focuses on creating the **Register** workflow, bridging the local UI with the **OCI SCIM API** to create real user identities in the Oracle Cloud Identity Domain.

## Implementation Plan
- [x] This issue is the trigger for a **New SPEC**.
- [x] Target Directory: `docs/`
- [x] Target Filename: `docs/SPEC-v1.5.md`

## Technical Requirements

### 1. Recruitment UI (Register)
- **Endpoint:** `/register`
- **Template:** Create `register.html` with the same B&W aesthetic as the Login.
- **Fields:** Email, Full Name, and Password (with validation).

### 2. OCI SCIM Integration (The Bridge)
- **Service:** Implement `OciIdentityService` to communicate with the OCI SCIM endpoint.
- **Action:** Perform a `POST` request to create a new user in the Identity Domain upon form submission.
- **Security:** Use Client Credentials flow (M2M) to authorize the registration request.

### 3. Workflow & UX
- Successful registration should redirect the user to the Login page with a "Success" message.
- Error handling for "User already exists" or "Password complexity" must be displayed in the UI.

## Acceptance Criteria (Definition of Done)
- [x] All stakeholders (or R&D) have reviewed the draft.
- [x] The document is merged into the `main` branch under the `docs/` folder.
- [ ] A new user can be created directly from the M.O.B.I.™ interface without manual OCI Console intervention.
- [ ] The registration form is consistent with the B&W Global Branding.
- [ ] Backend validation ensures all required fields are sent to OCI correctly.
- [ ] Integration tests verify the connection between the Register form and the OCI SCIM API.

---
Copyright © 2026 **M.O.B.I.™** (Machine Oriented Brilliant Ideas™)  
Transforming ideas into high-impact digital solutions. 🚀  
[wearemobi.com](https://wearemobi.com) · contact@wearemobi.com
