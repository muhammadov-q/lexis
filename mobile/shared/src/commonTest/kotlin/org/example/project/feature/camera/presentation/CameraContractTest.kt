package org.example.project.feature.camera.presentation

import org.example.project.feature.camera.presentation.models.CameraScreenState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class CameraContractTest {
    @Test
    fun cameraContractStartsWithoutPhotoOrPrediction() {
        val contract = CameraContract()

        assertEquals(CameraScreenState.NoPhotoCaptured, contract.cameraScreenState)
        assertNull(contract.photo)
        assertNull(contract.prediction)
        assertFalse(contract.isLoading)
    }
}