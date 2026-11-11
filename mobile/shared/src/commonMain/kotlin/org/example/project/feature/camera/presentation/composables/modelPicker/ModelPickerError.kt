package org.example.project.feature.camera.presentation.composables.modelPicker

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import scriptlens.shared.generated.resources.Res
import scriptlens.shared.generated.resources.ic_docs
import scriptlens.shared.generated.resources.ic_exclamation
import scriptlens.shared.generated.resources.model_default_fallback
import scriptlens.shared.generated.resources.model_recognition_title
import scriptlens.shared.generated.resources.model_reload

@Composable
fun ModelPickerError(
    isLoading: Boolean,
    onReload: () -> Unit,
    modifier: Modifier = Modifier
) {
    val errorColor = MaterialTheme.colorScheme.error

    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, errorColor.copy(alpha = 0.6f)),
        colors = CardDefaults.outlinedCardColors(
            containerColor = errorColor.copy(alpha = 0.06f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_exclamation),
                contentDescription = null,
                tint = errorColor
            )

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(64.dp)
                    .background(errorColor.copy(alpha = 0.3f))
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(Res.string.model_recognition_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = errorColor
                )

                Text(
                    text = stringResource(Res.string.model_default_fallback),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            OutlinedButton(
                onClick = onReload,
                enabled = !isLoading,
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, errorColor),
                contentPadding = PaddingValues(
                    horizontal = 12.dp,
                    vertical = 8.dp
                )
            ) {
                Text(
                    text = stringResource(Res.string.model_reload),
                    color = errorColor
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ModelPickerErrorPreview() {
    MaterialTheme {
        ModelPickerError(
            isLoading = false,
            onReload = {}
        )
    }
}
