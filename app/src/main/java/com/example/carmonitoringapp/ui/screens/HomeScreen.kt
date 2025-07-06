package com.example.carmonitoringapp.ui.screens

import android.graphics.PointF
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.carmonitoringapp.data.model.CustomBoundingBox
import com.example.carmonitoringapp.ui.components.CustomButton
import com.example.carmonitoringapp.ui.components.CustomSummaryBox

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun InCarMonitoringScreen(
  viewModel: HomeViewModel = hiltViewModel()
) {
  val state = viewModel.state.collectAsState()
  val context = LocalContext.current


  val exoPlayer = remember(state.value.selectedUri) {
    val trackSelector = DefaultTrackSelector(context)
    val parameters = trackSelector
      .buildUponParameters()
      .setMaxVideoFrameRate(10).setForceLowestBitrate(true)
    trackSelector.setParameters(parameters)

    state.value.selectedUri?.let { uri ->
      ExoPlayer.Builder(context).setTrackSelector(trackSelector).build().apply {
        val mediaItem = MediaItem.fromUri(uri)
        val dataSourceFactory = DefaultDataSource.Factory(context)
        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
          .createMediaSource(mediaItem)
        setMediaSource(mediaSource)
        prepare()
        playWhenReady = state.value.isPlaying
      }
    }
  }
  LaunchedEffect(state.value.isPlaying) {
    exoPlayer?.playWhenReady = state.value.isPlaying
  }
  DisposableEffect(exoPlayer) {
    onDispose { exoPlayer?.release() }
  }

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
        onVideoSelected = { uri -> viewModel.onEvent(HomeEvents.OnVideoSelected(uri)) },
        onCloseVideo = { viewModel.onEvent(HomeEvents.OnCloseVideoClick) },
        modifier = Modifier
          .weight(1f)
          .padding(10.dp),
        exoPlayer = exoPlayer,
        detectedObjects = state.value.boundingBoxes
      )
      ActionButtonRow(
        onStartClick = { viewModel.onEvent(HomeEvents.OnStartClick(exoPlayer)) },
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
  exoPlayer: ExoPlayer?,
  detectedObjects: List<CustomBoundingBox>,
  modifier: Modifier = Modifier,
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
      .then(
        if (exoPlayer == null) Modifier.clickable {
          launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
        } else Modifier
      ),
    contentAlignment = Alignment.Center
  ) {
    if (exoPlayer != null) {
      ShowVideo(exoPlayer)
      DetectionOverlay(
        exoPlayer = exoPlayer,
        faces = detectedObjects,
        modifier = Modifier.matchParentSize()
      )
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
  exoPlayer: ExoPlayer,
  modifier: Modifier = Modifier
) {
  AndroidView(
    factory = { ctx ->
      PlayerView(ctx).apply {
        player = exoPlayer
        useController = false
        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
        setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
      }
    },
    modifier = modifier
  )

}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun DetectionOverlay(
  exoPlayer: ExoPlayer,
  faces: List<CustomBoundingBox>,
  modifier: Modifier = Modifier
) {
  Canvas(modifier = modifier) {
    val canvasWidth = size.width
    val canvasHeight = size.height

    val videoWidth = exoPlayer.videoFormat?.width?.toFloat() ?: canvasWidth
    val videoHeight = exoPlayer.videoFormat?.height?.toFloat() ?: canvasHeight

    // Calculate scale to fit video into canvas (preserve aspect ratio)
    val scale = minOf(canvasWidth / videoWidth, canvasHeight / videoHeight)

    val scaledVideoWidth = videoWidth * scale
    val scaledVideoHeight = videoHeight * scale

    // Center the video inside the canvas (account for black borders)
    val offsetX = (canvasWidth - scaledVideoWidth) / 2f
    val offsetY = (canvasHeight - scaledVideoHeight) / 2f

    faces.forEach { box ->

      val boundingBox = box
      val left = offsetX + boundingBox.x * scale
      val top = offsetY + boundingBox.y * scale
      val width = boundingBox.width * scale
      val height = boundingBox.height * scale

      drawBounds(
        topLeft = PointF(left, top),
        size = Size(width, height),
        color = Color.Yellow,
        stroke = 8f
      )
    }
  }
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

fun DrawScope.drawBounds(topLeft: PointF, size: Size, color: Color, stroke: Float) {
  drawRect(
    color = color,
    size = size,
    topLeft = Offset(topLeft.x, topLeft.y),
    style = Stroke(width = stroke)
  )
}


//@Preview
//@Composable
//fun PreviewMainScreen() {
//  InCarMonitoringScreen(onStartClick = {}, onStopClick = {}, summaryText = "Summary Text")
//}
