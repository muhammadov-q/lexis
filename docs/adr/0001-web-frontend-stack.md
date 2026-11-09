# 1. Web frontend stack

- Status: accepted
- Date: 2026-09-23
- Issue: #7

## Context

Lexis needs a web app to upload manuscript pages and show what the model returns. It talks to a FastAPI backend that is still changing, so the contract between the two will change often. The course grades testing and CI, so the setup has to be testable from the start.

## Decision

- Next.js 16 (App Router) with TypeScript, over a Vite single-page app: file-based routing and a built-in server for the backend proxy.
- A same-origin proxy (`frontend/src/app/api/[...path]/route.ts`) instead of calling the backend from the browser. The backend needs no CORS, and its URL is a runtime setting (`API_INTERNAL_URL`). The proxy is transport only.
- Request and response types generated from the backend's OpenAPI schema (`npm run api:types`), so a backend change that breaks the frontend fails the type check.
- TanStack Query for server state. Tailwind CSS 4 and shadcn/ui for the UI.
- Vitest and Testing Library for unit tests, Playwright for end-to-end tests, ESLint and Prettier for style. `frontend/check.sh` runs them.
- npm and Node 24 (`.nvmrc`).

## Consequences

- Every backend call makes one extra hop through the Next.js server. That's negligible locally, and it buys no CORS and a configurable backend URL.
- `frontend/src/lib/api/schema.ts` is committed. Whoever changes an endpoint regenerates it and fixes what the type check flags, in the same PR.
- The proxy buffers uploads instead of streaming them. That's fine at page-image sizes; much larger files would need streaming.
