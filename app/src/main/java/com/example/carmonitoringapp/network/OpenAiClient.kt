package com.example.carmonitoringapp.network

import com.aallam.openai.api.http.Timeout
import com.aallam.openai.client.OpenAI
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds
import com.example.carmonitoringapp.BuildConfig

@Singleton
class OpenAIClient @Inject constructor() {
  val instance: OpenAI by lazy {
    OpenAI(
      token = BuildConfig.OPENAI_API_KEY,
      timeout = Timeout(socket = 60.seconds)
    )
  }
}
