package com.example.carmonitoringapp.ui.screens

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

  private val _state = MutableStateFlow(HomeState())
  val state: StateFlow<HomeState> = _state

  fun onEvent(event: HomeEvents) {
    when (event) {

      is HomeEvents.OnStartClick -> {
        //TODO:Start PLAYING
      }

      is HomeEvents.OnStopClick -> {
        //TODO: Stop PLAYING
      }

      HomeEvents.OnCloseVideoClick ->{
        _state.value = _state.value.copy(selectedUri = null)
      }
      is HomeEvents.OnVideoSelected -> {
        _state.value = _state.value.copy(selectedUri = event.uri)
      }
    }

  }


}
