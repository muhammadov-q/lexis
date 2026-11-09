from typing import Annotated, Any

from fastapi import APIRouter, Depends, HTTPException, Request, Response
from fastapi.responses import JSONResponse
from fastapi.security import APIKeyCookie
from pydantic import BaseModel, Field
from sqlalchemy.orm import Session

from app.auth.models import User
from app.auth.repository import (
    AuthSessionRepository,
    EmailAlreadyRegistered,
    UserRepository,
)
from app.auth.service import (
    SESSION_TTL,
    AuthService,
    InvalidCredentials,
    InvalidSignUp,
    NotSignedIn,
)
from app.db import get_db
from app.uploads import ErrorResponse

COOKIE_NAME = "lexis_session"

ERROR_401 = "Sign in to continue."
ERROR_401_CREDENTIALS = "Wrong email or password."
ERROR_409 = "An account with this email already exists. Sign in instead."

NOT_SIGNED_IN: dict[int | str, dict[str, Any]] = {
    401: {"model": ErrorResponse, "description": "No session, or it expired"}
}

router = APIRouter(prefix="/auth", tags=["auth"])
session_cookie = APIKeyCookie(name=COOKIE_NAME, auto_error=False)


def get_auth_service(db: Annotated[Session, Depends(get_db)]) -> AuthService:
    return AuthService(UserRepository(db), AuthSessionRepository(db))


Service = Annotated[AuthService, Depends(get_auth_service)]
SessionToken = Annotated[str | None, Depends(session_cookie)]


def current_user(service: Service, token: SessionToken) -> User:
    if token is None:
        raise NotSignedIn
    return service.current_user(token)


CurrentUser = Annotated[User, Depends(current_user)]


class Credentials(BaseModel):
    email: str = Field(max_length=320)
    password: str


class SignUpRequest(Credentials):
    first_name: str | None = Field(default=None, max_length=100)
    last_name: str | None = Field(default=None, max_length=100)


class UserResponse(BaseModel):
    id: int
    email: str
    first_name: str | None
    last_name: str | None

    @classmethod
    def of(cls, user: User) -> "UserResponse":
        return cls(
            id=user.id,
            email=user.email,
            first_name=user.first_name,
            last_name=user.last_name,
        )


def set_session_cookie(response: Response, token: str) -> None:
    # Not `secure`: the app runs over plain http on a laptop.
    response.set_cookie(
        COOKIE_NAME,
        token,
        max_age=int(SESSION_TTL.total_seconds()),
        httponly=True,
        samesite="lax",
    )


def clear_session_cookie(response: Response) -> None:
    response.delete_cookie(COOKIE_NAME, httponly=True, samesite="lax")


async def not_signed_in(_request: Request, _exc: Exception) -> JSONResponse:
    response = JSONResponse(status_code=401, content={"detail": ERROR_401})
    clear_session_cookie(response)
    return response


@router.post(
    "/sign-up",
    status_code=201,
    responses={
        400: {"model": ErrorResponse, "description": "Email or password not valid"},
        409: {"model": ErrorResponse, "description": "Email already registered"},
    },
)
def sign_up(
    details: SignUpRequest, service: Service, response: Response
) -> UserResponse:
    try:
        user, token = service.sign_up(
            details.email, details.password, details.first_name, details.last_name
        )
    except InvalidSignUp as e:
        raise HTTPException(status_code=400, detail=str(e)) from e
    except EmailAlreadyRegistered as e:
        raise HTTPException(status_code=409, detail=ERROR_409) from e
    set_session_cookie(response, token)
    return UserResponse.of(user)


@router.post(
    "/sign-in",
    responses={401: {"model": ErrorResponse, "description": "Wrong credentials"}},
)
def sign_in(
    credentials: Credentials, service: Service, response: Response
) -> UserResponse:
    try:
        user, token = service.sign_in(credentials.email, credentials.password)
    except InvalidCredentials as e:
        raise HTTPException(status_code=401, detail=ERROR_401_CREDENTIALS) from e
    set_session_cookie(response, token)
    return UserResponse.of(user)


@router.post("/sign-out", status_code=204)
def sign_out(service: Service, token: SessionToken, response: Response) -> None:
    if token is not None:
        service.sign_out(token)
    clear_session_cookie(response)


@router.get("/me", responses=NOT_SIGNED_IN)
def me(user: CurrentUser) -> UserResponse:
    return UserResponse.of(user)
