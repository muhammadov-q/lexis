package org.example.project.feature.camera.data.network

data class PhotoUploadConfig(
    val baseUrl: String,
    val guestUploadPath: String,
    val authenticatedUploadPath: String,
    val multipartFieldName: String,
)
