package com.example.carmonitoringapp.network

import com.example.carmonitoringapp.model.ReportRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType.Application
import io.ktor.http.contentType
import javax.inject.Inject

class ApiService @Inject constructor(
  private val client: HttpClient
) {

  suspend fun sendReport(report: ReportRequest): Result<String> {
    return try {
      val response = client.post("http://192.168.0.240:8000/submit-data") {
        contentType(Application.Json)
        setBody(report)
      }
      val body = response.bodyAsText()
      Result.success(body)
    } catch (e: Exception) {
      throw Exception("Failed to send report to the Server")
    }
  }
}
