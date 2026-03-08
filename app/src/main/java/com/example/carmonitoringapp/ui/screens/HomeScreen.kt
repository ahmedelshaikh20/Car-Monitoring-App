package com.example.carmonitoringapp.ui.screens

import android.graphics.PointF
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
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
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.carmonitoringapp.model.CustomBoundingBox
import com.example.carmonitoringapp.model.PersonDetectionEvent
import com.example.carmonitoringapp.ui.theme.Bg
import com.example.carmonitoringapp.ui.theme.Blue
import com.example.carmonitoringapp.ui.theme.Fill
import com.example.carmonitoringapp.ui.theme.Green
import com.example.carmonitoringapp.ui.theme.Label
import com.example.carmonitoringapp.ui.theme.LabelSecondary
import com.example.carmonitoringapp.ui.theme.LabelTertiary
import com.example.carmonitoringapp.ui.theme.Orange
import com.example.carmonitoringapp.ui.theme.Red
import com.example.carmonitoringapp.ui.theme.Separator
import com.example.carmonitoringapp.ui.theme.Surface
import kotlinx.coroutines.delay

@RequiresApi(Build.VERSION_CODES.O)
@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun InCarMonitoringScreen(
  viewModel: HomeViewModel = hiltViewModel()
) {
  val state = viewModel.state.collectAsState()
  val context = LocalContext.current
  val playerReady = remember { mutableStateOf(false) }
  var elapsedSeconds by remember { mutableIntStateOf(0) }

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

  LaunchedEffect(state.value.isPlaying) {
    if (!state.value.isPlaying) {
      elapsedSeconds = 0
    }
  }

  LaunchedEffect(state.value.isPlaying, exoPlayer) {
    if (state.value.isPlaying && exoPlayer != null) {
      while (state.value.isPlaying) {
        delay(1000)
        elapsedSeconds = (exoPlayer.currentPosition / 1000).toInt()
      }
    }
  }

  DisposableEffect(exoPlayer) {
    onDispose { exoPlayer?.release() }
  }

  LaunchedEffect(exoPlayer) {
    exoPlayer?.addListener(object : Player.Listener {
      override fun onPlaybackStateChanged(playbackState: Int) {
        playerReady.value = playbackState == Player.STATE_READY
      }
    })
  }

  LaunchedEffect(Unit) {
    if (viewModel.state.value.selectedUri == null) {
      val demoUri = Uri.parse("android.resource://${context.packageName}/raw/carpool")
      viewModel.onEvent(HomeEvents.OnVideoSelected(demoUri))
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .statusBarsPadding()
      .background(Bg)
  ) {
    NavBar()
    Column(
      modifier = Modifier
        .weight(1f)
        .verticalScroll(rememberScrollState())
        .padding(bottom = 12.dp)
    ) {
      VideoFeedCard(
        onVideoSelected = { uri -> viewModel.onEvent(HomeEvents.OnVideoSelected(uri)) },
        onCloseVideo = { viewModel.onEvent(HomeEvents.OnCloseVideoClick) },
        exoPlayer = exoPlayer,
        detectedObjects = state.value.boundingBoxes,
        isPlaying = state.value.isPlaying,
        elapsedSeconds = elapsedSeconds
      )
      ActionButtonRow(
        onStartClick = {
          if (exoPlayer != null && playerReady.value) {
            viewModel.onEvent(HomeEvents.OnStartClick(exoPlayer))
          }
        },
        onStopClick = { viewModel.onEvent(HomeEvents.OnStopClick) },
        startButtonEnabled = exoPlayer != null && !state.value.isPlaying,
        stopButtonEnabled = exoPlayer != null && state.value.isPlaying
      )
      AlertBanner(
        isPlaying = state.value.isPlaying,
        hasVideo = state.value.selectedUri != null,
        elapsedSeconds = elapsedSeconds
      )
      SessionSummaryCard(
        events = state.value.currentEvents,
        isPlaying = state.value.isPlaying,
        summary = state.value.currentSummary
      )
    }
  }
}

@Composable
private fun NavBar() {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 20.dp, vertical = 8.dp)
  ) {
    Text(
      text = "In-Car Monitor",
      fontSize = 34.sp,
      fontWeight = FontWeight.Bold,
      color = Label,
      letterSpacing = (-0.5).sp,
      lineHeight = 37.sp
    )
    Text(
      text = "Cabin Safety Analysis",
      fontSize = 15.sp,
      color = LabelSecondary,
      modifier = Modifier.padding(top = 2.dp),
      letterSpacing = (-0.1).sp
    )
  }
}

@Composable
private fun VideoFeedCard(
  onVideoSelected: (Uri) -> Unit,
  onCloseVideo: () -> Unit,
  exoPlayer: ExoPlayer?,
  detectedObjects: List<CustomBoundingBox>,
  isPlaying: Boolean,
  elapsedSeconds: Int,
  modifier: Modifier = Modifier
) {
  val launcher =
    rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
      uri?.let { onVideoSelected(it) }
    }

  Box(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 6.dp)
      .aspectRatio(16f / 9f)
      .clip(RoundedCornerShape(20.dp))
      .background(Color(0xFF1C1C1E))
      .then(
        if (exoPlayer == null) Modifier.clickable {
          launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
        } else Modifier
      )
  ) {
    if (exoPlayer != null) {
      AndroidView(
        factory = { ctx ->
          PlayerView(ctx).apply {
            player = exoPlayer
            useController = false
            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
          }
        },
        modifier = Modifier.matchParentSize()
      )
      DetectionOverlay(
        exoPlayer = exoPlayer,
        faces = detectedObjects,
        modifier = Modifier.matchParentSize()
      )
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(10.dp)
          .align(Alignment.TopCenter),
        contentAlignment = Alignment.CenterStart
      ) {
        if (isPlaying) {
          RecBadge(elapsedSeconds = elapsedSeconds)
        } else {
          Text(
            text = "CAM-01 · CABIN",
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier
              .background(
                Color.Black.copy(alpha = 0.35f),
                RoundedCornerShape(99.dp)
              )
              .padding(horizontal = 9.dp, vertical = 3.dp)
          )
        }
      }
      Box(
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(10.dp)
          .size(28.dp)
          .clip(CircleShape)
          .background(Color.Black.copy(alpha = 0.45f))
          .clickable { onCloseVideo() },
        contentAlignment = Alignment.Center
      ) {
        Text("✕", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
      }
      if (!isPlaying) {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "Ready to analyze",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.5f)
          )
        }
      }
    } else {
      Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        Box(
          modifier = Modifier
            .size(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.1f)),
          contentAlignment = Alignment.Center
        ) {
          Text("▶", fontSize = 20.sp, color = Color.White.copy(alpha = 0.6f))
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
          text = "No Video Source",
          fontSize = 15.sp,
          fontWeight = FontWeight.SemiBold,
          color = Color.White.copy(alpha = 0.85f)
        )
        Text(
          text = "Tap to select a video",
          fontSize = 13.sp,
          color = Color.White.copy(alpha = 0.4f)
        )
      }
    }
  }
}

@Composable
private fun RecBadge(elapsedSeconds: Int) {
  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val alpha by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 0.5f,
    animationSpec = infiniteRepeatable(
      animation = tween(1000),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse"
  )
  Row(
    modifier = Modifier
      .background(
        Red.copy(alpha = 0.75f),
        RoundedCornerShape(99.dp)
      )
      .padding(horizontal = 10.dp, vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(5.dp)
  ) {
    Box(
      modifier = Modifier
        .size(6.dp)
        .clip(CircleShape)
        .background(Color.White.copy(alpha = alpha))
    )
    val fmt = "${(elapsedSeconds / 60).toString().padStart(2, '0')}:${(elapsedSeconds % 60).toString().padStart(2, '0')}"
    Text(
      text = "REC $fmt",
      fontSize = 12.sp,
      fontWeight = FontWeight.SemiBold,
      color = Color.White
    )
  }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
private fun DetectionOverlay(
  exoPlayer: ExoPlayer,
  faces: List<CustomBoundingBox>,
  modifier: Modifier = Modifier
) {
  val driverColor = Green.copy(alpha = 0.7f)
  val passengerColor = Blue.copy(alpha = 0.6f)
  val colors = listOf(driverColor, passengerColor)

  androidx.compose.foundation.Canvas(modifier = modifier) {
    val canvasWidth = size.width
    val canvasHeight = size.height
    val videoWidth = exoPlayer.videoFormat?.width?.toFloat() ?: canvasWidth
    val videoHeight = exoPlayer.videoFormat?.height?.toFloat() ?: canvasHeight
    val scale = minOf(canvasWidth / videoWidth, canvasHeight / videoHeight)
    val scaledVideoWidth = videoWidth * scale
    val scaledVideoHeight = videoHeight * scale
    val offsetX = (canvasWidth - scaledVideoWidth) / 2f
    val offsetY = (canvasHeight - scaledVideoHeight) / 2f

    faces.forEachIndexed { index, box ->
      val left = offsetX + box.x.toFloat() * scale
      val top = offsetY + box.y.toFloat() * scale
      val width = box.width.toFloat() * scale
      val height = box.height.toFloat() * scale
      val color = colors.getOrElse(index) { driverColor }
      drawBounds(
        topLeft = PointF(left, top),
        size = Size(width, height),
        color = color,
        stroke = 2.5f
      )
    }
  }
}

private fun DrawScope.drawBounds(topLeft: PointF, size: Size, color: Color, stroke: Float) {
  drawRect(
    color = color,
    size = size,
    topLeft = Offset(topLeft.x, topLeft.y),
    style = Stroke(width = stroke)
  )
}

@Composable
private fun ActionButtonRow(
  onStartClick: () -> Unit,
  onStopClick: () -> Unit,
  startButtonEnabled: Boolean,
  stopButtonEnabled: Boolean,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 6.dp),
    horizontalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    Box(modifier = Modifier.weight(1f)) {
      IosButton(
        text = "Start",
        onClick = onStartClick,
        enabled = startButtonEnabled,
        isPrimary = true
      )
    }
    Box(modifier = Modifier.weight(1f)) {
      IosButton(
        text = "Stop",
        onClick = onStopClick,
        enabled = stopButtonEnabled,
        isPrimary = false
      )
    }
  }
}

@Composable
private fun IosButton(
  text: String,
  onClick: () -> Unit,
  enabled: Boolean,
  isPrimary: Boolean,
  modifier: Modifier = Modifier
) {
  val bgColor = when {
    isPrimary && enabled -> Green
    isPrimary && !enabled -> Green.copy(alpha = 0.32f)
    !isPrimary && enabled -> Red.copy(alpha = 0.1f)
    else -> Red.copy(alpha = 0.32f)
  }
  val textColor = when {
    isPrimary -> Color.White
    enabled -> Red
    else -> Red.copy(alpha = 0.32f)
  }
  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(54.dp)
      .clip(RoundedCornerShape(14.dp))
      .background(bgColor)
      .clickable(enabled = enabled) { onClick() },
    contentAlignment = Alignment.Center
  ) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      if (isPrimary) {
        Text("▶", fontSize = 12.sp, color = textColor)
      } else {
        Text("■", fontSize = 10.sp, color = textColor)
      }
      Text(
        text = text,
        fontSize = 17.sp,
        fontWeight = FontWeight.SemiBold,
        color = textColor,
        letterSpacing = (-0.3).sp
      )
    }
  }
}

@Composable
private fun AlertBanner(
  isPlaying: Boolean,
  hasVideo: Boolean,
  elapsedSeconds: Int,
  modifier: Modifier = Modifier
) {
  val timeStr = "${(elapsedSeconds / 3600).toString().padStart(2, '0')}:${((elapsedSeconds % 3600) / 60).toString().padStart(2, '0')}:${(elapsedSeconds % 60).toString().padStart(2, '0')}"
  val showWarn = isPlaying && elapsedSeconds > 10
  val showInfo = !isPlaying && hasVideo

  if (showWarn) {
    Row(
      modifier = modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 5.dp)
        .clip(RoundedCornerShape(14.dp))
        .background(Orange.copy(alpha = 0.1f))
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Text("⚠️", fontSize = 18.sp)
      Column {
        Text(
          text = "Phone usage detected",
          fontSize = 13.sp,
          fontWeight = FontWeight.Medium,
          color = Label
        )
        Text(
          text = "Driver alert issued · $timeStr",
          fontSize = 13.sp,
          color = LabelSecondary
        )
      }
    }
  }
  if (showInfo) {
    Row(
      modifier = modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 5.dp)
        .clip(RoundedCornerShape(14.dp))
        .background(Blue.copy(alpha = 0.07f))
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Text("ℹ️", fontSize = 18.sp)
      Text(
        text = "Video loaded — press Start to begin analysis",
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        color = Label
      )
    }
  }
}

@Composable
private fun SessionSummaryCard(
  events: List<PersonDetectionEvent>,
  isPlaying: Boolean,
  summary: String,
  modifier: Modifier = Modifier
) {
  var expanded by remember { mutableStateOf(false) }

  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 6.dp)
  ) {
    Text(
      text = "ANALYSIS REPORT",
      fontSize = 13.sp,
      fontWeight = FontWeight.SemiBold,
      color = LabelSecondary,
      letterSpacing = 0.06.sp,
      modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
    )
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(if (expanded) 20.dp else 20.dp))
        .background(Surface)
    ) {
      Column {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .padding(13.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Box(
            modifier = Modifier
              .size(34.dp)
              .clip(RoundedCornerShape(9.dp))
              .background(Blue.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
          ) {
            Text("📋", fontSize = 16.sp)
          }
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Session Summary",
              fontSize = 15.sp,
              fontWeight = FontWeight.Medium,
              color = Label,
              letterSpacing = (-0.2).sp
            )
            Text(
              text = if (isPlaying) "${events.size} events logged" else "Start analysis to generate report",
              fontSize = 13.sp,
              color = LabelSecondary,
              modifier = Modifier.padding(top = 1.dp)
            )
          }
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            if (isPlaying && events.isNotEmpty()) {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(10.dp))
                  .background(Orange)
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Text(
                  text = events.size.toString(),
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = Color.White
                )
              }
            }
            val rotation by animateFloatAsState(
              targetValue = if (expanded) 90f else 0f,
              label = "chevron"
            )
            Text(
              text = "›",
              fontSize = 13.sp,
              fontWeight = FontWeight.SemiBold,
              color = LabelTertiary,
              modifier = Modifier
                .padding(start = 4.dp)
                .graphicsLayer { rotationZ = rotation }
            )
          }
        }
        AnimatedVisibility(
          visible = expanded,
          enter = expandVertically(),
          exit = shrinkVertically()
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 0.dp)
          ) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Separator)
            )
            if (events.isNotEmpty()) {
              events.forEachIndexed { index, ev ->
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                  horizontalArrangement = Arrangement.spacedBy(10.dp),
                  verticalAlignment = Alignment.Top
                ) {
                  Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 4.dp)
                  ) {
                    Box(
                      modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                          when (ev.detectedAction?.lowercase()) {
                            "smiling", "neutral" -> Green
                            "looking left", "looking right" -> Orange
                            else -> Red
                          }
                        )
                    )
                    if (index < events.size - 1) {
                      Box(
                        modifier = Modifier
                          .width(1.dp)
                          .height(20.dp)
                          .padding(top = 3.dp)
                          .background(Separator)
                      )
                    }
                  }
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = ev.detectedAction ?: "Detection",
                      fontSize = 14.sp,
                      fontWeight = FontWeight.Medium,
                      color = Label
                    )
                    Text(
                      text = ev.timestamp,
                      fontSize = 12.sp,
                      color = LabelSecondary,
                      modifier = Modifier.padding(top = 1.dp)
                    )
                  }
                }
              }
            } else {
              Text(
                text = "No data available",
                fontSize = 14.sp,
                color = LabelTertiary,
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
              )
            }
            Spacer(modifier = Modifier.height(16.dp))
          }
        }
      }
    }
  }
}
