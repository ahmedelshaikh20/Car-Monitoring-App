package com.example.carmonitoringapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ReportRequest(
  val videoName: String,
  val timestamp: String,
  val summary: String,
  val analysis: String
)
