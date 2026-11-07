package org.example.project.app.presentation.composables

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.example.project.common.presentation.ui.theme.ScriptLensColors
import org.example.project.core.navigation.Screen
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun NavigationBottomBar(
    onClick: (Screen) -> Unit,
    currentRoute: String?,
    destinations: List<Screen>,
) {
    NavigationBar(
        containerColor = ScriptLensColors.Surface,
    ) {
        destinations.forEach { screen ->
            NavigationBarItem(
                selected = currentRoute == screen.route,
                onClick = { onClick(screen) },
                icon = {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(screen.icon),
                        contentDescription = "",
                        tint = if (currentRoute == screen.route) ScriptLensColors.Text else ScriptLensColors.Subtext
                    )
                },
                label = {
                    Text(
                        text = stringResource(screen.title),
                        color = ScriptLensColors.Subtext
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = ScriptLensColors.Surface,
                    selectedIconColor = ScriptLensColors.Surface,
                )
            )
        }
    }
}

@Preview
@Composable
private fun NavigationBottomBarPreview() {

    val screens = listOf(Screen.CameraView, Screen.Docs, Screen.Source)
    NavigationBottomBar(
        onClick = {},
        currentRoute = Screen.CameraView.route,
        destinations = screens,
    )
}



