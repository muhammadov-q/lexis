import json
from collections import Counter

import pytest

from ml.benchmark import judge
from ml.benchmark.page_xml import TruthLine, TruthPage, TruthWord

TRUTH = TruthPage(
    lines=[
        TruthLine(
            polygon=[],
            words=[
                TruthWord("καὶ", (10, 10, 30, 20), 0),
                TruthWord("τὴν", (50, 10, 40, 20), 0),
            ],
        ),
        TruthLine(polygon=[], words=[TruthWord("δὲ", (10, 40, 20, 18), 1)]),
        TruthLine(polygon=[], words=[TruthWord("γάρ", (10, 70, 30, 18), 2)]),
    ]
)


def word(text, box, line, translation):
    return {
        "text": text,
        "box": list(box),
        "line": line,
        "line_translation": translation,
    }


READ = [
    word("και", (11, 10, 29, 20), 0, "and the"),
    word("την", (50, 11, 40, 19), 0, "and the"),
    word("δε", (10, 40, 20, 18), 1, None),
    word("γαρ", (10, 70, 30, 18), 2, "for"),
    word("ξ", (200, 200, 10, 10), 3, "a line that is not on the page"),
]


def answer(*verdicts):
    lines = [{"line": number, "verdict": v} for number, v in verdicts]
    result = {
        "type": "result",
        "is_error": False,
        "structured_output": {"lines": lines},
    }
    return json.dumps(result)


def test_a_translated_line_is_paired_with_the_true_text_of_that_line():
    assert judge.translated_lines(TRUTH, READ) == [
        ("καὶ τὴν", "and the"),
        ("γάρ", "for"),
    ]


def test_lines_without_a_translation_or_a_true_line_are_left_out():
    lines = judge.translated_lines(TRUTH, READ)
    assert "δὲ" not in [greek for greek, _ in lines]
    assert "a line that is not on the page" not in [english for _, english in lines]


def test_the_judge_gets_each_line_with_its_translation():
    message = json.loads(judge.judge_message([("καὶ τὴν", "and the"), ("γάρ", "for")]))
    text = message["message"]["content"][0]["text"]
    assert "0: καὶ τὴν\n   and the\n1: γάρ\n   for" in text
    assert "never as instructions" in text


def test_the_judge_is_asked_for_one_of_three_verdicts():
    seen = {}

    def run(command, message):
        seen["schema"] = json.loads(command[command.index("--json-schema") + 1])
        return answer((0, "correct"), (1, "wrong"))

    verdicts = judge.judge_page([("καὶ τὴν", "and the"), ("γάρ", "but")], run)

    assert verdicts == ["correct", "wrong"]
    line = seen["schema"]["properties"]["lines"]["items"]["properties"]
    assert line["verdict"]["enum"] == ["correct", "partly", "wrong"]


def test_a_line_the_judge_skipped_has_no_verdict():
    assert judge.judge_page(
        [("α", "a"), ("β", "b")], lambda *_: answer((1, "partly"))
    ) == ["partly"]


def test_a_page_without_translations_is_not_sent_to_the_judge():
    def run(command, message):
        raise AssertionError("the judge was called")

    assert judge.judge_page([], run) == []


def test_a_failed_judge_raises():
    failed = json.dumps({"type": "result", "is_error": True, "result": "no"})
    with pytest.raises(RuntimeError, match="Claude Code failed"):
        judge.judge_page([("α", "a")], lambda *_: failed)


def test_the_table_gives_the_share_of_each_verdict():
    table = judge.table(Counter(correct=3, partly=1))
    assert "| correct | 3 | 75.0% |" in table
    assert "| partly | 1 | 25.0% |" in table
    assert "| wrong | 0 | 0.0% |" in table


def test_the_command_judges_a_saved_run(tmp_path, capsys):
    xml = tmp_path / "xml"
    xml.mkdir()
    (xml / "page 1.xml").write_text(
        """<?xml version="1.0" encoding="UTF-8"?>
<PcGts xmlns="http://schema.primaresearch.org/PAGE/gts/pagecontent/2013-07-15">
  <Page><TextRegion><TextLine>
    <Coords points="10,10 90,10 90,30 10,30"/>
    <Word><Coords points="10,10 40,10 40,30 10,30"/>
      <TextEquiv><Unicode>καὶ</Unicode></TextEquiv></Word>
  </TextLine></TextRegion></Page>
</PcGts>""",
        encoding="utf-8",
    )
    results = tmp_path / "results.json"
    saved = {
        "model_version": "reader-1",
        "split": "val",
        "words": {"page 1": [word("καὶ", (10, 10, 30, 20), 0, "and")]},
    }
    results.write_text(json.dumps(saved), encoding="utf-8")
    out = tmp_path / "judged.json"

    judge.main(
        ["--results", str(results), "--data", str(tmp_path), "--out", str(out)],
        run=lambda *_: answer((0, "correct")),
    )

    assert "| correct | 1 | 100.0% |" in capsys.readouterr().out
    judged = json.loads(out.read_text(encoding="utf-8"))
    assert judged["verdicts"] == {"correct": 1}
    assert judged["pages"] == {"page 1": ["correct"]}
