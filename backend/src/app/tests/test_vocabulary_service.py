import unicodedata
from datetime import UTC, datetime

import pytest

from app.recognitions.repository import RecognitionRepository
from app.recognitions.service import RecognitionNotFound
from app.vocabulary.repository import VocabularyRepository, WordAlreadySaved
from app.vocabulary.service import (
    NotAWord,
    VocabularyService,
    VocabularyWordNotFound,
    headword,
)

NOON = datetime(2026, 11, 22, 12, 0, tzinfo=UTC)
BOX = {"x": 1, "y": 2, "w": 3, "h": 4}
IS = {"lemma": "εἰμί", "translation": "is", "definition": "verb: to be"}
PAGE = [
    {"text": "Φύσις", "probabilities": {}, "box": BOX, "line": 0, "gloss": None},
    {"text": "ἐστί,", "probabilities": {}, "box": BOX, "line": 0, "gloss": IS},
    {"text": "·", "probabilities": {}, "box": BOX, "line": 1, "gloss": None},
    {"text": "ἐστί", "probabilities": {}, "box": BOX, "line": 1, "gloss": None},
]


@pytest.fixture
def ada(make_user):
    return make_user("ada@example.com")


@pytest.fixture
def bob(make_user):
    return make_user("bob@example.com")


@pytest.fixture
def page(make_recognition, ada):
    return make_recognition(ada, PAGE)


@pytest.fixture
def service(db):
    return VocabularyService(
        VocabularyRepository(db), RecognitionRepository(db), clock=lambda: NOON
    )


@pytest.mark.parametrize(
    ("text", "word"),
    [
        ("ἐστί,", "ἐστί"),
        ("«λόγος»", "λόγος"),
        (" καὶ· ", "καὶ"),
        ("·", ""),
        (unicodedata.normalize("NFD", "ἐστί"), "ἐστί"),
    ],
)
def test_headword_drops_the_punctuation_around_a_word(text, word):
    assert headword(text) == word


def test_adding_a_word_keeps_its_translation_and_place(service, ada, page):
    word = service.add(ada, page, 1)

    assert (word.word, word.gloss, word.line, word.word_index, word.note) == (
        "ἐστί",
        IS,
        0,
        1,
        "",
    )
    assert word.recognition_id == page
    assert word.created_at == NOON
    assert [w.id for w in service.words(ada)] == [word.id]


def test_the_same_word_from_another_line_is_already_saved(service, ada, page):
    service.add(ada, page, 1)

    with pytest.raises(WordAlreadySaved):
        service.add(ada, page, 3)


@pytest.mark.parametrize("word_index", [2, 4])
def test_only_a_word_on_the_page_can_be_added(service, ada, page, word_index):
    with pytest.raises(NotAWord):
        service.add(ada, page, word_index)


def test_words_come_only_from_your_own_pages(service, bob, page):
    with pytest.raises(RecognitionNotFound):
        service.add(bob, page, 0)


def test_a_note_is_saved_without_the_spaces_around_it(service, ada, page):
    word = service.add(ada, page, 0)

    service.update_note(ada, word.id, "  nature, as in physics \n")

    assert service.get(ada, word.id).note == "nature, as in physics"


def test_a_removed_word_is_gone(service, ada, page):
    word = service.add(ada, page, 0)

    service.remove(ada, word.id)

    assert service.words(ada) == []
    with pytest.raises(VocabularyWordNotFound):
        service.get(ada, word.id)


def test_someone_elses_word_is_not_found(service, ada, bob, page):
    word = service.add(ada, page, 0)

    for action in [
        lambda: service.get(bob, word.id),
        lambda: service.update_note(bob, word.id, "mine now"),
        lambda: service.remove(bob, word.id),
    ]:
        with pytest.raises(VocabularyWordNotFound):
            action()
    assert service.get(ada, word.id).note == ""
