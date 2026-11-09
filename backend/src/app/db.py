import os
import sqlite3
from collections.abc import Generator
from datetime import UTC, datetime
from pathlib import Path

from alembic import command
from alembic.config import Config
from sqlalchemy import DateTime, Engine, MetaData, create_engine, event
from sqlalchemy.engine.interfaces import Dialect
from sqlalchemy.orm import DeclarativeBase, Session, sessionmaker
from sqlalchemy.pool import ConnectionPoolEntry
from sqlalchemy.types import TypeDecorator

BACKEND_DIR = Path(__file__).resolve().parents[2]
DEFAULT_DATABASE_URL = f"sqlite:///{BACKEND_DIR / 'data' / 'lexis.db'}"
MIGRATIONS_DIR = Path(__file__).parent / "migrations"


class UtcDateTime(TypeDecorator[datetime]):
    """SQLite drops the timezone, so this stores UTC and reads it back as aware."""

    impl = DateTime
    cache_ok = True

    def process_bind_param(
        self, value: datetime | None, dialect: Dialect
    ) -> datetime | None:
        if value is None:
            return None
        if value.tzinfo is None:
            raise ValueError("Pass an aware datetime, not a naive one")
        return value.astimezone(UTC).replace(tzinfo=None)

    def process_result_value(
        self, value: datetime | None, dialect: Dialect
    ) -> datetime | None:
        return None if value is None else value.replace(tzinfo=UTC)


class Base(DeclarativeBase):
    metadata = MetaData(
        naming_convention={
            "ix": "ix_%(column_0_label)s",
            "uq": "uq_%(table_name)s_%(column_0_name)s",
            "ck": "ck_%(table_name)s_%(constraint_name)s",
            "fk": "fk_%(table_name)s_%(column_0_name)s_%(referred_table_name)s",
            "pk": "pk_%(table_name)s",
        }
    )
    type_annotation_map = {datetime: UtcDateTime}


def database_url() -> str:
    return os.getenv("DATABASE_URL", DEFAULT_DATABASE_URL)


def create_db_engine(url: str) -> Engine:
    if not url.startswith("sqlite"):
        return create_engine(url)

    # FastAPI runs sync routes in a thread pool, so a connection can change threads.
    engine = create_engine(url, connect_args={"check_same_thread": False})

    @event.listens_for(engine, "connect")
    def enforce_foreign_keys(
        dbapi_connection: sqlite3.Connection, _record: ConnectionPoolEntry
    ) -> None:
        dbapi_connection.execute("PRAGMA foreign_keys = ON")

    return engine


def create_session_factory(engine: Engine) -> sessionmaker[Session]:
    return sessionmaker(engine, expire_on_commit=False)


def migrate(engine: Engine) -> None:
    database = engine.url.database
    if engine.dialect.name == "sqlite" and database and database != ":memory:":
        Path(database).parent.mkdir(parents=True, exist_ok=True)

    config = Config()
    config.set_main_option("script_location", str(MIGRATIONS_DIR))
    with engine.begin() as connection:
        config.attributes["connection"] = connection
        command.upgrade(config, "head")


engine = create_db_engine(database_url())
SessionLocal = create_session_factory(engine)


def get_db() -> Generator[Session]:
    with SessionLocal() as session:
        yield session
