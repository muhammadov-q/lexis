# Authentication

People sign up with an email, a password and, if they like, their first and last name, then sign in with the email and password (#31). The backend keeps the session in the database and hands the browser a cookie.

## Endpoints

| Endpoint              | Does                                                                   | Errors                                          |
| --------------------- | ---------------------------------------------------------------------- | ----------------------------------------------- |
| `POST /auth/sign-up`  | Creates the account and signs it in. First and last name are optional. | 400 bad email or short password, 409 email used |
| `POST /auth/sign-in`  | Starts a new session. Returns the user.                                | 401 wrong email or password                     |
| `POST /auth/sign-out` | Ends the session and clears the cookie.                                | none                                            |
| `GET /auth/me`        | Returns the signed-in user.                                            | 401, and clears a stale cookie                  |

## Rules

- Emails are trimmed and lowercased, so `Ada@Example.com` and `ada@example.com` are one account.
- Names are trimmed; a blank name is stored as no name.
- Passwords need at least 8 characters. They're stored as Argon2 hashes (`pwdlib`), never as text.
- A wrong password and an unknown email get the same answer in about the same time, so sign-in doesn't reveal who has an account.
- A session lasts 7 days. Signing out ends it right away.
- The cookie `lexis_session` is `HttpOnly` and `SameSite=Lax`, so page scripts can't read it and other sites can't send it. It isn't `Secure`, because the app runs over plain http on a laptop.
- `/predict` and `/capture-sessions` don't need a session. The mobile app can use `/predict` as a guest or `/recognitions` while signed in; the phone in the QR flow does not sign in.
- `/recognitions` (the results history) needs one, and only ever returns your own results.

The rules live in `AuthService` in `backend/src/app/auth/service.py`; the routes in `backend/src/app/auth/router.py` only translate errors to status codes. A route that needs a signed-in user takes `CurrentUser` from the router module.

## Sign in

```mermaid
sequenceDiagram
    participant Client
    participant Router as auth router
    participant Service as AuthService
    participant DB as UserRepository, AuthSessionRepository
    Client->>Router: POST /auth/sign-in {email, password}
    Router->>Service: sign_in(email, password)
    Service->>DB: get_by_email
    DB-->>Service: User
    Service->>Service: verify the password hash
    Service->>DB: add AuthSession(hash of a new token)
    Service-->>Router: user, token
    Router-->>Client: 200 user, Set-Cookie lexis_session=token
    Client->>Router: GET /auth/me (cookie)
    Router->>Service: current_user(token)
    Service->>DB: get session by token hash, then the user
    Router-->>Client: 200 user
```

The tables are in [[architecture/database]]. How the web app signs in and guards its pages is in [[architecture/frontend]].
