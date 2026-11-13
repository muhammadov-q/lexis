import io
import os
import threading
import unicodedata
from collections.abc import Sequence
from importlib.util import find_spec
from pathlib import Path
from typing import TYPE_CHECKING, Any

import cv2
import numpy as np
from numpy.typing import NDArray

from ml.predictor import AbstractPredictor, Box, Prediction

if TYPE_CHECKING:
    from PIL.Image import Image

MODEL_PATH = Path(__file__).resolve().parent / "models" / "kraken_eparchos.safetensors"
BASE_MODEL_PATH = MODEL_PATH.with_name("ppocrv6_small.safetensors")

Polygon = Sequence[Sequence[float]]


def ink_box(page: NDArray[np.uint8], x0: int, y0: int, x1: int, y1: int) -> Box:
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
    A word's box runs from the space before it to the space after it, shrunk to
    the ink: on the Eparchos validation pages that boxes more words correctly
    than the character positions alone. `line_box` is (x0, y0, x1, y1).
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
    xs = [int(p[0]) for p in polygon]
    ys = [int(p[1]) for p in polygon]
    return max(min(xs), 0), max(min(ys), 0), min(max(xs), width), min(max(ys), height)


def open_page(image: bytes) -> "Image":
    from PIL import Image, ImageOps

    try:
        return ImageOps.exif_transpose(Image.open(io.BytesIO(image)))
    except OSError as e:  # not an image, or a truncated one
        raise ValueError(
            "Impossible to decode image: invalid or unsupported format."
        ) from e


def straight_baseline(polygon: Polygon) -> list[tuple[int, int]]:
    """Eparchos has no baselines: training used one at 75% of the line height."""
    xs = [p[0] for p in polygon]
    ys = [p[1] for p in polygon]
    y = int(min(ys) + 0.75 * (max(ys) - min(ys)))
    return [(int(min(xs)), y), (int(max(xs)), y)]


def rectangle(polygon: Polygon, width: int, height: int) -> list[tuple[int, int]]:
    x0, y0, x1, y1 = bounds(polygon, width, height)
    return [(x0, y0), (x1, y0), (x1, y1), (x0, y1)]


def straight_lines(lines: Sequence[Polygon]) -> Any:
    """
    The model was trained on Eparchos lines: rectangles with a straight baseline.
    Read in the curved shape kraken finds, it gets twice as many letters wrong.
    """
    from kraken.containers import BaselineLine, Segmentation

    return Segmentation(
        type="baselines",
        imagename="page",
        text_direction="horizontal-lr",
        script_detection=False,
        lines=[
            BaselineLine(
                id=str(number),
                baseline=straight_baseline(polygon),
                boundary=[(int(x), int(y)) for x, y in polygon],
            )
            for number, polygon in enumerate(lines)
        ],
    )


class KrakenPredictor(AbstractPredictor):
    display_name = "Kraken, fine-tuned on Eparchos"
    description = (
        "Reads 16th-century Greek handwriting. A page takes several seconds, "
        "and the first one longer while the model loads."
    )

    @classmethod
    def weights_path(cls) -> Path:
        return Path(os.getenv("KRAKEN_MODEL", str(MODEL_PATH)))

    @classmethod
    def is_available(cls) -> bool:
        return find_spec("kraken") is not None and cls.weights_path().is_file()

    def __init__(self, model_path: Path | None = None) -> None:
        super().__init__()
        path = model_path or self.weights_path()
        if not path.exists():
            raise FileNotFoundError(f"No kraken model at {path}, see backend/README.md")

        # kraken (and torch) is the optional "ml" extra: uv sync --extra ml
        from kraken.tasks import RecognitionTaskModel, SegmentationTaskModel

        self.model_name = f"kraken-{path.stem}"
        self.segmenter: Any = SegmentationTaskModel.load_model()
        self.recognizer: Any = RecognitionTaskModel.load_model(path)
        # kraken keeps per-line scales on the model, so concurrent reads mix them up
        self.lock = threading.Lock()

    @property
    def model_version(self) -> str:
        return self.model_name

    def predict(self, image: bytes) -> list[Prediction]:
        from kraken.configs import SegmentationInferenceConfig

        page = open_page(image)
        with self.lock:
            found = self.segmenter.predict(
                page, SegmentationInferenceConfig(accelerator="cpu")
            )
            lines = [
                rectangle(line.boundary, page.width, page.height)
                for line in found.lines
            ]
            return self._read(page, straight_lines(lines))

    def predict_lines(self, image: bytes, lines: Sequence[Polygon]) -> list[Prediction]:
        """Read the given lines instead of finding them, to judge the reading alone."""
        page = open_page(image)
        with self.lock:
            return self._read(page, straight_lines(lines))

    def _read(self, page: "Image", segmentation: Any) -> list[Prediction]:
        from kraken.configs import RecognitionInferenceConfig

        gray = np.asarray(page.convert("L"), dtype=np.uint8)
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


class KrakenBasePredictor(KrakenPredictor):
    display_name = "Kraken, not fine-tuned"
    description = (
        "The general PP-OCRv6 model, before we fine-tuned it. Use it to compare: "
        "on Eparchos pages it gets about one letter in three wrong."
    )

    @classmethod
    def weights_path(cls) -> Path:
        return BASE_MODEL_PATH
