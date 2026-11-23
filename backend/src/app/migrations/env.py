from typing import Any, Literal

from alembic import context
from alembic.autogenerate.api import AutogenContext
from sqlalchemy import Connection

import app.auth.models  # noqa: F401  registers the auth tables on Base.metadata
import app.recognitions.models  # noqa: F401
import app.vocabulary.models  # noqa: F401
from app.db import Base, UtcDateTime, create_db_engine, database_url


def render_item(
    type_: str, obj: Any, _autogen_context: AutogenContext
) -> str | Literal[False]:
    # Migrations must not import app code, which changes after they are written.
    if type_ == "type" and isinstance(obj, UtcDateTime):
        return "sa.DateTime()"
    return False


def run_migrations(connection: Connection) -> None:
    context.configure(
        connection=connection,
        target_metadata=Base.metadata,
        render_as_batch=True,
        render_item=render_item,
    )
    with context.begin_transaction():
        context.run_migrations()


connection = context.config.attributes.get("connection")
if connection is not None:
    run_migrations(connection)
else:
    engine = create_db_engine(database_url())
    with engine.connect() as cli_connection:
        run_migrations(cli_connection)
    engine.dispose()
