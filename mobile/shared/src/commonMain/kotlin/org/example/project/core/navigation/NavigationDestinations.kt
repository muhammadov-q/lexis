package org.example.project.core.navigation

import org.jetbrains.compose.resources.DrawableResource
import lexis.shared.generated.resources.Res
import lexis.shared.generated.resources.ic_camera
import lexis.shared.generated.resources.ic_docs
import lexis.shared.generated.resources.ic_source
import org.jetbrains.compose.resources.StringResource
import lexis.shared.generated.resources.nav_camera
import lexis.shared.generated.resources.nav_documents
import lexis.shared.generated.resources.nav_source

enum class Screen(
    val route: String,
    val title: StringResource,
    val icon : DrawableResource
) {
    CameraView("camera", Res.string.nav_camera, Res.drawable.ic_camera),
    Docs("docs", Res.string.nav_documents, Res.drawable.ic_docs),
    Source("source", Res.string.nav_source, Res.drawable.ic_source),
}
