package com.example.carmonitoringapp.ui.screens

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.carmonitoringapp.ui.components.CustomButton
import com.example.carmonitoringapp.ui.components.CustomSummaryBox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InCarMonitoringScreen(
  viewModel: HomeViewModel = hiltViewModel(),
) {
  val state = viewModel.state.collectAsState()
  Scaffold(
    topBar = { MainTopBar() }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .background(MaterialTheme.colorScheme.background),
      verticalArrangement = Arrangement.SpaceBetween,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      VideoFeedPlaceholder(
        selectedUri = state.value.selectedUri,
        onVideoSelected = { uri -> viewModel.onEvent(HomeEvents.OnVideoSelected(uri)) },
        onCloseVideo = { viewModel.onEvent(HomeEvents.OnCloseVideoClick) },
        modifier = Modifier
          .weight(1f)
          .padding(10.dp)
      )
      ActionButtonRow(
        onStartClick = { viewModel.onEvent(HomeEvents.OnStartClick) },
        onStopClick = { viewModel.onEvent(HomeEvents.OnStopClick) }
      )
      CustomSummaryBox(text = state.value.currentSummary, modifier = Modifier.padding(10.dp))
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopBar() {
  TopAppBar(
    title = {
      Text("In-Car Monitoring", color = Color.White)
    },
    colors = TopAppBarDefaults.topAppBarColors(
      containerColor = Color(0xFF4CAF50),
      titleContentColor = Color.White
    )
  )
}

@Composable
fun VideoFeedPlaceholder(
  onVideoSelected: (Uri) -> Unit,
  onCloseVideo: () -> Unit,
  modifier: Modifier = Modifier,
  selectedUri: Uri? = null,
) {

  val launcher =
    rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
      Log.d("VideoFeedPlaceholder", "Selected URI: $uri")
      uri?.let { onVideoSelected(it) }
    }

  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(200.dp)
      .clip(RoundedCornerShape(12.dp))
      .background(Color.Black)
      .clickable { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)) },
    contentAlignment = Alignment.Center
  ) {
    if (selectedUri != null) {
      ShowVideo(selectedUri)
      Box(
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(8.dp)
          .size(24.dp)
          .clip(CircleShape)
          .background(Color.White.copy(alpha = 0.8f))
          .clickable { onCloseVideo() },
        contentAlignment = Alignment.Center
      ) {
        Text("X", color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold)
      }
    } else {
      Text("Tap to Upload Video", color = Color.White, fontSize = 16.sp)
    }
  }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun ShowVideo(
  uri: Uri,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current

  // Build & remember the player for this URI
  val exoPlayer = remember(uri) {
    ExoPlayer.Builder(context).build().apply {
      val mediaItem = MediaItem.fromUri(uri)
      val dataSourceFactory = DefaultDataSource.Factory(context)
      val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
        .createMediaSource(mediaItem)

      setMediaSource(mediaSource)
      prepare()
      playWhenReady = true

    }
  }

  /** Pause when app is backgrounded, resume when foregrounded */
  DisposableEffect(lifecycleOwner, exoPlayer) {
    val observer = LifecycleEventObserver { _, event ->
      when (event) {
        Lifecycle.Event.ON_PAUSE -> exoPlayer.playWhenReady = false
        Lifecycle.Event.ON_RESUME -> exoPlayer.playWhenReady = true
        else -> Unit
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
      exoPlayer.release()
    }
  }

  /** The actual view wrapper */
  AndroidView(
    factory = { ctx ->
      PlayerView(ctx).apply {
        player = exoPlayer
        useController = true
        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
        setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
      }
    },
    modifier = modifier
  )
}


@Composable
fun ActionButtonRow(
  onStartClick: () -> Unit,
  onStopClick: () -> Unit
) {
  Row(
    Modifier
      .fillMaxWidth()
      .padding(16.dp),
    horizontalArrangement = Arrangement.SpaceEvenly
  ) {
    CustomButton(text = "Start", onClick = onStartClick, backgroundColor = Color(0xFF4CAF50))
    CustomButton(text = "Stop", onClick = onStopClick, backgroundColor = Color(0xFFF44336))
  }
}


//@Preview
//@Composable
//fun PreviewMainScreen() {
//  InCarMonitoringScreen(onStartClick = {}, onStopClick = {}, summaryText = "Summary Text")
//}
