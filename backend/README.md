# Backend for ASE project

Small FastAPI service exposing a mock `/predict` endpoint, taking in parameter an image and answering something.

## Structure

```bash
backend/
├── pyproject.toml
├── uv.lock
├── src/app/main.py          # FastAPI app
├── tests/                   # pytest tests
│   ├── conftest.py
│   └── integration/
└── bruno/                   # Bruno API tests
    ├── bruno.json
    ├── environments/local.bru
    └── predict/
```

## Setup

Install `uv` on the system the way you prefer. If you use nixos, you may use the `shell.nix` file to setup.

## Install dependencies

```bash
cd backend
uv sync
```

This create `.venv` and add everything in `uv.lock`.

## Install Bruno-cli

Via npm

```bash
npm install -g @usebruno/cli
bru --version
```

On nixos, you may found it on the packages.

## Train the k-NN

> [!IMPORTANT]
> You must train the k-nn **before** starting the API with the knn predictor. Skipping this step will result in a `FileNotFoundError`. You only need to do it once (and when you want to retrain the model).

Training take around 20 seconds on a Lenovo IdeaPad Pro 5.

```bash
cd backend
uv run src/ml/train_knn.py
```

## Run the API

Start the API with the knn predictor (requires the training step above),

```bash
cd backend
ML_PREDICTOR="knn" uv run uvicorn app.main:app --reload
```

For the word by word demo, run the `eparchos` mock. It ignores the uploaded image and always returns the 139 words of Eparchos page 6791 with their translations, so upload that page (`6791 (1).png` from the [Eparchos dataset](https://zenodo.org/records/4095301)) for the boxes to line up.

```bash
cd backend
ML_PREDICTOR="eparchos" uv run uvicorn app.main:app --reload
```

To read the uploaded page for real, run the `kraken` predictor. It needs the optional `ml` dependencies (kraken and torch, about 1 GB) and the recognition model in `src/ml/models/`, which git ignores. The model is fine-tuned on Eparchos and published as a release ([model card](../docs/models/kraken-eparchos.md)). A page takes about 8 seconds on a laptop CPU.

```bash
cd backend
uv sync --extra ml
curl -fL -o src/ml/models/kraken_eparchos.safetensors \
  https://github.com/muhammadov-q/team4/releases/download/model-kraken-eparchos-v1/kraken_eparchos.safetensors
ML_PREDICTOR="kraken" uv run uvicorn app.main:app --reload
```

`KRAKEN_MODEL=/path/to/model.safetensors` uses another kraken recognition model.

To compare, `kraken-base` reads with the same model before fine-tuning ([model card](../docs/models/ppocrv6-small.md)). It needs the `ml` dependencies too, and its own weights from Zenodo:

```bash
cd backend
curl -fL -o src/ml/models/ppocrv6_small.safetensors \
  https://zenodo.org/records/21788405/files/small.safetensors
```

## Choose a model

`ML_PREDICTOR` sets the default model, and a request can pick another one:

- `GET /models` lists the models this install can run, and the default. A model that needs weights or the `ml` extra is only listed once they are there.
- `POST /predict` and `POST /recognitions` take an optional form field `model` next to `image`. Without it they use the default. A model that isn't on the list gets a 422.

```bash
curl -F image=@page.png -F model=kraken http://localhost:8000/predict
```

`model_version` in the response says which model read the page, and the results history saves it.

The default model loads when the API starts, the others on their first page, which takes a few seconds longer. A loaded model stays in memory until the API stops. On a laptop, kraken took 4 to 5 GB while reading, and about 5.5 GB with both kraken models loaded.

To add a model, add its predictor to `PREDICTORS` in `src/ml/factory.py`. Give it a `display_name`, a `description` and, if it needs files, an `is_available`.

## Run the tests

### pytest (no server needed)

```bash
cd backend
uv run pytest
uv run pytest --cov --cov-report=term-missing   # with coverage
```

### Type check

```bash
cd backend
uv run mypy
```

mypy runs in strict mode (settings in `pyproject.toml`), so new functions need type annotations. Tests are checked too, but their functions don't need annotations.

### Bruno (needs the API)

```bash
# terminal 1
cd backend
ML_PREDICTOR="knn" uv run uvicorn app.main:app --reload

# terminal 2
cd backend/bruno
bru run --env local
```

## Database

The backend stores accounts in SQLite at `data/team4.db` (not committed). It creates the file and runs any pending migrations when it starts, so there's no setup step. Set `DATABASE_URL` to use another file; delete `data/` to start over.

After changing a model in `src/app/auth/models.py`, add a migration and read it before committing. The command compares the models with your local database, so start the backend once first:

```bash
cd backend
uv run alembic revision --autogenerate -m "describe the change"
```

More in `docs/architecture/database.md`.

## Sign-in

`/auth/sign-up`, `/auth/sign-in`, `/auth/sign-out` and `/auth/me` handle accounts. A signed-in browser holds the `team4_session` cookie. The code is in `src/app/auth/`, the rules in `docs/architecture/authentication.md`.

## Results history

`/recognitions` reads a page like `/predict` and also saves it for the signed-in user, image included. `GET /recognitions` lists your results, newest first; `GET /recognitions/{id}` and `/recognitions/{id}/image` return one. The web app uses these; `/predict` stays open for the mobile app. The code is in `src/app/recognitions/`.

## Phone capture sessions

`/capture-sessions` lets a phone send photos to the web app (the "Use your phone" QR code). Sessions
and their last photo are kept in memory, so a restart drops them, and they end 30 minutes after the
last photo. The code is in `src/app/capture/`.

## API Documentation

After running the server,

```bash
# terminal 1
cd backend
ML_PREDICTOR="knn" uv run uvicorn app.main:app --reload
```

you may find the Swagger documentation at the adress: [http://localhost:8000/docs](http://localhost:8000/docs).
