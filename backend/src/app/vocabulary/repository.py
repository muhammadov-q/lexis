from sqlalchemy import Select, select
from sqlalchemy.exc import IntegrityError
from sqlalchemy.orm import Session, joinedload, load_only

from app.recognitions.models import Recognition
from app.vocabulary.models import VocabularyWord


class WordAlreadySaved(Exception):
    pass


def _with_source() -> Select[VocabularyWord]:
    return select(VocabularyWord).options(
        joinedload(VocabularyWord.recognition).options(
            load_only(Recognition.id, Recognition.filename)
        )
    )


class VocabularyRepository:
    def __init__(self, session: Session) -> None:
        self._session = session

    def add(self, word: VocabularyWord) -> VocabularyWord:
        self._session.add(word)
        try:
            self._session.commit()
        except IntegrityError as e:
            self._session.rollback()
            raise WordAlreadySaved(word.word) from e
        saved = self.get_for_user(word.user_id, word.id)
        assert saved is not None
        return saved

    def list_for_user(self, user_id: int) -> list[VocabularyWord]:
        query = (
            _with_source()
            .where(VocabularyWord.user_id == user_id)
            .order_by(VocabularyWord.created_at.desc(), VocabularyWord.id.desc())
        )
        return list(self._session.scalars(query))

    def get_for_user(self, user_id: int, word_id: int) -> VocabularyWord | None:
        return self._session.scalar(
            _with_source().where(
                VocabularyWord.id == word_id, VocabularyWord.user_id == user_id
            )
        )

    def save(self, word: VocabularyWord) -> VocabularyWord:
        self._session.commit()
        return word

    def delete(self, word: VocabularyWord) -> None:
        self._session.delete(word)
        self._session.commit()
