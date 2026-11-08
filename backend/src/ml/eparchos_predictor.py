import copy
import xml.etree.ElementTree as ET
from pathlib import Path

from ml.predictor import AbstractPredictor, Box, Prediction

XML_PATH = Path(__file__).resolve().parent / "data" / "eparchos_6791.xml"


def polygon_to_box(points: str) -> Box:
    """Turn PAGE coordinates "x1,y1 x2,y2 ..." into an (x, y, w, h) box."""
    xs, ys = [], []
    for point in points.split():
        x, y = point.split(",")
        xs.append(int(x))
        ys.append(int(y))
    return min(xs), min(ys), max(xs) - min(xs), max(ys) - min(ys)


def parse_page_xml(xml: bytes) -> list[Prediction]:
    """
    Return every word of a PAGE XML document, in reading order.

    Lines are kept in document order (top to bottom) and numbered from 0, but
    words inside a line are not stored left to right in Eparchos, so they are
    sorted by x.
    Words without text or coordinates are skipped. "{*}" matches any
    namespace, so every PAGE schema version works.
    """
    root = ET.fromstring(xml)
    words: list[Prediction] = []
    for line_number, line in enumerate(root.findall(".//{*}TextLine")):
        line_words = []
        for word in line.findall("{*}Word"):
            text = (word.findtext("{*}TextEquiv/{*}Unicode") or "").strip()
            coords = word.find("{*}Coords")
            if not text or coords is None or not coords.get("points"):
                continue
            box = polygon_to_box(coords.get("points", ""))
            line_words.append(
                Prediction(
                    text=text, probabilities={text: 1.0}, box=box, line=line_number
                )
            )
        line_words.sort(key=lambda w: w.box[0])
        words.extend(line_words)
    return words


class EparchosPredictor(AbstractPredictor):
    """
    Placeholder for handwritten text recognition: ignores the image and always
    returns the ground-truth words of one Eparchos page. Boxes are in that
    page's coordinates (788 x 805 px for 6791).
    """

    display_name = "Eparchos page 6791 (mock)"
    description = (
        "Ignores the page and returns the words of Eparchos page 6791, "
        "with translations. For demos."
    )

    def __init__(self, xml_path: Path = XML_PATH) -> None:
        super().__init__()
        self.model_name = f"eparchos-{xml_path.stem.removeprefix('eparchos_')}"
        self.words = parse_page_xml(xml_path.read_bytes())

    @property
    def model_version(self) -> str:
        return self.model_name

    def predict(self, image: bytes) -> list[Prediction]:
        return copy.deepcopy(self.words)
