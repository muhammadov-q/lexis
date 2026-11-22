from datetime import datetime
from typing import Annotated, Any

from fastapi import APIRouter, Depends, HTTPException, Request
from fastapi.responses import JSONResponse
from pydantic import BaseModel, Field
from sqlalchemy.orm import Session

from app.auth.router import NOT_SIGNED_IN, CurrentUser
from app.db import get_db
from app.prediction import GlossResponse
from app.recognitions.repository import RecognitionRepository
from app.uploads import ErrorResponse
from app.vocabulary.models import VocabularyWord
from app.vocabulary.repository import VocabularyRepository, WordAlreadySaved
from app.vocabulary.service import NotAWord, VocabularyService

ERROR_404 = "This word is not in your vocabulary."
ERROR_409 = "This word is already in your vocabulary."

MAX_NOTE_LENGTH = 2000

NOT_FOUND: dict[int | str, dict[str, Any]] = {
    404: {"model": ErrorResponse, "description": "Not one of your words"}
}

router = APIRouter(prefix="/vocabulary", tags=["vocabulary"], responses=NOT_SIGNED_IN)


def get_vocabulary_service(
    db: Annotated[Session, Depends(get_db)],
) -> VocabularyService:
    return VocabularyService(VocabularyRepository(db), RecognitionRepository(db))


Service = Annotated[VocabularyService, Depends(get_vocabulary_service)]


class AddWordRequest(BaseModel):
    recognition_id: int
    word_index: int = Field(ge=0, description="Position of the word in predictions")
    note: str = Field("", max_length=MAX_NOTE_LENGTH)


class NoteRequest(BaseModel):
    note: str = Field(max_length=MAX_NOTE_LENGTH)


class WordSource(BaseModel):
    recognition_id: int
    filename: str
    line: int
    word_index: int


class VocabularyWordResponse(BaseModel):
    id: int
    created_at: datetime
    word: str
    gloss: GlossResponse | None
    note: str
    source: WordSource

    @classmethod
    def of(cls, word: VocabularyWord) -> "VocabularyWordResponse":
        return cls(
            id=word.id,
            created_at=word.created_at,
            word=word.word,
            gloss=None if word.gloss is None else GlossResponse(**word.gloss),
            note=word.note,
            source=WordSource(
                recognition_id=word.recognition_id,
                filename=word.recognition.filename,
                line=word.line,
                word_index=word.word_index,
            ),
        )


async def vocabulary_word_not_found(_request: Request, _exc: Exception) -> JSONResponse:
    return JSONResponse(status_code=404, content={"detail": ERROR_404})


@router.post(
    "",
    status_code=201,
    responses={
        404: {"model": ErrorResponse, "description": "Not one of your results"},
        409: {"model": ErrorResponse, "description": "Word already saved"},
        422: {"model": ErrorResponse, "description": "No word at that position"},
    },
)
def add_word(
    request: AddWordRequest, user: CurrentUser, service: Service
) -> VocabularyWordResponse:
    try:
        word = service.add(
            user.id, request.recognition_id, request.word_index, request.note
        )
    except NotAWord as e:
        raise HTTPException(status_code=422, detail=str(e)) from e
    except WordAlreadySaved as e:
        raise HTTPException(status_code=409, detail=ERROR_409) from e
    return VocabularyWordResponse.of(word)


@router.get("")
def list_words(user: CurrentUser, service: Service) -> list[VocabularyWordResponse]:
    return [VocabularyWordResponse.of(w) for w in service.words(user.id)]


@router.get("/{word_id}", responses=NOT_FOUND)
def get_word(
    word_id: int, user: CurrentUser, service: Service
) -> VocabularyWordResponse:
    return VocabularyWordResponse.of(service.get(user.id, word_id))


@router.patch("/{word_id}", responses=NOT_FOUND)
def update_note(
    word_id: int, request: NoteRequest, user: CurrentUser, service: Service
) -> VocabularyWordResponse:
    return VocabularyWordResponse.of(
        service.update_note(user.id, word_id, request.note)
    )


@router.delete("/{word_id}", status_code=204, responses=NOT_FOUND)
def remove_word(word_id: int, user: CurrentUser, service: Service) -> None:
    service.remove(user.id, word_id)
