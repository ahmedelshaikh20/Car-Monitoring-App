package com.example.carmonitoringapp.ui.screens

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carmonitoringapp.model.ReportRequest
import com.example.carmonitoringapp.network.ApiService
import com.example.carmonitoringapp.network.OpenAiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.carmonitoringapp.service.VideoAnalysisService
import com.example.carmonitoringapp.util.PromptFormatter
import io.ktor.client.HttpClient
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType.Application
import io.ktor.http.contentType


@HiltViewModel
class HomeViewModel @Inject constructor(
  private val videoAnalysisService: VideoAnalysisService,
  private val openAiService: OpenAiService,
  private val apiService: ApiService,
) : ViewModel() {

  private val _state = MutableStateFlow(HomeState())
  val state: StateFlow<HomeState> = _state

  @RequiresApi(Build.VERSION_CODES.O)
  fun onEvent(event: HomeEvents) {
    when (event) {
      is HomeEvents.OnStartClick -> {

        _state.value = _state.value.copy(isPlaying = true)
        videoAnalysisService.startDetection(event.player) { personDetectionEvent ->
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
            try {
              updateStatus("Generating report summary...")

              val fullResponse = generateSummary()
              val report = createReport(fullResponse)
              _state.value = _state.value.copy(currentSummary = fullResponse)
              updateStatus("Sending report to server...")

              sendReportToServer(report)

              _state.value = _state.value.copy(
                currentSummary = fullResponse,
                statusMessage = "Report submitted successfully!"
              )

            } catch (e: SocketTimeoutException) {
              updateStatus("Timeout error :Failed to send the report to the server (timeout).")
            } catch (e: Exception) {
              updateStatus("${e.message}")
            }
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
            currentSummary = "There is no summary yet.",
            statusMessage = ""
          )
      }

      is HomeEvents.OnVideoSelected -> {
        _state.value = _state.value.copy(selectedUri = event.uri)
      }
    }

  }


  private fun updateStatus(message: String) {
    _state.value = _state.value.copy(statusMessage = message)
  }

  @RequiresApi(Build.VERSION_CODES.O)
  private suspend fun generateSummary(): String {
    val prompt = PromptFormatter.createRearViewCameraPrompt(_state.value.currentEvents)
    return openAiService.getResponse(prompt)
  }

  @RequiresApi(Build.VERSION_CODES.O)
  private fun createReport(fullResponse: String): ReportRequest {
    val timestamp = java.time.Instant.now().toString()

    val summaryRegex = Regex("""Summary:\s*(.*?)\s*(?=Analysis:)""", RegexOption.DOT_MATCHES_ALL)
    val analysisRegex = Regex("""Analysis:\s*(.*)""", RegexOption.DOT_MATCHES_ALL)

    val summary =
      summaryRegex.find(fullResponse)?.groupValues?.get(1)?.trim() ?: "Summary not found"
    val analysis =
      analysisRegex.find(fullResponse)?.groupValues?.get(1)?.trim() ?: "Analysis not found"

    return ReportRequest(
      videoName = _state.value.selectedUri?.lastPathSegment.toString(),
      timestamp = timestamp,
      summary = summary,
      analysis = analysis
    )
  }

  private suspend fun sendReportToServer(report: ReportRequest) {
    val response = apiService.sendReport(report)
    Log.d("Report sent", response.isSuccess.toString())
  }

}



