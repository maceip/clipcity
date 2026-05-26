package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import com.example.data.VideoJob
import com.example.ui.theme.RetroCharcoal
import com.example.ui.theme.RetroCream
import com.example.ui.viewmodel.CloudClip
import com.example.ui.viewmodel.VideoViewModel
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.LinearProgressIndicator
import top.yukonga.miuix.kmp.basic.Slider
import top.yukonga.miuix.kmp.basic.Surface
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun Sparkline(
    points: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = Color(0xFFFF0055)
) {
    Canvas(modifier = modifier) {
        if (points.isEmpty()) return@Canvas
        val width = size.width
        val height = size.height
        val path = androidx.compose.ui.graphics.Path()

        val maxVal = (points.maxOrNull() ?: 1f).coerceAtLeast(1f)
        val minVal = (points.minOrNull() ?: 0f)
        val range = (maxVal - minVal).coerceAtLeast(0.1f)

        val stepX = width / (points.size - 1).coerceAtLeast(1)

        points.forEachIndexed { index, value ->
            val x = index * stepX
            val normalizedY = (value - minVal) / range
            val y = height - (normalizedY * (height - 4.dp.toPx()) + 2.dp.toPx())
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = lineColor,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 2.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        )
    }
}

@Composable
fun MonitorScreen(
    viewModel: VideoViewModel,
    onJobClick: (VideoJob) -> Unit,
    onNavigateToQueue: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val jobs by viewModel.allJobs.collectAsState()
    val speed by viewModel.ingestSpeed.collectAsState()
    val gpuLoad by viewModel.gpuLoad.collectAsState()

    var speedHistory by remember { mutableStateOf(List(12) { 420f + it * 2f }) }
    var gpuHistory by remember { mutableStateOf(List(12) { 50f + it * 1.5f }) }
    var pingHistory by remember { mutableStateOf(List(12) { 15f + (it % 3) * 1f }) }

    LaunchedEffect(speed, gpuLoad) {
        speedHistory = (speedHistory.drop(1) + speed.toFloat())
        gpuHistory = (gpuHistory.drop(1) + gpuLoad.toFloat())
        pingHistory = (pingHistory.drop(1) + (14f + kotlin.random.Random.nextInt(6).toFloat()))
    }

    // Slots allocation state
    var morningJobId by remember { mutableStateOf<Int?>(null) }
    var lunchJobId by remember { mutableStateOf<Int?>(null) }
    var nightJobId by remember { mutableStateOf<Int?>(null) }

    // Pre-populate slots with completed items for immediate high visual satisfaction
    LaunchedEffect(jobs) {
        val postedList = jobs.filter { it.status == "Posted" }
        if (postedList.isNotEmpty() && morningJobId == null && lunchJobId == null && nightJobId == null) {
            if (postedList.size >= 3) {
                morningJobId = postedList[1].id
                lunchJobId = postedList[0].id
                nightJobId = postedList[2].id
            } else {
                morningJobId = postedList.first().id
            }
        }
    }

    val morningJob = jobs.find { it.id == morningJobId }
    val lunchJob = jobs.find { it.id == lunchJobId }
    val nightJob = jobs.find { it.id == nightJobId }

    // State for interactive overlays
    var activeAssigningSlot by remember { mutableStateOf<String?>(null) } // "morning" | "lunch" | "night"
    var showPublisherToast by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(showPublisherToast) {
        if (showPublisherToast != null) {
            kotlinx.coroutines.delay(3000)
            showPublisherToast = null
        }
    }

    // Channels tab selection: 0 = ALL, 1 = TIKTOK, 2 = INSTAGRAM, 3 = YOUTUBE
    var selectedChannelTab by remember { mutableStateOf(0) }

    // Simulate active audience state (toggle back/forth from zero-likes real mode)
    var simulateAudienceFeed by remember { mutableStateOf(false) }

    // Selected Creator Segment Niche for dynamic trending slot analysis
    var activeSegmentNiche by remember { mutableStateOf("Autopilot (AI Detect) 🤖") }

    val activeJob = jobs.firstOrNull {
        it.status != "Posted" && it.status != "Failed" && it.status != "Queued"
    }

    val morningTrend = when (activeSegmentNiche) {
        "Gym & Fitness" -> "Explosive morning cardio loops, pre-workout focus hooks & high energy trap tracks"
        "Tech & Gadgets" -> "Desk setup tour ASMR, clean wakeup routines with aesthetic smart lights & minimalistic unboxings"
        "Food & Cooking" -> "Creamy iced coffee pour techniques, fast breakfast egg wraps & fluffy pancake slow-motion loops"
        "Autopilot (AI Detect) 🤖" -> "✨ Auto-Profiled: Syncs natural morning vibes, warm sunlight B-roll, or active setups based on video DNA"
        else -> "Aesthetic coffee preparations, morning skincare routine hooks, cosy study/work space vlogs"
    }

    val lunchTrend = when (activeSegmentNiche) {
        "Gym & Fitness" -> "High protein lunch prep hacks, fast hypertrophy form instruction guides & workout kit checklists"
        "Tech & Gadgets" -> "Quick 30s hardware macro reviews, phone durability scratches & fast software app reviews"
        "Food & Cooking" -> "Sizzling lunch burger recipes, 30s noodle stir-fry hacks & college student budget box lunch recipes"
        "Autopilot (AI Detect) 🤖" -> "✨ Auto-Profiled: Creates mid-day cinematic segment hooks, text-overlay hooks & retention-optimized loops"
        else -> "Daily outfit of the day (OOTD) mini reels, casual urban walk vlogs & productivity office workspace logs"
    }

    val nightTrend = when (activeSegmentNiche) {
        "Gym & Fitness" -> "Cool down stretching routines, late-night recovery shake recipes & dynamic aesthetic transformation edits"
        "Tech & Gadgets" -> "Neon illuminated gaming setup loops, mechanical keyboard sound checks & developer dark mode desktop vibes"
        "Food & Cooking" -> "Satisfying slow dessert baking loops, late-night comfort cookie recipe edits & relaxing culinary ASMR slicing"
        "Autopilot (AI Detect) 🤖" -> "✨ Auto-Profiled: Cozy evening journaling, ASMR unboxing, or sunset twilight timelapses synced with trending lofi"
        else -> "Sunset B-roll timelapses, cozy bedroom lighting setup tours & journaling/reflection self-care highlight guides"
    }

    val segmentSoundTrack = when (activeSegmentNiche) {
        "Gym & Fitness" -> "Hyper-Pulse Gym Trap"
        "Tech & Gadgets" -> "Synthwave Glow Minimal ASMR"
        "Food & Cooking" -> "Cozy Culinary Chill Beats"
        "Autopilot (AI Detect) 🤖" -> "AI Sync: Auto-detects track from video visual energy (Lofi or Trap)"
        else -> "Golden Hour Lofi Sunset"
    }

    val segmentTargetAudience = when (activeSegmentNiche) {
        "Gym & Fitness" -> "Active Gym-Goers, Calisthenics & Fitness Enthusiasts"
        "Tech & Gadgets" -> "Desk setup designers, minimalist gadget hunters & tech fans"
        "Food & Cooking" -> "Home chefs, culinary aesthetic curators & recipe searchers"
        "Autopilot (AI Detect) 🤖" -> "Automatically optimized per platform algorithm demands"
        else -> "Lifestyle diary fans, aesthetic students & design lovers"
    }

    val segmentApplyPrompt = when (activeSegmentNiche) {
        "Gym & Fitness" -> "High intensity gym workout drops and muscle reps syncing to high tempo heavy electronic beats"
        "Tech & Gadgets" -> "Aesthetic minimal ASMR desk cleanups and tech unboxing macro angles with cozy synth glow"
        "Food & Cooking" -> "Satisfying home kitchen recipe slicing and sizzling food steps syncing with delicious background music"
        "Autopilot (AI Detect) 🤖" -> "Identify high-retaining transitions, humorous or peak action states, and extract polished 30-40s clips automatically"
        else -> "Cozy ambient afternoon vlog highlighting coffee pour and aesthetic room lighting setups syncing to warm gentle acoustic hum"
    }

    // Render the layout
    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
        ) {
            // Active processing banner
            item {
                AnimatedVisibility(
                    visible = activeJob != null,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    activeJob?.let { job ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onJobClick(job) },
                            cornerRadius = 20.dp,
                            colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.primaryContainer)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "ACTIVE CLIP PIPELINE WORKING",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MiuixTheme.colorScheme.primary,
                                                letterSpacing = 1.sp
                                            )
                                        )
                                        Text(
                                            text = "Gemini Processing Subsections...",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MiuixTheme.colorScheme.onPrimaryContainer
                                            )
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(MiuixTheme.colorScheme.secondary, RoundedCornerShape(20.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "LIVE RENDER",
                                            color = MiuixTheme.colorScheme.onSecondary,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = job.originalName,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "${job.uploadProgressPercent}%",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    progress = job.uploadProgressPercent / 100f,
                                )
                            }
                        }
                    }
                }
            }

            // Google Analytics Header Widget
            item {
                Column {
                    Text(
                        text = "CHANNELS GOOGLE ANALYTICS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MiuixTheme.colorScheme.secondary,
                            letterSpacing = 1.2.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Analytics Channel Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("All Channels", "TikTok Mode", "Instagram Reels", "YouTube Shorts").forEachIndexed { index, name ->
                            val isSelected = selectedChannelTab == index
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (isSelected) MiuixTheme.colorScheme.primaryContainer else MiuixTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedChannelTab = index }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = name,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MiuixTheme.colorScheme.onPrimaryContainer else MiuixTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }

            // Analytics Grid (Show Zero Likes/Views strictly according to request, but elegantly designed)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, MiuixTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                    cornerRadius = 16.dp,
                    colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "REAL-TIME CREATOR TRAFFIC",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            )

                            // Visual button to simulate engagement to help play with the layout
                            Button(
                                onClick = { simulateAudienceFeed = !simulateAudienceFeed },
                                modifier = Modifier.height(28.dp),
                                cornerRadius = 8.dp,
                                colors = ButtonDefaults.buttonColors(
                                    color = if (simulateAudienceFeed) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.surfaceVariant
                                ),
                                insideMargin = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (simulateAudienceFeed) "ACTIVE FEED" else "SHOW REAL ZERO",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (simulateAudienceFeed) Color.White else MiuixTheme.colorScheme.onSurfaceVariantSummary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Analytics metric matrix rows
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Metrics 1: Views
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, MiuixTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                                colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("VIEWS", fontSize = 8.sp, fontWeight = FontWeight.Black, color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val viewsText = if (simulateAudienceFeed) {
                                        when (selectedChannelTab) {
                                            0 -> "42.5K"
                                            1 -> "24.1K"
                                            2 -> "11.2K"
                                            else -> "7.2K"
                                        }
                                    } else "0"
                                    val viewsTrend = if (simulateAudienceFeed) "+14.2%" else "+0.0%"
                                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text(viewsText, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                                        Text(viewsTrend, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (simulateAudienceFeed) Color(0xFF00E676) else MiuixTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    val viewsPoints = if (simulateAudienceFeed) listOf(10f, 25f, 15f, 40f, 35f, 50f, 45f, 62f, 58f, 75f, 72f, 85f) else List(12) { 0f }
                                    Sparkline(points = viewsPoints, modifier = Modifier.fillMaxWidth().height(16.dp), lineColor = MiuixTheme.colorScheme.primary)
                                }
                            }

                            // Metrics 2: Engagement / Likes
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, MiuixTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                                colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("LIKES & COMM", fontSize = 8.sp, fontWeight = FontWeight.Black, color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val likesText = if (simulateAudienceFeed) {
                                        when (selectedChannelTab) {
                                            0 -> "8.1K"
                                            1 -> "4.2K"
                                            2 -> "2.4K"
                                            else -> "1.5K"
                                        }
                                    } else "0"
                                    val likesTrend = if (simulateAudienceFeed) "+8.8%" else "+0.0%"
                                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text(likesText, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                                        Text(likesTrend, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (simulateAudienceFeed) Color(0xFF00E676) else MiuixTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    val likesPoints = if (simulateAudienceFeed) listOf(5f, 12f, 8f, 22f, 18f, 31f, 29f, 44f, 40f, 52f, 48f, 60f) else List(12) { 0f }
                                    Sparkline(points = likesPoints, modifier = Modifier.fillMaxWidth().height(16.dp), lineColor = MiuixTheme.colorScheme.secondary)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Metrics 3: Shares
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, MiuixTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                                colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("REPOST & SHARE", fontSize = 8.sp, fontWeight = FontWeight.Black, color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val sharesText = if (simulateAudienceFeed) {
                                        when (selectedChannelTab) {
                                            0 -> "1.4K"
                                            1 -> "850"
                                            2 -> "410"
                                            else -> "140"
                                        }
                                    } else "0"
                                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text(sharesText, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                                        Text(if (simulateAudienceFeed) "+24.1%" else "+0.0%", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (simulateAudienceFeed) Color(0xFF00E676) else MiuixTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    val sharesPoints = if (simulateAudienceFeed) listOf(2f, 4f, 3f, 9f, 6f, 12f, 10f, 18f, 15f, 22f, 19f, 28f) else List(12) { 0f }
                                    Sparkline(points = sharesPoints, modifier = Modifier.fillMaxWidth().height(16.dp), lineColor = MiuixTheme.colorScheme.onTertiaryContainer)
                                }
                            }

                            // Metrics 4: Retention/Attention
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, MiuixTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                                colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("RETENTION RT", fontSize = 8.sp, fontWeight = FontWeight.Black, color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val retentionText = if (simulateAudienceFeed) "68.4%" else "0.0%"
                                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text(retentionText, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                                        Text(if (simulateAudienceFeed) "+12.0%" else "+0.0%", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (simulateAudienceFeed) Color(0xFF00E676) else MiuixTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    val retentionPoints = if (simulateAudienceFeed) listOf(12f, 14f, 18f, 25f, 28f, 35f, 42f, 48f, 52f, 58f, 64f, 68f) else List(12) { 0f }
                                    Sparkline(points = retentionPoints, modifier = Modifier.fillMaxWidth().height(16.dp), lineColor = MiuixTheme.colorScheme.primary.copy(alpha = 0.6f))
                                }
                            }
                        }

                        // Cold Channel Assistant Alert Callout
                        AnimatedVisibility(visible = !simulateAudienceFeed) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                cornerRadius = 8.dp,
                                colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "🛡️", fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Channel Stream Off: No viewers recorded. Schedule ready content to the peak hour slots below to activate the tracking loop.",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MiuixTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Creator niche segment selector and daily trends board
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MiuixTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(16.dp)),
                    cornerRadius = 16.dp,
                    colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "💡 CREATOR TRENDS INTELLIGENCE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MiuixTheme.colorScheme.primary,
                                    letterSpacing = 1.1.sp
                                )
                            )
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF00E676).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "LIVE TRENDS",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF00E676)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Gemini automatically analyzes your imported media to detect target niches, hashtags, and sound styles. Zero sifting or configuration needed! Tap any segment below to manually override the Autopilot settings:",
                            style = MaterialTheme.typography.bodySmall.copy(color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.7f), lineHeight = 16.sp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Dynamic Niche Segment Buttons (Beautiful Scrollable LazyRow preventing squishing)
                        val segmentOptions = listOf("Autopilot (AI Detect) 🤖", "Lifestyle & Vlog", "Gym & Fitness", "Tech & Gadgets", "Food & Cooking")
                        androidx.compose.foundation.lazy.LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(segmentOptions) { opt ->
                                val isSelected = opt == activeSegmentNiche
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (isSelected) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.surface.copy(alpha = 0.5f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .border(
                                            1.dp,
                                            if (isSelected) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.outline.copy(alpha = 0.2f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { activeSegmentNiche = opt }
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = opt,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else MiuixTheme.colorScheme.onSurface,
                                        lineHeight = 14.sp,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Active Niche Insights report container
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MiuixTheme.colorScheme.surface.copy(alpha = 0.8f), RoundedCornerShape(10.dp))
                                .border(1.dp, MiuixTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "🎯 DEEP ANALYSIS: ${activeSegmentNiche.uppercase()}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, color = MiuixTheme.colorScheme.secondary)
                                    )
                                    Text(
                                        text = when (activeSegmentNiche) {
                                            "Gym & Fitness" -> "TRAFFIC INDEX: 142%"
                                            "Tech & Gadgets" -> "TRAFFIC INDEX: 138%"
                                            "Food & Cooking" -> "TRAFFIC INDEX: 151%"
                                            else -> "TRAFFIC INDEX: 135%"
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF00E676)
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "Target Audience: $segmentTargetAudience",
                                    fontSize = 11.sp,
                                    color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                    fontWeight = FontWeight.Medium
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "🔥 Trending Sound: $segmentSoundTrack",
                                    fontSize = 11.sp,
                                    color = MiuixTheme.colorScheme.onTertiaryContainer,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MiuixTheme.colorScheme.outline.copy(alpha = 0.15f)))
                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Smart Gemini Clipper Instruction Filter Prompt Recommended:",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "\"$segmentApplyPrompt\"",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    style = MaterialTheme.typography.bodyMedium.copy(color = MiuixTheme.colorScheme.onSurface)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = {
                                        viewModel.geminiClipperPrompt.value = segmentApplyPrompt
                                        viewModel.selectedFilter.value = when (activeSegmentNiche) {
                                            "Gym & Fitness" -> "Cinematic Teal"
                                            "Tech & Gadgets" -> "Cyberpunk Neon"
                                            "Food & Cooking" -> "Warm Gourmet"
                                            else -> "Sunset Vibe"
                                        }
                                        viewModel.selectedCropRatio.value = "Vertical (9:16)"
                                        onNavigateToQueue()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    cornerRadius = 8.dp,
                                    colors = ButtonDefaults.buttonColors(color = MiuixTheme.colorScheme.primary),
                                    insideMargin = PaddingValues(vertical = 10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Send,
                                        contentDescription = "Apply prompt",
                                        modifier = Modifier.size(14.dp),
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "USE TREND SYNC (APPLY HIGH-PERF SETTINGS & CLIP VIDEO)",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Peak Publishing slots
            item {
                Column {
                    Text(
                        text = "PEAK DAILY PUBLISHING BOARD",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MiuixTheme.colorScheme.secondary,
                            letterSpacing = 1.2.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Divide the day into peak response windows mapped to $activeSegmentNiche",
                        style = MaterialTheme.typography.bodySmall.copy(color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    )
                }
            }

            // Morning Slot Card
            item {
                SlotCard(
                    slotName = "🌅 Morning Segment",
                    timeRange = "07:30 AM - 09:30 AM",
                    recommendedTrend = morningTrend,
                    job = morningJob,
                    onAssignClick = { activeAssigningSlot = "morning" },
                    onPublishClick = {
                        val videoTitle = morningJob?.originalName ?: "Vlog"
                        showPublisherToast = "Publish scheduled for $videoTitle to all active channels!"
                    }
                )
            }

            // Lunch Slot Card
            item {
                SlotCard(
                    slotName = "🍕 Lunch Segment",
                    timeRange = "12:00 PM - 01:30 PM",
                    recommendedTrend = lunchTrend,
                    job = lunchJob,
                    onAssignClick = { activeAssigningSlot = "lunch" },
                    onPublishClick = {
                        val videoTitle = lunchJob?.originalName ?: "Vlog"
                        showPublisherToast = "Publish scheduled for $videoTitle to all active channels!"
                    }
                )
            }

            // Night Slot Card
            item {
                SlotCard(
                    slotName = "🌃 Night Segment",
                    timeRange = "07:00 PM - 09:30 PM",
                    recommendedTrend = nightTrend,
                    job = nightJob,
                    onAssignClick = { activeAssigningSlot = "night" },
                    onPublishClick = {
                        val videoTitle = nightJob?.originalName ?: "Vlog"
                        showPublisherToast = "Publish scheduled for $videoTitle to all active channels!"
                    }
                )
            }

            // Zeitgeist News Feed
            item {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    Text(
                        text = "CREATIVE ZEITGEIST FEED",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MiuixTheme.colorScheme.secondary,
                            letterSpacing = 1.2.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Trending styles updated three times a day. Tap to apply prompts.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    )
                }
            }

            // News Item 1
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MiuixTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(16.dp)),
                    cornerRadius = 16.dp,
                    colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surface)
                ) {
                    VideoFirstFrameSurface(
                        videoName = "Vlog_morning_style.mp4",
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(modifier = Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.76f)))
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFE91E63), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("TRENDING: MORNING", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color.White)
                                }
                                Text("NEWS BULLETIN", fontSize = 8.sp, color = Color.White.copy(alpha = 0.4f), fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Lofi Sunset & Morning Chill Beats Outperforming Pacing Targets",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Creators pairing sunset B-rolls or early coffee vlogs with the 'Golden Hour Lofi' track are receiving 1.4x higher initial audience response rates. Suggesting a 1.2s text bubble hook.",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.7f))
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    viewModel.geminiClipperPrompt.value = "Upbeat lofi aesthetic morning vlog with cozy warm visual filter and 1.2s bold hook overlay"
                                    viewModel.selectedFilter.value = "Sunset Vibe"
                                    viewModel.selectedCropRatio.value = "Vertical (9:16)"
                                    onNavigateToQueue()
                                },
                                modifier = Modifier.align(Alignment.End),
                                cornerRadius = 8.dp,
                                colors = ButtonDefaults.buttonColors(color = MiuixTheme.colorScheme.primary),
                                insideMargin = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text("APPLY STYLE SYSTEM", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }

            // News Item 2
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MiuixTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(16.dp)),
                    cornerRadius = 16.dp,
                    colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surface)
                ) {
                    VideoFirstFrameSurface(
                        videoName = "Gym_Workout_POV.mov",
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(modifier = Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.76f)))
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF9C27B0), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("TRENDING: LUNCH", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color.White)
                                }
                                Text("ALGO RADAR", fontSize = 8.sp, color = Color.White.copy(alpha = 0.4f), fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Gym POV & Healthy Recipe Hacks Dominating Intermediate Swipes",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "High contrast yellow captions mixed with rapid jump cuts during 12pm lunch break are achieving massive retention boosts. Ideal for TikTok and YouTube Shorts syndication.",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.7f))
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    viewModel.geminiClipperPrompt.value = "Rapid jump cuts POV fitness hack, yellow subtitle captions, studio audio voiceover"
                                    viewModel.selectedFilter.value = "Cinematic Teal"
                                    viewModel.selectedCropRatio.value = "Vertical (9:16)"
                                    onNavigateToQueue()
                                },
                                modifier = Modifier.align(Alignment.End),
                                cornerRadius = 8.dp,
                                colors = ButtonDefaults.buttonColors(color = MiuixTheme.colorScheme.onTertiaryContainer),
                                insideMargin = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text("APPLY STYLE SYSTEM", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }

            // News Item 3
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MiuixTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(16.dp)),
                    cornerRadius = 16.dp,
                    colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surface)
                ) {
                    VideoFirstFrameSurface(
                        videoName = "Drone_UltraHD_Tokyo.mp4",
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(modifier = Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.76f)))
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF4CAF50), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("TRENDING: NIGHT", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color.White)
                                }
                                Text("CREATIVE ADVANTAGE", fontSize = 8.sp, color = Color.White.copy(alpha = 0.4f), fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Late Night Ambient & City Sunset Walks Trend Cycle Spikes",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Calm, slow loop durations with twilight lighting and neon colors. Engagement increases 45% when posted between 7-10pm to Instagram Reels.",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.7f))
                              )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    viewModel.geminiClipperPrompt.value = "Calm twilight sunset with cyberpunk urban lofi cinematic ambient cycle"
                                    viewModel.selectedFilter.value = "Sunset Vibe"
                                    viewModel.selectedCropRatio.value = "Vertical (9:16)"
                                    onNavigateToQueue()
                                },
                                modifier = Modifier.align(Alignment.End),
                                cornerRadius = 8.dp,
                                colors = ButtonDefaults.buttonColors(color = MiuixTheme.colorScheme.primary),
                                insideMargin = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text("APPLY STYLE SYSTEM", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // Real-Time Action notification Toast
        AnimatedVisibility(
            visible = showPublisherToast != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        ) {
            Card(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .border(1.dp, MiuixTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                cornerRadius = 12.dp,
                colors = CardDefaults.defaultColors(color = RetroCharcoal)
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🚀", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = showPublisherToast ?: "",
                        color = RetroCream,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        // Custom assignment sheet / select dialog (Inline pop-up overlay modal)
        if (activeAssigningSlot != null) {
            val uncompletedList = jobs.filter { it.status == "Posted" }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable { activeAssigningSlot = null },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(16.dp)
                        .border(1.5.dp, MiuixTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                        .clickable(enabled = false) {},
                    cornerRadius = 20.dp,
                    colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SELECT COMPLETED VIDEO",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                            )
                            IconButton(onClick = { activeAssigningSlot = null }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close overlay modal")
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Choose from your fully-rendered movie frames to assign this slot.",
                            style = MaterialTheme.typography.bodySmall.copy(color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        if (uncompletedList.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No fully processed clips available yet.\nRender some in the Queue tab first!",
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        } else {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.heightIn(max = 280.dp)
                            ) {
                                uncompletedList.forEach { job ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, MiuixTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                            .clickable {
                                                when (activeAssigningSlot) {
                                                    "morning" -> morningJobId = job.id
                                                    "lunch" -> lunchJobId = job.id
                                                    "night" -> nightJobId = job.id
                                                }
                                                activeAssigningSlot = null
                                            },
                                        colors = CardDefaults.defaultColors(color = Color.Transparent)
                                    ) {
                                        VideoFirstFrameSurface(
                                            videoName = job.originalName,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Box(modifier = Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.65f)))
                                            Row(
                                                modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("🎬", fontSize = 16.sp)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(job.originalName, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                    Text("${job.socialPlatform} • ${job.durationSeconds}s", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SlotCard(
    slotName: String,
    timeRange: String,
    recommendedTrend: String,
    job: VideoJob?,
    onAssignClick: () -> Unit,
    onPublishClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, MiuixTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
        cornerRadius = 16.dp,
        colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Slot metadata header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = slotName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = MiuixTheme.colorScheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(MiuixTheme.colorScheme.primaryContainer.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = timeRange,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MiuixTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Suggested Content: $recommendedTrend",
                        style = MaterialTheme.typography.bodySmall.copy(color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))

            if (job != null) {
                // Video spanning the entire background card, with text on top!
                VideoFirstFrameSurface(
                    videoName = job.originalName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, MiuixTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                        .clickable { onAssignClick() }
                ) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.Black.copy(alpha = 0.68f))
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = job.originalName,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = job.socialPlatform.uppercase(),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${job.durationSeconds} seconds",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.8f))
                                )
                            }
                        }

                        // Action buttons
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(
                                onClick = onAssignClick,
                                cornerRadius = 8.dp,
                                colors = ButtonDefaults.buttonColors(color = Color.White.copy(alpha = 0.15f)),
                                insideMargin = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("CHANGE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Button(
                                onClick = onPublishClick,
                                cornerRadius = 8.dp,
                                colors = ButtonDefaults.buttonColors(color = MiuixTheme.colorScheme.primary),
                                insideMargin = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("PUBLISH", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            } else {
                // Unassigned slot card
                VideoFirstFrameSurface(
                    videoName = "unassigned_slot.mp4",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, MiuixTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                        .clickable { onAssignClick() }
                ) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.Black.copy(alpha = 0.75f))
                    )
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "+ ASSIGN READY CLIP TO PUBLISH",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = MiuixTheme.colorScheme.secondaryContainer
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Prepare asset scheduling now",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.4f))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QueueScreen(
    viewModel: VideoViewModel,
    modifier: Modifier = Modifier
) {
    val cloudPoolClips by viewModel.cloudPoolClips.collectAsState()
    val cloudSource by viewModel.selectedCloudSource.collectAsState()
    val cropRatio by viewModel.selectedCropRatio.collectAsState()
    val filter by viewModel.selectedFilter.collectAsState()
    val compression by viewModel.selectedCompression.collectAsState()
    val selectedPlatforms by viewModel.selectedPlatforms.collectAsState()
    val authStatusMap by viewModel.cloudAuthStatus.collectAsState()

    var showImportDialogType by remember { mutableStateOf<String?>(null) }
    var importFileName by remember { mutableStateOf("") }
    var importFileSize by remember { mutableStateOf("120") }
    var importFileDuration by remember { mutableStateOf("45") }

    var showToastMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(showToastMessage) {
        if (showToastMessage != null) {
            kotlinx.coroutines.delay(3500)
            showToastMessage = null
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            // Source Config Box
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MiuixTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(20.dp)),
                    cornerRadius = 20.dp,
                    colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "IMPORT SOURCE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MiuixTheme.colorScheme.primary,
                                letterSpacing = 1.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Active Cloud Directory Profile",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Cloud Provider Selector (Snapchat Cloud updated to Snapchat with self-auth)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("Google Drive", "AWS S3", "Snapchat", "Dropbox").forEach { provider ->
                                val isSelected = provider == cloudSource
                                val authStatus = authStatusMap[provider] ?: "Disconnected"

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (authStatus == "Connecting") {
                                                MiuixTheme.colorScheme.primary.copy(alpha = 0.4f)
                                            } else if (isSelected) {
                                                MiuixTheme.colorScheme.primary
                                            } else {
                                                MiuixTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                                            }
                                        )
                                        .clickable {
                                            if (authStatus == "Disconnected") {
                                                viewModel.authenticateProvider(provider)
                                            }
                                            viewModel.selectedCloudSource.value = provider
                                        }
                                        .padding(vertical = 10.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = provider,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color(0xFF07060C) else Color.White,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                        if (authStatus == "Connecting") {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            CircularProgressIndicator(
                                                strokeWidth = 1.5.dp,
                                                size = 10.dp
                                            )
                                        } else {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = if (authStatus == "Connected") "OAuth OK" else "Tap Auth",
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color(0xFF07060C).copy(alpha = 0.8f) else MiuixTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Target settings options
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MiuixTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(20.dp)),
                    cornerRadius = 20.dp,
                    colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "DELIVERY CONFIGURATION",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MiuixTheme.colorScheme.primary,
                                letterSpacing = 1.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Param 1: Platform Routing (Supports multiple selected destinations simultaneously)
                        Text(text = "Target Social Media Delivery (Select Multiple)", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("TikTok", "Instagram", "YouTube", "Twitter/X").forEach { item ->
                                val isSelected = selectedPlatforms.contains(item)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.outline.copy(alpha = 0.3f),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .background(
                                            if (isSelected) MiuixTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            val current = viewModel.selectedPlatforms.value
                                            val updated = if (current.contains(item)) {
                                                if (current.size > 1) current - item else current
                                            } else {
                                                current + item
                                            }
                                            viewModel.selectedPlatforms.value = updated
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (isSelected) {
                                            Text(text = "✓ ", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MiuixTheme.colorScheme.primary)
                                        }
                                        Text(
                                            text = item,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                            color = if (isSelected) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Media Import Terminal
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, MiuixTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(20.dp)),
                    cornerRadius = 20.dp,
                    colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "INTEGRATED MEDIA SOURCE IMPORT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MiuixTheme.colorScheme.primary,
                                letterSpacing = 1.2.sp
                            )
                        )
                        Text(
                            text = "Select a source to ingest full-fidelity multimedia streams directly into the active render terminal pipeline.",
                            style = MaterialTheme.typography.bodySmall.copy(color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    importFileName = "User_Local_Clip_${System.currentTimeMillis().toString().takeLast(3)}.mp4"
                                    showImportDialogType = "local"
                                },
                                modifier = Modifier.weight(1f),
                                cornerRadius = 10.dp,
                                colors = ButtonDefaults.buttonColors(color = MiuixTheme.colorScheme.secondary),
                                insideMargin = PaddingValues(vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("LOCAL FILE", fontSize = 10.sp, fontWeight = FontWeight.Black)
                                }
                            }

                            Button(
                                onClick = { showImportDialogType = "photos" },
                                modifier = Modifier.weight(1f),
                                cornerRadius = 10.dp,
                                colors = ButtonDefaults.buttonColors(color = MiuixTheme.colorScheme.onTertiaryContainer),
                                insideMargin = PaddingValues(vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Face, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("PHOTOS", fontSize = 10.sp, fontWeight = FontWeight.Black)
                                }
                            }

                            Button(
                                onClick = { showImportDialogType = "gdrive" },
                                modifier = Modifier.weight(1f),
                                cornerRadius = 10.dp,
                                colors = ButtonDefaults.buttonColors(color = MiuixTheme.colorScheme.primary),
                                insideMargin = PaddingValues(vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("G-DRIVE", fontSize = 10.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                }
            }

            // List Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Raw Video Stream Pool",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${cloudPoolClips.size} items available",
                        style = MaterialTheme.typography.bodySmall.copy(color = MiuixTheme.colorScheme.primary)
                    )
                }
            }

            // Available Pool Items
            items(cloudPoolClips) { clip ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MiuixTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
                    cornerRadius = 16.dp,
                    colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surface)
                ) {
                    VideoFirstFrameSurface(
                        videoName = clip.name,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                    .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = clip.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row {
                                    Text(
                                        text = "RAW: ${"%.1f".format(clip.sizeMb)} MB",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.8f))
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Duration: ${clip.durationSec}s",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFFFB300), fontWeight = FontWeight.Bold)
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    viewModel.processClip(clip)
                                    val targets = selectedPlatforms.joinToString(" & ")
                                    showToastMessage = "Porting process initiated seamlessly for $targets channel(s)."
                                },
                                cornerRadius = 10.dp,
                                colors = ButtonDefaults.buttonColors(color = MiuixTheme.colorScheme.primary),
                                insideMargin = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(text = "AUTO PORT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // Elegant Overlay Notification that does NOT shift layout items
        AnimatedVisibility(
            visible = showToastMessage != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp, start = 16.dp, end = 16.dp)
        ) {
            showToastMessage?.let { msg ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFD0BCFF).copy(alpha = 0.35f), RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xEB0F0E17), // Deep elegant dark obsidian tint mirroring design aesthetics
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(MiuixTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "✓", color = Color(0xFFD0BCFF), fontSize = 12.sp, fontWeight = FontWeight.Black)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = msg,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Interactive Modals for Media Source Selection
        if (showImportDialogType != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable { showImportDialogType = null },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .border(1.5.dp, MiuixTheme.colorScheme.outline, RoundedCornerShape(24.dp))
                        .clickable(enabled = false) {},
                    cornerRadius = 24.dp,
                    colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when(showImportDialogType) {
                                    "local" -> "LOCAL FILE PICKER"
                                    "photos" -> "PHOTOS LIBRARY EXPLORER"
                                    else -> "GOOGLE DRIVE STORAGE"
                                },
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            IconButton(onClick = { showImportDialogType = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Close modal")
                            }
                        }

                        if (showImportDialogType == "local") {
                            Text(
                                text = "Enter filename and size parameters. In a production environment, this triggers a native Android file chooser.",
                                style = MaterialTheme.typography.bodySmall.copy(color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                            )

                            TextField(
                                value = importFileName,
                                onValueChange = { importFileName = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = "File Name (e.g., Cooking_Vlog.mp4)",
                                useLabelAsPlaceholder = true
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                TextField(
                                    value = importFileSize,
                                    onValueChange = { importFileSize = it },
                                    modifier = Modifier.weight(1f),
                                    label = "Size (MB)",
                                    useLabelAsPlaceholder = true
                                )
                                TextField(
                                    value = importFileDuration,
                                    onValueChange = { importFileDuration = it },
                                    modifier = Modifier.weight(1f),
                                    label = "Duration (sec)",
                                    useLabelAsPlaceholder = true
                                )
                            }

                            Button(
                                onClick = {
                                    val size = importFileSize.toDoubleOrNull() ?: 75.0
                                    val duration = importFileDuration.toIntOrNull() ?: 30
                                    val name = if (importFileName.isNotBlank()) importFileName else "Imported_Clip.mp4"
                                    viewModel.importClip(name, size, duration)
                                    showToastMessage = "Success: Ingested '$name' into active stream pool."
                                    showImportDialogType = null
                                },
                                modifier = Modifier.fillMaxWidth(),
                                cornerRadius = 12.dp
                            ) {
                                Text("INGEST LOCAL RUNTIME STREAM")
                            }
                        } else if (showImportDialogType == "photos") {
                            Text(
                                text = "Pick from device Photo Album or Camera roll streams:",
                                style = MaterialTheme.typography.bodySmall.copy(color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                            )

                            val photoOptions = listOf(
                                Triple("Tokyo_Street_Food_Vibe.mp4", 95.0, 30),
                                Triple("Skiing_Hyperlapse_POV.mp4", 180.0, 60),
                                Triple("Gym_Workout_Motivation.mp4", 65.0, 15)
                            )

                            photoOptions.forEach { opt ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.importClip(opt.first, opt.second, opt.third)
                                            showToastMessage = "Ingested photos video: ${opt.first}"
                                            showImportDialogType = null
                                        }
                                        .border(0.5.dp, MiuixTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                                    cornerRadius = 12.dp,
                                    colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.background)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("📸", fontSize = 20.sp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(opt.first, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text("${opt.second} MB • ${opt.third}s", fontSize = 11.sp, color = MiuixTheme.colorScheme.primary)
                                        }
                                    }
                                }
                            }
                        } else if (showImportDialogType == "gdrive") {
                            Text(
                                text = "Browse and sync from configured cloud Google Drive compartments:",
                                style = MaterialTheme.typography.bodySmall.copy(color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                            )

                            val driveOptions = listOf(
                                Triple("Google_Tech_Panel_Summary.mp4", 450.0, 240),
                                Triple("Ad_Campaign_Short_Vertical.mp4", 30.0, 10),
                                Triple("Coffee_Roastery_B_Roll.mov", 125.0, 35)
                            )

                            driveOptions.forEach { opt ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.importClip(opt.first, opt.second, opt.third)
                                            showToastMessage = "Synced from G-Drive: ${opt.first}"
                                            showImportDialogType = null
                                        }
                                        .border(0.5.dp, MiuixTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                                    cornerRadius = 12.dp,
                                    colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.background)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("📁", fontSize = 20.sp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(opt.first, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text("${opt.second} MB • ${opt.third}s", fontSize = 11.sp, color = MiuixTheme.colorScheme.primary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlatformsScreen(
    viewModel: VideoViewModel,
    modifier: Modifier = Modifier
) {
    val jobs by viewModel.allJobs.collectAsState()

    val connectedAccounts = listOf(
        PlatformAccount("TikTok Professional Route", "@ryan_clips", "241K Followers", "Direct API Posted: ${jobs.count { it.socialPlatform.lowercase() == "tiktok" }}", "🚀 Active"),
        PlatformAccount("Instagram Reels Stream", "@ryan_reels_channel", "89K Followers", "Reels Posted: ${jobs.count { it.socialPlatform.lowercase() == "instagram" || it.socialPlatform.contains("Instagram") }}", "🚀 Active"),
        PlatformAccount("YouTube Shorts Engine", "Ryan MacArthur Shorts", "504K Subscribers", "Shorts Posted: ${jobs.count { it.socialPlatform.lowercase() == "youtube" || it.socialPlatform.contains("YouTube") }}", "🚀 Active"),
        PlatformAccount("Twitter / X Broadcast", "@ryan_tech_video", "42K Followers", "Posts Active: ${jobs.count { it.socialPlatform.contains("Twitter") || it.socialPlatform.contains("X") || it.socialPlatform.lowercase() == "twitter/x" }}", "🚀 Active")
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp,
                colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SOCIAL BROADCAST MANAGER",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MiuixTheme.colorScheme.secondary,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Auto-Posting Live Status",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "All completed WorkManager loops are automatically transcoded to high-efficiency HEVC formatting, meta-tagged, and dispatched via secure OAuth client pathways.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MiuixTheme.colorScheme.secondary.copy(alpha = 0.8f))
                    )
                }
            }
        }

        items(connectedAccounts) { account ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MiuixTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(20.dp)),
                cornerRadius = 20.dp,
                colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = when {
                                    account.platform.contains("TikTok") -> "📱"
                                    account.platform.contains("Instagram") -> "📸"
                                    account.platform.contains("YouTube") -> "🔴"
                                    else -> "🐤"
                                },
                                fontSize = 24.sp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = account.platform,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = account.handle,
                                    style = MaterialTheme.typography.bodySmall.copy(color = MiuixTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFE8F5E9), CircleShape)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(text = "LIVE API", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32)))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MiuixTheme.colorScheme.outline.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "AUDIENCE SIZE", style = MaterialTheme.typography.labelSmall.copy(color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.5f)))
                            Text(text = account.metrics, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "BATON LOOPS", style = MaterialTheme.typography.labelSmall.copy(color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.5f)))
                            Text(text = account.totalPosted, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConfigScreen(
    viewModel: VideoViewModel,
    modifier: Modifier = Modifier
) {
    val jobs by viewModel.allJobs.collectAsState()
    val hardwareAccel by viewModel.hardwareAccelEnabled.collectAsState()

    val cropRatio by viewModel.selectedCropRatio.collectAsState()
    val filter by viewModel.selectedFilter.collectAsState()
    val compression by viewModel.selectedCompression.collectAsState()

    var showResetToastMessage by remember { mutableStateOf(false) }

    LaunchedEffect(showResetToastMessage) {
        if (showResetToastMessage) {
            kotlinx.coroutines.delay(2000)
            showResetToastMessage = false
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            // Toggle items for latest Android version features
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MiuixTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(20.dp)),
                    cornerRadius = 20.dp,
                    colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "HARDWARE TRANSCODING ENGINE RULES",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MiuixTheme.colorScheme.primary,
                                letterSpacing = 1.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Direct GPU Hardware Acceleration", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                Text(
                                    text = "Bypasses slow CPU pipelines for automatic hardware-integrated HEVC transcoding.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                )
                            }
                            Switch(
                                checked = hardwareAccel,
                                onCheckedChange = { viewModel.toggleHardwareAccel() }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = MiuixTheme.colorScheme.outline.copy(alpha = 0.15f))
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Strict Energy Bypass Integration", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                Text(
                                    text = "Uses Android WorkManager expedited jobs to override strict background battery throttling.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                )
                            }
                            Switch(checked = true, onCheckedChange = {})
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = MiuixTheme.colorScheme.outline.copy(alpha = 0.15f))
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Adaptive Pixel Fold layout canvas", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                Text(
                                    text = "Dynamic side navigation rail swaps when folding and unfolding devices.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                )
                            }
                            Switch(checked = true, onCheckedChange = {}, enabled = false)
                        }
                    }
                }
            }

            // Expose the parameters we migrated from the Queue view
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MiuixTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(20.dp)),
                    cornerRadius = 20.dp,
                    colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "DEFAULT TRANSCODING PROFILE CONFIGURATION",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MiuixTheme.colorScheme.primary,
                                letterSpacing = 1.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // Param 1: Smart Crop Mode
                        Text(text = "Smart Reframing Canvas Aspect", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = MiuixTheme.colorScheme.onSurface))
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                            listOf("Vertical (9:16)", "Landscape (16:9)", "Square (1:1)").forEach { item ->
                                val isSelected = item == cropRatio
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.outline.copy(alpha = 0.3f),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .background(
                                            if (isSelected) MiuixTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { viewModel.selectedCropRatio.value = item }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = item,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                        color = if (isSelected) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Param 2: Video Compression settings
                        Text(text = "Compression Tech Spec (Codecs)", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = MiuixTheme.colorScheme.onSurface))
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                            listOf("H.265 (HEVC)", "AV1", "H.264 (AVC)").forEach { item ->
                                val isSelected = item == compression
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.outline.copy(alpha = 0.3f),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .background(
                                            if (isSelected) MiuixTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { viewModel.selectedCompression.value = item }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = item,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                        color = if (isSelected) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Param 3: Renderer LUT Filters
                        Text(text = "GPU Acceleration Filter Style", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = MiuixTheme.colorScheme.onSurface))
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                            listOf("Cinematic Teal", "Sunset Vibe", "Noir Vibe", "Retro Film").forEach { item ->
                                val isSelected = item == filter
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.outline.copy(alpha = 0.3f),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .background(
                                            if (isSelected) MiuixTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { viewModel.selectedFilter.value = item }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = item,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                        color = if (isSelected) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Action database cleanups
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MiuixTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(20.dp)),
                    cornerRadius = 20.dp,
                    colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "MAINTENANCE & TEST ACTIONS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MiuixTheme.colorScheme.primary,
                                letterSpacing = 1.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                viewModel.clearAllHistory()
                                showResetToastMessage = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            cornerRadius = 12.dp,
                            colors = ButtonDefaults.buttonColors(color = MiuixTheme.colorScheme.error)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "CLEAR ALL DATABASE JOBS", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Debug Info: ${jobs.size} jobs registered in local SQLite Room instance. Enqueued jobs process sequentially. Adaptive Fold layout checks WindowSize runtime specifications.",
                            style = MaterialTheme.typography.bodySmall.copy(color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        )
                    }
                }
            }
        }

        // Floating non-shifting snackbar overlay
        AnimatedVisibility(
            visible = showResetToastMessage,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp, start = 16.dp, end = 16.dp)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFD0BCFF).copy(alpha = 0.35f), RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xEB0F0E17),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color(0xFFE57373).copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "✓", color = Color(0xFFEF9A9A), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Database cleared! System is empty.",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

data class PlatformAccount(
    val platform: String,
    val handle: String,
    val metrics: String,
    val totalPosted: String,
    val status: String
)

@Composable
fun GalleryScreen(
    viewModel: VideoViewModel,
    onJobClick: (VideoJob) -> Unit,
    modifier: Modifier = Modifier
) {
    val jobs by viewModel.allJobs.collectAsState()
    val postedClips = remember(jobs) { jobs.filter { it.status == "Posted" || it.uploadProgressPercent == 100 } }

    var selectedClip by remember { mutableStateOf<VideoJob?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var playProgress by remember { mutableStateOf(0.4f) }

    // Auto-animate visualizer ticks if playing
    var visualizerPulse by remember { mutableStateOf(0f) }
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (true) {
                kotlinx.coroutines.delay(100)
                visualizerPulse = (visualizerPulse + 1f) % 10f
                playProgress = (playProgress + 0.01f).let { if (it > 1f) 0f else it }
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        item {
            // Screen title & dynamic metrics
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0x3300E5FF), RoundedCornerShape(20.dp)),
                cornerRadius = 20.dp,
                colors = CardDefaults.defaultColors(color = Color(0x1F00E5FF))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                    text = "GALLERY COMPARTMENT",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MiuixTheme.colorScheme.primary,
                                        letterSpacing = 1.sp
                                    )
                                )
                                Text(
                                    text = "Generated Assets & Clips",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MiuixTheme.colorScheme.onSurface
                                    )
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .background(MiuixTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                                    .border(1.dp, MiuixTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "ONLINE STATE",
                                    color = MiuixTheme.colorScheme.primary,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "TOTAL CLIPS", fontSize = 10.sp, color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                Text(text = "${postedClips.size}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = MiuixTheme.colorScheme.onSurface)
                            }
                            Column {
                                Text(text = "CONVERTED RATIO", fontSize = 10.sp, color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                Text(text = "100.0%", fontSize = 16.sp, fontWeight = FontWeight.Black, color = MiuixTheme.colorScheme.secondary)
                            }
                            Column {
                                Text(text = "BITRATE STATE", fontSize = 10.sp, color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                Text(text = "OPTIMIZED", fontSize = 16.sp, fontWeight = FontWeight.Black, color = MiuixTheme.colorScheme.onTertiaryContainer)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { viewModel.syncAllPlatformEngagements() },
                            modifier = Modifier.fillMaxWidth().height(36.dp),
                            cornerRadius = 10.dp,
                            colors = ButtonDefaults.buttonColors(color = MiuixTheme.colorScheme.primaryContainer),
                            insideMargin = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Sync Metrics",
                                modifier = Modifier.size(14.dp),
                                tint = MiuixTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("RE-SYNC ALL PLATFORM ENGAGEMENT METRICS 📡", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MiuixTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
            }

            // Active Preview Player Section
            item {
                AnimatedContent(
                    targetState = selectedClip ?: postedClips.firstOrNull(),
                    label = "ActivePlayerAnim"
                ) { clip ->
                    if (clip != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(2.dp, MiuixTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                            cornerRadius = 12.dp,
                            colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surface)
                        ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Video playback viewport area
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.Black)
                                    .border(2.dp, MiuixTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                // Black screen for high-contrast video view
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black)
                                )

                                // Play indicator / audio pulses
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    if (isPlaying) {
                                        // Animated audio frequency bars
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.height(36.dp)
                                        ) {
                                            for (barIdx in 0..5) {
                                                val heightMultiplier = ((visualizerPulse + barIdx) % 5f) / 5f
                                                Box(
                                                    modifier = Modifier
                                                        .width(4.dp)
                                                        .fillMaxHeight(0.2f + 0.8f * heightMultiplier)
                                                        .background(MiuixTheme.colorScheme.primary, RoundedCornerShape(2.dp))
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "PLAYING: ${clip.originalName}",
                                            fontSize = 9.sp,
                                            color = MiuixTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    } else {
                                        IconButton(
                                            onClick = { isPlaying = true },
                                            modifier = Modifier
                                                .size(48.dp)
                                                .background(MiuixTheme.colorScheme.primaryContainer, CircleShape)
                                                .border(2.dp, MiuixTheme.colorScheme.outline, CircleShape)
                                        ) {
                                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play", tint = MiuixTheme.colorScheme.primary)
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "SOURCE FORMAT: ${clip.videoFormat}",
                                            fontSize = 9.sp,
                                            color = Color.White.copy(alpha = 0.8f),
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Scrub bar on the light card surface
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "0:${String.format("%02d", (playProgress * clip.durationSeconds).toInt())}",
                                    fontSize = 11.sp,
                                    color = MiuixTheme.colorScheme.onSurface,
                                    fontFamily = FontFamily.Monospace
                                )
                                Slider(
                                    value = playProgress,
                                    onValueChange = { playProgress = it },
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "0:${clip.durationSeconds}",
                                    fontSize = 11.sp,
                                    color = MiuixTheme.colorScheme.onSurface,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            // Media controls
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { playProgress = 0f }) {
                                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Rewind", tint = MiuixTheme.colorScheme.onSurface)
                                }
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(MiuixTheme.colorScheme.secondaryContainer, CircleShape)
                                        .border(2.dp, MiuixTheme.colorScheme.outline, CircleShape)
                                        .clickable { isPlaying = !isPlaying },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isPlaying) "⏸" else "▶",
                                        color = MiuixTheme.colorScheme.secondary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                val localUri = LocalUriHandler.current
                                Button(
                                    onClick = {
                                        if (clip.postUrl.isNotEmpty()) {
                                            localUri.openUri(clip.postUrl)
                                        }
                                    },
                                    modifier = Modifier.border(2.dp, MiuixTheme.colorScheme.outline, RoundedCornerShape(10.dp)),
                                    cornerRadius = 10.dp,
                                    colors = ButtonDefaults.buttonColors(
                                        color = MiuixTheme.colorScheme.primary,
                                        contentColor = MiuixTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    Text("Watch On ${clip.socialPlatform}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Engagement Performance Board
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MiuixTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                    .border(1.dp, MiuixTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "📊 LIVE ENGAGEMENT METRICS",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MiuixTheme.colorScheme.primary,
                                            letterSpacing = 0.8.sp
                                        )
                                        Text(
                                            text = if (clip.engagementLastUpdated > 0) {
                                                val diffSec = (System.currentTimeMillis() - clip.engagementLastUpdated) / 1000
                                                when {
                                                    diffSec < 60 -> "Synced: Just now"
                                                    diffSec < 3600 -> "Synced: ${diffSec / 60}m ago"
                                                    else -> "Synced: ${diffSec / 3600}h ago"
                                                }
                                            } else {
                                                "Never Synced"
                                            },
                                            fontSize = 9.sp,
                                            color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        // Views Tracker
                                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                            Text("👁 Views", fontSize = 10.sp, color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                            Text(
                                                text = if (clip.viewsCount >= 1000) String.format("%.1fk", clip.viewsCount / 1000.0) else clip.viewsCount.toString(),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Black,
                                                color = MiuixTheme.colorScheme.onSurface
                                            )
                                        }
                                        // Likes Tracker
                                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                            Text("❤ Likes", fontSize = 10.sp, color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                            Text(
                                                text = if (clip.likesCount >= 1000) String.format("%.1fk", clip.likesCount / 1000.0) else clip.likesCount.toString(),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color(0xFFFF1744)
                                            )
                                        }
                                        // Shares Tracker
                                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                            Text("↩ Shares", fontSize = 10.sp, color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                            Text(
                                                text = if (clip.sharesCount >= 1000) String.format("%.1fk", clip.sharesCount / 1000.0) else clip.sharesCount.toString(),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color(0xFF00E676)
                                            )
                                        }
                                        // Comments Tracker
                                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                            Text("💬 Comments", fontSize = 10.sp, color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                            Text(
                                                text = if (clip.commentsCount >= 1000) String.format("%.1fk", clip.commentsCount / 1000.0) else clip.commentsCount.toString(),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Black,
                                                color = MiuixTheme.colorScheme.secondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Clip items section title
        item {
            Text(
                text = "PORTED MULTIMEDIA TIMELINE (${postedClips.size} CLIPS)",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MiuixTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
            )
        }

        if (postedClips.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🎞️", fontSize = 32.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No fully processed clips available yet.",
                            color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Enqueue and render cloud assets in Monitor/Queue.",
                            color = MiuixTheme.colorScheme.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            items(postedClips) { clip ->
                val isCurrent = selectedClip?.id == clip.id
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .border(
                            if (isCurrent) 3.dp else 2.dp,
                            if (isCurrent) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.outline,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            selectedClip = clip
                            isPlaying = true
                        },
                    cornerRadius = 12.dp,
                    colors = CardDefaults.defaultColors(color = Color.Transparent)
                ) {
                    VideoFirstFrameSurface(
                        videoName = clip.originalName,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Drop shadow-like dim grid to guarantee high text contrast and visual sharpness
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(Color.Black.copy(alpha = 0.65f))
                        )
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Live Thumbnail Icon block
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black.copy(alpha = 0.5f))
                                    .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "🎬", fontSize = 20.sp)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Video metadata texts
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = clip.originalName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = clip.socialPlatform,
                                        fontSize = 11.sp,
                                        color = MiuixTheme.colorScheme.primaryContainer,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = " • ${clip.durationSeconds}s • ${clip.targetCompression}",
                                        fontSize = 10.sp,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                                Text(
                                    text = "Filter: ${clip.filterApplied} • Effects: ${clip.appliedMedia3Effects}",
                                    fontSize = 9.sp,
                                    color = Color.White.copy(alpha = 0.6f),
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                if (clip.viewsCount > 0) {
                                    Row(
                                        modifier = Modifier.padding(top = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFF00E5FF).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "👁 ${if (clip.viewsCount >= 1000) String.format("%.1fK", clip.viewsCount / 1000.0) else clip.viewsCount}",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF00E5FF)
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFFFF1744).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "❤ ${if (clip.likesCount >= 1000) String.format("%.1fK", clip.likesCount / 1000.0) else clip.likesCount}",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFFF1744)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Open telemetry HUD drawer
                            IconButton(
                                onClick = { onJobClick(clip) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color.White.copy(alpha = 0.15f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "View telemetry",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
