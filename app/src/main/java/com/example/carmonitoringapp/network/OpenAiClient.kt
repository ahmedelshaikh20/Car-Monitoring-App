package com.example.carmonitoringapp.network

import com.aallam.openai.api.http.Timeout
import com.aallam.openai.client.OpenAI
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds


// I know leaving the key like this is not correct but all for the sake to get into BMW
// (i set limit for it too dw:)))))

@Singleton
class OpenAIClient @Inject constructor() {
  val instance: OpenAI by lazy {
    OpenAI(
      token = System.getenv("OPENAI_API_KEY")
        ?: "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",
      timeout = Timeout(socket = 60.seconds)
    )
  }
}
