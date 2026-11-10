package org.example.project.feature.camera.presentation.composables.modelPicker

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.example.project.feature.camera.domain.model.OcrModel

@Composable
fun ModelPickerDropdown(
    models: List<OcrModel>,
    selectedModelId: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val selectedModel = models.firstOrNull {
        it.id == selectedModelId
    }

    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "dropdownArrow"
    )

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth()
    ) {
        ModelPickerDropdownButton(
            selectedModel = selectedModel,
            expanded = expanded,
            rotation = rotation,
            onClick = { expanded = !expanded }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(maxWidth),
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            shadowElevation = 8.dp
        ) {
            ModelPickerDropdownContent(
                models = models,
                selectedModelId = selectedModelId,
                onSelect = onSelect,
                onDismiss = { expanded = false }
            )
        }
    }
}


@Preview
@Composable
private fun ModelPickerDropdownPreview() {
    MaterialTheme {
        Box(
            modifier = Modifier.padding(16.dp)
        ) {
            ModelPickerDropdown(
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
                selectedModelId = "eparchos",
                onSelect = {}
            )
        }
    }
}
