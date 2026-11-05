from collections.abc import Sequence
from pathlib import Path
from typing import Any, Protocol

import cv2
import joblib
import numpy as np
from cv2.typing import MatLike
from numpy.typing import NDArray

from ml.predictor import AbstractPredictor, Box, Prediction

MODEL_PATH = Path(__file__).resolve().parent / "models" / "knn_mnist.joblib"


class DigitClassifier(Protocol):
    @property
    def classes_(self) -> NDArray[Any]: ...

    def predict_proba(self, x: NDArray[np.float32], /) -> NDArray[np.float64]: ...


class KnnPredictor(AbstractPredictor):
    display_name = "Handwritten digits (k-NN)"
    description = "Finds and reads handwritten digits. Not made for manuscripts."

    @classmethod
    def is_available(cls) -> bool:
        return MODEL_PATH.exists()

    def __init__(self) -> None:
        super().__init__()
        self.model_name = "KNN-100126"
        self.model: DigitClassifier = joblib.load(MODEL_PATH)

    def load_image_in_grayscale(
        self, image: bytes, target_width: int = 1000
    ) -> tuple[MatLike, float]:
        """
        Decode an image in raw bytes and return the grayscale version.
        It also scale to a fixed size.
        """
        buffer = np.frombuffer(image, dtype=np.uint8)
        img = cv2.imdecode(buffer, cv2.IMREAD_GRAYSCALE)
        if img is None:
            raise ValueError(
                "Impossible to decode image: invalid or unsupported format."
            )

        scale = target_width / img.shape[1]
        interp = cv2.INTER_AREA if scale < 1 else cv2.INTER_CUBIC
        return cv2.resize(img, None, fx=scale, fy=scale, interpolation=interp), scale

    def add_blur_on_image(self, img: MatLike) -> MatLike:
        return cv2.GaussianBlur(img, (9, 9), 0)

    def binarize(self, img: MatLike) -> MatLike:
        _, binary = cv2.threshold(img, 0, 255, cv2.THRESH_BINARY_INV + cv2.THRESH_OTSU)
        return binary

    def dilate(self, img: MatLike) -> MatLike:
        kernel = cv2.getStructuringElement(cv2.MORPH_RECT, (5, 5))
        return cv2.dilate(img, kernel, iterations=2)

    def find_contours(self, img: MatLike) -> Sequence[MatLike]:
        contours, _ = cv2.findContours(img, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)

        return contours

    def find_bounding_boxes(
        self, contours: Sequence[MatLike], min_area: float = 200
    ) -> list[Box]:
        boxes = []
        for c in contours:
            if cv2.contourArea(c) <= min_area:
                continue  # too small: noise
            x, y, w, h = cv2.boundingRect(c)
            if w >= 2 * h:
                continue  # at least twice as wide as tall: not a digit
            boxes.append((x, y, w, h))

        boxes.sort(key=lambda b: (b[1] // 50, b[0]))

        return boxes

    def to_mnist(self, d: MatLike, stroke: float = 0.0) -> MatLike:
        """
        d:      crop with white ink on black background
        stroke: thickening relative to digit size (0 = none, try 0.03 to 0.08)
        """
        # 1. Tight crop around the ink, so padding no longer affects the scale
        ys, xs = np.nonzero(d)
        if len(xs) == 0:
            return np.zeros((28, 28), dtype=np.uint8)
        d = d[ys.min() : ys.max() + 1, xs.min() : xs.max() + 1]
        h, w = d.shape

        # 2. Optional thickening, proportional to the digit's size
        k = round(max(h, w) * stroke)
        if k > 0:
            d = cv2.dilate(d, cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (k, k)))
            d = np.pad(d, k)  # dilation can grow past the crop
            ys, xs = np.nonzero(d)
            d = d[ys.min() : ys.max() + 1, xs.min() : xs.max() + 1]
            h, w = d.shape

        # 3. Fit in 20x20, keeping aspect ratio
        scale = 20.0 / max(h, w)
        resized = cv2.resize(
            d,
            (max(1, round(w * scale)), max(1, round(h * scale))),
            interpolation=cv2.INTER_AREA,
        )

        # 4. Paste in the middle of a 28x28 canvas
        canvas: MatLike = np.zeros((28, 28), dtype=np.uint8)
        rh, rw = resized.shape
        y_off, x_off = (28 - rh) // 2, (28 - rw) // 2
        canvas[y_off : y_off + rh, x_off : x_off + rw] = resized

        # 5. Shift so the center of mass sits at the center, like MNIST
        m = cv2.moments(canvas)
        if m["m00"] > 0:
            cx, cy = m["m10"] / m["m00"], m["m01"] / m["m00"]
            M = np.array([[1, 0, 14 - cx], [0, 1, 14 - cy]], dtype=np.float32)
            canvas = cv2.warpAffine(canvas, M, (28, 28))
        return canvas

    def extract_digits(
        self, img: MatLike, boxes: Sequence[Box], pad: int = 3
    ) -> list[tuple[MatLike, Box]]:
        H, W = img.shape
        samples = []
        for x, y, w, h in boxes:
            x0, y0 = max(x - pad, 0), max(y - pad, 0)
            x1, y1 = min(x + w + pad, W), min(y + h + pad, H)
            samples.append((self.to_mnist(img[y0:y1, x0:x1]), (x, y, w, h)))

        return samples

    def predict_digit(
        self, digit: MatLike, model: DigitClassifier
    ) -> tuple[int, dict[int, float]]:
        """
        digit: 28x28 uint8 image, white digit on black background
        Returns (prediction, {digit: probability})
        """
        x = digit.reshape(1, -1).astype(np.float32)
        probs = model.predict_proba(x)[0]
        pred = int(model.classes_[np.argmax(probs)])
        probabilities = {
            int(c): float(p) for c, p in zip(model.classes_, probs, strict=False)
        }
        return pred, probabilities

    @property
    def model_version(self) -> str:
        return self.model_name

    def to_original_coordinates(self, box: Box, scale: float) -> Box:
        """Map a box (x, y, w, h) from the resized image back to the uploaded image."""
        x, y, w, h = box
        return (
            round(x / scale),
            round(y / scale),
            round(w / scale),
            round(h / scale),
        )

    def predict(self, image: bytes) -> list[Prediction]:
        grayscale, scale = self.load_image_in_grayscale(image)
        blurred = self.add_blur_on_image(grayscale)
        binary = self.binarize(blurred)
        dilated = self.dilate(binary)
        contours = self.find_contours(dilated)
        boxes = self.find_bounding_boxes(contours)
        samples = self.extract_digits(binary, boxes)

        results = []
        for digit, box in samples:
            pred, probs = self.predict_digit(digit, self.model)
            results.append(
                Prediction(
                    text=str(pred),
                    probabilities={str(d): p for d, p in probs.items()},
                    box=self.to_original_coordinates(box, scale),
                )
            )
        return results
