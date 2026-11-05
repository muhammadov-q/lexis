import threading
import time
from concurrent.futures import ThreadPoolExecutor
from unittest.mock import patch

import pytest

from ml.factory import PREDICTORS, create_predictor
from ml.predictor import AbstractPredictor, Prediction
from ml.registry import ModelInfo, ModelRegistry, UnknownModel


class ReadyPredictor(AbstractPredictor):
    display_name = "Ready"
    description = "Can run."

    @property
    def model_version(self) -> str:
        return "ready"

    def predict(self, image: bytes) -> list[Prediction]:
        return []


class OtherPredictor(ReadyPredictor):
    display_name = "Other"
    description = "Can run too."


class MissingPredictor(ReadyPredictor):
    display_name = "Missing"
    description = "Its weights are not here."

    @classmethod
    def is_available(cls) -> bool:
        return False


READY = ModelInfo(id="ready", name="Ready", description="Can run.")
OTHER = ModelInfo(id="other", name="Other", description="Can run too.")


@pytest.fixture(autouse=True)
def only_these_models():
    models = {
        "ready": ReadyPredictor,
        "missing": MissingPredictor,
        "other": OtherPredictor,
    }
    with patch.dict(PREDICTORS, models, clear=True):
        yield


class CountingBuild:
    def __init__(self):
        self.built = []

    def __call__(self, name):
        self.built.append(name)
        return create_predictor(name)


@pytest.fixture
def build():
    return CountingBuild()


# --- Which models are offered -----------------------------------------------


def test_lists_the_models_that_can_run_in_registry_order():
    assert ModelRegistry(default="ready").available() == [READY, OTHER]


def test_the_default_comes_from_the_argument():
    assert ModelRegistry(default="other").default == "other"


def test_the_default_comes_from_the_env_var(monkeypatch):
    monkeypatch.setenv("ML_PREDICTOR", "other")
    assert ModelRegistry().default == "other"


def test_a_loaded_model_stays_listed_when_its_files_disappear():
    registry = ModelRegistry(default="missing")

    assert "missing" in [model.id for model in registry.available()]
    assert isinstance(registry.get("missing"), MissingPredictor)


# --- Which model a request gets ---------------------------------------------


@pytest.mark.parametrize("nothing_chosen", [None, ""])
def test_no_choice_means_the_default(nothing_chosen):
    registry = ModelRegistry(default="other")
    assert isinstance(registry.get(nothing_chosen), OtherPredictor)


def test_a_choice_gets_that_model():
    registry = ModelRegistry(default="ready")
    assert isinstance(registry.get("other"), OtherPredictor)


@pytest.mark.parametrize("name", ["does-not-exist", "missing"])
def test_a_model_that_cannot_be_used_is_rejected_with_the_choices(name):
    registry = ModelRegistry(default="ready")
    with pytest.raises(UnknownModel, match="ready, other"):
        registry.get(name)


def test_unknown_model_is_a_value_error():
    assert issubclass(UnknownModel, ValueError)


# --- When models are built --------------------------------------------------


def test_only_the_default_is_built_at_startup(build):
    ModelRegistry(build=build, default="ready")
    assert build.built == ["ready"]


def test_listing_models_builds_nothing(build):
    ModelRegistry(build=build, default="ready").available()
    assert build.built == ["ready"]


def test_each_model_is_built_once_and_shared(build):
    registry = ModelRegistry(build=build, default="ready")

    assert registry.get("other") is registry.get("other")
    assert registry.get() is registry.get("ready")
    assert build.built == ["ready", "other"]


def test_a_broken_default_fails_at_startup():
    with pytest.raises(ValueError, match="does-not-exist"):
        ModelRegistry(default="does-not-exist")


def test_a_model_that_failed_to_build_is_tried_again(build):
    outcomes = [RuntimeError("corrupt weights"), OtherPredictor()]

    def flaky(name):
        if name == "ready":
            return ReadyPredictor()
        outcome = outcomes.pop(0)
        if isinstance(outcome, Exception):
            raise outcome
        return outcome

    registry = ModelRegistry(build=flaky, default="ready")

    with pytest.raises(RuntimeError, match="corrupt"):
        registry.get("other")
    assert isinstance(registry.get("other"), OtherPredictor)


def test_requests_arriving_together_build_one_model():
    built = []

    def slow_build(name):
        built.append(name)
        time.sleep(0.05)  # so the other requests arrive during the build
        return create_predictor(name)

    registry = ModelRegistry(build=slow_build, default="ready")
    barrier = threading.Barrier(8)

    def first_use(_):
        barrier.wait(timeout=5)
        return registry.get("other")

    with ThreadPoolExecutor(max_workers=8) as pool:
        predictors = list(pool.map(first_use, range(8)))

    assert built == ["ready", "other"]
    assert all(predictor is predictors[0] for predictor in predictors)


def test_a_loaded_model_answers_while_another_one_loads():
    loading = threading.Event()
    finish_loading = threading.Event()

    def slow_other(name):
        if name == "other":
            loading.set()
            assert finish_loading.wait(timeout=5)
        return create_predictor(name)

    registry = ModelRegistry(build=slow_other, default="ready")

    with ThreadPoolExecutor(max_workers=1) as pool:
        other = pool.submit(registry.get, "other")
        assert loading.wait(timeout=5)
        assert isinstance(registry.get("ready"), ReadyPredictor)
        finish_loading.set()
        assert isinstance(other.result(timeout=5), OtherPredictor)
