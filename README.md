# CarMonitoringApp
When I first read the project description, I assumed it would be a relatively simple task — build an Android app that analyzes video, detects people with bounding boxes, and sends data to OpenAI to generate a summary.
But once I started working on it, I realized the real challenge wasn’t just the implementation it was the open-ended nature of the project. Being given complete freedom to choose the libraries, architecture, and tools forced me to think more deeply about design decisions. That freedom was both exciting and intimidating.

Throughout the process, I learned a lot  from integrating ExoPlayer and video analysis, to setting up communication between the app and a Flask backend using Ktor. Working with OpenAI’s API.However, there are still challenges I haven’t fully overcome. Things like optimizing performance during real-time detection.

Overall, the project has pushed me to grow as a developer — not just in terms of writing code, but in learning how to navigate ambiguity, make architectural choices, and deal with real-world complexity.
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
- Number of people in frame
- Repeated or suspicious behavior

This formatted text is used to ask OpenAI for a summary like:

> **Summary**: Both the driver and co-driver exhibited neutral behavior without any notable distractions.  
> **Analysis**: The consistent neutral behavior from both occupants indicates a lack of risky or distracting actions, maintaining a safe environment in the cabin.


### Example Code

```kotlin
val messages = listOf(
    ChatMessage(
        role = ChatRole.System,
        content = "You are a helpful AI assistant that summarizes in-cabin driver and co-driver behavior..."
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



