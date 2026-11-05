import unicodedata
from importlib.util import find_spec

import numpy as np
import pytest

from ml.kraken_predictor import (
    BASE_MODEL_PATH,
    MODEL_PATH,
    KrakenBasePredictor,
    KrakenPredictor,
    bounds,
    ink_box,
    words_in_line,
)
from ml.predictor import Prediction

LINE_BOX = (10, 20, 200, 70)
AB_CD = list("ab cd")
AB_CD_XS = [30, 50, 90, 130, 170]


@pytest.fixture
def page():
    """A white 100 x 300 page with two inked words: x 20-60 and x 120-180."""
    img = np.full((100, 300), 255, dtype=np.uint8)
    img[30:60, 20:60] = 0
    img[35:65, 120:180] = 0
    return img


def cut(x: float) -> list[list[float]]:
    return [[x, 20], [x, 70], [x, 70], [x, 20]]


def read(labels, xs, page, confidences=None, line=0):
    confidences = confidences or [1.0] * len(labels)
    return words_in_line(
        labels, [cut(x) for x in xs], confidences, LINE_BOX, page, line
    )


def test_splits_the_line_at_spaces(page):
    words = read(AB_CD, AB_CD_XS, page)
    assert [w.text for w in words] == ["ab", "cd"]


def test_word_box_runs_between_spaces_and_shrinks_to_the_ink(page):
    words = read(AB_CD, AB_CD_XS, page)
    assert [w.box for w in words] == [(20, 30, 40, 30), (120, 35, 60, 30)]


def test_confidence_is_the_mean_of_the_word_characters(page):
    words = read(AB_CD, AB_CD_XS, page, [0.2, 0.4, 1.0, 0.6, 0.8])
    assert words[0].probabilities == {"ab": pytest.approx(0.3)}
    assert words[1].probabilities == {"cd": pytest.approx(0.7)}


def test_extra_spaces_make_no_empty_words(page):
    words = read(list(" ab  cd "), [15, 30, 50, 85, 90, 130, 170, 190], page)
    assert [w.text for w in words] == ["ab", "cd"]


def test_text_is_nfc_for_the_glossary_and_the_frontend(page):
    labels = list(unicodedata.normalize("NFD", "καὶ"))
    words = read(labels, [30 + 8 * i for i in range(len(labels))], page)
    assert words[0].text == "καὶ"
    assert unicodedata.is_normalized("NFC", words[0].text)


def test_words_keep_their_line_number(page):
    words = read(AB_CD, AB_CD_XS, page, line=3)
    assert {w.line for w in words} == {3}


def test_an_empty_line_has_no_words(page):
    assert read([], [], page) == []


def test_ink_box_without_ink_keeps_the_box():
    blank = np.full((50, 50), 255, dtype=np.uint8)
    assert ink_box(blank, 5, 5, 25, 15) == (5, 5, 20, 10)


def test_bounds_are_clipped_to_the_page():
    box = bounds([(-5, 10), (120, 10), (120, 90), (-5, 90)], 100, 80)
    assert box == (0, 10, 100, 80)


def test_missing_model_says_where_it_looked(tmp_path):
    with pytest.raises(FileNotFoundError, match="nope.safetensors"):
        KrakenPredictor(tmp_path / "nope.safetensors")


def test_available_with_the_extra_and_the_model(monkeypatch, tmp_path):
    weights = tmp_path / "weights.safetensors"
    weights.touch()
    monkeypatch.setenv("KRAKEN_MODEL", str(weights))
    monkeypatch.setattr("ml.kraken_predictor.find_spec", lambda _: object())
    assert KrakenPredictor.is_available()


def test_not_available_without_the_model(monkeypatch, tmp_path):
    monkeypatch.setenv("KRAKEN_MODEL", str(tmp_path / "missing.safetensors"))
    monkeypatch.setattr("ml.kraken_predictor.find_spec", lambda _: object())
    assert not KrakenPredictor.is_available()


def test_not_available_without_the_extra(monkeypatch, tmp_path):
    weights = tmp_path / "weights.safetensors"
    weights.touch()
    monkeypatch.setenv("KRAKEN_MODEL", str(weights))
    monkeypatch.setattr("ml.kraken_predictor.find_spec", lambda _: None)
    assert not KrakenPredictor.is_available()


@pytest.mark.parametrize("path", ["", "."])
def test_not_available_when_the_path_is_not_a_file(monkeypatch, path):
    monkeypatch.setenv("KRAKEN_MODEL", path)
    monkeypatch.setattr("ml.kraken_predictor.find_spec", lambda _: object())
    assert not KrakenPredictor.is_available()


def test_base_model_ignores_the_fine_tuned_model_setting(monkeypatch, tmp_path):
    monkeypatch.setenv("KRAKEN_MODEL", str(tmp_path / "other.safetensors"))
    assert KrakenBasePredictor.weights_path() == BASE_MODEL_PATH


def test_base_model_is_available_once_downloaded(monkeypatch, tmp_path):
    weights = tmp_path / "base.safetensors"
    monkeypatch.setattr("ml.kraken_predictor.BASE_MODEL_PATH", weights)
    monkeypatch.setattr("ml.kraken_predictor.find_spec", lambda _: object())
    assert not KrakenBasePredictor.is_available()

    weights.touch()
    assert KrakenBasePredictor.is_available()


needs_model = pytest.mark.skipif(
    find_spec("kraken") is None or not MODEL_PATH.exists(),
    reason="needs the ml extra and the kraken model, see backend/README.md",
)


@needs_model
def test_reads_a_rendered_line(digit_image):
    words = KrakenPredictor().predict(digit_image)
    assert all(isinstance(w, Prediction) for w in words)
    assert all(w.box[2] >= 0 and w.box[3] >= 0 for w in words)


@needs_model
def test_broken_images_raise_value_error(digit_image):
    predictor = KrakenPredictor()
    for broken in (b"not an image", digit_image[: len(digit_image) // 2]):
        with pytest.raises(ValueError, match="decode"):
            predictor.predict(broken)
