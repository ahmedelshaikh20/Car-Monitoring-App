package com.example.carmonitoringapp.service

import android.graphics.Rect
import android.util.Log
import androidx.media3.exoplayer.ExoPlayer
import com.example.carmonitoringapp.data.model.CustomBoundingBox
import com.example.carmonitoringapp.data.model.PersonDetectionEvent
import com.example.carmonitoringapp.tlf.TFLitePersonDetector
import com.example.carmonitoringapp.util.TimeStampUtils
import com.example.carmonitoringapp.video.FaceAnalyzer
import com.example.carmonitoringapp.video.VideoFrameExtractor
import com.google.mlkit.vision.face.Face
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class VideoAnalysisService @Inject constructor(
  private val extractor: VideoFrameExtractor,
  private val faceAnalyzer: FaceAnalyzer,
  private val tfDetector: TFLitePersonDetector,
) {
  private var detectionJob: Job? = null

  fun startDetection(
    player: ExoPlayer,
    onResult: (List<PersonDetectionEvent>) -> Unit
  ) {
    detectionJob = CoroutineScope(Dispatchers.Default).launch {
      val videoUri = withContext(Dispatchers.Main) {
        player.currentMediaItem?.localConfiguration?.uri
      }
      if (videoUri == null) {
        Log.e("VideoFrameExtraction", "No video URI available")
        return@launch
      }

      extractor.init(videoUri)

      while (isActive) {
        val isPlaying = withContext(Dispatchers.Main) { player.isPlaying }
        if (!isPlaying) break

        val currentPosition = withContext(Dispatchers.Main) { player.currentPosition }
        val frame = extractor.extractFrameAt(currentPosition)

        if (frame != null && !frame.isRecycled) {
          val timestamp = TimeStampUtils.formatTimestamp(currentPosition)

          val (persons , faces) = awaitAll(
          // TFLite person detection
          async { tfDetector.detectPersons(frame, 0, timestamp) },

          // ML Kit face analysis
          async { faceAnalyzer.detectBestRotation(frame) }
          )
          val events = (persons as List<PersonDetectionEvent>).map { person ->
            val matchingFace = (faces as List<Face>).find { face ->
              iou(face.boundingBox, person.boundingBox.toRect()) > 0.5
            }


            val action = matchingFace?.let {
              when {
                it.headEulerAngleY > 20 -> "looking right"
                it.headEulerAngleY < -20 -> "looking left"
                it.smilingProbability != null && it.smilingProbability!! > 0.7 -> "smiling"
                else -> "neutral"
              }
            } ?: "unknown"

            person.copy(detectedAction = action)
          }

          withContext(Dispatchers.Main) {
            onResult(events)
          }

          frame.recycle()
        }

        delay(150)
      }
    }
  }

  fun stopDetection() {
    detectionJob?.cancel()
    detectionJob = null
    extractor.release()
  }

  private fun iou(a: Rect, b: Rect): Float {
    val intersection = Rect().apply { setIntersect(a, b) }
    val intersectionArea = intersection.width() * intersection.height()
    val unionArea = a.width() * a.height() + b.width() * b.height() - intersectionArea
    println("Intersection: $intersectionArea, Union: $unionArea")
    return if (unionArea > 0) (intersectionArea.toFloat() / unionArea) * 100 else 0f
  }
}

fun CustomBoundingBox.toRect(): Rect {
  return Rect(
    x,
    y,
    x + width,
    y + height
  )
}
