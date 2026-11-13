# kraken Eparchos

The line recognition model behind `KrakenPredictor` in `backend/src/ml/kraken_predictor.py`. kraken's default model finds the lines; this model reads them and gives a position for every character, which places the words ([[ml/README]]). The weights (13 MB) are the release `model-kraken-eparchos-v1`, not in git.

## Data

[Eparchos](https://zenodo.org/records/4095301) (CC BY 4.0): 120 scanned pages of two 16th-century Greek codices, 75 pages of 6791 and 45 of 6971, with the lines and words transcribed in PAGE XML.

The split is fixed per page in `backend/src/ml/data/eparchos_split.json`: 84 train, 12 validation and 24 test pages. Pages are sorted by name, and positions 0 and 5 of every ten go to test, position 3 to validation. Page 6791 (1), the demo page, is a test page. Test pages are not used for training, for picking the checkpoint, or for the choices below, which use the validation pages.

Eparchos has line polygons but no baselines, so each line gets a straight baseline at 75% of its height.

## Training

- Base model: PP-OCRv6 small ([Zenodo 21788405](https://zenodo.org/records/21788405), Apache-2.0), kraken's multilingual CTC line recognizer, 3.2M parameters. Untrained, it reads the validation pages at the same 36% CER as the Greek minuscule model ([Zenodo 15837901](https://zenodo.org/records/15837901), CC BY-SA); it was picked for its license and because it is made as a base for fine-tuning.
- kraken 7.1.1 `ketos train`, seed 42: 6 epochs, batch 4, AdamW, learning rate 2e-4 with cosine decay, augmentation on, NFD normalization, output layer extended with the Eparchos characters (`--resize union`). The epoch with the best validation accuracy (95.6%, epoch 5) was kept.
- About 25 minutes on an M2 Pro laptop GPU (MPS). Cap the GPU memory with `PYTORCH_MPS_HIGH_WATERMARK_RATIO=0.3 PYTORCH_MPS_LOW_WATERMARK_RATIO=0.2`: at batch 16 training took about 20 GB and the laptop started swapping. On macOS `ketos` also needs `TORCHDYNAMO_DISABLE=1`.

- Word boxes (`words_in_line`): on the validation pages, the box from the space before a word to the space after it, shrunk to the ink, boxes 88.7% of the words correctly (IoU of at least 0.5). The character positions alone box 63.8%.
- Line shape (#102): the model was trained on the Eparchos lines, which are rectangles with a straight baseline. kraken finds curved lines, so `KrakenPredictor` turns each found line into the rectangle around it with a straight baseline before reading. On the validation pages that took the character error rate from 11.3% to 6.9% and the words merged with a neighbour from 12.4% to 7.0%. The rectangle or the straight baseline alone did not help.
- Tried and dropped (#102), on the validation pages: splitting a word at the blank gaps in its ink, because the gaps at missed word boundaries (3 px in the middle) are as narrow as the gaps inside words (2 px), so it split several words wrongly for each merge it fixed. Trimming a box to its main band of ink rows gained 1.8 points of box F1, but almost all of it on two pages.

The training script is not in the repo yet. The word benchmark is: [[ml/benchmark]].

## Results

The 24 test pages, 3728 words. CER and WER are on NFC text with accents and punctuation.

Reading lines, with the line polygons from the ground truth:

| Model                          | CER   | WER   |
| ------------------------------ | ----- | ----- |
| PP-OCRv6 small, not fine-tuned | 36.5% | 90.3% |
| kraken Eparchos                | 8.1%  | 34.5% |

The word benchmark ([[ml/benchmark]]) on the same 24 test pages, end to end through `KrakenPredictor` and with the true lines. The range is the 95% interval over pages. A page takes about 8 seconds on a laptop CPU.

| Test pages                      | kraken finds the lines | True lines           |
| ------------------------------- | ---------------------- | -------------------- |
| Word boxes, F1                  | 81.2% (76.4 to 85.4)   | 84.6% (82.0 to 87.0) |
| Word boxes, precision           | 82.9% (78.0 to 87.0)   | 85.3% (82.5 to 87.8) |
| Word boxes, recall              | 79.6% (74.9 to 83.8)   | 84.0% (81.4 to 86.3) |
| Overlap of matched boxes (IoU)  | 76.3% (74.8 to 77.7)   | 79.5% (78.2 to 80.7) |
| Words merged with a neighbour   | 8.6% (6.7 to 10.5)     | 8.0% (6.5 to 9.5)    |
| Words split in two or more      | 1.3% (0.8 to 1.8)      | 1.9% (1.3 to 2.6)    |
| Word boundaries in the text, F1 | 95.2% (94.0 to 96.3)   | 94.8% (93.7 to 95.9) |
| Right word in the right box     | 60.1% (54.8 to 64.7)   | 63.3% (59.0 to 67.1) |
| Character error rate            | 8.1% (6.9 to 9.3)      | 7.9% (6.7 to 9.2)    |

Before the found lines were straightened (#102), the end to end numbers were 44.5% for the right word in the right box, 12.8% for the character error rate and 15.1% for merged words. About one word in twelve is still merged with a neighbour and few are split: where the scribe wrote two words together, the model reads them as one.

For comparison, Tsochatzidis et al. (2021) report 4.16% CER on Eparchos with their own split.

## Limits

- Trained on two codices from one century. Other hands, older scripts and printed Greek will read worse.
- Tested on clean scans only. Phone photos are not measured yet (#80).
- About one word in three has at least one wrong character, so the text needs checking by the reader (#79).
- It follows the Eparchos transcription: abbreviations and ligatures come out the way that dataset writes them.
