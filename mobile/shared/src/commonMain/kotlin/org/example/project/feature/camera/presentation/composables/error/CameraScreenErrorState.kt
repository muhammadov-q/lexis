package org.example.project.feature.camera.presentation.composables.error

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import org.example.project.common.presentation.ui.helpers.asString
import org.example.project.common.presentation.ui.composables.buttons.LexisButton
import org.example.project.common.presentation.ui.composables.buttons.LexisButtonSize
import org.example.project.common.presentation.ui.composables.buttons.LexisButtonStyle
import org.example.project.common.presentation.ui.theme.LexisColors
import org.example.project.common.presentation.ui.theme.LexisSpacing
import org.example.project.common.presentation.ui.theme.LexisTypography
import org.example.project.feature.camera.presentation.models.CameraScreenErrorState
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import lexis.shared.generated.resources.Res
import lexis.shared.generated.resources.action_choose_another_photo
import lexis.shared.generated.resources.action_retry
import lexis.shared.generated.resources.action_take_new_photo

@Composable
fun ErrorState(
    errorState: CameraScreenErrorState,
    onTakeNewPhotoClicked: () -> Unit,
    onChooseFromPhotosClicked: () -> Unit,
    onRetryClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(LexisColors.Surface)
            .padding(LexisSpacing.padding_24),
        verticalArrangement = Arrangement.spacedBy(LexisSpacing.padding_8),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            modifier = Modifier.size(64.dp),
            painter = painterResource(errorState.icon),
            contentDescription = null
        )

        Text(
            modifier = Modifier.padding(bottom = LexisSpacing.padding_8),
            text = stringResource(errorState.title),
            style = LexisTypography.Heading,
            color = LexisColors.Text,
            textAlign = TextAlign.Center
        )

        Text(
            text = errorState.message.asString(),
            style = LexisTypography.Body,
            color = LexisColors.Subtext,
            textAlign = TextAlign.Center
        )

        if (errorState.canRetry) {
            LexisButton(
                modifier = Modifier.padding(top = LexisSpacing.padding_24),
                buttonStyle = LexisButtonStyle.Primary,
                onClick = onRetryClicked,
                text = stringResource(Res.string.action_retry),
                buttonSize = LexisButtonSize.Medium
            )
        }

        LexisButton(
            text = stringResource(Res.string.action_take_new_photo),
            onClick = onTakeNewPhotoClicked,
            buttonStyle = LexisButtonStyle.Secondary,
            buttonSize = LexisButtonSize.Medium
        )

        LexisButton(
            text = stringResource(Res.string.action_choose_another_photo),
            onClick = onChooseFromPhotosClicked,
            buttonStyle = LexisButtonStyle.Secondary,
            buttonSize = LexisButtonSize.Medium
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorStatePreview(
    @PreviewParameter(CameraScreenErrorStatePreviewProvider::class)
    errorState: CameraScreenErrorState,
) {
    MaterialTheme {
        ErrorState(
            errorState = errorState,
            onTakeNewPhotoClicked = {},
            onChooseFromPhotosClicked = {},
            onRetryClicked = {},
        )
    }
}

