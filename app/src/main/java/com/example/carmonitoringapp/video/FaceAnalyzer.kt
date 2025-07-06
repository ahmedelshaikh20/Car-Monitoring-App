package com.example.carmonitoringapp.video

import android.graphics.Bitmap
import com.example.carmonitoringapp.service.FaceDetectionService
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import javax.inject.Inject

class FaceAnalyzer @Inject constructor(
    private val faceDetectionService: FaceDetectionService
) {
    private val rotations = listOf(0, 90, 180, 270)

    suspend fun detectBestRotation(bitmap: Bitmap): List<Face> {
        val rotationResults = rotations.map { rotation ->
            val input = InputImage.fromBitmap(bitmap, rotation)
            faceDetectionService.detect(input)
        }

        return rotationResults.maxByOrNull { it.size } ?: emptyList()
    }
}
