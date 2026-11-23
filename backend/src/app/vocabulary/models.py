from datetime import datetime

from sqlalchemy import JSON, ForeignKey, String, Text, UniqueConstraint
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.db import Base
from app.recognitions.models import Recognition


class VocabularyWord(Base):
    __tablename__ = "vocabulary_words"
    __table_args__ = (
        UniqueConstraint("user_id", "word", name="uq_vocabulary_words_user_id_word"),
    )

    id: Mapped[int] = mapped_column(primary_key=True)
    user_id: Mapped[int] = mapped_column(
        ForeignKey("users.id", ondelete="CASCADE"), index=True
    )
    recognition_id: Mapped[int] = mapped_column(
        ForeignKey("recognitions.id", ondelete="CASCADE"), index=True
    )
    created_at: Mapped[datetime]
    word: Mapped[str] = mapped_column(String(200))
    gloss: Mapped[dict[str, str] | None] = mapped_column(JSON)
    line: Mapped[int]
    word_index: Mapped[int]
    note: Mapped[str] = mapped_column(Text, default="")

    recognition: Mapped[Recognition] = relationship(lazy="raise")
