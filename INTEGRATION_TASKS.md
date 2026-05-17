# Frontend ↔ Backend integration

Track progress here. Work top to bottom; check boxes as each step is done.

## Context

| Piece | Location | Default URL |
|-------|----------|-------------|
| API | `AgoraCampus/` | http://localhost:8080 |
| Keycloak | `docker/keycloak/` | http://localhost:8090, realm `agora-campus` |
| Frontend | `AgoraCampusFrontEnd/` | http://localhost:3000 |
| OIDC client | realm import | `agora-frontend` (PKCE) |

Auth flow (from backend README): Keycloak login → JWT → `GET /api/users/me` → `POST /api/users` if 404 → other APIs with `Authorization: Bearer` and `actingUserId` query param.

---

## Phase 1 — Dev environment

- [x] **1.1** Add root `.env.example` for Docker / Spring overrides
- [x] **1.2** Add `AgoraCampusFrontEnd/.env.local.example` with `NEXT_PUBLIC_*` URLs
- [x] **1.3** Document full-stack startup in root `README.md`

## Phase 2 — Frontend auth layer

- [x] **2.1** Add `keycloak-js` dependency
- [x] **2.2** `src/lib/config.ts` — read public env vars
- [x] **2.3** `src/lib/keycloak.ts` — Keycloak singleton
- [x] **2.4** `src/lib/api.ts` — `apiFetch` with Bearer token
- [x] **2.5** `src/lib/users.ts` — `ensureAppUser()` (`/api/users/me` + create)
- [x] **2.6** `src/contexts/AuthContext.tsx` — init Keycloak, hold `appUser`
- [x] **2.7** `src/components/AuthProvider.tsx` + wire in `app/layout.tsx`
- [x] **2.8** `src/components/RequireAuth.tsx` — guard app routes

## Phase 3 — Wire UI to auth

- [x] **3.1** `LoginForm` — Keycloak login redirect (not mock redirect)
- [x] **3.2** `CreateAccountForm` — Keycloak registration redirect
- [x] **3.3** Protect `/feed`, `/messages`, `/jobs`, `/alerts`, `/network`, `/profile`
- [x] **3.4** `AppShell` — sign out + show signed-in username
- [x] **3.5** `profile` page — display `/api/users/me` data (proves API works)

## Phase 4 — Verify end-to-end

- [ ] **4.1** `./scripts/dev/dev-up.sh` → backend → frontend starts without errors
- [ ] **4.2** Login as `demo`/`demo` → lands on feed → user row created in API
- [ ] **4.3** Refresh page → session restored via Keycloak SSO

## Later (not in this pass)

- [ ] Profile onboarding (`POST /api/individuals` or organization) after signup
- [ ] Replace mock feed/jobs/messages data with real API calls + `actingUserId`
- [ ] Copy missing static assets into `AgoraCampusFrontEnd/public/`
