package com.example.carmonitoringapp.network

import android.util.Log
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import javax.inject.Inject
import com.example.carmonitoringapp.BuildConfig

class OpenAiService @Inject constructor(
  private val client: OpenAIClient
) {
  suspend fun getResponse(prompt: String): String {
    Log.d("OpenAiService", "Prompt: ${BuildConfig.OPENAI_API_KEY}")
    val messages = listOf(
        ChatMessage(
            role = ChatRole.System,
            content = "You are a helpful AI assistant that summarizes in-cabin driver and passengers behavior based on detection events."
        ),
        ChatMessage(role = ChatRole.User, content = prompt)
    )

    val request = ChatCompletionRequest(
        model = ModelId("gpt-3.5-turbo"),
        messages = messages,
        temperature = 0.7
    )

    Log.d("OpenAiService", "Sending request: $request")
    return try {
      val response = client.instance.chatCompletion(request)
      response.choices.firstOrNull()?.message?.content?.trim()
        ?: "No response from AI."
    } catch (e: Exception) {
      throw Exception("Error contacting AI:wrong API key or network error.")
    }
  }

}
