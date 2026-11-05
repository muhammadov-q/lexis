from abc import ABC, abstractmethod
from dataclasses import dataclass
from typing import ClassVar

from ml.translator import Gloss

Box = tuple[int, int, int, int]  # x, y, w, h


@dataclass
class Prediction:
    text: str
    probabilities: dict[str, float]
    box: Box
    line: int = 0
    gloss: Gloss | None = None


class AbstractPredictor(ABC):
    """Base class for every model that predicts the content of an image."""

    display_name: ClassVar[str] = ""
    description: ClassVar[str] = ""

    @classmethod
    def is_available(cls) -> bool:
        """Whether this install can run the model, checked without loading it."""
        return True

    @property
    @abstractmethod
    def model_version(self) -> str:
        """Return the version of the model."""

    @abstractmethod
    def predict(self, image: bytes) -> list[Prediction]:
        """Take the raw image bytes and return what it reads, word by word."""


class DummyPredictor(AbstractPredictor):
    """Placeholder: ignores the image and always returns the dummy digits."""

    display_name = "Mock digits"
    description = "Ignores the page and returns the same six digits. For testing."

    def __init__(self) -> None:
        super().__init__()
        self.model = "mock"
        self.dummy = ["2", "5", "0", "0", "0", "0"]

    @property
    def model_version(self) -> str:
        return self.model

    def predict(self, image: bytes) -> list[Prediction]:
        return [
            Prediction(text=d, probabilities={d: 1.0}, box=(0, 0, 0, 0))
            for d in self.dummy
        ]
