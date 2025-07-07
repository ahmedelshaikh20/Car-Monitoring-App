package com.example.carmonitoringapp.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carmonitoringapp.data.model.ReportRequest
import com.example.carmonitoringapp.network.OpenAiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.carmonitoringapp.service.VideoAnalysisService
import com.example.carmonitoringapp.util.PromptFormatter
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType.Application
import io.ktor.http.contentType


@HiltViewModel
class HomeViewModel @Inject constructor(
  private val videoAnalysisService: VideoAnalysisService,
  private val openAiService: OpenAiService,
  private val client: HttpClient
) : ViewModel() {

  private val _state = MutableStateFlow(HomeState())
  val state: StateFlow<HomeState> = _state

  @RequiresApi(Build.VERSION_CODES.O)
  fun onEvent(event: HomeEvents) {
    when (event) {
      is HomeEvents.OnStartClick -> {
        _state.value = _state.value.copy(isPlaying = true)
        videoAnalysisService.startDetection(event.player!!) { personDetectionEvent ->
          _state.value =
            _state.value.copy(
              boundingBoxes = personDetectionEvent.map { it.boundingBox },
              currentEvents = _state.value.currentEvents + personDetectionEvent
            )
        }
      }

      is HomeEvents.OnStopClick -> {
        _state.value = _state.value.copy(isPlaying = false)
        if (_state.value.currentEvents.isNotEmpty()) {
          viewModelScope.launch {
            val fullResponse = openAiService.getResponse(
              PromptFormatter.createRearViewCameraPrompt(_state.value.currentEvents)
            )

            val timestamp = java.time.Instant.now().toString()

            val summaryRegex =
              Regex("""Summary:\s*(.*?)\s*(?=Analysis:)""", RegexOption.DOT_MATCHES_ALL)
            val analysisRegex = Regex("""Analysis:\s*(.*)""", RegexOption.DOT_MATCHES_ALL)

            val summary =
              summaryRegex.find(fullResponse)?.groupValues?.get(1)?.trim() ?: "Summary not found"
            val analysis =
              analysisRegex.find(fullResponse)?.groupValues?.get(1)?.trim() ?: "Analysis not found"


            val report = ReportRequest(
              videoName = _state.value.selectedUri?.lastPathSegment.toString(),
              timestamp = timestamp,
              summary = summary,
              analysis = analysis
            )

            try {
              val response = client.post("http://192.168.0.240:8000/submit-data") {
                contentType(Application.Json)
                setBody(report)
              }

              val responseText = response.bodyAsText()
              println("Report sent: $responseText")

            } catch (e: Exception) {
              println("Error sending report: ${e.message}")
            }

            _state.value = _state.value.copy(currentSummary = fullResponse)
          }
        }
      }

      HomeEvents.OnCloseVideoClick -> {
        _state.value =
          _state.value.copy(
            selectedUri = null,
            isPlaying = false,
            currentEvents = emptyList(),
            boundingBoxes = emptyList(),
            currentSummary = "There is no summary yet."
          )
      }

      is HomeEvents.OnVideoSelected -> {
        _state.value = _state.value.copy(selectedUri = event.uri)
      }
    }

  }


}


