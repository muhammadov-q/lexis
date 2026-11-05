from collections.abc import AsyncIterator
from contextlib import asynccontextmanager
from typing import Annotated

from fastapi import Depends, FastAPI, HTTPException
from pydantic import BaseModel
from starlette.concurrency import run_in_threadpool

from app.auth.router import not_signed_in
from app.auth.router import router as auth_router
from app.auth.service import NotSignedIn
from app.capture.router import capture_session_not_found
from app.capture.router import router as capture_router
from app.capture.service import CaptureSessionNotFound
from app.db import engine, migrate
from app.prediction import (
    ModelResponse,
    ModelsResponse,
    PredictionItem,
    get_predictor,
    get_registry,
)
from app.recognitions.router import recognition_not_found
from app.recognitions.router import router as recognitions_router
from app.recognitions.service import RecognitionNotFound
from app.uploads import UPLOAD_ERRORS, capped_image
from ml import AbstractPredictor, ModelRegistry


@asynccontextmanager
async def lifespan(_app: FastAPI) -> AsyncIterator[None]:
    migrate(engine)
    yield


app = FastAPI(title="Prediction API", version="0.1.0", lifespan=lifespan)
app.include_router(auth_router)
app.include_router(capture_router)
app.include_router(recognitions_router)
app.add_exception_handler(CaptureSessionNotFound, capture_session_not_found)
app.add_exception_handler(NotSignedIn, not_signed_in)
app.add_exception_handler(RecognitionNotFound, recognition_not_found)


class PredictResponse(BaseModel):
    predictions: list[PredictionItem]
    model_version: str


@app.get("/models")
def list_models(
    registry: Annotated[ModelRegistry, Depends(get_registry)],
) -> ModelsResponse:
    return ModelsResponse(
        models=[ModelResponse.of(m) for m in registry.available()],
        default_model=registry.default,
    )


@app.post(
    "/predict",
    response_model=PredictResponse,
    responses=UPLOAD_ERRORS,
)
async def predict(
    # In this order, so a rejected upload doesn't load the chosen model
    content: Annotated[bytes, Depends(capped_image)],
    predictor: Annotated[AbstractPredictor, Depends(get_predictor)],
) -> PredictResponse:
    try:
        results = await run_in_threadpool(predictor.predict, content)
    except ValueError as e:
        raise HTTPException(status_code=422, detail=str(e)) from e

    return PredictResponse(
        predictions=[PredictionItem.of(r) for r in results],
        model_version=predictor.model_version,
    )
