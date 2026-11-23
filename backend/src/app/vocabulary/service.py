import unicodedata
from collections.abc import Callable
from datetime import UTC, datetime

from app.recognitions.repository import RecognitionRepository
from app.recognitions.service import RecognitionNotFound
from app.vocabulary.models import VocabularyWord
from app.vocabulary.repository import VocabularyRepository


class VocabularyWordNotFound(Exception):
    pass


class NotAWord(Exception):
    pass


def headword(text: str) -> str:
    """The word without the punctuation around it, so "ἐστί," and "ἐστί" match."""
    word = unicodedata.normalize("NFC", text)
    start, end = 0, len(word)
    while start < end and _is_punctuation(word[start]):
        start += 1
    while end > start and _is_punctuation(word[end - 1]):
        end -= 1
    return word[start:end]


def _is_punctuation(char: str) -> bool:
    return char.isspace() or unicodedata.category(char).startswith("P")


class VocabularyService:
    def __init__(
        self,
        words: VocabularyRepository,
        recognitions: RecognitionRepository,
        clock: Callable[[], datetime] = lambda: datetime.now(UTC),
    ) -> None:
        self._words = words
        self._recognitions = recognitions
        self._clock = clock

    def add(
        self, user_id: int, recognition_id: int, word_index: int, note: str = ""
    ) -> VocabularyWord:
        recognition = self._recognitions.get_for_user(user_id, recognition_id)
        if recognition is None:
            raise RecognitionNotFound(recognition_id)
        if not 0 <= word_index < len(recognition.predictions):
            raise NotAWord(f"This page has no word number {word_index}.")

        prediction = recognition.predictions[word_index]
        word = headword(prediction["text"])
        if not word:
            raise NotAWord("Only words can be saved, not punctuation.")

        return self._words.add(
            VocabularyWord(
                user_id=user_id,
                recognition_id=recognition.id,
                created_at=self._clock(),
                word=word,
                gloss=prediction.get("gloss"),
                line=prediction.get("line", 0),
                word_index=word_index,
                note=note.strip(),
            )
        )

    def words(self, user_id: int) -> list[VocabularyWord]:
        return self._words.list_for_user(user_id)

    def get(self, user_id: int, word_id: int) -> VocabularyWord:
        word = self._words.get_for_user(user_id, word_id)
        if word is None:
            raise VocabularyWordNotFound(word_id)
        return word

    def update_note(self, user_id: int, word_id: int, note: str) -> VocabularyWord:
        word = self.get(user_id, word_id)
        word.note = note.strip()
        return self._words.save(word)

    def remove(self, user_id: int, word_id: int) -> None:
        self._words.delete(self.get(user_id, word_id))
