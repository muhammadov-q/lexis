package org.example.project.feature.camera.data.network

data class PhotoUploadConfig(
    val guestUploadPath: String,
    val authenticatedUploadPath: String,
    val multipartFieldName: String,
)
