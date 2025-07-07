package com.example.carmonitoringapp.tlf

import android.graphics.Bitmap
import android.util.Log
import com.example.carmonitoringapp.model.CustomBoundingBox
import com.example.carmonitoringapp.model.PersonDetectionEvent
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.Rot90Op

import org.tensorflow.lite.task.vision.detector.ObjectDetector
import javax.inject.Inject

class TFLitePersonDetector @Inject constructor(
  val objectDetector: ObjectDetector
) {
  fun detectPersons(
    bitmap: Bitmap,
    rotation: Int,
    timestamp: String
  ): List<PersonDetectionEvent> {
    val imageProcessor = ImageProcessor.Builder()
      .add(Rot90Op(-rotation / 90))
      .build()

    val tensorImage = imageProcessor.process(TensorImage.fromBitmap(bitmap))
    val detections = objectDetector.detect(tensorImage) ?: emptyList()
    Log.d("TFLitePersonDetector", "Detected ${detections} persons")
    return detections.filter { it.categories[0].label == "person" }.mapIndexed { index, detection ->
      val box = detection.boundingBox
      PersonDetectionEvent(
        timestamp = timestamp,
        personId = "tflite_person_$index",
        boundingBox = CustomBoundingBox(
          x = box.left.toInt(),
          y = box.top.toInt(),
          width = box.width().toInt(),
          height = box.height().toInt()
        ),
        detectedAction = "unknown"
      )
    }
  }
}
