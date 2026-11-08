# Machine learning
The first version of the backend runs a mock predictor, `DummyPredictor`, that ignore the image and returns a fixed value following the current Predictor output. 

For now, we have a `KnnPredictor`. It consist of a pipeline finding artifacts on a picture that may be a digit. Then it preprocess the artifact to have a 28x28 sample to compare using a KNN and then predict the digit. More details can be found on the [dedicated model page](models/KNN).

For the word by word reading, `EparchosPredictor` is a mock too: it ignores the image and returns the words of one page of the [Eparchos dataset](https://zenodo.org/records/4095301) (page 6791) with their boxes and line numbers. `TranslatingPredictor` then adds a gloss to each word (dictionary form, translation, definition) from `GlossaryTranslator`, a small hand-written Greek to English glossary in `backend/src/ml/data/glossary.json` that only covers that page.

`KrakenPredictor` reads the uploaded page for real with [kraken](https://kraken.re). kraken's default model finds the text lines, a CTC recognition model reads each line and gives a position for every character, and `words_in_line` splits the line into words at its spaces. A word's box runs from the space before it to the space after it, shrunk to the ink inside. #72 compared the options. The model, its data split and its results are in [[models/kraken-eparchos]].

`KrakenBasePredictor` reads the same way with the model before fine-tuning ([[models/ppocrv6-small]]), so the two can be compared on a page.

`ModelRegistry` lets a request choose which of these models reads its page: `GET /models` lists the ones this install can run, and the upload routes take a `model` form field. The usage is in `backend/README.md`.

This note gets the tasks, datasets, splits and metrics once the team agrees on them. Each model we then use gets a card in [[models/README|models]].
