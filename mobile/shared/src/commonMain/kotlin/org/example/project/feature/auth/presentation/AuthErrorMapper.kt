package org.example.project.feature.auth.presentation

import org.example.project.common.presentation.ui.helpers.UiText
import org.example.project.feature.auth.domain.model.AuthApiException
import scriptlens.shared.generated.resources.Res
import scriptlens.shared.generated.resources.auth_error_conflict
import scriptlens.shared.generated.resources.auth_error_network
import scriptlens.shared.generated.resources.auth_error_unauthorized
import scriptlens.shared.generated.resources.auth_error_unknown
import scriptlens.shared.generated.resources.auth_error_validation

fun mapAuthError(exception: Throwable): UiText = when (exception) {
    is AuthApiException -> when (exception.status) {
        0 -> UiText.Resource(Res.string.auth_error_network)
        400 -> exception.detail?.let(UiText::Plain) ?: UiText.Resource(Res.string.auth_error_validation)
        401 -> exception.detail?.let(UiText::Plain) ?: UiText.Resource(Res.string.auth_error_unauthorized)
        409 -> exception.detail?.let(UiText::Plain) ?: UiText.Resource(Res.string.auth_error_conflict)
        else -> exception.detail?.let(UiText::Plain) ?: UiText.Resource(Res.string.auth_error_unknown)
    }

    else -> UiText.Resource(Res.string.auth_error_unknown)
}
