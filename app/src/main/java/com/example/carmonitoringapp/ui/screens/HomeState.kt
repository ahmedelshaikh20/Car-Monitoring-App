package com.example.carmonitoringapp.ui.screens


import android.net.Uri
import com.example.carmonitoringapp.model.CustomBoundingBox
import com.example.carmonitoringapp.model.PersonDetectionEvent

data class HomeState(
  val selectedUri: Uri? = null,
  val currentSummary: String = "There is no summary yet.",
  val isPlaying: Boolean = false,
  val boundingBoxes: List<CustomBoundingBox> = emptyList(),
  val currentEvents: List<PersonDetectionEvent> = emptyList(),
  val statusMessage: String = ""
)
