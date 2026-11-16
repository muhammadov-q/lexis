# Claude Code reader

The model behind `ClaudeCodePredictor` in `backend/src/ml/claude_code_predictor.py`, offered as `claude-code`. kraken ([[models/kraken-eparchos]]) finds the lines and the word boxes. Claude Opus 5.5, run through the Claude Code command line at medium effort, gets the page with those line boxes and writes the words of each line, each with a dictionary form, a translation and a short definition, plus an English translation of each line. Why it runs this way: [[adr/0005-claude-code-reader]].

## Data

We did not train it, and we don't know what Anthropic trained Claude on. Eparchos is Michael Psellos, a published author, so Claude may know parts of the text. That would make these results better than on unknown manuscripts.

## Training

None. The prompt is `page_prompt` in the predictor. These choices were made on the 12 validation pages:

- Word boxes: Claude's words are paired with kraken's in reading order. A paired word keeps kraken's box. Where the two split a stretch into a different number of words, that stretch is divided by word length and each part shrunk to its ink. This boxed 84.6% of the validation words, against 73.7% when the whole line was divided.
- Effort: at the `xhigh` effort of our own Claude Code settings a page took 221 s and one page timed out. At medium a page took 96 s, with no failures and as many words read exactly. The predictor sets the effort itself.
- The prompt asks Claude to keep the punctuation and capitals, and never to correct, complete or replace a word.
- Confidence: Claude gives none. A word kraken read the same way keeps kraken's confidence, and every other word has none, which the web app shows as not confirmed. On the validation pages, words with kraken's confidence were right 97% of the time and unconfirmed words 53%. Scoring by how similar the two readings are did not sort right from wrong any better.

## Results

The word benchmark ([[ml/benchmark]]) on the 24 test pages. Both read the uploaded page end to end with the lines kraken finds, straightened as in #102. The range is the 95% interval over pages.

| Test pages                      | kraken Eparchos      | Claude Code reader   |
| ------------------------------- | -------------------- | -------------------- |
| Word boxes, F1                  | 81.2% (76.4 to 85.4) | 80.5% (75.6 to 84.4) |
| Words merged with a neighbour   | 8.6% (6.7 to 10.5)   | 5.0% (3.3 to 7.0)    |
| Words split in two or more      | 1.3% (0.8 to 1.8)    | 1.4% (0.9 to 2.0)    |
| Word boundaries in the text, F1 | 95.2% (94.0 to 96.3) | 97.2% (96.3 to 98.0) |
| Right word in the right box     | 60.1% (54.8 to 64.7) | 57.1% (52.1 to 61.6) |
| Character error rate            | 8.1% (6.9 to 9.3)    | 15.2% (12.2 to 18.3) |
| Time per page (median)          | about 8 s            | 102 s                |

Claude merges fewer words and puts the spaces in the right places more often, because it knows where Greek words end. It gets almost twice as many letters wrong, because it sometimes writes fluent Greek that isn't on the page. With the straightened lines kraken alone puts the right word in the right box as often (the intervals overlap). Before #102 the reader was ahead: 54.5% against 44.5%, with a CER of 17.2% (an older script that gave Claude the page through its Read tool and asked for no line translations).

### Line translations

`python -m ml.benchmark.judge` on the same run: Claude Opus judged each line translation against the true Greek text of that line ([[ml/benchmark]]).

| Test pages, 433 lines | Lines | Share |
| --------------------- | ----- | ----- |
| Correct               | 273   | 63.0% |
| Partly                | 129   | 29.8% |
| Wrong                 | 31    | 7.2%  |

The share judged correct runs from 22% to 89% per page and follows the reading: 77% on the 12 pages with the lowest CER, 49% on the 12 with the highest. A misread line is translated from the wrong Greek. This is a light check: the judge is the same model that wrote the translations, and nobody who reads Greek has checked its verdicts.

A page used $0.27 of Claude Code usage on average in the earlier runs.

## Limits

- It sometimes writes fluent Greek that isn't on the page, from a few words to a whole line. That is why its CER is higher although it reads more words exactly. Words marked not confirmed are where kraken read something else, so check those first.
- Only an LLM has checked the line translations, and nobody has checked the word glosses and definitions. Nobody who reads Greek has checked either.
- A page takes one to two minutes and is sent to Anthropic. It only runs with the Claude Code login of the person running the backend.
- Measured on clean scans of two codices only.
