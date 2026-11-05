from collections.abc import Callable
from datetime import UTC, datetime

from app.prediction import PredictionItem
from app.recognitions.models import Recognition
from app.recognitions.repository import RecognitionRepository
from app.uploads import ImageUpload
from ml import AbstractPredictor

MAX_FILENAME_LENGTH = 255


class RecognitionNotFound(Exception):
    pass


class RecognitionService:
    def __init__(
        self,
        repository: RecognitionRepository,
        clock: Callable[[], datetime] = lambda: datetime.now(UTC),
    ) -> None:
        self._repository = repository
        self._clock = clock

    def recognize(
        self, user_id: int, upload: ImageUpload, predictor: AbstractPredictor
    ) -> Recognition:
        results = predictor.predict(upload.content)
        return self._repository.add(
            Recognition(
                user_id=user_id,
                created_at=self._clock(),
                filename=upload.filename[:MAX_FILENAME_LENGTH],
                content_type=upload.content_type,
                image=upload.content,
                model_version=predictor.model_version,
                predictions=[PredictionItem.of(r).model_dump() for r in results],
            )
        )

    def history(self, user_id: int) -> list[Recognition]:
        return self._repository.list_for_user(user_id)

    def get(self, user_id: int, recognition_id: int) -> Recognition:
        recognition = self._repository.get_for_user(user_id, recognition_id)
        if recognition is None:
            raise RecognitionNotFound(recognition_id)
        return recognition

    def image(self, user_id: int, recognition_id: int) -> tuple[bytes, str]:
        recognition = self.get(user_id, recognition_id)
        return recognition.image, recognition.content_type
