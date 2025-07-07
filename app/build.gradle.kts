import com.android.build.api.dsl.Packaging

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.compose)
  id("kotlin-kapt")
  id("dagger.hilt.android.plugin")
  id ("kotlinx-serialization")
}

android {
  namespace = "com.example.carmonitoringapp"
  compileSdk = 35

  defaultConfig {
    applicationId = "com.example.carmonitoringapp"
    minSdk = 24
    targetSdk = 35
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }
  defaultConfig {
    buildConfigField("String", "OPENAI_API_KEY", "\"${project.properties["OPENAI_API_KEY"]}\"")
  }
  android.buildFeatures.buildConfig = true
  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  kotlinOptions {
    jvmTarget = "11"
  }
  buildFeatures {
    compose = true
    mlModelBinding = true
  }
  packaging {
    resources {
      excludes += setOf(
        "META-INF/*.kotlin_module",
        "META-INF/atomicfu.kotlin_module",
        "META-INF/kotlinx-io.kotlin_module",
        "META-INF/kotlinx-coroutines-core.kotlin_module"
      )
    }
  }

}



dependencies {

  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.activity.compose)
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.ui)
  implementation(libs.androidx.ui.graphics)
  implementation(libs.androidx.ui.tooling.preview)
  implementation(libs.androidx.material3)
  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.ui.test.junit4)
  debugImplementation(libs.androidx.ui.tooling)
  debugImplementation(libs.androidx.ui.test.manifest)
  //Exo player
  implementation(libs.androidx.media3.exoplayer)
  implementation(libs.androidx.media3.ui)


  //Hilt
  implementation(libs.hilt.android)
  kapt(libs.hilt.compiler)
  implementation(libs.androidx.hilt.navigation.compose)

  //Ml kit
  implementation(libs.object1.detection)
  implementation(libs.kotlinx.coroutines.play.services)
  implementation(libs.object1.detection.custom.v1700)
  implementation(libs.pose.detection)
  implementation("com.google.mlkit:face-detection:16.1.7")

  implementation(libs.image.labeling)
  implementation(libs.common)
  implementation(libs.mediapipe.internal)
  implementation(libs.tasks.vision)
  //Ktor Dependencies for OpenAI
  implementation("com.aallam.openai:openai-client:4.0.1")
  implementation(platform("io.ktor:ktor-bom:3.0.0"))

  implementation("io.ktor:ktor-client-core")
  implementation("io.ktor:ktor-client-okhttp")
  implementation("io.ktor:ktor-client-content-negotiation")
  implementation("io.ktor:ktor-client-logging")
  implementation("io.ktor:ktor-serialization-kotlinx-json")

  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")


  //Tensor flow
  implementation("org.tensorflow:tensorflow-lite-task-vision:0.4.3")
  implementation("org.tensorflow:tensorflow-lite-support:0.4.3")
  implementation("org.tensorflow:tensorflow-lite-gpu-delegate-plugin:0.4.3")


}
