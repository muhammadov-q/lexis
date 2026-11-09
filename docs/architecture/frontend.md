# Frontend

The web app in `frontend/`. Setup and scripts are in `frontend/README.md`; why this stack is in [[adr/0001-web-frontend-stack]].

## Request path

```mermaid
sequenceDiagram
    participant B as Browser
    participant N as Next.js server (/api proxy)
    participant F as FastAPI backend
    B->>N: POST /api/recognitions (multipart "image", session cookie)
    N->>F: POST /recognitions (same body and cookie)
    F-->>N: 201 {id, created_at, filename, model_version, predictions}
    N-->>B: same status, headers and body
```

- The browser only calls `/api/*`. The proxy (`frontend/src/app/api/[...path]/route.ts`) forwards to the backend URL in `API_INTERNAL_URL`, read at runtime, so the backend needs no CORS.
- It's a Route Handler rather than a rewrite, so FastAPI's redirects are followed on the server instead of sending the browser to the backend.
- It holds no logic. If the backend is down it answers 502 with a FastAPI-style `detail`, so the UI handles one error shape.

## Code layout

| Where                                    | What                                                                                   |
| ---------------------------------------- | -------------------------------------------------------------------------------------- |
| `frontend/src/lib/api/client.ts`         | `apiRequest` and `ApiError`, the only way to call the backend.                         |
| `frontend/src/lib/api/recognitions.ts`   | One module per endpoint group, typed from the generated schema.                        |
| `frontend/src/hooks/use-recognitions.ts` | TanStack Query hooks. All server state goes through hooks.                             |
| `frontend/src/components/recognition/`   | The upload and recognition flow.                                                       |
| `frontend/src/components/transcript/`    | The transcript, the word boxes on the page and the panel for the picked word.          |
| `frontend/src/components/history/`       | The results history (`/dashboard/history`) and one saved result.                       |
| `frontend/src/components/phone-capture/` | The QR code dialog on the computer and the camera page on the phone (`/capture/[id]`). |
| `frontend/src/components/ui/`            | shadcn primitives plus `LoadingOrb` and `Skeleton`.                                    |
| `frontend/src/components/auth/`          | Sign-in and sign-up forms, the sign-out button and the account card in the sidebar.    |
| `frontend/src/proxy.ts`                  | Sends visitors without a session cookie to `/sign-in`.                                 |

## Signing in

- `/sign-in` and `/sign-up` post to the backend, which sets the `lexis_session` cookie. The rules are in [[architecture/authentication]].
- `frontend/src/proxy.ts` sends visitors without the cookie to `/sign-in`, and signed-in visitors away from it. It only checks that the cookie is there; the backend checks the session.
- When a session has ended, `/auth/me` answers 401 and clears the cookie, and `useCurrentUser` in `frontend/src/hooks/use-auth.ts` sends the page to `/sign-in`.
- `/api/*` and the phone's `/capture/*` page stay open, since the phone never signs in.
- In the e2e tests, `frontend/e2e/auth.setup.ts` signs up once and the other specs reuse its cookie.

## Loaders

- `LoadingOrb` is for waits the user watches, like the model reading a page.
- `Skeleton` stands in for content that is loading. It has the same shape as that content, so nothing jumps when the data arrives.

## Styling

Tokens live in `frontend/src/app/globals.css`, and components use tokens, never raw colours. The rules are in [[architecture/design-system]].

## Gotcha

The root `.gitignore` ignores every `lib/` folder. `frontend/.gitignore` re-includes `src/lib`; without that line the API client never reaches git.
