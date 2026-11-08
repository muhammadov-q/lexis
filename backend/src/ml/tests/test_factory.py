from importlib.util import find_spec

import pytest

from ml.factory import PREDICTORS, create_predictor
from ml.knn_predictor import MODEL_PATH
from ml.kraken_predictor import MODEL_PATH as KRAKEN_MODEL_PATH
from ml.predictor import AbstractPredictor, DummyPredictor, Prediction


class FakePredictor(AbstractPredictor):
    @property
    def model_version(self):
        return ""

    def predict(self, image: bytes) -> list[Prediction]:
        return []


@pytest.fixture
def fake_registered(monkeypatch):
    """Temporarily add FakePredictor to the registry under the name 'fake'."""
    monkeypatch.setitem(PREDICTORS, "fake", FakePredictor)


def test_create_by_name():
    assert isinstance(create_predictor("dummy"), DummyPredictor)


def test_default_is_dummy(monkeypatch):
    monkeypatch.delenv("ML_PREDICTOR", raising=False)
    assert isinstance(create_predictor(), DummyPredictor)


def test_env_var_selects_predictor(monkeypatch, fake_registered):
    monkeypatch.setenv("ML_PREDICTOR", "fake")
    assert isinstance(create_predictor(), FakePredictor)


def test_explicit_name_wins_over_env_var(monkeypatch, fake_registered):
    monkeypatch.setenv("ML_PREDICTOR", "fake")
    assert isinstance(create_predictor("dummy"), DummyPredictor)


def test_unknown_name_raises_with_available_names():
    with pytest.raises(ValueError, match="dummy"):
        create_predictor("does-not-exist")


def test_unknown_env_var_raises(monkeypatch):
    monkeypatch.setenv("ML_PREDICTOR", "does-not-exist")
    with pytest.raises(ValueError):
        create_predictor()


def test_each_call_returns_a_new_instance():
    assert create_predictor("dummy") is not create_predictor("dummy")


@pytest.mark.parametrize("name", sorted(PREDICTORS))
def test_every_registered_predictor_follows_the_contract(name, digit_image):
    if name == "knn" and not MODEL_PATH.exists():
        pytest.skip("KNN model not trained (run train_knn.py)")
    if name == "kraken" and (
        find_spec("kraken") is None or not KRAKEN_MODEL_PATH.exists()
    ):
        pytest.skip("needs the ml extra and the kraken model, see backend/README.md")

    predictor = create_predictor(name)
    assert isinstance(predictor, AbstractPredictor)
    assert isinstance(predictor.model_version, str)

    results = predictor.predict(digit_image)
    assert isinstance(results, list)
    for r in results:
        assert isinstance(r, Prediction)
        assert isinstance(r.text, str)
        assert isinstance(r.probabilities, dict)
        assert len(r.box) == 4
