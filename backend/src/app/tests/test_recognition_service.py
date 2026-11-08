from datetime import UTC, datetime

import pytest

from app.recognitions.repository import RecognitionRepository
from app.recognitions.service import RecognitionNotFound, RecognitionService
from app.uploads import ImageUpload
from ml.predictor import AbstractPredictor, DummyPredictor, Prediction

PAGE = ImageUpload(content=b"png bytes", content_type="image/png", filename="1r.png")
NOON = datetime(2026, 10, 15, 12, 0, tzinfo=UTC)


class UnreadablePredictor(AbstractPredictor):
    @property
    def model_version(self) -> str:
        return "unreadable"

    def predict(self, image: bytes) -> list[Prediction]:
        raise ValueError("Impossible to decode image")


@pytest.fixture
def ada(make_user):
    return make_user("ada@example.com")


@pytest.fixture
def bob(make_user):
    return make_user("bob@example.com")


@pytest.fixture
def service(db):
    return RecognitionService(RecognitionRepository(db), clock=lambda: NOON)


def test_recognize_saves_the_page_and_what_the_model_read(service, db, ada):
    recognition = service.recognize(ada, PAGE, DummyPredictor())
    db.expire_all()

    saved = service.get(ada, recognition.id)
    assert saved.filename == "1r.png"
    assert saved.model_version == "mock"
    assert saved.created_at == NOON
    assert [p["text"] for p in saved.predictions] == ["2", "5", "0", "0", "0", "0"]
    assert saved.predictions[0] == {
        "text": "2",
        "probabilities": {"2": 1.0},
        "box": {"x": 0, "y": 0, "w": 0, "h": 0},
        "line": 0,
        "gloss": None,
    }
    assert service.image(ada, recognition.id) == (b"png bytes", "image/png")
    assert [r.id for r in service.history(ada)] == [recognition.id]


def test_an_image_the_model_cannot_read_is_not_saved(service, ada):
    with pytest.raises(ValueError, match="decode"):
        service.recognize(ada, PAGE, UnreadablePredictor())

    assert service.history(ada) == []


def test_someone_elses_result_is_not_found(service, ada, bob):
    recognition = service.recognize(ada, PAGE, DummyPredictor())

    with pytest.raises(RecognitionNotFound):
        service.get(bob, recognition.id)
    with pytest.raises(RecognitionNotFound):
        service.image(bob, recognition.id)


def test_an_unknown_result_is_not_found(service, ada):
    with pytest.raises(RecognitionNotFound):
        service.get(ada, 999)


def test_long_filenames_are_shortened(service, ada):
    upload = ImageUpload(content=b"x", content_type="image/png", filename="a" * 300)

    assert len(service.recognize(ada, upload, DummyPredictor()).filename) == 255
