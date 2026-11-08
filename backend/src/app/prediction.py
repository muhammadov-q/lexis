from dataclasses import asdict
from typing import Annotated

from fastapi import Depends, Form, HTTPException
from pydantic import BaseModel, ConfigDict

from ml import (
    AbstractPredictor,
    ModelInfo,
    ModelRegistry,
    UnknownModel,
    create_predictor,
)
from ml.predictor import Prediction
from ml.translating_predictor import TranslatingPredictor
from ml.translator import GlossaryTranslator

translator = GlossaryTranslator()


def build_predictor(name: str) -> AbstractPredictor:
    return TranslatingPredictor(create_predictor(name), translator)


registry = ModelRegistry(build=build_predictor)


def get_registry() -> ModelRegistry:
    return registry


def get_predictor(
    registry: Annotated[ModelRegistry, Depends(get_registry)],
    model: Annotated[
        str | None,
        Form(description="A model id from GET /models. Without it, the default model."),
    ] = None,
) -> AbstractPredictor:
    try:
        return registry.get(model)
    except UnknownModel as e:
        raise HTTPException(status_code=422, detail=str(e)) from e


class ModelResponse(BaseModel):
    id: str
    name: str
    description: str

    @classmethod
    def of(cls, model: ModelInfo) -> "ModelResponse":
        return cls(**asdict(model))


class ModelsResponse(BaseModel):
    models: list[ModelResponse]
    default_model: str


class BoxResponse(BaseModel):
    x: int
    y: int
    w: int
    h: int


class GlossResponse(BaseModel):
    lemma: str
    translation: str
    definition: str


class PredictionItem(BaseModel):
    # The defaults are for old saved results; every response has the fields
    model_config = ConfigDict(json_schema_serialization_defaults_required=True)

    text: str
    probabilities: dict[str, float]
    box: BoxResponse
    line: int = 0
    gloss: GlossResponse | None = None

    @classmethod
    def of(cls, result: Prediction) -> "PredictionItem":
        x, y, w, h = result.box
        return cls(
            text=result.text,
            probabilities=result.probabilities,
            box=BoxResponse(x=x, y=y, w=w, h=h),
            line=result.line,
            gloss=None
            if result.gloss is None
            else GlossResponse(**asdict(result.gloss)),
        )
