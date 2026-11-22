import pytest
from fastapi.testclient import TestClient

from app.auth.router import ERROR_401
from app.main import app
from app.prediction import get_predictor
from app.recognitions.router import ERROR_404 as RESULT_NOT_FOUND
from app.vocabulary.router import ERROR_404, ERROR_409, MAX_NOTE_LENGTH
from ml.predictor import AbstractPredictor, Prediction
from ml.translator import Gloss

NATURE = Gloss(lemma="φύσις", translation="nature", definition="noun: nature")


class GreekPredictor(AbstractPredictor):
    @property
    def model_version(self) -> str:
        return "greek"

    def predict(self, image: bytes) -> list[Prediction]:
        return [
            Prediction("Φύσις,", {"Φύσις,": 0.9}, (1, 2, 3, 4), line=0, gloss=NATURE),
            Prediction("·", {"·": 0.9}, (5, 2, 3, 4), line=0),
            Prediction("Φύσις", {"Φύσις": 0.9}, (1, 9, 3, 4), line=1),
        ]


def sign_up(client, email="ada@example.com"):
    response = client.post(
        "/auth/sign-up", json={"email": email, "password": "correct horse"}
    )
    assert response.status_code == 201


@pytest.fixture
def ada(client):
    app.dependency_overrides[get_predictor] = lambda: GreekPredictor()
    sign_up(client)
    return client


@pytest.fixture
def page(ada):
    response = ada.post(
        "/recognitions", files={"image": ("folio-1r.png", b"png", "image/png")}
    )
    assert response.status_code == 201
    return response.json()["id"]


def add(client, recognition_id, word_index, **fields):
    return client.post(
        "/vocabulary",
        json={"recognition_id": recognition_id, "word_index": word_index, **fields},
    )


def test_a_word_from_a_saved_page_goes_into_your_vocabulary(ada, page):
    response = add(ada, page, 0)

    assert response.status_code == 201
    word = response.json()
    assert set(word) == {"id", "created_at", "word", "gloss", "note", "source"}
    assert word["word"] == "Φύσις"
    assert word["gloss"] == {
        "lemma": "φύσις",
        "translation": "nature",
        "definition": "noun: nature",
    }
    assert word["note"] == ""
    assert word["source"] == {
        "recognition_id": page,
        "filename": "folio-1r.png",
        "line": 0,
        "word_index": 0,
    }
    assert word["created_at"].endswith("Z")

    assert ada.get("/vocabulary").json() == [word]
    assert ada.get(f"/vocabulary/{word['id']}").json() == word


def test_a_note_can_be_written_and_changed(ada, page):
    word_id = add(ada, page, 0).json()["id"]

    first = ada.patch(f"/vocabulary/{word_id}", json={"note": "as in physics"})
    second = ada.patch(f"/vocabulary/{word_id}", json={"note": "as in physical"})

    assert first.status_code == second.status_code == 200
    assert ada.get(f"/vocabulary/{word_id}").json()["note"] == "as in physical"


def test_a_word_can_be_added_with_a_note(ada, page):
    response = add(ada, page, 0, note="as in physics")

    assert response.status_code == 201
    assert response.json()["note"] == "as in physics"


def test_a_note_has_a_length_limit(ada, page):
    too_long = "x" * (MAX_NOTE_LENGTH + 1)
    word_id = add(ada, page, 0).json()["id"]

    assert add(ada, page, 1, note=too_long).status_code == 422
    assert (
        ada.patch(f"/vocabulary/{word_id}", json={"note": too_long}).status_code == 422
    )
    assert ada.get(f"/vocabulary/{word_id}").json()["note"] == ""


def test_a_removed_word_is_gone(ada, page):
    word_id = add(ada, page, 0).json()["id"]

    assert ada.delete(f"/vocabulary/{word_id}").status_code == 204

    assert ada.get("/vocabulary").json() == []
    response = ada.get(f"/vocabulary/{word_id}")
    assert response.status_code == 404
    assert response.json() == {"detail": ERROR_404}


def test_the_same_word_is_saved_once(ada, page):
    add(ada, page, 0)

    response = add(ada, page, 2)

    assert response.status_code == 409
    assert response.json() == {"detail": ERROR_409}


@pytest.mark.parametrize(
    ("word_index", "detail"),
    [
        (1, "Only words can be saved, not punctuation."),
        (3, "This page has no word number 3."),
    ],
)
def test_only_words_on_the_page_can_be_added(ada, page, word_index, detail):
    response = add(ada, page, word_index)

    assert response.status_code == 422
    assert response.json() == {"detail": detail}


@pytest.mark.parametrize(
    ("method", "path"),
    [
        ("GET", "/vocabulary"),
        ("POST", "/vocabulary"),
        ("GET", "/vocabulary/1"),
        ("PATCH", "/vocabulary/1"),
        ("DELETE", "/vocabulary/1"),
    ],
)
def test_vocabulary_needs_you_to_be_signed_in(client, method, path):
    response = client.request(method, path)

    assert response.status_code == 401
    assert response.json() == {"detail": ERROR_401}


def test_someone_elses_page_and_words_are_not_found(ada, page):
    word_id = add(ada, page, 0).json()["id"]
    bob = TestClient(app)
    sign_up(bob, "bob@example.com")

    response = add(bob, page, 0)
    assert response.status_code == 404
    assert response.json() == {"detail": RESULT_NOT_FOUND}

    assert bob.get("/vocabulary").json() == []
    for response in [
        bob.get(f"/vocabulary/{word_id}"),
        bob.patch(f"/vocabulary/{word_id}", json={"note": "mine"}),
        bob.delete(f"/vocabulary/{word_id}"),
    ]:
        assert response.status_code == 404
        assert response.json() == {"detail": ERROR_404}
    assert ada.get(f"/vocabulary/{word_id}").json()["note"] == ""
