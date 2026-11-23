"""create vocabulary words

Revision ID: 4f3fc148433a
Revises: 1d3c22799b79
Create Date: 2026-11-22 17:41:22.050902

"""

from collections.abc import Sequence

import sqlalchemy as sa
from alembic import op

revision: str = "4f3fc148433a"
down_revision: str | Sequence[str] | None = "1d3c22799b79"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    op.create_table(
        "vocabulary_words",
        sa.Column("id", sa.Integer(), nullable=False),
        sa.Column("user_id", sa.Integer(), nullable=False),
        sa.Column("recognition_id", sa.Integer(), nullable=False),
        sa.Column("created_at", sa.DateTime(), nullable=False),
        sa.Column("word", sa.String(length=200), nullable=False),
        sa.Column("gloss", sa.JSON(), nullable=True),
        sa.Column("line", sa.Integer(), nullable=False),
        sa.Column("word_index", sa.Integer(), nullable=False),
        sa.Column("note", sa.Text(), nullable=False),
        sa.ForeignKeyConstraint(
            ["recognition_id"],
            ["recognitions.id"],
            name=op.f("fk_vocabulary_words_recognition_id_recognitions"),
            ondelete="CASCADE",
        ),
        sa.ForeignKeyConstraint(
            ["user_id"],
            ["users.id"],
            name=op.f("fk_vocabulary_words_user_id_users"),
            ondelete="CASCADE",
        ),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_vocabulary_words")),
        sa.UniqueConstraint(
            "user_id", "word", name=op.f("uq_vocabulary_words_user_id_word")
        ),
    )
    with op.batch_alter_table("vocabulary_words", schema=None) as batch_op:
        batch_op.create_index(
            batch_op.f("ix_vocabulary_words_recognition_id"),
            ["recognition_id"],
            unique=False,
        )
        batch_op.create_index(
            batch_op.f("ix_vocabulary_words_user_id"), ["user_id"], unique=False
        )


def downgrade() -> None:
    with op.batch_alter_table("vocabulary_words", schema=None) as batch_op:
        batch_op.drop_index(batch_op.f("ix_vocabulary_words_user_id"))
        batch_op.drop_index(batch_op.f("ix_vocabulary_words_recognition_id"))

    op.drop_table("vocabulary_words")
