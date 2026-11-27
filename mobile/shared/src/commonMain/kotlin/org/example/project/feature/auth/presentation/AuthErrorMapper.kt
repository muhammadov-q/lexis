package org.example.project.feature.auth.presentation

import org.example.project.common.presentation.ui.helpers.UiText
import org.example.project.core.network.ApiException
import lexis.shared.generated.resources.Res
import lexis.shared.generated.resources.auth_error_conflict
import lexis.shared.generated.resources.auth_error_network
import lexis.shared.generated.resources.auth_error_unauthorized
import lexis.shared.generated.resources.auth_error_unknown
import lexis.shared.generated.resources.auth_error_validation

fun mapAuthError(exception: Throwable): UiText = when (exception) {
    is ApiException -> when (exception.status) {
        0 -> UiText.Resource(Res.string.auth_error_network)
        400 -> exception.detail?.let(UiText::Plain) ?: UiText.Resource(Res.string.auth_error_validation)
        401 -> exception.detail?.let(UiText::Plain) ?: UiText.Resource(Res.string.auth_error_unauthorized)
        409 -> exception.detail?.let(UiText::Plain) ?: UiText.Resource(Res.string.auth_error_conflict)
        else -> exception.detail?.let(UiText::Plain) ?: UiText.Resource(Res.string.auth_error_unknown)
    }

    else -> UiText.Resource(Res.string.auth_error_unknown)
}
