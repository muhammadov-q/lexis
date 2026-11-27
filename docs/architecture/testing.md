# Testing

Tests are written with the code, in the same PR ([[workflow/definition-of-done]]).

| What                          | Tool                           | Where                                                | How it runs                                                  |
| ----------------------------- | ------------------------------ | ---------------------------------------------------- | ------------------------------------------------------------ |
| Frontend units and components | Vitest, Testing Library        | next to the code, `*.test.ts(x)`                     | `frontend/check.sh`, so on pre-push and in CI                |
| Frontend types vs the backend | generated types, `tsc`         | [[architecture/api-contract]]                        | `frontend/check.sh`                                          |
| Backend units and endpoints   | pytest                         | `backend/src/app/tests/` and `backend/src/ml/tests/` | `backend/check.sh`, so on pre-push and in CI                 |
| Backend types                 | mypy, strict                   | settings in `backend/pyproject.toml`                 | `backend/check.sh`                                           |
| Backend API                   | Bruno                          | `backend/bruno/`                                     | `backend/check.sh`, against a throwaway database             |
| Mobile shared code            | kotlin.test, Ktor `MockEngine` | `mobile/shared/src/commonTest/`                      | `mobile/check.sh`, on Android and iOS, on pre-push and in CI |
| End to end                    | Playwright                     | `frontend/e2e/`                                      | `npm run e2e` in `frontend/`, backend on port 8000           |

## Backend conventions

- Tests that need the database take the `db` fixture from `backend/src/app/tests/conftest.py`: a fresh, migrated SQLite file per test.
- App and ML code is fully annotated. Tests are type checked too, but test functions and fixtures don't need annotations.
- Tests that need a trained model skip when it's missing, so they don't run in CI. Swap in a fake model where you can, like `FakeModel` in `backend/src/ml/tests/test_knn_predictor.py`.

## Mobile conventions

- Tests sit in the same package as the code they test, so they can reach `internal` code.
- Stub the backend with Ktor's `MockEngine`, or give view models a fake repository. Tests never call a real backend.
- View model tests set `Dispatchers.Main` to a `StandardTestDispatcher` and call `advanceUntilIdle()`.

## Frontend conventions

- Query by role and label (`getByRole`, `getByLabelText`), not by class names.
- `renderWithProviders` in `frontend/src/test/render.tsx` gives each test a fresh query client.
- Stub the network with `vi.stubGlobal('fetch', ...)`. Unit tests never call a real backend.
