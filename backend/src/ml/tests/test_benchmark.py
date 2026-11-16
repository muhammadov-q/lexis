import json
import unicodedata

import pytest

from ml.benchmark import run as benchmark
from ml.benchmark.page_xml import load_page
from ml.predictor import AbstractPredictor, Prediction

PAGE_XML = """<?xml version="1.0" encoding="UTF-8"?>
<PcGts xmlns="http://schema.primaresearch.org/PAGE/gts/pagecontent/2013-07-15">
  <Page imageFilename="page.png" imageWidth="100" imageHeight="60">
    <TextRegion id="r0">
      <TextLine id="l0">
        <Coords points="10,10 90,10 90,30 10,30"/>
        <Word id="w1">
          <Coords points="50,10 90,10 90,30 50,30"/>
          <TextEquiv><Unicode>τὴν</Unicode></TextEquiv>
        </Word>
        <Word id="w0">
          <Coords points="10,12 40,12 40,28 10,28"/>
          <TextEquiv><Unicode>{first}</Unicode></TextEquiv>
        </Word>
      </TextLine>
      <TextLine id="l1">
        <Coords points="10,32 90,32 90,38 10,38"/>
      </TextLine>
      <TextLine id="l2">
        <Coords points="10,40 90,40 90,58 10,58"/>
        <Word id="w2">
          <Coords points="12.6,41 30,41 30,57 12.6,57"/>
          <TextEquiv><Unicode>δὲ</Unicode></TextEquiv>
        </Word>
        <Word id="w3">
          <Coords points="40,41 60,41 60,57 40,57"/>
          <TextEquiv><Unicode> </Unicode></TextEquiv>
        </Word>
      </TextLine>
    </TextRegion>
  </Page>
</PcGts>
"""


class Reader(AbstractPredictor):
    def __init__(self, words: list[Prediction]) -> None:
        super().__init__()
        self.words = words

    @property
    def model_version(self) -> str:
        return "reader-1"

    def predict(self, image: bytes) -> list[Prediction]:
        return self.words


@pytest.fixture
def data(tmp_path):
    (tmp_path / "xml").mkdir()
    (tmp_path / "image").mkdir()
    first = unicodedata.normalize("NFD", "καὶ")
    xml = PAGE_XML.format(first=first)
    (tmp_path / "xml" / "page 1.xml").write_text(xml, encoding="utf-8")
    (tmp_path / "image" / "page 1.png").write_bytes(b"png")
    return tmp_path


@pytest.fixture
def truth_words(data):
    page = load_page(data / "xml" / "page 1.xml")
    return [Prediction(w.text, {w.text: 1.0}, w.box, w.line) for w in page.words]


def test_words_come_with_their_box_and_line(data):
    page = load_page(data / "xml" / "page 1.xml")
    assert [(w.text, w.box, w.line) for w in page.words] == [
        ("καὶ", (10, 12, 30, 16), 0),
        ("τὴν", (50, 10, 40, 20), 0),
        ("δὲ", (12, 41, 18, 16), 1),
    ]


def test_words_of_a_line_are_put_in_reading_order(data):
    page = load_page(data / "xml" / "page 1.xml")
    assert [w.text for w in page.lines[0].words] == ["καὶ", "τὴν"]


def test_text_is_nfc_like_the_predictions(data):
    page = load_page(data / "xml" / "page 1.xml")
    assert unicodedata.is_normalized("NFC", page.words[0].text)


def test_lines_without_words_and_words_without_text_are_left_out(data):
    page = load_page(data / "xml" / "page 1.xml")
    assert [len(line.words) for line in page.lines] == [2, 1]
    assert page.lines[1].polygon == [(10, 40), (90, 40), (90, 58), (10, 58)]


def test_reading_the_truth_scores_full_marks(data, truth_words):
    pages = benchmark.run(Reader(truth_words), data, ["page 1"])
    page = pages["page 1"]
    assert page.truth_words == page.matched == page.exact == 3
    assert page.char_errors == 0


def test_reading_nothing_finds_no_words(data):
    page = benchmark.run(Reader([]), data, ["page 1"])["page 1"]
    assert (page.truth_words, page.predicted_words, page.matched) == (3, 0, 0)


def test_only_kraken_can_read_the_true_lines(data, truth_words):
    with pytest.raises(ValueError, match="true lines"):
        benchmark.run(Reader(truth_words), data, ["page 1"], true_lines=True)


def test_the_table_names_every_metric(data, truth_words):
    pages = benchmark.run(Reader(truth_words), data, ["page 1"])
    table = benchmark.table(list(pages.values()))
    for label in benchmark.LABELS.values():
        assert label in table
    assert "| Word boxes, F1 | 100.0% | 100.0% to 100.0% |" in table


def test_validation_and_test_pages_do_not_overlap():
    validation = set(benchmark.page_names("val"))
    test = set(benchmark.page_names("test"))
    train = set(benchmark.page_names("train"))
    assert (len(validation), len(test)) == (12, 24)
    assert not validation & test
    assert not train & (validation | test)


def test_the_command_prints_and_saves_the_results(
    data, truth_words, monkeypatch, capsys, tmp_path
):
    monkeypatch.setattr(benchmark, "create_predictor", lambda _: Reader(truth_words))
    monkeypatch.setattr(benchmark, "page_names", lambda _: ["page 1"])
    out = tmp_path / "results.json"

    benchmark.main(["--data", str(data), "--model", "dummy", "--out", str(out)])

    printed = capsys.readouterr().out
    assert "reader-1, val, 1 pages, lines found by the model" in printed
    result = json.loads(out.read_text(encoding="utf-8"))
    assert result["model_version"] == "reader-1"
    assert result["summary"]["box_f1"] == 1.0
    assert result["pages"]["page 1"]["truth_words"] == 3


def test_training_pages_cannot_be_scored(data):
    with pytest.raises(SystemExit):
        benchmark.main(["--data", str(data), "--split", "train"])
