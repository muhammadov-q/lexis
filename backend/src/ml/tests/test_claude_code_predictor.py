import io
import json
import os
import subprocess

import numpy as np
import pytest
from PIL import Image

from ml.claude_code_predictor import (
    EFFORT,
    MAX_SIDE,
    MODEL,
    TIMEOUT_S,
    UNCONFIRMED,
    ClaudeCodePredictor,
    run_claude,
)
from ml.kraken_predictor import KrakenPredictor
from ml.predictor import AbstractPredictor, Prediction
from ml.translator import Gloss

INK = ((20, 30, 60, 60), (120, 35, 180, 65))


def page(width=300, height=100, ink=INK, image_format="PNG"):
    pixels = np.full((height, width), 255, dtype=np.uint8)
    for x0, y0, x1, y1 in ink:
        pixels[y0:y1, x0:x1] = 0
    out = io.BytesIO()
    Image.fromarray(pixels).save(out, format=image_format)
    return out.getvalue()


def found(text, box, line=0, confidence=0.8):
    return Prediction(text=text, probabilities={text: confidence}, box=box, line=line)


def read(text, lemma="λῆμμα", translation="word", definition="noun: a word"):
    return {
        "text": text,
        "lemma": lemma,
        "translation": translation,
        "definition": definition,
    }


def answer(*lines):
    return json.dumps(
        {
            "type": "result",
            "subtype": "success",
            "is_error": False,
            "structured_output": {
                "lines": [{"line": i, "words": words} for i, words in lines]
            },
        }
    )


class FoundLines(AbstractPredictor):
    def __init__(self, words):
        self.words = words

    @property
    def model_version(self):
        return "kraken-test"

    def predict(self, image):
        return self.words


class Claude:
    def __init__(self, output):
        self.output = output
        self.calls = []

    def __call__(self, command, folder):
        saved = Image.open(folder / "page.png")
        self.calls.append(
            {
                "command": command,
                "files": sorted(p.name for p in folder.iterdir()),
                "format": saved.format,
                "size": saved.size,
            }
        )
        return self.output


TWO_WORDS = [found("αβ", (20, 30, 40, 30)), found("γδ", (120, 35, 60, 30))]


def predictor(words, output):
    return ClaudeCodePredictor(lines=FoundLines(words), run=Claude(output))


def test_is_an_abstract_predictor():
    assert isinstance(predictor([], answer()), AbstractPredictor)


def test_model_version_names_both_models():
    assert predictor([], answer()).model_version == f"{MODEL}+kraken-test"


def test_claude_reads_the_words_and_kraken_keeps_their_boxes():
    words = predictor(TWO_WORDS, answer((0, [read("ἀβ"), read("γδ")]))).predict(page())

    assert [w.text for w in words] == ["ἀβ", "γδ"]
    assert [w.box for w in words] == [(20, 30, 40, 30), (120, 35, 60, 30)]
    assert [w.line for w in words] == [0, 0]


def test_each_word_gets_claudes_translation():
    words = predictor(TWO_WORDS, answer((0, [read("ἀβ"), read("γδ")]))).predict(page())

    assert words[0].gloss == Gloss(
        lemma="λῆμμα", translation="word", definition="noun: a word"
    )


def test_a_word_keeps_krakens_confidence_only_when_both_read_it_the_same():
    words = predictor(TWO_WORDS, answer((0, [read("ἀβ"), read("γδ")]))).predict(page())

    assert words[0].probabilities == {"ἀβ": UNCONFIRMED}
    assert words[1].probabilities == {"γδ": 0.8}


def test_words_are_nfc():
    decomposed = "ἀ"
    words = predictor(TWO_WORDS, answer((0, [read(decomposed), read("γδ")]))).predict(
        page()
    )

    assert words[0].text == "ἀ"


def test_another_word_count_splits_the_line_and_shrinks_each_word_to_its_ink():
    one_word = [found("αβγδ", (20, 30, 160, 35))]

    words = predictor(one_word, answer((0, [read("αβ"), read("γδ")]))).predict(page())

    assert [w.box for w in words] == [(20, 30, 40, 30), (120, 35, 60, 30)]
    assert all(w.probabilities[w.text] == UNCONFIRMED for w in words)


def test_words_both_models_read_the_same_hold_the_rest_of_the_line_in_place():
    ink = ((10, 30, 40, 60), (100, 30, 130, 60), (150, 30, 180, 60), (240, 30, 270, 60))
    kraken = [
        found("αα", (10, 30, 30, 30)),
        found("ββγγ", (100, 30, 80, 30)),
        found("δδ", (240, 30, 30, 30)),
    ]
    claude = answer((0, [read("αα"), read("ββ"), read("γγ"), read("δδ")]))

    words = predictor(kraken, claude).predict(page(ink=ink))

    assert [w.box for w in words] == [
        (10, 30, 30, 30),
        (100, 30, 30, 30),
        (150, 30, 30, 30),
        (240, 30, 30, 30),
    ]
    assert [w.probabilities[w.text] for w in words] == [
        0.8,
        UNCONFIRMED,
        UNCONFIRMED,
        0.8,
    ]


def test_a_line_claude_leaves_out_keeps_what_kraken_read():
    words = [*TWO_WORDS, found("εζ", (20, 70, 30, 20), line=1)]

    result = predictor(words, answer((0, [read("ἀβ"), read("γδ")]))).predict(page())

    assert result[-1] == words[-1]


def test_an_empty_line_is_dropped_and_the_next_one_renumbered():
    words = [*TWO_WORDS, found("εζ", (20, 70, 30, 20), line=1)]

    result = predictor(words, answer((0, []), (1, [read("εζ")]))).predict(page())

    assert [(w.text, w.line) for w in result] == [("εζ", 0)]


def test_no_lines_found_means_no_call_to_claude():
    claude = Claude(answer())

    assert ClaudeCodePredictor(lines=FoundLines([]), run=claude).predict(page()) == []
    assert claude.calls == []


def test_claude_code_only_gets_the_read_tool():
    claude = Claude(answer((0, [read("αβ"), read("γδ")])))
    ClaudeCodePredictor(lines=FoundLines(TWO_WORDS), run=claude).predict(page())
    command = claude.calls[0]["command"]

    assert command[:2] == ["claude", "-p"]
    assert command[command.index("--tools") + 1] == "Read"
    assert command[command.index("--allowedTools") + 1] == "Read"
    assert command[command.index("--model") + 1] == MODEL
    assert command[command.index("--effort") + 1] == EFFORT
    assert {"--strict-mcp-config", "--no-session-persistence"} <= set(command)


def test_claude_sees_only_the_page_as_png():
    claude = Claude(answer((0, [read("αβ"), read("γδ")])))
    ClaudeCodePredictor(lines=FoundLines(TWO_WORDS), run=claude).predict(
        page(image_format="JPEG")
    )

    assert claude.calls[0]["files"] == ["page.png"]
    assert claude.calls[0]["format"] == "PNG"


def test_the_prompt_lists_each_line_box():
    claude = Claude(answer((0, [read("αβ"), read("γδ")])))
    ClaudeCodePredictor(lines=FoundLines(TWO_WORDS), run=claude).predict(page())
    prompt = claude.calls[0]["command"][2]

    assert "300 x 100 pixels" in prompt
    assert "0: [20, 30, 180, 65]" in prompt


def test_a_large_page_is_scaled_down_with_its_line_boxes():
    claude = Claude(answer((0, [read("αβ")])))
    big = [found("αβ", (200, 100, 400, 50))]

    ClaudeCodePredictor(lines=FoundLines(big), run=claude).predict(
        page(width=2 * MAX_SIDE, height=1000, ink=())
    )

    assert claude.calls[0]["size"] == (MAX_SIDE, 500)
    assert "0: [100, 50, 300, 75]" in claude.calls[0]["command"][2]


@pytest.mark.parametrize(
    "output",
    [
        "not json",
        json.dumps({"type": "result", "is_error": True, "result": "Not logged in"}),
    ],
)
def test_an_answer_without_words_is_an_error(output):
    with pytest.raises(RuntimeError, match="Claude Code"):
        predictor(TWO_WORDS, output).predict(page())


def test_run_claude_runs_in_the_folder_with_a_time_limit(monkeypatch, tmp_path):
    seen = {}

    def fake_run(command, **kwargs):
        seen.update(kwargs)
        return subprocess.CompletedProcess(command, 0, stdout="out", stderr="")

    monkeypatch.setattr("ml.claude_code_predictor.subprocess.run", fake_run)

    assert run_claude(["claude"], tmp_path) == "out"
    assert seen["cwd"] == tmp_path
    assert seen["stdin"] is subprocess.DEVNULL
    assert seen["timeout"] == TIMEOUT_S


def test_run_claude_reports_a_failed_run(monkeypatch, tmp_path):
    monkeypatch.setattr(
        "ml.claude_code_predictor.subprocess.run",
        lambda command, **kwargs: subprocess.CompletedProcess(
            command, 1, stdout="", stderr="Not logged in"
        ),
    )
    with pytest.raises(RuntimeError, match="Not logged in"):
        run_claude(["claude"], tmp_path)


def test_run_claude_reports_a_timeout(monkeypatch, tmp_path):
    def too_slow(command, **kwargs):
        raise subprocess.TimeoutExpired(command, TIMEOUT_S)

    monkeypatch.setattr("ml.claude_code_predictor.subprocess.run", too_slow)
    with pytest.raises(RuntimeError, match="did not answer"):
        run_claude(["claude"], tmp_path)


def test_available_with_kraken_and_claude_installed(monkeypatch):
    monkeypatch.setattr(KrakenPredictor, "is_available", classmethod(lambda cls: True))
    monkeypatch.setattr(
        "ml.claude_code_predictor.shutil.which", lambda _: "/usr/bin/claude"
    )
    assert ClaudeCodePredictor.is_available()


def test_not_available_without_claude(monkeypatch):
    monkeypatch.setattr(KrakenPredictor, "is_available", classmethod(lambda cls: True))
    monkeypatch.setattr("ml.claude_code_predictor.shutil.which", lambda _: None)
    assert not ClaudeCodePredictor.is_available()


def test_not_available_without_kraken(monkeypatch):
    monkeypatch.setattr(KrakenPredictor, "is_available", classmethod(lambda cls: False))
    monkeypatch.setattr(
        "ml.claude_code_predictor.shutil.which", lambda _: "/usr/bin/claude"
    )
    assert not ClaudeCodePredictor.is_available()


@pytest.mark.skipif(
    not os.getenv("RUN_CLAUDE_CODE") or not ClaudeCodePredictor.is_available(),
    reason="calls Claude Code: set RUN_CLAUDE_CODE=1, see backend/README.md",
)
def test_reads_a_rendered_line_with_claude_code(digit_image):
    words = ClaudeCodePredictor().predict(digit_image)

    assert all(isinstance(w, Prediction) for w in words)
    assert all(w.box[2] >= 0 and w.box[3] >= 0 for w in words)
