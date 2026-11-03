import io
import os
import threading
import unicodedata
from collections.abc import Sequence
from pathlib import Path
from typing import Any

import cv2
import numpy as np
from numpy.typing import NDArray

from ml.predictor import AbstractPredictor, Box, Prediction

MODEL_PATH = Path(__file__).resolve().parent / "models" / "kraken_eparchos.safetensors"

Polygon = Sequence[Sequence[float]]


def ink_box(page: NDArray[np.uint8], x0: int, y0: int, x1: int, y1: int) -> Box:
    """Shrink a box to the ink inside it (Otsu threshold) and return (x, y, w, h)."""
    crop = page[y0:y1, x0:x1]
    if crop.size == 0:
        return x0, y0, max(x1 - x0, 0), max(y1 - y0, 0)
    _, ink = cv2.threshold(crop, 0, 255, cv2.THRESH_BINARY_INV + cv2.THRESH_OTSU)
    ys, xs = np.nonzero(ink)
    if len(xs) == 0:
        return x0, y0, x1 - x0, y1 - y0
    left, top = x0 + int(xs.min()), y0 + int(ys.min())
    return left, top, x0 + int(xs.max()) + 1 - left, y0 + int(ys.max()) + 1 - top


def words_in_line(
    labels: Sequence[str],
    cuts: Sequence[Polygon],
    confidences: Sequence[float],
    line_box: tuple[int, int, int, int],
    page: NDArray[np.uint8],
    line: int,
) -> list[Prediction]:
    """
    Split one read line into words at its spaces.

    kraken gives a position for every character. A word runs from the space
    before it to the space after it (the line edges at the ends) and is then
    shrunk to the ink inside: on the Eparchos validation pages that boxes more
    words correctly than the character positions alone. `line_box` is
    (x0, y0, x1, y1).
    """
    centers = [(min(p[0] for p in cut) + max(p[0] for p in cut)) / 2 for cut in cuts]
    x0, y0, x1, y1 = line_box
    words: list[Prediction] = []
    start: int | None = None
    for i, label in enumerate([*labels, " "]):
        if not label.isspace():
            if start is None:
                start = i
            continue
        if start is None:
            continue
        left = centers[start - 1] if start > 0 else x0
        right = centers[i] if i < len(centers) else x1
        text = unicodedata.normalize("NFC", "".join(labels[start:i]))
        confidence = float(np.mean(confidences[start:i]))
        words.append(
            Prediction(
                text=text,
                probabilities={text: confidence},
                box=ink_box(page, int(left), y0, int(right), y1),
                line=line,
            )
        )
        start = None
    return words


def bounds(polygon: Polygon, width: int, height: int) -> tuple[int, int, int, int]:
    """(x0, y0, x1, y1) of a polygon, clipped to the page."""
    xs = [int(p[0]) for p in polygon]
    ys = [int(p[1]) for p in polygon]
    return max(min(xs), 0), max(min(ys), 0), min(max(xs), width), min(max(ys), height)


class KrakenPredictor(AbstractPredictor):
    """
    Reads a page with kraken: its default model finds the text lines, a CTC
    model reads each line, and `words_in_line` places the words.
    """

    def __init__(self, model_path: Path | None = None) -> None:
        super().__init__()
        path = model_path or Path(os.getenv("KRAKEN_MODEL", str(MODEL_PATH)))
        if not path.exists():
            raise FileNotFoundError(f"No kraken model at {path}, see backend/README.md")

        # kraken (and torch) is the optional "ml" extra: uv sync --extra ml
        from kraken.tasks import RecognitionTaskModel, SegmentationTaskModel

        self.model_name = f"kraken-{path.stem}"
        self.segmenter: Any = SegmentationTaskModel.load_model()
        self.recognizer: Any = RecognitionTaskModel.load_model(path)
        # kraken keeps per-line scales on the model, so two pages read at once
        # get each other's character positions
        self.lock = threading.Lock()

    @property
    def model_version(self) -> str:
        return self.model_name

    def predict(self, image: bytes) -> list[Prediction]:
        from kraken.configs import (
            RecognitionInferenceConfig,
            SegmentationInferenceConfig,
        )
        from PIL import Image, ImageOps

        try:
            page = ImageOps.exif_transpose(Image.open(io.BytesIO(image)))
        except OSError as e:  # not an image, or a truncated one
            raise ValueError(
                "Impossible to decode image: invalid or unsupported format."
            ) from e
        gray = np.asarray(page.convert("L"), dtype=np.uint8)
        with self.lock:
            segmentation = self.segmenter.predict(
                page, SegmentationInferenceConfig(accelerator="cpu")
            )
            records = list(
                self.recognizer.predict(
                    page,
                    segmentation,
                    RecognitionInferenceConfig(accelerator="cpu", num_line_workers=0),
                )
            )
        words: list[Prediction] = []
        line = 0
        for record in records:
            line_words = words_in_line(
                record.prediction,
                record.cuts,
                record.confidences,
                bounds(record.boundary, page.width, page.height),
                gray,
                line,
            )
            if line_words:
                words += line_words
                line += 1
        return words
