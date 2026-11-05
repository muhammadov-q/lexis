from unittest.mock import patch

import pytest

from app.main import app
from app.prediction import get_predictor, get_registry, registry
from ml.eparchos_predictor import EparchosPredictor
from ml.factory import PREDICTORS, create_predictor
from ml.predictor import DummyPredictor
from ml.registry import ModelRegistry
from ml.translating_predictor import TranslatingPredictor


class NotInstalledPredictor(DummyPredictor):
    display_name = "Not installed"
    description = "Needs something this install does not have."

    @classmethod
    def is_available(cls) -> bool:
        return False


@pytest.fixture(autouse=True)
def only_these_models():
    models = {
        "eparchos": EparchosPredictor,
        "dummy": DummyPredictor,
        "not-installed": NotInstalledPredictor,
    }
    with patch.dict(PREDICTORS, models, clear=True):
        yield


@pytest.fixture
def built():
    return []


@pytest.fixture
def choosing(client, built):
    """A client whose requests really pick their model, with dummy as the default."""

    def build(name):
        built.append(name)
        return create_predictor(name)

    selectable = ModelRegistry(build=build, default="dummy")
    del app.dependency_overrides[get_predictor]
    app.dependency_overrides[get_registry] = lambda: selectable
    return client


def post_page(client, path, model=None, content=b"png bytes", content_type="image/png"):
    data = {} if model is None else {"model": model}
    files = {"image": ("page.png", content, content_type)}
    return client.post(path, files=files, data=data)


def sign_up(client):
    response = client.post(
        "/auth/sign-up", json={"email": "ada@example.com", "password": "correct horse"}
    )
    assert response.status_code == 201


# --- GET /models ------------------------------------------------------------


def test_models_returns_what_can_run_and_the_default(choosing):
    response = choosing.get("/models")

    assert response.status_code == 200
    assert response.json() == {
        "models": [
            {
                "id": "eparchos",
                "name": EparchosPredictor.display_name,
                "description": EparchosPredictor.description,
            },
            {
                "id": "dummy",
                "name": DummyPredictor.display_name,
                "description": DummyPredictor.description,
            },
        ],
        "default_model": "dummy",
    }


def test_models_is_open_without_signing_in(choosing):
    assert "team4_session" not in choosing.cookies
    assert choosing.get("/models").status_code == 200


def test_listing_models_loads_none_of_them(choosing, built):
    choosing.get("/models")
    assert built == ["dummy"]


# --- POST /predict ----------------------------------------------------------


def test_predict_uses_the_chosen_model(choosing):
    response = post_page(choosing, "/predict", model="eparchos")

    assert response.status_code == 200
    body = response.json()
    assert body["model_version"] == "eparchos-6791"
    assert body["predictions"][0]["text"] == "Ἐπιλύσεις"


@pytest.mark.parametrize("nothing_chosen", [None, ""])
def test_predict_uses_the_default_when_nothing_is_chosen(choosing, nothing_chosen):
    response = post_page(choosing, "/predict", model=nothing_chosen)

    assert response.status_code == 200
    assert response.json()["model_version"] == "mock"


@pytest.mark.parametrize("model", ["does-not-exist", "not-installed"])
def test_predict_rejects_a_model_it_cannot_use(choosing, model):
    response = post_page(choosing, "/predict", model=model)

    assert response.status_code == 422
    assert response.json() == {
        "detail": "This model is not available. Choose one of: eparchos, dummy."
    }


def test_a_rejected_upload_does_not_load_the_chosen_model(choosing, built):
    response = post_page(
        choosing, "/predict", model="eparchos", content_type="text/plain"
    )

    assert response.status_code == 415
    assert built == ["dummy"]


def test_a_rejected_page_does_not_load_the_chosen_model(choosing, built):
    sign_up(choosing)

    response = post_page(
        choosing, "/recognitions", model="eparchos", content_type="text/plain"
    )

    assert response.status_code == 415
    assert built == ["dummy"]


def test_a_model_is_loaded_once_across_requests(choosing, built):
    post_page(choosing, "/predict", model="eparchos")
    post_page(choosing, "/predict", model="eparchos")

    assert built == ["dummy", "eparchos"]


# --- POST /recognitions -----------------------------------------------------


def test_recognitions_uses_the_chosen_model_and_saves_which_one(choosing):
    sign_up(choosing)

    created = post_page(choosing, "/recognitions", model="eparchos").json()

    assert created["model_version"] == "eparchos-6791"
    assert created["predictions"][0]["text"] == "Ἐπιλύσεις"
    saved = choosing.get(f"/recognitions/{created['id']}").json()
    assert saved["model_version"] == "eparchos-6791"


def test_recognitions_uses_the_default_when_nothing_is_chosen(choosing):
    sign_up(choosing)

    created = post_page(choosing, "/recognitions").json()

    assert created["model_version"] == "mock"


def test_recognitions_rejects_an_unknown_model_and_saves_nothing(choosing):
    sign_up(choosing)

    response = post_page(choosing, "/recognitions", model="does-not-exist")

    assert response.status_code == 422
    assert "Choose one of" in response.json()["detail"]
    assert choosing.get("/recognitions").json() == []


def test_signing_in_comes_before_choosing_a_model(choosing):
    response = post_page(choosing, "/recognitions", model="does-not-exist")
    assert response.status_code == 401


def test_reading_the_history_loads_no_model(choosing, built):
    sign_up(choosing)

    assert choosing.get("/recognitions").status_code == 200
    assert built == ["dummy"]


# --- The API contract -------------------------------------------------------


@pytest.mark.parametrize("path", ["/predict", "/recognitions"])
def test_the_schema_offers_an_optional_model_on_uploads(path):
    schema = app.openapi()
    body = schema["paths"][path]["post"]["requestBody"]["content"][
        "multipart/form-data"
    ]["schema"]
    form = schema["components"]["schemas"][body["$ref"].rsplit("/", 1)[-1]]

    assert "model" in form["properties"]
    assert form["required"] == ["image"]


@pytest.mark.parametrize(
    "path",
    ["/recognitions", "/recognitions/{recognition_id}"],
)
def test_reading_routes_take_no_form(path):
    assert "requestBody" not in app.openapi()["paths"][path]["get"]


# --- The app's real registry ------------------------------------------------


@pytest.mark.parametrize("name", [None, "eparchos"])
def test_every_model_the_app_serves_is_translated(name):
    predictor = registry.get(name)

    assert isinstance(predictor, TranslatingPredictor)
