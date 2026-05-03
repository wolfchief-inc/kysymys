# Sub-D: React/TS Frontend — Implementation Plan

> **For agentic workers:** Use superpowers:executing-plans (inline). Steps use checkbox tracking.

**Goal:** Build the SPA frontend for Sub-A〜C's API.

**Tech Stack:** React 19, Vite, TS 5.x, TanStack Router/Query, Zod, Tailwind, shadcn/ui, react-i18next, Vitest.

---

## File Map

```
kysymys-frontend/
  package.json, tsconfig.json, vite.config.ts, tailwind.config.ts,
  postcss.config.js, index.html, components.json (shadcn config)
  src/
    main.tsx, App.tsx, router.tsx, routeTree.gen.ts (auto)
    i18n/{index.ts, ja.json, en.json}
    lib/{api/{client.ts,lesson.ts,user.ts,notification.ts,avatar.ts,schemas.ts},
         auth.ts, utils.ts}
    components/
      ui/  (shadcn primitives, generated)
      layout/{AppShell,Header,Sidebar,NotificationBell,LanguageSwitch}.tsx
      problem/{ProblemForm,ProblemRepositoryInput}.tsx
      answer/{AnswerForm,AnswerCard,CommentList}.tsx
      user/{UserCard,ProfileForm,AvatarImage}.tsx
      common/{ErrorBoundary,LoadingSpinner,PermissionGate,RequireAuth}.tsx
    routes/
      __root.tsx, index.tsx, login.tsx, teachers.tsx, offers.tsx,
      notifications.tsx, followers.answers.tsx
      problems/{index.tsx, new.tsx, $id.tsx, $id.edit.tsx, $id.answer.tsx}
      answers/{index.tsx, $id.tsx}
      users/{index.tsx, $id.tsx, $id.edit.tsx}
    styles/globals.css
  tests/api/client.test.ts, tests/components/PermissionGate.test.tsx

kysymys-app modifications:
  src/main/java/.../system/CorsMiddleware.java  (new)
  KysymysApplicationFactory.java  (mount cors)
```

## Phase 1 (Sub-D-1): toolchain + AppShell + API client + CORS

- [ ] **1.1** Scaffold Vite project: `cd /Users/kawasima/workspace/kysymys && npm create vite@latest kysymys-frontend -- --template react-ts`. Then `cd kysymys-frontend && npm install`.
- [ ] **1.2** Install deps: `npm install @tanstack/react-router @tanstack/react-query zod react-i18next i18next i18next-browser-languagedetector clsx tailwind-merge lucide-react`. Dev deps: `npm install -D tailwindcss postcss autoprefixer @tanstack/router-plugin @tanstack/router-devtools @tanstack/react-query-devtools @testing-library/react @testing-library/jest-dom vitest jsdom @types/node`.
- [ ] **1.3** Initialize Tailwind: `npx tailwindcss init -p`. Configure `tailwind.config.ts` content paths.
- [ ] **1.4** Initialize shadcn/ui: `npx shadcn@latest init` (defaults: TypeScript, slate base color, css variables, RSC=no). Then add components: `npx shadcn@latest add button card input label dialog dropdown-menu sheet badge avatar form textarea select tabs separator skeleton sonner`.
- [ ] **1.5** Configure `vite.config.ts` with TanStack Router plugin and `/api` proxy to localhost:3000 with rewrite.
- [ ] **1.6** Set up i18n (`src/i18n/index.ts`, `ja.json`, `en.json` with initial keys for nav/header).
- [ ] **1.7** Implement `lib/auth.ts` (token getter/setter on `localStorage.kysymysToken`, decode payload to get `sub` / `permissions`).
- [ ] **1.8** Implement `lib/api/client.ts` with `apiFetch<T>(path, init, schema)` using fetch + Zod parse + auto `x-bouncr-credential` header.
- [ ] **1.9** Define `lib/api/schemas.ts` with Zod schemas for all aggregates (Problem with discriminated union, Answer, ReviewComment, User, Offer, WhatsNew, etc.).
- [ ] **1.10** Define `lib/api/{lesson,user,notification,avatar}.ts` with one function per endpoint.
- [ ] **1.11** Build `components/layout/{AppShell,Header,Sidebar,LanguageSwitch}.tsx`. Mobile first: <768px Sheet drawer for sidebar, >=768 collapsed icon nav, >=1024 full sidebar.
- [ ] **1.12** Build `components/common/{ErrorBoundary,LoadingSpinner,PermissionGate,RequireAuth}.tsx`.
- [ ] **1.13** Define routes scaffold with `__root.tsx` (AppShell + Outlet) and stub all 15 leaf routes that render placeholder text.
- [ ] **1.14** kysymys-app: write `system/CorsMiddleware.java` (allow `KYSYMYS_CORS_ORIGINS` env, default `http://localhost:5173`). Mount in `KysymysApplicationFactory` immediately after `ParamsMiddleware`. Verify Sub-A〜C Hurl scenarios still pass.
- [ ] **1.15** Run `npm run dev`, browse to `http://localhost:5173`, see AppShell + sidebar nav rendering. Commit.

## Phase 2 (Sub-D-2): auth + Dashboard

- [ ] **2.1** `routes/login.tsx`: textarea for token + "Save & Continue" button. On save, store and `navigate('/')`.
- [ ] **2.2** `RequireAuth` redirects to `/login` when no token. Wrap `__root.tsx` outlet (except `/login` itself).
- [ ] **2.3** Header shows authenticated user's display name + Avatar (from `/users/:caller_id/avatar`) + NotificationBell with unread count from `useQuery(['whats-news'])` filtered.
- [ ] **2.4** `routes/index.tsx` Dashboard: 3 cards — recent WhatsNew (top 5), my recent answers (top 5), followers' recent answers (top 5).
- [ ] **2.5** Commit.

## Phase 3 (Sub-D-3): Lesson screens

- [ ] **3.1** `routes/problems/index.tsx`: list active problems via `listProblems()`. "New Problem" button visible only for TEACHER.
- [ ] **3.2** `routes/problems/$id.tsx`: detail view + link to README (`problemUrl`). "Submit Answer" button. "Edit"/"Archive" for TEACHER.
- [ ] **3.3** `routes/problems/new.tsx` + `$id.edit.tsx`: shared `ProblemForm` component. `ProblemRepositoryInput` for the discriminated union (radio for type + per-type fields).
- [ ] **3.4** `routes/problems/$id.answer.tsx`: AnswerForm (repository type+url + commitHash 40 hex).
- [ ] **3.5** `routes/answers/index.tsx`: My answers list.
- [ ] **3.6** `routes/answers/$id.tsx`: Answer detail + comments thread + comment post form.
- [ ] **3.7** `routes/followers.answers.tsx`: Following feed.
- [ ] **3.8** Commit.

## Phase 4 (Sub-D-4): User screens

- [ ] **4.1** `routes/users/index.tsx`: search input + list (`listUsers(q?)`).
- [ ] **4.2** `routes/users/$id.tsx`: profile view with Avatar, name, roles, followers count. "Follow" button if not self and not following. "Edit" if self.
- [ ] **4.3** `routes/users/$id.edit.tsx`: ProfileForm (email + name).
- [ ] **4.4** `routes/teachers.tsx`: teacher list. For TEACHER: "Grant teacher role" form (target userId).
- [ ] **4.5** `routes/offers.tsx`: incoming offers list with Accept buttons.
- [ ] **4.6** Commit.

## Phase 5 (Sub-D-5): Notifications + polish + tests

- [ ] **5.1** `routes/notifications.tsx`: WhatsNew list with read/unread distinction; click to mark as read; render template based on `templatePath` ("submittedAnswer", "offeredToFollow") with i18n.
- [ ] **5.2** Wire NotificationBell badge to use TanStack Query's `useQuery` with refetch interval (10s) for unread count.
- [ ] **5.3** Vitest tests: `tests/api/client.test.ts` (Zod parse on mocked fetch), `tests/components/PermissionGate.test.tsx`.
- [ ] **5.4** Final `npm run build` + smoke walk-through: open each route, check no console errors.
- [ ] **5.5** Commit.

## Verification

```bash
# 1. backend
cd kysymys-app && mvn exec:java &
sleep 5

# 2. frontend
cd kysymys-frontend && npm install && npm run dev &

# 3. browser test (manual)
open http://localhost:5173
# - Login (dev) -> paste teacher token -> Dashboard
# - Walk all 15 routes
# - <768px viewport -> sidebar drawer
# - language switch ja/en

# 4. unit tests
cd kysymys-frontend && npm run test

# 5. backend regression
TOKEN=$(java /tmp/GenTokenWithRole.java aaaaaaaaaaaaaaaaaaaaa teacher@example.com Tanaka TEACHER)
hurl --variable host=http://localhost:3000 --variable token="$TOKEN" \
  kysymys-app/src/test/hurl/{lesson,user,avatar,notification}.hurl
```
