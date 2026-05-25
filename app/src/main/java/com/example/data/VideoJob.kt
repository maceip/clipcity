package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "video_jobs")
data class VideoJob(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val originalName: String,
    val fileSizeOriginalMb: Double,
    val fileSizeCompressedMb: Double,
    val cloudSource: String, // Google Drive, Dropbox, AWS S3, local_high_perf
    val durationSeconds: Int,
    val cropRatio: String, // Vertical (9:16), Landscape (16:9), Square (1:1)
    val filterApplied: String, // Cinematic, Sunset, Noir, Retro, None
    val targetCompression: String, // H.265 (HEVC), H.264 (AVC), AV1
    val socialPlatform: String, // TikTok, Instagram Reels, YouTube Shorts, Twitter/X
    val uploadProgressPercent: Int, // 0..100
    val status: String, // "Queued", "Ingesting", "Clipping", "Filtering", "Compressing", "Uploading", "Posted", "Failed"
    val timestamp: Long = System.currentTimeMillis(),
    val postUrl: String = "",
    val failureReason: String? = null,
    
    // Engagement Statistics / Platform Performance indicators
    val viewsCount: Int = 0,
    val likesCount: Int = 0,
    val sharesCount: Int = 0,
    val commentsCount: Int = 0,
    val engagementLastUpdated: Long = 0,
    
    // Modern High-Perf Features requested by user
    val videoFormat: String = "MP4", // MP4, MOV, FLV, AVI, MKV
    val geminiClipperPrompt: String = "Extract high-climax highlights based on audio amplitude peaks & dual-vocal intensity",
    val geminiGeneratedHighlights: String = "[]", // Timestamp highlights list JSON
    val media3AiEffectsEnabled: Boolean = true,
    val appliedMedia3Effects: String = "Studio Sound + Magic Video Eraser",
    val codecDbChipsetOpt: String = "CodecDB Qualcomm SoC Adreno 8 Gen 2 HEVC Lossless (99.8% Perfect Export)",
    val liveScrubSeekingLatencyMs: Double = 6.4
)
