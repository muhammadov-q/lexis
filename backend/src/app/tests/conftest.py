from datetime import UTC, datetime

import pytest
from fastapi.testclient import TestClient

from app.auth.models import User
from app.db import create_db_engine, create_session_factory, get_db, migrate
from app.main import app
from app.prediction import get_predictor
from app.recognitions.models import Recognition
from ml.predictor import DummyPredictor


@pytest.fixture
def client(engine):
    """Test client that always uses the DummyPredictor, whatever ML_PREDICTOR says."""
    session_factory = create_session_factory(engine)

    def test_db():
        with session_factory() as session:
            yield session

    app.dependency_overrides[get_predictor] = lambda: DummyPredictor()
    app.dependency_overrides[get_db] = test_db
    yield TestClient(app)
    app.dependency_overrides.clear()


@pytest.fixture
def engine(tmp_path):
    engine = create_db_engine(f"sqlite:///{tmp_path / 'test.db'}")
    migrate(engine)
    yield engine
    engine.dispose()


@pytest.fixture
def db(engine):
    with create_session_factory(engine)() as session:
        yield session


@pytest.fixture
def make_user(db):
    def make(email):
        user = User(email=email, password_hash="x", created_at=datetime.now(UTC))
        db.add(user)
        db.commit()
        return user.id

    return make


@pytest.fixture
def make_recognition(db):
    def make(user_id, predictions, filename="1r.png"):
        recognition = Recognition(
            user_id=user_id,
            created_at=datetime.now(UTC),
            filename=filename,
            content_type="image/png",
            image=b"png bytes",
            model_version="mock",
            predictions=predictions,
        )
        db.add(recognition)
        db.commit()
        return recognition.id

    return make
