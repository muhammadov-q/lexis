# kraken Eparchos

The line recognition model behind `KrakenPredictor` in `backend/src/ml/kraken_predictor.py`. kraken's default model finds the lines; this model reads them and gives a position for every character, which places the words ([[ml/README]]). The weights (13 MB) are the release `model-kraken-eparchos-v1`, not in git.

## Data

[Eparchos](https://zenodo.org/records/4095301) (CC BY 4.0): 120 scanned pages of two 16th-century Greek codices, 75 pages of 6791 and 45 of 6971, with the lines and words transcribed in PAGE XML.

The split is fixed per page in `backend/src/ml/data/eparchos_split.json`: 84 train, 12 validation and 24 test pages. Pages are sorted by name, and positions 0 and 5 of every ten go to test, position 3 to validation. Page 6791 (1), the demo page, is a test page. Test pages are not used for training or for picking the checkpoint.

Eparchos has line polygons but no baselines, so each line gets a straight baseline at 75% of its height.

## Training

- Base model: PP-OCRv6 small ([Zenodo 21788405](https://zenodo.org/records/21788405), Apache-2.0), kraken's multilingual CTC line recognizer, 3.2M parameters.
- kraken 7.1.1 `ketos train`: 6 epochs, batch 4, AdamW, learning rate 2e-4 with cosine decay, augmentation on, NFD normalization, output layer extended with the Eparchos characters (`--resize union`). The epoch with the best validation accuracy (95.6%, epoch 5) was kept.
- About 25 minutes on an M2 Pro laptop GPU (MPS). Cap the GPU memory with `PYTORCH_MPS_HIGH_WATERMARK_RATIO=0.3 PYTORCH_MPS_LOW_WATERMARK_RATIO=0.2`: at batch 16 training took about 20 GB and the laptop started swapping. On macOS `ketos` also needs `TORCHDYNAMO_DISABLE=1`.

The training and evaluation scripts are not in the repo yet (#80).

## Results

The 24 test pages, 3728 words. CER and WER are on NFC text with accents and punctuation.

Reading lines, with the line polygons from the ground truth:

| Model                          | CER   | WER   |
| ------------------------------ | ----- | ----- |
| PP-OCRv6 small, not fine-tuned | 36.5% | 90.3% |
| kraken Eparchos                | 8.1%  | 34.5% |

End to end through `KrakenPredictor`, with kraken finding the lines: 77.0% of the words get a box that overlaps the true one (IoU of at least 0.5), 44.5% are read exactly, 66.6% have the right letters when accents are ignored. A page takes about 8 seconds on a laptop CPU.

For comparison, Tsochatzidis et al. (2021) report 4.16% CER on Eparchos with their own split.

## Limits

- Trained on two codices from one century. Other hands, older scripts and printed Greek will read worse.
- Tested on clean scans only. Phone photos are not measured yet (#80).
- About one word in three has at least one wrong character, so the text needs checking by the reader (#79).
- It follows the Eparchos transcription: abbreviations and ligatures come out the way that dataset writes them.
