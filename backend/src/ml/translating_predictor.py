from dataclasses import replace

from ml.predictor import AbstractPredictor, Prediction
from ml.translator import AbstractTranslator


class TranslatingPredictor(AbstractPredictor):
    def __init__(
        self, predictor: AbstractPredictor, translator: AbstractTranslator
    ) -> None:
        super().__init__()
        self.predictor = predictor
        self.translator = translator

    @property
    def model_version(self) -> str:
        return self.predictor.model_version

    def predict(self, image: bytes) -> list[Prediction]:
        words = self.predictor.predict(image)
        glosses = self.translator.translate([word.text for word in words])
        return [
            replace(word, gloss=word.gloss or gloss)
            for word, gloss in zip(words, glosses, strict=True)
        ]
