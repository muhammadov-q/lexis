package org.example.project.common.presentation.ui.composables.reading

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import lexis.shared.generated.resources.Res
import lexis.shared.generated.resources.machine_output
import org.example.project.common.presentation.ui.theme.LexisColors
import org.jetbrains.compose.resources.stringResource
import org.example.project.common.presentation.ui.theme.LexisTypography

@Composable
internal fun MachineOutputChip(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(LexisColors.Success),
        )
        Text(text = stringResource(Res.string.machine_output), style = LexisTypography.Annotation)
    }
}

@Preview(showBackground = true)
@Composable
private fun MachineOutputChipPreview() {
    MaterialTheme {
        MachineOutputChip()
    }
}
