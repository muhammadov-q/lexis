import unicodedata
import xml.etree.ElementTree as ET
from dataclasses import dataclass
from pathlib import Path

from ml.predictor import Box

Point = tuple[int, int]


@dataclass(frozen=True)
class TruthWord:
    text: str
    box: Box
    line: int


@dataclass(frozen=True)
class TruthLine:
    polygon: list[Point]
    words: list[TruthWord]


@dataclass(frozen=True)
class TruthPage:
    lines: list[TruthLine]

    @property
    def words(self) -> list[TruthWord]:
        return [word for line in self.lines for word in line.words]


def points(element: ET.Element, ns: dict[str, str]) -> list[Point]:
    coords = element.find("p:Coords", ns)
    if coords is None:
        return []
    pairs = (pair.split(",") for pair in coords.get("points", "").split())
    return [(int(float(x)), int(float(y))) for x, y in pairs]


def bounding_box(polygon: list[Point]) -> Box:
    xs = [x for x, _ in polygon]
    ys = [y for _, y in polygon]
    return min(xs), min(ys), max(xs) - min(xs), max(ys) - min(ys)


def load_page(path: Path) -> TruthPage:
    """Read the lines and words of a PAGE XML file. Lines without words are left out."""
    root = ET.parse(path).getroot()
    ns = {"p": root.tag.split("}")[0].strip("{")}
    lines: list[TruthLine] = []
    for line in root.iterfind(".//p:TextLine", ns):
        words = []
        for word in line.iterfind("p:Word", ns):
            polygon = points(word, ns)
            text = word.findtext("p:TextEquiv/p:Unicode", "", ns).strip()
            if polygon and text:
                words.append(
                    TruthWord(
                        text=unicodedata.normalize("NFC", text),
                        box=bounding_box(polygon),
                        line=len(lines),
                    )
                )
        if words:
            # the file does not always list a line's words in reading order
            words.sort(key=lambda word: word.box[0])
            lines.append(TruthLine(polygon=points(line, ns), words=words))
    return TruthPage(lines=lines)
