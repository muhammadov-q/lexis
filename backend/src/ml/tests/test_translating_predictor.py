from collections.abc import Sequence
from dataclasses import replace

from ml.predictor import AbstractPredictor, Prediction
from ml.translating_predictor import TranslatingPredictor
from ml.translator import AbstractTranslator, Gloss

AND = Gloss(lemma="καί", translation="and", definition="conjunction: and")


class TwoWords(AbstractPredictor):
    @property
    def model_version(self) -> str:
        return "two-words"

    def predict(self, image: bytes) -> list[Prediction]:
        return [
            Prediction(text="καὶ", probabilities={"καὶ": 0.9}, box=(1, 2, 3, 4)),
            Prediction(text="?", probabilities={"?": 0.5}, box=(5, 6, 7, 8), line=1),
        ]


class KnowsAnd(AbstractTranslator):
    def __init__(self) -> None:
        self.calls: list[list[str]] = []

    def translate(self, words: Sequence[str]) -> list[Gloss | None]:
        self.calls.append(list(words))
        return [AND if word == "καὶ" else None for word in words]


class GivesItsOwnGloss(TwoWords):
    def predict(self, image: bytes) -> list[Prediction]:
        first, second = super().predict(image)
        return [replace(first, gloss=OWN), second]


OWN = Gloss(lemma="καί", translation="also", definition="adverb: also, even")


def test_keeps_a_gloss_the_predictor_already_gave():
    first, second = TranslatingPredictor(GivesItsOwnGloss(), KnowsAnd()).predict(b"x")

    assert first.gloss == OWN
    assert second.gloss is None


def test_is_an_abstract_predictor():
    assert isinstance(TranslatingPredictor(TwoWords(), KnowsAnd()), AbstractPredictor)


def test_keeps_the_model_version_of_the_predictor():
    assert TranslatingPredictor(TwoWords(), KnowsAnd()).model_version == "two-words"


def test_adds_a_gloss_to_each_word_it_knows():
    first, second = TranslatingPredictor(TwoWords(), KnowsAnd()).predict(b"img")
    assert first.gloss == AND
    assert second.gloss is None


def test_keeps_what_the_predictor_read():
    words = TranslatingPredictor(TwoWords(), KnowsAnd()).predict(b"img")
    assert [(w.text, w.probabilities, w.box, w.line) for w in words] == [
        ("καὶ", {"καὶ": 0.9}, (1, 2, 3, 4), 0),
        ("?", {"?": 0.5}, (5, 6, 7, 8), 1),
    ]


def test_translates_the_whole_page_in_one_call():
    translator = KnowsAnd()
    TranslatingPredictor(TwoWords(), translator).predict(b"img")
    assert translator.calls == [["καὶ", "?"]]
