import argparse
import json
import sys
from collections.abc import Sequence
from dataclasses import asdict
from pathlib import Path

from ml.benchmark.page_xml import TruthPage, load_page
from ml.benchmark.word_metrics import (
    PageCounts,
    Word,
    intervals,
    score_page,
    summarise,
)
from ml.factory import PREDICTORS, create_predictor
from ml.kraken_predictor import KrakenPredictor
from ml.predictor import AbstractPredictor, Prediction

SPLIT_PATH = Path(__file__).resolve().parents[1] / "data" / "eparchos_split.json"
SPLITS = ("val", "test")
LABELS = {
    "box_f1": "Word boxes, F1",
    "box_precision": "Word boxes, precision",
    "box_recall": "Word boxes, recall",
    "mean_iou": "Overlap of matched boxes (IoU)",
    "merged_words": "Words merged with a neighbour",
    "split_words": "Words split in two or more",
    "boundary_f1": "Word boundaries in the text, F1",
    "boundary_precision": "Word boundaries in the text, precision",
    "boundary_recall": "Word boundaries in the text, recall",
    "exact_words": "Right word in the right box",
    "cer": "Character error rate",
}


def page_names(split: str) -> list[str]:
    names: list[str] = json.loads(SPLIT_PATH.read_text(encoding="utf-8"))[split]
    return names


def read_page(
    predictor: AbstractPredictor, image: bytes, truth: TruthPage, true_lines: bool
) -> list[Prediction]:
    if not true_lines:
        return predictor.predict(image)
    if not isinstance(predictor, KrakenPredictor):
        raise ValueError("Only the kraken models can read the true lines.")
    return predictor.predict_lines(image, [line.polygon for line in truth.lines])


def run(
    predictor: AbstractPredictor,
    data: Path,
    names: Sequence[str],
    true_lines: bool = False,
) -> dict[str, PageCounts]:
    """Score every named page of an Eparchos folder (image/ and xml/)."""
    pages = {}
    for number, name in enumerate(names, 1):
        truth = load_page(data / "xml" / f"{name}.xml")
        image = (data / "image" / f"{name}.png").read_bytes()
        read = read_page(predictor, image, truth, true_lines)
        pages[name] = score_page(
            [Word(word.text, word.box) for word in truth.words],
            [Word(word.text, word.box) for word in read],
        )
        print(f"{number}/{len(names)} {name}", file=sys.stderr)
    return pages


def table(pages: Sequence[PageCounts]) -> str:
    summary = summarise(pages)
    ranges = intervals(pages)
    rows = ["| Metric | Value | 95% interval |", "| --- | --- | --- |"]
    for name, label in LABELS.items():
        low, high = ranges[name]
        rows.append(f"| {label} | {summary[name]:.1%} | {low:.1%} to {high:.1%} |")
    return "\n".join(rows)


def main(argv: Sequence[str] | None = None) -> None:
    parser = argparse.ArgumentParser(
        prog="python -m ml.benchmark",
        description="Measure how well a model finds and reads the words of Eparchos.",
    )
    parser.add_argument("--data", type=Path, required=True, help="Eparchos folder")
    parser.add_argument("--model", choices=sorted(PREDICTORS), default="kraken")
    parser.add_argument("--split", choices=SPLITS, default="val")
    parser.add_argument(
        "--true-lines",
        action="store_true",
        help="give the model the true lines instead of letting it find them",
    )
    parser.add_argument("--out", type=Path, help="write the results as JSON")
    args = parser.parse_args(argv)

    predictor = create_predictor(args.model)
    pages = run(predictor, args.data, page_names(args.split), args.true_lines)
    counts = list(pages.values())
    lines = "true lines" if args.true_lines else "lines found by the model"
    print(f"{predictor.model_version}, {args.split}, {len(counts)} pages, {lines}\n")
    print(table(counts))
    if args.out:
        result = {
            "model": args.model,
            "model_version": predictor.model_version,
            "split": args.split,
            "true_lines": args.true_lines,
            "summary": summarise(counts),
            "intervals": intervals(counts),
            "pages": {name: asdict(page) for name, page in pages.items()},
        }
        args.out.write_text(json.dumps(result, indent=2), encoding="utf-8")
