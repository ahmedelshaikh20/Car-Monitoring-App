package com.example.carmonitoringapp.util

import com.example.carmonitoringapp.data.model.PersonDetectionEvent

object PromptFormatter {
  fun createRearViewCameraPrompt(events: List<PersonDetectionEvent>): String {
    return buildString {
      appendLine("You are an in-car AI assistant analyzing passenger behavior from a forward-facing rear-view camera.")
      appendLine("Each event below includes a timestamp, bounding box, and action (e.g., head turn, eye closed, smiling).")
      appendLine("Do not reference person IDs. Make the output human-readable.")
      appendLine()

      events.forEachIndexed { index, event ->
        appendLine("Event ${index + 1}:")
        appendLine("- Time: ${event.timestamp}")
        appendLine("- Box: [x=${event.boundingBox.x}, y=${event.boundingBox.y}, w=${event.boundingBox.width}, h=${event.boundingBox.height}]")
        appendLine("- Action: ${event.detectedAction}")
        appendLine()
      }

      appendLine("Instructions:")
      appendLine("• Write a **one-sentence Summary** describing the key behavior in plain English.")
      appendLine("• Write a **one-sentence Analysis** stating whether there is any risk or distraction.")
      appendLine("• Be clear and concise. Do not list events.")
      appendLine()
      appendLine("Format:")
      appendLine("Summary: <one-line>")
      appendLine("Analysis: <one-line>")
    }
  }
}
