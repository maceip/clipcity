package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.data.AppDatabase
import com.example.data.VideoJob
import com.example.data.VideoJobRepository
import com.example.worker.VideoProcessingWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

class VideoViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = VideoJobRepository(database.videoJobDao())

    // All jobs from the database
    val allJobs: StateFlow<List<VideoJob>> = repository.allJobs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Hardware/System state registries to power telemetry instrumentation
    private val _ingestSpeed = MutableStateFlow(442.5)
    val ingestSpeed = _ingestSpeed.asStateFlow()

    private val _gpuLoad = MutableStateFlow(64)
    val gpuLoad = _gpuLoad.asStateFlow()

    private val _hardwareAccelEnabled = MutableStateFlow(true)
    val hardwareAccelEnabled = _hardwareAccelEnabled.asStateFlow()

    private val _activeCloudPath = MutableStateFlow("High-Perf Pipeline (AWS + S3)")
    val activeCloudPath = _activeCloudPath.asStateFlow()

    // Forms and configuration states
    var selectedCloudSource = MutableStateFlow("Google Drive")
    
    // Cloud provider OAuth authentication states: "Disconnected" | "Connecting" | "Connected"
    private val _cloudAuthStatus = MutableStateFlow<Map<String, String>>(
        mapOf(
            "Google Drive" to "Connected",
            "AWS S3" to "Connected",
            "Snapchat" to "Disconnected",
            "Dropbox" to "Disconnected"
        )
    )
    val cloudAuthStatus = _cloudAuthStatus.asStateFlow()

    fun authenticateProvider(provider: String) {
        viewModelScope.launch {
            _cloudAuthStatus.value = _cloudAuthStatus.value.toMutableMap().apply {
                put(provider, "Connecting")
            }
            // Perform network delay or auth token registration
            kotlinx.coroutines.delay(1200)
            _cloudAuthStatus.value = _cloudAuthStatus.value.toMutableMap().apply {
                put(provider, "Connected")
            }
        }
    }

    fun disconnectProvider(provider: String) {
        _cloudAuthStatus.value = _cloudAuthStatus.value.toMutableMap().apply {
            put(provider, "Disconnected")
        }
    }

    var selectedCropRatio = MutableStateFlow("Vertical (9:16)")
    var selectedFilter = MutableStateFlow("Cinematic Teal")
    var selectedCompression = MutableStateFlow("H.265 (HEVC)")
    var selectedPlatforms = MutableStateFlow(setOf("TikTok"))
    
    // User custom prompt and settings for Gemini AI Clipper and Media3 Effects
    var geminiClipperPrompt = MutableStateFlow("Find high-intensity laughing or crowd cheering moments paired with rapid scene transitions")
    var media3AiEffectsEnabled = MutableStateFlow(true)
    var selectedMedia3EffectsOption = MutableStateFlow("Studio Sound + Magic Video Eraser")

    // State of list of pool cloud clips available for ingestion
    private val _cloudPoolClips = MutableStateFlow(listOf(
        CloudClip("Raw_Vlog_082.mov", 425.0, 122),
        CloudClip("Sunset_Coast_B_Roll.mp4", 154.0, 45),
        CloudClip("Gym_Workout_POV.mov", 89.0, 15),
        CloudClip("Drone_UltraHD_Tokyo.mp4", 1210.0, 180),
        CloudClip("Interview_Clip_09.mov", 3200.0, 600),
        CloudClip("Retro_Street_Walk.mov", 75.0, 30),
        CloudClip("Summer_Cooking_Recipe.mp4", 112.0, 50),
        CloudClip("Neon_Night_Timelapse.mp4", 68.0, 20)
    ))
    val cloudPoolClips: StateFlow<List<CloudClip>> = _cloudPoolClips.asStateFlow()

    fun importClip(name: String, sizeMb: Double, durationSec: Int) {
        val updated = _cloudPoolClips.value.toMutableList()
        updated.add(0, CloudClip(name, sizeMb, durationSec))
        _cloudPoolClips.value = updated
    }

    init {
        // Seed initial history items if db is empty so first-launch is stunning
        viewModelScope.launch {
            repository.allJobs.collect { list ->
                if (list.isEmpty()) {
                    seedSampleHistory()
                }
            }
        }

        // Read-write hardware monitoring loop updating live bus telemetry
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(2000)
                // Random walk for visual richness
                if (Random.nextBoolean()) {
                    _ingestSpeed.value = (410.0 + Random.nextDouble() * 60.0)
                    _gpuLoad.value = Random.nextInt(55, 78)
                }
            }
        }
    }

    private suspend fun seedSampleHistory() {
        val samples = listOf(
            VideoJob(
                originalName = "Sunset_Coast_B_Roll.mp4",
                fileSizeOriginalMb = 154.0,
                fileSizeCompressedMb = 53.9,
                cloudSource = "AWS S3",
                durationSeconds = 45,
                cropRatio = "Vertical (9:16)",
                filterApplied = "Sunset Vibe",
                targetCompression = "H.265 (HEVC)",
                socialPlatform = "TikTok",
                uploadProgressPercent = 100,
                status = "Posted",
                timestamp = System.currentTimeMillis() - 120000, // 2m ago
                postUrl = "https://www.tiktok.com/share/video/849312",
                viewsCount = 14200,
                likesCount = 3840,
                sharesCount = 612,
                commentsCount = 184,
                engagementLastUpdated = System.currentTimeMillis() - 50000
            ),
            VideoJob(
                originalName = "Gym_Workout_POV.mov",
                fileSizeOriginalMb = 89.0,
                fileSizeCompressedMb = 22.25,
                cloudSource = "Google Drive",
                durationSeconds = 15,
                cropRatio = "Square (1:1)",
                filterApplied = "None",
                targetCompression = "AV1",
                socialPlatform = "Instagram Reels",
                uploadProgressPercent = 100,
                status = "Posted",
                timestamp = System.currentTimeMillis() - 900000, // 15m ago
                postUrl = "https://www.instagram.com/reels/2940291",
                viewsCount = 28430,
                likesCount = 8920,
                sharesCount = 1240,
                commentsCount = 492,
                engagementLastUpdated = System.currentTimeMillis() - 40000
            ),
            VideoJob(
                originalName = "Drone_UltraHD_Tokyo.mp4",
                fileSizeOriginalMb = 1210.0,
                fileSizeCompressedMb = 665.5,
                cloudSource = "AWS S3",
                durationSeconds = 180,
                cropRatio = "Landscape (16:9)",
                filterApplied = "Cinematic Teal",
                targetCompression = "H.264 (AVC)",
                socialPlatform = "YouTube Shorts",
                uploadProgressPercent = 100,
                status = "Posted",
                timestamp = System.currentTimeMillis() - 3600000, // 1h ago
                postUrl = "https://youtu.be/shorts/110940",
                viewsCount = 8400,
                likesCount = 1120,
                sharesCount = 98,
                commentsCount = 45,
                engagementLastUpdated = System.currentTimeMillis() - 30000
            )
        )
        for (sample in samples) {
            repository.insertJob(sample)
        }
    }

    // Dynamic Tracking & Engagement syncing logic simulated on top-level provider metrics
    fun refreshEngagementForJob(job: VideoJob) {
        viewModelScope.launch {
            // Simulate platform API request
            kotlinx.coroutines.delay(800)
            val currentViews = if (job.viewsCount == 0) Random.nextInt(1200, 8500) else job.viewsCount
            val additionalViews = Random.nextInt(450, 2500)
            val updatedViews = currentViews + additionalViews
            val updatedLikes = (updatedViews * (0.08 + Random.nextDouble() * 0.12)).toInt()
            val updatedShares = (updatedLikes * (0.04 + Random.nextDouble() * 0.08)).toInt()
            val updatedComments = (updatedLikes * (0.02 + Random.nextDouble() * 0.04)).toInt()
            
            val updatedJob = job.copy(
                viewsCount = updatedViews,
                likesCount = updatedLikes,
                sharesCount = updatedShares,
                commentsCount = updatedComments,
                engagementLastUpdated = System.currentTimeMillis()
            )
            repository.updateJob(updatedJob)
        }
    }

    fun syncAllPlatformEngagements() {
        viewModelScope.launch {
            val jobsList = allJobs.value
            for (job in jobsList) {
                if (job.status == "Posted") {
                    refreshEngagementForJob(job)
                }
            }
        }
    }

    // Trigger standard enqueue ingestion pipeline
    fun processClip(clip: CloudClip) {
        viewModelScope.launch {
            val extension = clip.name.substringAfterLast('.', "MP4").uppercase()
            val platforms = selectedPlatforms.value.ifEmpty { setOf("TikTok") }
            for (platform in platforms) {
                val job = VideoJob(
                    originalName = clip.name,
                    fileSizeOriginalMb = clip.sizeMb,
                    fileSizeCompressedMb = 0.0,
                    cloudSource = selectedCloudSource.value,
                    durationSeconds = clip.durationSec,
                    cropRatio = selectedCropRatio.value,
                    filterApplied = selectedFilter.value,
                    targetCompression = selectedCompression.value,
                    socialPlatform = platform,
                    uploadProgressPercent = 0,
                    status = "Queued",
                    videoFormat = extension,
                    geminiClipperPrompt = geminiClipperPrompt.value,
                    media3AiEffectsEnabled = media3AiEffectsEnabled.value,
                    appliedMedia3Effects = if (media3AiEffectsEnabled.value) selectedMedia3EffectsOption.value else "Disabled"
                )
                // Save to DB
                val jobId = repository.insertJob(job).toInt()

                // Schedule WorkManager CoroutineWorker
                val workRequest = OneTimeWorkRequestBuilder<VideoProcessingWorker>()
                    .setInputData(workDataOf("jobId" to jobId))
                    .build()

                WorkManager.getInstance(getApplication()).enqueue(workRequest)
            }
        }
    }

    // Toggle hardware acceleration
    fun toggleHardwareAccel() {
        _hardwareAccelEnabled.value = !_hardwareAccelEnabled.value
    }

    // Clear all entries
    fun clearAllHistory() {
        viewModelScope.launch {
            repository.deleteAllJobs()
        }
    }
}

data class CloudClip(
    val name: String,
    val sizeMb: Double,
    val durationSec: Int
)
