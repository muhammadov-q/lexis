from datetime import datetime
from typing import Annotated, Any

from fastapi import APIRouter, Depends, HTTPException, Request, Response
from fastapi.responses import JSONResponse
from pydantic import BaseModel
from sqlalchemy.orm import Session

from app.auth.router import NOT_SIGNED_IN, CurrentUser
from app.db import get_db
from app.prediction import PredictionItem, get_predictor
from app.recognitions.models import Recognition
from app.recognitions.repository import RecognitionRepository
from app.recognitions.service import RecognitionService
from app.uploads import (
    ERROR_415_PAGE,
    PAGE_TYPES,
    UPLOAD_ERRORS,
    ErrorResponse,
    ImageUpload,
    capped_upload,
)
from ml import AbstractPredictor

ERROR_404 = "This result is not in your history."

NOT_FOUND: dict[int | str, dict[str, Any]] = {
    404: {"model": ErrorResponse, "description": "Not one of your results"}
}

router = APIRouter(
    prefix="/recognitions", tags=["recognitions"], responses=NOT_SIGNED_IN
)


def get_recognition_service(
    db: Annotated[Session, Depends(get_db)],
) -> RecognitionService:
    return RecognitionService(RecognitionRepository(db))


Service = Annotated[RecognitionService, Depends(get_recognition_service)]


class RecognitionResponse(BaseModel):
    id: int
    created_at: datetime
    filename: str
    model_version: str
    predictions: list[PredictionItem]

    @classmethod
    def of(cls, recognition: Recognition) -> "RecognitionResponse":
        return cls(
            id=recognition.id,
            created_at=recognition.created_at,
            filename=recognition.filename,
            model_version=recognition.model_version,
            predictions=[
                PredictionItem.model_validate(p) for p in recognition.predictions
            ],
        )


async def recognition_not_found(_request: Request, _exc: Exception) -> JSONResponse:
    return JSONResponse(status_code=404, content={"detail": ERROR_404})


@router.post("", status_code=201, responses=UPLOAD_ERRORS)
def create_recognition(
    user: CurrentUser,
    service: Service,
    upload: Annotated[ImageUpload, Depends(capped_upload)],
    predictor: Annotated[AbstractPredictor, Depends(get_predictor)],
) -> RecognitionResponse:
    if upload.content_type not in PAGE_TYPES:
        raise HTTPException(status_code=415, detail=ERROR_415_PAGE)
    try:
        recognition = service.recognize(user.id, upload, predictor)
    except ValueError as e:
        raise HTTPException(status_code=422, detail=str(e)) from e
    return RecognitionResponse.of(recognition)


@router.get("")
def list_recognitions(user: CurrentUser, service: Service) -> list[RecognitionResponse]:
    return [RecognitionResponse.of(r) for r in service.history(user.id)]


@router.get("/{recognition_id}", responses=NOT_FOUND)
def get_recognition(
    recognition_id: int, user: CurrentUser, service: Service
) -> RecognitionResponse:
    return RecognitionResponse.of(service.get(user.id, recognition_id))


@router.get(
    "/{recognition_id}/image",
    response_class=Response,
    responses={
        200: {
            "description": "The page as it was uploaded",
            "content": {"image/*": {"schema": {"type": "string", "format": "binary"}}},
        },
        **NOT_FOUND,
    },
)
def download_recognition_image(
    recognition_id: int, user: CurrentUser, service: Service
) -> Response:
    image, content_type = service.image(user.id, recognition_id)
    return Response(
        content=image, media_type=content_type, headers={"Cache-Control": "no-store"}
    )
