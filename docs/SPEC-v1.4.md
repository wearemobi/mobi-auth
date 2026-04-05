# <img src="https://wearemobi.com/icon-light.svg" width="24" height="24" valign="middle"> M.O.B.I.™ · SPEC or DOCS change request v1.0

## Context & Goal
Following the successful capture of OCI tokens in **v1.3**, the application currently responds with raw JSON. The goal of **SPEC v1.4** is to implement a professional **Black & White (B&W)** interface using **Thymeleaf** and the existing `global.css`. This will provide users with a visual dashboard to view their identity and session status.

## Implementation Plan
- [x] This issue is the trigger for a **New SPEC**.
- [x] Target Directory: `docs/`
- [x] Target Filename: `docs/SPEC-v1.4.md`

## Technical Requirements

### 1. Visual Refinement (Login)
- Ensure the existing `/login` page is 100% compliant with the `global.css` architecture.
- Maintain a minimalist B&W aesthetic (clean typography, no shadows, high contrast).

### 2. Dashboard Implementation (Home)
- **Endpoint:** `/home`
- **Controller Refactor:** Change `@RestController` to `@Controller` and return a "home" view.
- **Template:** Create `home.html` using Thymeleaf.
- **Content:** Display a "Welcome" message and the "All Blue" connectivity status.

### 3. User Detail View (Profile)
- **Endpoint:** `/profile`
- **Data Injection:** Pass the `OciTokenResponse` object to the model.
- **Display:** Show User Email, Token Type, and Session Expiration in a clean B&W table.
- **Security:** Ensure the Access Token is visible for verification but protected by the session.

## Acceptance Criteria (Definition of Done)
- [x] All stakeholders (or R&D) have reviewed the draft.
- [x] The document is merged into the `main` branch under the `docs/` folder.
- [ ] Users are redirected to a visual `/home` page instead of a JSON response after login.
- [ ] Navigation between Login, Home, and Profile is seamless and follows the B&W branding.
- [ ] Logout functionality is visually integrated and redirects back to the Login port.
- [ ] All pages share the same `global.css` and a common header fragment.

---
Copyright © 2026 **M.O.B.I.™** (Machine Oriented Brilliant Ideas™)  
Transforming ideas into high-impact digital solutions. 🚀  
[wearemobi.com](https://wearemobi.com) · contact@wearemobi.com
