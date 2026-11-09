# 3. SQLite database

- Status: accepted
- Date: 2026-10-07
- Issue: #31
- Related: [[architecture/database]]

## Context

Sign-in (#31) needs to store users and sessions, so the backend needs a database. The course brief asks for local deployment on a personal laptop, so nothing is hosted. Five of us run the backend on different systems, and CI runs `backend/check.sh` on every pull request.

## Decision

- SQLite, one file at `backend/data/lexis.db`, over PostgreSQL. Nobody has to install or start a database server, and CI needs no database container.
- SQLAlchemy 2 for models and queries, Alembic for migrations. Only repositories talk to the database.
- The backend applies migrations when it starts, so a fresh clone gets its database on the first run.
- `DATABASE_URL` overrides the location. `backend/check.sh` and the tests use a throwaway file.

## Consequences

- One writer at a time. That's fine for one person using the app on their laptop.
- SQLite doesn't store time zones. `UtcDateTime` in `backend/src/app/db.py` stores UTC and reads it back as UTC.
- SQLite can't alter most columns in place. Alembic runs in batch mode, which rebuilds the table instead.
- Moving to PostgreSQL later means a new `DATABASE_URL` and a driver, not new queries. Reasons to move: a shared server, several backend workers, or Postgres-only features like full-text or vector search.
