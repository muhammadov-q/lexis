from datetime import UTC, datetime, timedelta, timezone

import pytest
from alembic.autogenerate import compare_metadata
from alembic.migration import MigrationContext
from fastapi.testclient import TestClient
from sqlalchemy import inspect, select
from sqlalchemy.exc import IntegrityError, StatementError

from app.auth.models import AuthSession, User
from app.auth.repository import AuthSessionRepository
from app.db import Base, UtcDateTime, create_db_engine, engine, get_db, migrate
from app.main import app


def test_migrations_match_the_models(engine):
    with engine.connect() as connection:
        diff = compare_metadata(MigrationContext.configure(connection), Base.metadata)
    assert diff == []


def test_other_databases_get_no_sqlite_settings(monkeypatch):
    calls = []
    monkeypatch.setattr(
        "app.db.create_engine", lambda url, **kwargs: calls.append((url, kwargs))
    )

    create_db_engine("postgresql://localhost/lexis")

    assert calls == [("postgresql://localhost/lexis", {})]


def test_migrate_creates_the_folder_and_can_run_again(tmp_path):
    path = tmp_path / "nested" / "app.db"
    engine = create_db_engine(f"sqlite:///{path}")

    migrate(engine)
    migrate(engine)

    assert path.exists()
    assert {"users", "auth_sessions"} <= set(inspect(engine).get_table_names())
    engine.dispose()


def test_backend_start_migrates_the_database(tmp_path, monkeypatch):
    engine = create_db_engine(f"sqlite:///{tmp_path / 'fresh.db'}")
    monkeypatch.setattr("app.main.engine", engine)

    with TestClient(app):
        assert "users" in inspect(engine).get_table_names()
    engine.dispose()


def test_datetimes_are_read_back_as_utc(db):
    in_bern = datetime(2026, 10, 7, 14, 0, tzinfo=timezone(timedelta(hours=2)))
    db.add(User(email="ada@example.com", password_hash="x", created_at=in_bern))
    db.commit()
    db.expire_all()

    created_at = db.scalars(select(User)).one().created_at

    assert created_at == datetime(2026, 10, 7, 12, 0, tzinfo=UTC)
    assert created_at.tzinfo is UTC


def test_missing_datetimes_stay_missing(engine):
    column_type = UtcDateTime()
    assert column_type.process_bind_param(None, engine.dialect) is None
    assert column_type.process_result_value(None, engine.dialect) is None


def test_naive_datetimes_are_rejected(db):
    db.add(User(email="ada@example.com", password_hash="x", created_at=datetime.now()))
    with pytest.raises(StatementError, match="aware datetime"):
        db.commit()


def test_foreign_keys_are_enforced(db):
    now = datetime.now(UTC)
    orphan = AuthSession(
        token_hash="a" * 64, user_id=999, created_at=now, expires_at=now
    )
    with pytest.raises(IntegrityError):
        AuthSessionRepository(db).add(orphan)


def test_get_db_hands_out_a_session_on_the_app_database():
    sessions = get_db()
    assert next(sessions).get_bind() is engine
    sessions.close()
