package org.example.project.feature.camera.data.network.models

import kotlinx.serialization.Serializable

@Serializable
data class GlossDto(
    val lemma: String? = null,
    val translation: String? = null,
    val definition: String? = null,
)

