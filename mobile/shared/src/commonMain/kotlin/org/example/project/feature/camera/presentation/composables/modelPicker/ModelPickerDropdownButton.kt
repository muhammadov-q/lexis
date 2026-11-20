package org.example.project.feature.camera.presentation.composables.modelPicker

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.example.project.common.presentation.ui.theme.LexisColors
import org.example.project.feature.camera.domain.model.OcrModel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import lexis.shared.generated.resources.Res
import lexis.shared.generated.resources.ic_arrow_down
import lexis.shared.generated.resources.model_select
import lexis.shared.generated.resources.model_select_description

@Composable
internal fun ModelPickerDropdownButton(
    selectedModel: OcrModel?,
    expanded: Boolean,
    rotation: Float,
    onClick: () -> Unit
) {
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            1.dp,
            if (expanded) {
                LexisColors.Accent.copy(alpha = 0.7f)
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }
        ),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (expanded) {
                LexisColors.Accent.copy(alpha = 0.05f)
            } else {
                LexisColors.Surface
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = selectedModel?.name ?: stringResource(Res.string.model_select),
                    style = MaterialTheme.typography.bodyLarge
                )

                Text(
                    text = selectedModel?.description.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                painter = painterResource(Res.drawable.ic_arrow_down),
                contentDescription = stringResource(Res.string.model_select_description),
                modifier = Modifier.rotate(rotation),
                tint = if (expanded) {
                    LexisColors.Accent
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ModelPickerDropdownButtonPreview() {
    MaterialTheme {
        ModelPickerDropdownButton(
            selectedModel = OcrModel(
                id = "eparchos",
                name = "Eparchos",
                description = "Recognizes Greek manuscripts"
            ),
            expanded = false,
            rotation = 0f,
            onClick = {}
        )
    }
}
