package com.example.carmonitoringapp.ui.screens

import android.net.Uri

data class HomeState(
  val selectedUri: Uri? = null,
  val currentSummary : String =""
)
