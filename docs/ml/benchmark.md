# Word benchmark

`python -m ml.benchmark` (`backend/src/ml/benchmark/`) measures how well a model finds and reads the words of a page. It compares the model's words with the words the Eparchos authors marked by hand, on the fixed split of [[models/kraken-eparchos]]. The results are in the model cards.

## Run it

```bash
cd backend
uv run python -m ml.benchmark --data /path/to/eparchos --model kraken --split val
```

- `--data` is the unpacked [Eparchos](https://zenodo.org/records/4095301) folder with `image/` and `xml/`. It is not in git.
- `--split val` (12 pages) is for trying a change. `--split test` (24 pages) is only for the final numbers of a model. Training pages can't be scored.
- `--true-lines` gives the model the true lines instead of letting it find them. That shows the word step on its own. Only the kraken models can do this.
- `--out results.json` saves the numbers, with the counts of every page.

## What it measures

A predicted word matches a true word when their boxes overlap with an IoU of at least 0.5. Each word is matched at most once, best overlap first.

| Metric | Meaning |
| --- | --- |
| Word boxes: precision, recall, F1 | Predicted boxes that match a true word, true words that are matched, and the two combined. |
| Overlap of matched boxes | The mean IoU of the matched pairs: how tight the boxes are. |
| Words merged with a neighbour | True words that share one predicted box with another true word. The box covers at least half of each. |
| Words split in two or more | True words that hold two or more predicted boxes. At least half of each box is inside the word. |
| Word boundaries in the text | Are the spaces in the right places, whatever the boxes say. The letters of both readings are lined up without accents or case, and the word ends are compared on the letters both have. |
| Right word in the right box | True words matched by a box with exactly the same text, accents and punctuation included. |
| Character error rate | Edits needed to turn the read text of the page into the true text, over the length of the true text (NFC). |

Each number is taken over all the words of the pages together, not as a mean of page scores. The 95% interval comes from drawing the pages again with replacement 1000 times, so it shows how much a number depends on which pages are in the split. Two models differ for real when their intervals don't overlap.

Eparchos does not always list the words of a line in reading order, so the loader sorts them from left to right.
