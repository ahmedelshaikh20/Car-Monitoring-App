package com.example.carmonitoringapp.service

import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetector
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FaceDetectionService @Inject constructor(
  val faceDetector: FaceDetector
) {
  suspend fun detect(image: InputImage): List<Face> {
    return try {
      Log.d("FaceDetection", "Processing image: ${image.width}x${image.height}")
      val all = faceDetector.process(image).await()
      Log.d("FaceDetection", "Total faces detected: ${all}")
      all
    } catch (e: Exception) {
      Log.e("FaceDetection", "Detection error", e)
      emptyList()
    }
  }


}
