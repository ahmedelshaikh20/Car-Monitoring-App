package com.example.carmonitoringapp.ui.screens

import android.net.Uri
import androidx.media3.exoplayer.ExoPlayer

sealed class HomeEvents(){
  data class OnStartClick(val player: ExoPlayer): HomeEvents()
  object OnStopClick : HomeEvents()
  data class OnVideoSelected(val uri: Uri) : HomeEvents()
  object OnCloseVideoClick : HomeEvents()
}
