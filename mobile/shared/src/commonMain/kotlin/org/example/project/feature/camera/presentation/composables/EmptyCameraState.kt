package org.example.project.feature.camera.presentation.composables

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import org.example.project.common.presentation.ui.theme.LexisSpacing
import org.example.project.common.presentation.ui.composables.buttons.LexisButton
import org.example.project.common.presentation.ui.composables.buttons.LexisButtonStyle
import org.jetbrains.compose.resources.painterResource
import lexis.shared.generated.resources.Res
import lexis.shared.generated.resources.ic_camera
import org.jetbrains.compose.resources.stringResource
import lexis.shared.generated.resources.camera_empty_hint
import lexis.shared.generated.resources.action_take_photo
import lexis.shared.generated.resources.action_choose_photo
import org.example.project.common.presentation.ui.theme.LexisColors


@Composable
fun EmptyCameraState(
    modifier: Modifier = Modifier,
    onTakePhotoClicked: () -> Unit,
    onChooseFromPhotosClicked: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(LexisColors.Surface)
            .padding(LexisSpacing.padding_24),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Icon(
            modifier = Modifier.padding(bottom = LexisSpacing.padding_12).size(28.dp),
            painter = painterResource(Res.drawable.ic_camera),
            contentDescription = null,
        )

        Text(
            modifier = Modifier.padding(bottom = LexisSpacing.padding_56),
            text = stringResource(Res.string.camera_empty_hint),
            color = LexisColors.Subtext,
            textAlign = TextAlign.Center,
        )

        LexisButton(
            modifier = Modifier.padding(bottom = LexisSpacing.padding_8),
            text = stringResource(Res.string.action_take_photo),
            onClick = onTakePhotoClicked,
            buttonStyle = LexisButtonStyle.Primary
        )

        LexisButton(
            modifier = Modifier.padding(bottom = LexisSpacing.padding_4),
            text = stringResource(Res.string.action_choose_photo),
            onClick = onChooseFromPhotosClicked,
            buttonStyle = LexisButtonStyle.Secondary
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyCameraStatePreview() {
    MaterialTheme {
        EmptyCameraState(
            onTakePhotoClicked = {},
            onChooseFromPhotosClicked = {},
        )
    }
}

