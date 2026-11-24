package org.example.project.feature.vocabulary.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.example.project.feature.camera.data.network.models.GlossDto

@Serializable
data class VocabularyWordDto(
    val id: Long,
    @SerialName("created_at") val createdAt: String,
    val word: String,
    val gloss: GlossDto? = null,
    val note: String = "",
    val source: WordSourceDto,
)

@Serializable
data class WordSourceDto(
    @SerialName("recognition_id") val recognitionId: Long,
    val filename: String,
    val line: Int,
    @SerialName("word_index") val wordIndex: Int,
)

@Serializable
data class AddWordRequestDto(
    @SerialName("recognition_id") val recognitionId: Long,
    @SerialName("word_index") val wordIndex: Int,
    val note: String = "",
)

@Serializable
data class NoteRequestDto(val note: String)
