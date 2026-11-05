import threading
from collections.abc import Callable
from dataclasses import dataclass

from ml.factory import PREDICTORS, create_predictor, default_predictor_name
from ml.predictor import AbstractPredictor


class UnknownModel(ValueError):
    pass


@dataclass(frozen=True)
class ModelInfo:
    id: str
    name: str
    description: str


class ModelRegistry:
    """
    The models a request can choose from. The default is built right away, so a
    broken setup fails at startup. The others are built on first use, then shared
    and kept: kraken takes seconds and gigabytes to load, and its lock only works on
    one instance.
    """

    def __init__(
        self,
        build: Callable[[str], AbstractPredictor] = create_predictor,
        default: str | None = None,
    ) -> None:
        self.default = default or default_predictor_name()
        self._build = build
        self._loaded = {self.default: build(self.default)}
        self._lock = threading.Lock()

    def available(self) -> list[ModelInfo]:
        return [
            ModelInfo(
                id=model_id,
                name=predictor_class.display_name,
                description=predictor_class.description,
            )
            for model_id, predictor_class in PREDICTORS.items()
            if self._can_serve(model_id)
        ]

    def get(self, model_id: str | None = None) -> AbstractPredictor:
        model_id = model_id or self.default
        if (loaded := self._loaded.get(model_id)) is not None:
            return loaded
        if not self._can_serve(model_id):
            choices = ", ".join(model.id for model in self.available())
            raise UnknownModel(
                f"This model is not available. Choose one of: {choices}."
            )
        with self._lock:
            if model_id not in self._loaded:
                self._loaded[model_id] = self._build(model_id)
            return self._loaded[model_id]

    def _can_serve(self, model_id: str) -> bool:
        if model_id in self._loaded:
            return True
        predictor_class = PREDICTORS.get(model_id)
        return predictor_class is not None and predictor_class.is_available()
