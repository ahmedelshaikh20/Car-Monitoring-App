package com.example.carmonitoringapp.model

data class PersonDetectionEvent(
  val timestamp: String,
  val personId: String,
  val boundingBox: CustomBoundingBox,
  val detectedAction: String? = null
)
