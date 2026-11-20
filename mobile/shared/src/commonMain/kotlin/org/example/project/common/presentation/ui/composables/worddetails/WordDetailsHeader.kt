package org.example.project.common.presentation.ui.composables.worddetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import lexis.shared.generated.resources.Res
import lexis.shared.generated.resources.word_title
import org.example.project.common.presentation.ui.composables.reading.MachineOutputChip
import org.jetbrains.compose.resources.stringResource
import org.example.project.common.presentation.ui.theme.LexisTypography

@Composable
internal fun WordDetailsHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(Res.string.word_title),
            style = LexisTypography.Annotation.copy(fontSize = 14.sp),
        )
        MachineOutputChip()
    }
}

@Preview(showBackground = true)
@Composable
private fun WordDetailsHeaderPreview() {
    MaterialTheme {
        WordDetailsHeader()
    }
}
