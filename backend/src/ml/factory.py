import os

from ml.eparchos_predictor import EparchosPredictor
from ml.knn_predictor import KnnPredictor
from ml.kraken_predictor import KrakenPredictor
from ml.predictor import AbstractPredictor, DummyPredictor

PREDICTORS: dict[str, type[AbstractPredictor]] = {
    "dummy": DummyPredictor,
    "knn": KnnPredictor,
    "eparchos": EparchosPredictor,
    "kraken": KrakenPredictor,
}


def create_predictor(name: str | None = None) -> AbstractPredictor:
    """Build the predictor chosen by name, or by ML_PREDICTOR env var"""
    if not name:
        name = os.getenv("ML_PREDICTOR", "dummy")
    try:
        predictor_class = PREDICTORS[name]
    except KeyError:
        raise ValueError(
            f"Unknown predictor {name!r}. Available: {sorted(PREDICTORS)}"
        ) from None
    return predictor_class()
