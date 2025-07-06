package com.example.carmonitoringapp.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carmonitoringapp.network.OpenAiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.carmonitoringapp.service.VideoAnalysisService
import com.example.carmonitoringapp.util.PromptFormatter


@HiltViewModel
class HomeViewModel @Inject constructor(
  private val videoAnalysisService: VideoAnalysisService,
  private val openAiService: OpenAiService
) : ViewModel() {

  private val _state = MutableStateFlow(HomeState())
  val state: StateFlow<HomeState> = _state

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
        viewModelScope.launch {
          val res =
            openAiService.getResponse(PromptFormatter.createRearViewCameraPrompt(_state.value.currentEvents))
          _state.value = _state.value.copy(currentSummary = res)

        }
        _state.value = _state.value.copy(isPlaying = false)
        videoAnalysisService.stopDetection()
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


