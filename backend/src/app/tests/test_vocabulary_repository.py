from datetime import UTC, datetime, timedelta

import pytest
from sqlalchemy import delete, select

from app.auth.models import User
from app.recognitions.models import Recognition
from app.vocabulary.models import VocabularyWord
from app.vocabulary.repository import VocabularyRepository, WordAlreadySaved

NOON = datetime(2026, 11, 22, 12, 0, tzinfo=UTC)
GLOSS = {"lemma": "φύσις", "translation": "nature", "definition": "noun: nature"}


@pytest.fixture
def ada(make_user):
    return make_user("ada@example.com")


@pytest.fixture
def bob(make_user):
    return make_user("bob@example.com")


@pytest.fixture
def repository(db):
    return VocabularyRepository(db)


def vocabulary_word(user_id, recognition_id, word="φύσις", created_at=NOON):
    return VocabularyWord(
        user_id=user_id,
        recognition_id=recognition_id,
        created_at=created_at,
        word=word,
        gloss=GLOSS,
        line=2,
        word_index=5,
        note="",
    )


def test_a_saved_word_reads_back_with_its_page(repository, db, make_recognition, ada):
    page = make_recognition(ada, [], filename="folio-1r.png")
    saved = repository.add(vocabulary_word(ada, page))
    db.expire_all()

    word = repository.get_for_user(ada, saved.id)

    assert word is not None
    assert (word.word, word.gloss, word.line, word.word_index, word.note) == (
        "φύσις",
        GLOSS,
        2,
        5,
        "",
    )
    assert word.created_at == NOON
    assert word.recognition.filename == "folio-1r.png"


def test_the_list_is_yours_and_newest_first(repository, make_recognition, ada, bob):
    page = make_recognition(ada, [])
    older = repository.add(vocabulary_word(ada, page, "λόγος", NOON))
    newer = repository.add(vocabulary_word(ada, page, "φύσις", NOON + timedelta(1)))
    repository.add(vocabulary_word(bob, make_recognition(bob, []), "λόγος"))

    assert [w.id for w in repository.list_for_user(ada)] == [newer.id, older.id]
    assert repository.get_for_user(bob, older.id) is None


def test_a_word_is_saved_once_per_user(repository, make_recognition, ada, bob):
    repository.add(vocabulary_word(ada, make_recognition(ada, [])))

    with pytest.raises(WordAlreadySaved):
        repository.add(vocabulary_word(ada, make_recognition(ada, [])))

    repository.add(vocabulary_word(bob, make_recognition(bob, [])))
    assert len(repository.list_for_user(ada)) == 1


def test_a_note_and_a_removal_are_saved(repository, db, make_recognition, ada):
    word = repository.add(vocabulary_word(ada, make_recognition(ada, [])))

    word.note = "Seen in Aristotle too"
    repository.save(word)
    db.expire_all()
    saved = repository.get_for_user(ada, word.id)
    assert saved is not None
    assert saved.note == "Seen in Aristotle too"

    repository.delete(saved)
    assert repository.list_for_user(ada) == []


@pytest.mark.parametrize("parent", [User, Recognition])
def test_deleting_the_user_or_the_page_deletes_the_word(
    repository, db, make_recognition, ada, parent
):
    page = make_recognition(ada, [])
    repository.add(vocabulary_word(ada, page))

    db.execute(delete(parent).where(parent.id == (ada if parent is User else page)))
    db.commit()

    assert db.scalars(select(VocabularyWord)).all() == []
