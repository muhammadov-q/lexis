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

The 24 test pages, 3728 words. Both read the uploaded page end to end, with kraken finding the lines. CER is on the text of the whole page, NFC with accents and punctuation, so it is not the line CER in [[models/kraken-eparchos]], which used the true lines.

| Test pages                                   | kraken Eparchos | Claude Code reader |
| -------------------------------------------- | --------------- | ------------------ |
| CER                                          | 12.8%           | 17.2%              |
| Words read exactly                           | 51.2%           | 67.5%              |
| Words with a box on the true one (IoU ≥ 0.5) | 77.0%           | 74.7%              |
| Right word in the right box                  | 44.5%           | 54.5%              |
| Time per page (median, 3 pages at a time)    | 8 s             | 104 s              |

Claude Code reads a third more words exactly and puts the right word in the right place more often, but it gets more letters wrong overall: it had the lower CER on only 8 of the 24 pages. The validation pages showed the same pattern (71.4% of words exactly against 54.0%, CER 13.3% against 11.3%).

A page used $0.27 of Claude Code usage on average. These runs gave Claude Code the page through its Read tool and asked for no line translations. It now gets the same image inside the request, with no tools, and translates each line too. The evaluation script is not in the repo yet (#80). These numbers are from before #102 straightened the lines kraken finds, which the reader builds on: the current kraken numbers are in [[models/kraken-eparchos]], and the reader has not been measured again.

## Limits

- It sometimes writes fluent Greek that isn't on the page, from a few words to a whole line. That is why its CER is higher although it reads more words exactly. Words marked not confirmed are where kraken read something else, so check those first.
- The translations and definitions come from Claude and nobody has checked them.
- A page takes one to two minutes and is sent to Anthropic. It only runs with the Claude Code login of the person running the backend.
- Measured on clean scans of two codices only.
