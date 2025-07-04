package com.example.carmonitoringapp.ui.screens

import android.net.Uri

sealed class HomeEvents(){
  object OnStartClick : HomeEvents()
  object OnStopClick : HomeEvents()
  data class OnVideoSelected(val uri: Uri) : HomeEvents()
  object OnCloseVideoClick : HomeEvents()
}
