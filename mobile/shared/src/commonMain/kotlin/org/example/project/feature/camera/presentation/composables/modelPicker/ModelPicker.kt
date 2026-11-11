package org.example.project.feature.camera.presentation.composables.modelPicker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.example.project.common.presentation.ui.theme.ScriptLensSpacing
import org.example.project.feature.camera.domain.model.OcrModel
import org.example.project.feature.camera.presentation.CameraContract
import org.jetbrains.compose.resources.stringResource
import scriptlens.shared.generated.resources.Res
import scriptlens.shared.generated.resources.model_recognition_title

@Composable
fun ModelPicker(
    state: CameraContract,
    onSelect: (String) -> Unit,
    onReload: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = ScriptLensSpacing.padding_8),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(Res.string.model_recognition_title),
            style = MaterialTheme.typography.titleSmall
        )

        when {
            state.modelsLoading -> {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            state.modelsError || state.models.isEmpty() -> {
                ModelPickerError(
                    isLoading = state.isLoading,
                    onReload = onReload
                )
            }

            else -> {
                ModelPickerDropdown(
                    models = state.models,
                    selectedModelId = state.selectedModel,
                    onSelect = onSelect,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ModelPickerPreview() {
    MaterialTheme {
        ModelPicker(
            state = CameraContract(
                models = listOf(
                    OcrModel(
                        id = "eparchos",
                        name = "Eparchos",
                        description = "Recognizes Greek manuscripts",
                    ),
                    OcrModel(
                        id = "kraken",
                        name = "Kraken",
                        description = "General handwriting recognition",
                    ),
                ),
                selectedModel = "eparchos",
                modelsLoading = false,
            ),
            onSelect = {},
            onReload = {},
        )
    }
}
