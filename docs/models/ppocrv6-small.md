# PP-OCRv6 small

The base model behind `KrakenBasePredictor` in `backend/src/ml/kraken_predictor.py`, offered as `kraken-base` so a page can be read with and without fine-tuning. kraken's default model finds the lines and this model reads them, like [[models/kraken-eparchos]]. The weights (13 MB) are on [Zenodo 21788405](https://zenodo.org/records/21788405), Apache-2.0, not in git.

## Data

We did not train it. Its authors trained it from scratch on handwritten and printed text in 44 languages across 10 scripts, Greek among them. Their training data leans towards a few languages such as English, French, German and Latin.

## Training

None. These are the published weights, unchanged.

## Results

The 24 Eparchos test pages, with the line polygons from the ground truth. Same split and metrics as [[models/kraken-eparchos]].

| Model                          | CER   | WER   |
| ------------------------------ | ----- | ----- |
| PP-OCRv6 small, not fine-tuned | 36.5% | 90.3% |
| kraken Eparchos                | 8.1%  | 34.5% |

Its authors report 5.43% CER and 20.22% WER on their own test data, which is mostly the languages above.

## Limits

- About one letter in three is wrong on Eparchos, so it is here to compare with, not to read with.
- End to end results on Eparchos, with kraken finding the lines, are not measured.
