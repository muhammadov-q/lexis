package org.example.project.feature.vocabulary.presentation

import lexis.shared.generated.resources.Res
import lexis.shared.generated.resources.vocabulary_error_network
import lexis.shared.generated.resources.vocabulary_error_not_found
import lexis.shared.generated.resources.vocabulary_error_unauthorized
import org.example.project.common.presentation.ui.helpers.UiText
import org.example.project.core.network.ApiException
import org.jetbrains.compose.resources.StringResource

internal fun vocabularyError(exception: Exception, fallback: StringResource): UiText {
    val status = (exception as? ApiException)?.status
    return UiText.Resource(
        when (status) {
            0 -> Res.string.vocabulary_error_network
            401 -> Res.string.vocabulary_error_unauthorized
            404 -> Res.string.vocabulary_error_not_found
            else -> fallback
        }
    )
}

internal fun Exception.isUnauthorized(): Boolean = this is ApiException && status == 401
