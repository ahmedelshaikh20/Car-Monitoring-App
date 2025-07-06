package com.example.carmonitoringapp.data.model

import com.google.mlkit.vision.face.Face

data class PersonDetectionEvent(
  val timestamp: String,
  val personId: String,
  val boundingBox: CustomBoundingBox,
  val detectedAction: String? = null
)



