package org.example.project.common.presentation.ui.composables.worddetails

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import lexis.shared.generated.resources.Res
import lexis.shared.generated.resources.ic_arrow_down
import org.example.project.common.presentation.ui.theme.LexisColors
import org.jetbrains.compose.resources.painterResource

internal enum class StepDirection(val rotation: Float) {
    Previous(90f),
    Next(-90f),
}

private val StepButtonShape = RoundedCornerShape(8.dp)

@Composable
internal fun WordStepButton(
    direction: StepDirection,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(StepButtonShape)
            .border(1.dp, LexisColors.Border, StepButtonShape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_arrow_down),
            contentDescription = null,
            tint = if (enabled) LexisColors.Text else LexisColors.Border,
            modifier = Modifier.size(16.dp).rotate(direction.rotation),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WordStepButtonPreview() {
    MaterialTheme {
        WordStepButton(
            direction = StepDirection.Next,
            description = "Next word",
            enabled = true,
            onClick = {},
        )
    }
}
