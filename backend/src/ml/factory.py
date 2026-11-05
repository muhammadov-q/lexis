import os

from ml.eparchos_predictor import EparchosPredictor
from ml.knn_predictor import KnnPredictor
from ml.kraken_predictor import KrakenBasePredictor, KrakenPredictor
from ml.predictor import AbstractPredictor, DummyPredictor

# GET /models lists them in this order, so the real readers come first
PREDICTORS: dict[str, type[AbstractPredictor]] = {
    "kraken": KrakenPredictor,
    "kraken-base": KrakenBasePredictor,
    "knn": KnnPredictor,
    "eparchos": EparchosPredictor,
    "dummy": DummyPredictor,
}


def default_predictor_name() -> str:
    return os.getenv("ML_PREDICTOR", "dummy")


def create_predictor(name: str | None = None) -> AbstractPredictor:
    """Build the predictor chosen by name, or by ML_PREDICTOR env var"""
    if not name:
        name = default_predictor_name()
    try:
        predictor_class = PREDICTORS[name]
    except KeyError:
        raise ValueError(
            f"Unknown predictor {name!r}. Available: {sorted(PREDICTORS)}"
        ) from None
    return predictor_class()
