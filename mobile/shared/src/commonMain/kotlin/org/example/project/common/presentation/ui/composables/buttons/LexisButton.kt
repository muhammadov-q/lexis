package org.example.project.common.presentation.ui.composables.buttons

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.example.project.common.presentation.ui.theme.LexisSpacing
import org.example.project.common.presentation.ui.theme.LexisTypography

@Composable
fun LexisButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    buttonStyle: LexisButtonStyle = LexisButtonStyle.Primary,
    buttonSize: LexisButtonSize = LexisButtonSize.Large
) {

    Button(
        modifier = modifier
            .fillMaxWidth()
            .height(buttonSize.height),
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonStyle.containerColor,
            contentColor = buttonStyle.textColor
        ),
        contentPadding = PaddingValues(horizontal = LexisSpacing.padding_24),
        border = buttonStyle.borderStroke

    ) {
        Text(
            text = text,
            style = LexisTypography.Label,
            color = buttonStyle.textColor
        )
    }
}


@Preview
@Composable
private fun LexisButtonPreview() {
    LexisButton(
        onClick = {},
        text = "Click Me",
        buttonStyle = LexisButtonStyle.Primary
    )
}