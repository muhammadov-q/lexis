package org.example.project.feature.camera.presentation.models

import org.example.project.common.presentation.ui.helpers.UiText
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import scriptlens.shared.generated.resources.Res
import scriptlens.shared.generated.resources.error_image_rejected_message
import scriptlens.shared.generated.resources.error_image_rejected_title
import scriptlens.shared.generated.resources.error_image_too_large_message
import scriptlens.shared.generated.resources.error_image_too_large_title
import scriptlens.shared.generated.resources.error_photo_unavailable_title
import scriptlens.shared.generated.resources.error_recognition_failed_message
import scriptlens.shared.generated.resources.error_recognition_failed_title
import scriptlens.shared.generated.resources.error_service_offline_message
import scriptlens.shared.generated.resources.error_service_offline_title
import scriptlens.shared.generated.resources.error_validation_title
import scriptlens.shared.generated.resources.error_validation_message
import scriptlens.shared.generated.resources.ic_image_rejected
import scriptlens.shared.generated.resources.ic_no_service
import scriptlens.shared.generated.resources.image_too_large

sealed class CameraScreenErrorState(
    val title: StringResource,
    val message: UiText,
    val canRetry: Boolean,
    val icon: DrawableResource
) {
    constructor(
        title: StringResource,
        message: StringResource,
        canRetry: Boolean,
        icon: DrawableResource,
    ) : this(title, UiText.Resource(message), canRetry, icon)

    object ServiceOffline : CameraScreenErrorState(
        Res.string.error_service_offline_title,
        Res.string.error_service_offline_message,
        true,
        Res.drawable.ic_no_service
    )

    object ImageTooLarge : CameraScreenErrorState(
        Res.string.error_image_too_large_title,
        Res.string.error_image_too_large_message,
        false,
        Res.drawable.image_too_large
    )

    object ImageRejected : CameraScreenErrorState(
        Res.string.error_image_rejected_title,
        Res.string.error_image_rejected_message,
        false,
        Res.drawable.ic_image_rejected
    )

    object RecognitionFailed : CameraScreenErrorState(
        Res.string.error_recognition_failed_title,
        Res.string.error_recognition_failed_message,
        true,
        Res.drawable.ic_image_rejected
    )

    object ValidationFailed : CameraScreenErrorState(
        Res.string.error_validation_title,
        Res.string.error_validation_message,
        false,
        Res.drawable.ic_image_rejected
    )

    data class PhotoUnavailable(val detail: UiText) : CameraScreenErrorState(
        Res.string.error_photo_unavailable_title,
        detail,
        false,
        Res.drawable.ic_image_rejected
    )
}
