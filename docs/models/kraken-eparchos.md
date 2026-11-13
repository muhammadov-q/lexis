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

The training script is not in the repo yet. The word benchmark is: [[ml/benchmark]].

## Results

The 24 test pages, 3728 words. CER and WER are on NFC text with accents and punctuation.

Reading lines, with the line polygons from the ground truth:

| Model                          | CER   | WER   |
| ------------------------------ | ----- | ----- |
| PP-OCRv6 small, not fine-tuned | 36.5% | 90.3% |
| kraken Eparchos                | 8.1%  | 34.5% |

End to end through `KrakenPredictor`, with kraken finding the lines: 77.0% of the words get a box that overlaps the true one (IoU of at least 0.5), 44.5% are read exactly, 66.6% have the right letters when accents are ignored. A page takes about 8 seconds on a laptop CPU.

The word benchmark ([[ml/benchmark]]) on the same 24 test pages. The range is the 95% interval over pages.

| Test pages                      | kraken finds the lines | True lines           |
| ------------------------------- | ---------------------- | -------------------- |
| Word boxes, F1                  | 80.3% (76.0 to 83.8)   | 84.6% (82.0 to 87.0) |
| Word boxes, precision           | 83.8% (79.4 to 87.3)   | 85.3% (82.5 to 87.8) |
| Word boxes, recall              | 77.0% (72.5 to 80.8)   | 84.0% (81.4 to 86.3) |
| Overlap of matched boxes (IoU)  | 76.6% (75.1 to 78.0)   | 79.5% (78.2 to 80.7) |
| Words merged with a neighbour   | 15.1% (11.5 to 19.5)   | 8.0% (6.5 to 9.5)    |
| Words split in two or more      | 1.1% (0.6 to 1.7)      | 1.9% (1.3 to 2.6)    |
| Word boundaries in the text, F1 | 91.1% (88.5 to 93.0)   | 91.7% (90.3 to 92.9) |
| Right word in the right box     | 44.5% (39.2 to 49.0)   | 63.3% (59.0 to 67.1) |
| Character error rate            | 12.8% (11.0 to 15.0)   | 7.9% (6.7 to 9.2)    |

About one word in seven is merged with a neighbour and few are split: the model misses spaces far more often than it adds them (#102). With the true lines the right word lands in the right box for 63.3% of the words, against 44.5% with the lines kraken finds, so line finding costs about as much as reading.

For comparison, Tsochatzidis et al. (2021) report 4.16% CER on Eparchos with their own split.

## Limits

- Trained on two codices from one century. Other hands, older scripts and printed Greek will read worse.
- Tested on clean scans only. Phone photos are not measured yet (#80).
- About one word in three has at least one wrong character, so the text needs checking by the reader (#79).
- It follows the Eparchos transcription: abbreviations and ligatures come out the way that dataset writes them.
