# InCar Monitoring App
⚠️ **Attention: Add Your Own OpenAI API Key**

To use the OpenAI integration in this project, you must add your own API key.

**How to do it:**

1. Open `gradle.properties` (create it if it doesn’t exist in the project root).
2. Add your OpenAI API key:
   ```properties
   OPENAI_API_KEY=sk-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx



# Overall Architecture

The `CarMonitoringApp` is built with a modular, layered architecture that separates UI, video analysis, AI integration, and backend communication. This structure allows the app to handle real-time video processing and intelligent summarization effectively.


- **UI Layer**  
  A minimal interface with only essential components: video playback, a start button, and a summary display box. This simplicity allows the focus to remain on performance and intelligent behavior detection.

- **Video Analysis Layer**  
  Real-time frame extraction and two-layer detection logic are used here to identify people and infer behavior inside the cabin.

- **AI Integration Layer**  
  Uses OpenAI’s `gpt-3.5-turbo` model to turn detection data into natural language summaries. Prompts are dynamically constructed from real-time detection events.

# Why Two-Layer Detection?

To ensure accurate and context-aware detection of in-cabin behavior, the app uses **two independent detection systems** that complement each other:

### 1. **TFLitePersonDetector**  
A lightweight on-device TensorFlow Lite model that detects people in video frames by identifying bounding boxes. It's fast and suitable for real-time applications but doesn't provide detailed facial analysis.

### 2. **FaceAnalyzer (ML Kit)**  
Uses Google’s ML Kit Face Detection to analyze facial features — including:
- **Head orientation**
- **Smiling probability**

This model adds **behavioral context** to the detected persons, allowing the app to infer whether someone is:
- Looking left/right
- Smiling
- Neutral or unknown


# Ui 
The UI was by far the most straightforward part of the project just two buttons, a video placeholder, and a summary display box. It was quick and easy to build, and didn’t require much effort compared to the rest of the system.

# Video analysing 
The video analysis component is responsible for processing video input and detecting faces (or people) within specific frames. This step is essential for generating meaningful behavior summaries using the OpenAI API.
####  `VideoFrameExtractor`

Handles extracting frames from a video file using Android's `MediaMetadataRetriever`.

- **`init(uri: Uri)`** – Initializes the retriever with a video file URI.
- **`extractFrameAt(positionMs: Long)`** – Extracts a single frame from the video at the given timestamp (in milliseconds).
- **`release()`** – Releases the retriever resources after use.
####  `FaceAnalyzer`

Uses ML Kit's Face Detection API to detect faces in each extracted frame.

- Attempts face detection on multiple rotations: `0°`, `90°`, `180°`, and `270°`.
- Selects the rotation that detects the **most faces**.
- Helps handle videos recorded with different orientations.

####  `Real-Time Video Analysis Service (The most challenging part)`

The `VideoAnalysisService` is the heart of the app's intelligent behavior detection. It analyzes the video frame-by-frame during playback, identifies people and their actions, and triggers real-time events for further processing and summarization.This was the most challenging part because the idea of extracting info from frames and how to utilize scopes and decide which parts should run on the main thread or the background thread


- **Video Initialization**  
   The service initializes using the currently loaded `ExoPlayer` video and starts analyzing frames as the video plays.
   
- **Frame Extraction**  
   For each frame (sampled every ~150ms), it uses `VideoFrameExtractor` to capture the current video frame.

- **Concurrent Detection**  
   The extracted frame is sent to two detection services in parallel:
   - **TFLitePersonDetector** – Detects people and returns bounding boxes.
   - **FaceAnalyzer (ML Kit)** – Identifies facial features and orientation (rotates frame to maximize face detection).

- **Action Mapping**  
   Each person is matched with a face using Intersection over Union (IoU), and their behavior is interpreted:
   - Looking left or right (based on head rotation)
   - Smiling (if smiling probability is high)
   - Neutral or unknown

- **Results Callback**  
   The processed `PersonDetectionEvent` objects, including their bounding boxes and inferred actions, are returned to the UI via a callback.



#  OpenAI Integration

The app uses OpenAI's `gpt-3.5-turbo` model to generate natural language summaries based on the behavior detected in the video analysis phase. This allows for a more user-friendly and insightful understanding of what occurred inside the vehicle during playback.


###  `OpenAiService`

This class acts as a wrapper around the OpenAI Kotlin client library, sending detection prompts and retrieving AI-generated summaries.

####  How it works:

1. Builds a `ChatCompletionRequest` using:
   - A **System message**: to define the assistant’s role (e.g., summarizing driver behavior).
   - A **User message**: containing the generated prompt from detection events.

2. Sends the request using the injected `OpenAIClient`.

3. Extracts the assistant's response and returns it as plain text.

####  Model Used:
- **Model ID**: `gpt-3.5-turbo`
- **Temperature**: `0.7` 


###  Prompt Structure

The prompt is dynamically created from detection events using the `PromptFormatter`. It includes:
- Timestamps
- Detected actions (e.g., looking left, smiling, etc.)
- Bounding boxes for their locations

This formatted text is used to ask OpenAI for a summary like:

> **Summary**: Both the driver and co-driver exhibited neutral behavior without any notable distractions.  
> **Analysis**: The consistent neutral behavior from both occupants indicates a lack of risky or distracting actions, maintaining a safe environment in the cabin.


### Example Code

```kotlin
val messages = listOf(
    ChatMessage(
        role = ChatRole.System,
        content = "You are a helpful AI assistant that summarizes in-cabin driver and passengers behavior..."
    ),
    ChatMessage(role = ChatRole.User, content = prompt)
)

val request = ChatCompletionRequest(
    model = ModelId("gpt-3.5-turbo"),
    messages = messages,
    temperature = 0.7  
)

val response = client.instance.chatCompletion(request)
```

# App Flow 
```sql
[Get current video URI from ExoPlayer]
             |
             v
[Initialize VideoFrameExtractor with URI]
             |
             v
[Loop while video is playing]
             |
             v
[Extract frame at current playback position]
             |
             v
+---------------------------------------------+
|           Run detection in parallel:        |
|                                             |
|  ┌───────────────────────────────────────┐  |
|  | 1. TFLitePersonDetector.detectPersons |  |
|  |    → Detect bounding boxes of people  |  |
|  └───────────────────────────────────────┘  |
|                                             |
|  ┌───────────────────────────────────────┐  |
|  | 2. FaceAnalyzer.detectBestRotation    |  |
|  |    → Rotate bitmap (0°, 90°, 180°,    |  |
|  |      270°), detect faces via ML Kit   |  |
|  └───────────────────────────────────────┘  |
+---------------------------------------------+
             |
             v
[Wait for both results using awaitAll()]
             |
             v
[Match faces with persons using IoU > 0.5]
             |
             v
[Infer behavior for each person:]
  - looking left  → headEulerAngleY < -20
  - looking right → headEulerAngleY > 20
  - smiling       → smilingProbability > 0.7
  - otherwise     → neutral / unknown
             |
             v
[Create PersonDetectionEvent list with bounding box + action]
             |
             v
[Return events to UI via onResult()]
             |
             v
[Delay 150ms and repeat next frame...]
```



