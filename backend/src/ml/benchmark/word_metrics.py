import random
import unicodedata
from collections.abc import Sequence
from dataclasses import dataclass, fields
from difflib import SequenceMatcher

from ml.predictor import Box

IOU_THRESHOLD = 0.5
SHARE_THRESHOLD = 0.5


@dataclass(frozen=True)
class Word:
    text: str
    box: Box


@dataclass(frozen=True)
class PageCounts:
    truth_words: int = 0
    predicted_words: int = 0
    matched: int = 0
    matched_iou: float = 0.0
    exact: int = 0
    merged: int = 0
    split: int = 0
    boundary_hits: int = 0
    boundary_predicted: int = 0
    boundary_truth: int = 0
    char_errors: int = 0
    truth_chars: int = 0


def area(box: Box) -> int:
    return box[2] * box[3]


def intersection(a: Box, b: Box) -> int:
    width = min(a[0] + a[2], b[0] + b[2]) - max(a[0], b[0])
    height = min(a[1] + a[3], b[1] + b[3]) - max(a[1], b[1])
    return max(width, 0) * max(height, 0)


def iou(a: Box, b: Box) -> float:
    shared = intersection(a, b)
    union = area(a) + area(b) - shared
    return shared / union if union else 0.0


def match_boxes(
    truth: Sequence[Box], predicted: Sequence[Box], threshold: float = IOU_THRESHOLD
) -> list[tuple[int, int, float]]:
    """Pair each true box with at most one predicted box, best overlap first."""
    candidates = sorted(
        (
            (overlap, t, p)
            for t, true_box in enumerate(truth)
            for p, predicted_box in enumerate(predicted)
            if (overlap := iou(true_box, predicted_box)) >= threshold
        ),
        reverse=True,
    )
    used_truth: set[int] = set()
    used_predicted: set[int] = set()
    pairs = []
    for overlap, t, p in candidates:
        if t not in used_truth and p not in used_predicted:
            used_truth.add(t)
            used_predicted.add(p)
            pairs.append((t, p, overlap))
    return pairs


def merged_words(truth: Sequence[Box], predicted: Sequence[Box]) -> int:
    """True words that share one predicted box with another true word."""
    merged: set[int] = set()
    for box in predicted:
        inside = [
            t
            for t, true_box in enumerate(truth)
            if area(true_box)
            and intersection(box, true_box) / area(true_box) >= SHARE_THRESHOLD
        ]
        if len(inside) > 1:
            merged.update(inside)
    return len(merged)


def split_words(truth: Sequence[Box], predicted: Sequence[Box]) -> int:
    """True words that hold two or more predicted boxes."""
    return sum(
        sum(
            1
            for box in predicted
            if area(box) and intersection(box, true_box) / area(box) >= SHARE_THRESHOLD
        )
        > 1
        for true_box in truth
    )


def fold(text: str) -> str:
    """Letters without accents or case, so a missed accent is not a missed boundary."""
    decomposed = unicodedata.normalize("NFD", text)
    bare = "".join(c for c in decomposed if unicodedata.category(c) != "Mn")
    return bare.lower().replace("ς", "σ")


def boundaries(words: Sequence[str]) -> tuple[str, set[int]]:
    """The letters of the words as one string, and the index of each word's last one."""
    stream = ""
    ends: set[int] = set()
    for word in words:
        folded = fold(word)
        if folded:
            stream += folded
            ends.add(len(stream) - 1)
    return stream, ends


def boundary_counts(
    truth: Sequence[str], predicted: Sequence[str]
) -> tuple[int, int, int]:
    """
    Compare where the words end, on the letters both readings share. A boundary
    after a letter the other reading does not have cannot be judged and is left out.
    Returns (boundaries in both, predicted boundaries, true boundaries).
    """
    truth_stream, truth_ends = boundaries(truth)
    predicted_stream, predicted_ends = boundaries(predicted)
    matcher = SequenceMatcher(a=truth_stream, b=predicted_stream, autojunk=False)
    shared_truth: set[int] = set()
    shared_predicted: set[int] = set()
    for block in matcher.get_matching_blocks():
        for offset in range(block.size):
            if block.a + offset in truth_ends:
                shared_truth.add(block.a + offset)
            if block.b + offset in predicted_ends:
                shared_predicted.add(block.a + offset)
    return (
        len(shared_truth & shared_predicted),
        len(shared_predicted),
        len(shared_truth),
    )


def edit_distance(a: str, b: str) -> int:
    previous = list(range(len(b) + 1))
    for i, char_a in enumerate(a, 1):
        current = [i]
        for j, char_b in enumerate(b, 1):
            current.append(
                min(
                    previous[j] + 1,
                    current[j - 1] + 1,
                    previous[j - 1] + (char_a != char_b),
                )
            )
        previous = current
    return previous[-1]


def score_page(truth: Sequence[Word], predicted: Sequence[Word]) -> PageCounts:
    truth_boxes = [word.box for word in truth]
    predicted_boxes = [word.box for word in predicted]
    pairs = match_boxes(truth_boxes, predicted_boxes)
    truth_text = " ".join(word.text for word in truth)
    predicted_text = " ".join(word.text for word in predicted)
    hits, predicted_ends, truth_ends = boundary_counts(
        [word.text for word in truth], [word.text for word in predicted]
    )
    return PageCounts(
        truth_words=len(truth),
        predicted_words=len(predicted),
        matched=len(pairs),
        matched_iou=sum(overlap for _, _, overlap in pairs),
        exact=sum(truth[t].text == predicted[p].text for t, p, _ in pairs),
        merged=merged_words(truth_boxes, predicted_boxes),
        split=split_words(truth_boxes, predicted_boxes),
        boundary_hits=hits,
        boundary_predicted=predicted_ends,
        boundary_truth=truth_ends,
        char_errors=edit_distance(truth_text, predicted_text),
        truth_chars=len(truth_text),
    )


def ratio(part: float, whole: float) -> float:
    return part / whole if whole else 0.0


def f1(precision: float, recall: float) -> float:
    return ratio(2 * precision * recall, precision + recall)


def summarise(pages: Sequence[PageCounts]) -> dict[str, float]:
    """One number per metric over all the words of the pages."""
    total = {
        field.name: sum(getattr(page, field.name) for page in pages)
        for field in fields(PageCounts)
    }
    box_precision = ratio(total["matched"], total["predicted_words"])
    box_recall = ratio(total["matched"], total["truth_words"])
    boundary_precision = ratio(total["boundary_hits"], total["boundary_predicted"])
    boundary_recall = ratio(total["boundary_hits"], total["boundary_truth"])
    return {
        "box_precision": box_precision,
        "box_recall": box_recall,
        "box_f1": f1(box_precision, box_recall),
        "mean_iou": ratio(total["matched_iou"], total["matched"]),
        "merged_words": ratio(total["merged"], total["truth_words"]),
        "split_words": ratio(total["split"], total["truth_words"]),
        "boundary_precision": boundary_precision,
        "boundary_recall": boundary_recall,
        "boundary_f1": f1(boundary_precision, boundary_recall),
        "exact_words": ratio(total["exact"], total["truth_words"]),
        "cer": ratio(total["char_errors"], total["truth_chars"]),
    }


def intervals(
    pages: Sequence[PageCounts], rounds: int = 1000, seed: int = 0
) -> dict[str, tuple[float, float]]:
    """95% interval of each metric, from drawing the pages again with replacement."""
    rng = random.Random(seed)
    draws = [
        summarise(rng.choices(pages, k=len(pages)))
        for _ in range(rounds if pages else 0)
    ]
    result = {}
    for name in summarise(pages):
        values = sorted(draw[name] for draw in draws) or [0.0]
        low = values[int(0.025 * (len(values) - 1))]
        high = values[int(0.975 * (len(values) - 1))]
        result[name] = (low, high)
    return result
