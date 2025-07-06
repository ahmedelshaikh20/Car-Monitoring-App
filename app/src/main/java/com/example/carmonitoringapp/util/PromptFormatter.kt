package com.example.carmonitoringapp.util

import com.example.carmonitoringapp.data.model.PersonDetectionEvent

object PromptFormatter {
  fun createRearViewCameraPrompt(events: List<PersonDetectionEvent>): String {
    return buildString {
      appendLine("You are an in-car generative AI assistant analyzing behavior from a rear-view camera mounted between the driver and co-driver, facing forward.")
      appendLine("Below is a time-ordered list of person detection events captured by an on-device ML model.")
      appendLine("Each event includes a timestamp, bounding box, and detected action (e.g., head movement, facial expression).")
      appendLine()

      events.forEachIndexed { index, event ->
        appendLine("Event ${index + 1}:")
        appendLine("- Timestamp: ${event.timestamp}")
        appendLine("- Person ID: ${event.personId}")
        appendLine("- Bounding Box: [x=${event.boundingBox.x}, y=${event.boundingBox.y}, width=${event.boundingBox.width}, height=${event.boundingBox.height}]")
        appendLine("- Detected Action: ${event.detectedAction ?: "neutral"}")
        appendLine()
      }

      appendLine("Instructions:")
      appendLine("• Count how many unique persons are visible. If possible, estimate seat (driver or co-driver).")
      appendLine("• Generate a short bullet-point **Summary** of key observations.")
      appendLine("• Follow with a short bullet-point **Analysis** focusing on behavioral trends, distraction, and safety.")
      appendLine("• Keep it concise — each bullet point should be one sentence max.")
      appendLine()
      appendLine("Format your output like this:")
      appendLine()
      appendLine("Summary:")
      appendLine("• <bullet 1>")
      appendLine("• <bullet 2>")
      appendLine()
      appendLine("Analysis:")
      appendLine("• <bullet 1>")
      appendLine("• <bullet 2>")
    }
  }

}
