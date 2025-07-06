package com.example.carmonitoringapp.di

import android.content.Context
import android.util.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.tensorflow.lite.task.core.BaseOptions
import javax.inject.Singleton
import org.tensorflow.lite.task.vision.detector.ObjectDetector


@Module
@InstallIn(SingletonComponent::class)
object ObjectDetectorModule {

  @Provides
  @Singleton
  fun provideObjectDetector(
    @ApplicationContext context: Context
  ): ObjectDetector {
    val modelName = "1.tflite"
    val threshold = 0.5f
    val maxResults = 6
    val baseOptions = BaseOptions.builder().setNumThreads(2).build()


    val options = ObjectDetector.ObjectDetectorOptions.builder()
      .setScoreThreshold(threshold)
      .setMaxResults(maxResults)
      .setBaseOptions(baseOptions)
      .build()

    return try {
      ObjectDetector.createFromFileAndOptions(context, modelName, options)
    } catch (e: Exception) {
      Log.e("ObjectDetectorModule", "Failed to load model: ${e.message}")
      throw RuntimeException("ObjectDetector initialization failed: ${e.message}", e)
    }
  }
}
