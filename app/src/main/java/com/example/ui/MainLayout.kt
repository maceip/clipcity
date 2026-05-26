package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.navigation.Nav3Controller
import com.example.ui.navigation.Nav3Host
import com.example.ui.navigation.Screen
import com.example.ui.screens.*
import com.example.ui.theme.RetroCharcoal
import com.example.ui.theme.RetroCream
import com.example.ui.viewmodel.VideoViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.LinearProgressIndicator
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.basic.NavigationRail
import top.yukonga.miuix.kmp.basic.NavigationRailItem
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Surface
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun MainLayout(
    viewModel: VideoViewModel = viewModel()
) {
    val nav3Controller = remember { Nav3Controller(Screen.Monitor) }
    val jobs by viewModel.allJobs.collectAsState()
    val hazeState = remember { HazeState() }

    val cropRatio by viewModel.selectedCropRatio.collectAsState()
    val compression by viewModel.selectedCompression.collectAsState()
    val filter by viewModel.selectedFilter.collectAsState()

    val activeOverlayJobId = nav3Controller.activeJobOverlayId
    val activeJob = jobs.firstOrNull { it.id == activeOverlayJobId }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MiuixTheme.colorScheme.background)
    ) {
        val isWideScreen = maxWidth >= 600.dp

        Row(
            modifier = Modifier
                .fillMaxSize()
                .haze(hazeState)
        ) {
            // Adaptive Navigation Rail for wide layouts (tablets / unfolded foldables)
            if (isWideScreen) {
                NavigationRail(
                    color = MiuixTheme.colorScheme.surface,
                    modifier = Modifier
                        .border(width = 2.dp, color = MiuixTheme.colorScheme.outline),
                    header = {
                        Box(
                            modifier = Modifier
                                .padding(vertical = 16.dp)
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MiuixTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "JD",
                                fontWeight = FontWeight.Black,
                                color = MiuixTheme.colorScheme.onPrimary,
                                fontSize = 16.sp
                            )
                        }
                    }
                ) {
                    Spacer(modifier = Modifier.height(12.dp))
                    val mainScreens = listOf(Screen.Monitor, Screen.Gallery, Screen.Queue)
                    mainScreens.forEach { screen ->
                        NavigationRailItem(
                            selected = nav3Controller.currentScreen == screen,
                            onClick = { nav3Controller.navigate(screen) },
                            icon = getScreenIcon(screen),
                            label = screen.title,
                        )
                    }
                }
            }

            // Main Scaffold Frame
            Scaffold(
                modifier = Modifier.weight(1f),
                topBar = {
                    SmallTopAppBar(
                        title = nav3Controller.currentScreen.title,
                        color = Color.Transparent,
                        titleColor = MiuixTheme.colorScheme.onBackground,
                        navigationIcon = {
                            if (nav3Controller.backstack.size > 1) {
                                IconButton(onClick = { nav3Controller.pop() }) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "Navigate back",
                                        tint = MiuixTheme.colorScheme.onBackground
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier.padding(start = 12.dp, end = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "🛰️", fontSize = 20.sp)
                                }
                            }
                        },
                        actions = {
                            Box(
                                modifier = Modifier
                                    .background(MiuixTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
                                    .border(1.dp, MiuixTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "V16.1",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MiuixTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = { nav3Controller.navigate(Screen.Platforms) }) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Social Channels Hub",
                                    tint = if (nav3Controller.currentScreen == Screen.Platforms) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onBackground
                                )
                            }
                            IconButton(onClick = { nav3Controller.navigate(Screen.Config) }) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Engine Profiles",
                                    tint = if (nav3Controller.currentScreen == Screen.Config) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onBackground
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .padding(end = 12.dp)
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MiuixTheme.colorScheme.secondary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "JD",
                                    fontWeight = FontWeight.Bold,
                                    color = MiuixTheme.colorScheme.onSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        },
                    )
                },
                bottomBar = {
                    if (!isWideScreen) {
                        NavigationBar(
                            color = MiuixTheme.colorScheme.surface,
                            modifier = Modifier
                                .border(width = 2.dp, color = MiuixTheme.colorScheme.outline)
                        ) {
                            val mainScreens = listOf(Screen.Monitor, Screen.Gallery, Screen.Queue)
                            mainScreens.forEach { screen ->
                                NavigationBarItem(
                                    selected = nav3Controller.currentScreen == screen,
                                    onClick = { nav3Controller.navigate(screen) },
                                    icon = getScreenIcon(screen),
                                    label = screen.title,
                                )
                            }
                        }
                    }
                },
                containerColor = MiuixTheme.colorScheme.background
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (isWideScreen) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(modifier = Modifier.weight(0.6f)) {
                                Nav3Host(controller = nav3Controller) { currentScreen ->
                                    when (currentScreen) {
                                        Screen.Monitor -> MonitorScreen(
                                            viewModel = viewModel,
                                            onJobClick = { job -> nav3Controller.openOverlay(job.id) },
                                            onNavigateToQueue = { nav3Controller.navigate(Screen.Queue) }
                                        )
                                        Screen.Gallery -> GalleryScreen(
                                            viewModel = viewModel,
                                            onJobClick = { job -> nav3Controller.openOverlay(job.id) }
                                        )
                                        Screen.Queue -> QueueScreen(viewModel = viewModel)
                                        Screen.Platforms -> PlatformsScreen(viewModel = viewModel)
                                        Screen.Config -> ConfigScreen(viewModel = viewModel)
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(0.4f)
                                    .fillMaxHeight()
                                    .background(MiuixTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                                    .border(2.dp, MiuixTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                                    .padding(16.dp)
                            ) {
                                val inlineActiveJob = activeJob
                                if (inlineActiveJob != null) {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "INLINE TELEMETRY CONSOLE",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MiuixTheme.colorScheme.primary,
                                            )
                                            IconButton(onClick = { nav3Controller.closeOverlay() }) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Close overlay pane",
                                                    tint = MiuixTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))

                                        VideoFirstFrameSurface(
                                            videoName = inlineActiveJob.originalName,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(120.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                        ) {
                                            Box(modifier = Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = inlineActiveJob.originalName,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                                    modifier = Modifier.padding(12.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(14.dp))

                                        Text(
                                            text = "Status: ${inlineActiveJob.status}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(text = "Codec: ${inlineActiveJob.targetCompression}", fontSize = 12.sp)
                                        Text(text = "Platform: ${inlineActiveJob.socialPlatform}", fontSize = 12.sp)
                                        Text(
                                            text = "Sync Node: ${inlineActiveJob.cloudSource}",
                                            fontSize = 12.sp
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))
                                        LinearProgressIndicator(
                                            modifier = Modifier.fillMaxWidth(),
                                            progress = inlineActiveJob.uploadProgressPercent / 100f,
                                        )
                                    }
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text(
                                            text = "CANONICAL PRESET STATE",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MiuixTheme.colorScheme.primary,
                                        )
                                        Text(
                                            text = "Device Multi-Pane Console",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                        )
                                        HorizontalDivider(color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.15f))

                                        Text(text = "Default Crop: ${cropRatio}", fontSize = 13.sp)
                                        Text(text = "Rendering CoDec: ${compression}", fontSize = 13.sp)
                                        Text(text = "Active Filter: ${filter}", fontSize = 13.sp)

                                        Spacer(modifier = Modifier.height(14.dp))

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(MiuixTheme.colorScheme.background, RoundedCornerShape(12.dp))
                                                .padding(12.dp)
                                        ) {
                                            Column {
                                                Text(
                                                    text = "BUS HARNESS BUS RATE",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    color = MiuixTheme.colorScheme.secondary
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "Inherent TPU Load: ${viewModel.gpuLoad.collectAsState().value}%",
                                                    fontSize = 11.sp,
                                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                                )
                                                Text(
                                                    text = "Ingest: ${"%.1f".format(viewModel.ingestSpeed.collectAsState().value)} MB/s",
                                                    fontSize = 11.sp,
                                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Nav3Host(controller = nav3Controller) { currentScreen ->
                            when (currentScreen) {
                                Screen.Monitor -> MonitorScreen(
                                    viewModel = viewModel,
                                    onJobClick = { job -> nav3Controller.openOverlay(job.id) },
                                    onNavigateToQueue = { nav3Controller.navigate(Screen.Queue) }
                                )
                                Screen.Gallery -> GalleryScreen(
                                    viewModel = viewModel,
                                    onJobClick = { job -> nav3Controller.openOverlay(job.id) }
                                )
                                Screen.Queue -> QueueScreen(viewModel = viewModel)
                                Screen.Platforms -> PlatformsScreen(viewModel = viewModel)
                                Screen.Config -> ConfigScreen(viewModel = viewModel)
                            }
                        }
                    }

                    // Persistent Dynamic Pipeline HUD Overlay
                    val ongoingJobs = jobs.filter { it.status != "Posted" && it.status != "Failed" }
                    val currentlyRunningJob = ongoingJobs.firstOrNull { it.status != "Queued" } ?: ongoingJobs.firstOrNull()
                    val showMiniProgressHUD = currentlyRunningJob != null && activeOverlayJobId == null

                    androidx.compose.animation.AnimatedVisibility(
                        visible = showMiniProgressHUD,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp, start = 12.dp, end = 12.dp)
                    ) {
                        currentlyRunningJob?.let { job ->
                            Surface(
                                color = MiuixTheme.colorScheme.surface,
                                shape = RoundedCornerShape(12.dp),
                                shadowElevation = 4.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(2.dp, MiuixTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                                    .clickable { nav3Controller.openOverlay(job.id) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(MiuixTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            progress = job.uploadProgressPercent / 100f,
                                            strokeWidth = 2.5.dp,
                                            size = 18.dp,
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "BACKGROUND TRANSCODING & PORTING STATUS",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MiuixTheme.colorScheme.primary,
                                            letterSpacing = 0.8.sp
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "${job.status}: ${job.originalName}",
                                                color = MiuixTheme.colorScheme.onSurface,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )
                                            if (ongoingJobs.size > 1) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .background(MiuixTheme.colorScheme.primaryContainer, RoundedCornerShape(6.dp))
                                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                                ) {
                                                    Text(
                                                        text = "+${ongoingJobs.size - 1} more",
                                                        fontSize = 8.sp,
                                                        color = MiuixTheme.colorScheme.primary,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(MiuixTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "VIEW HUD",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MiuixTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom overlay sheet with WorkManager tracking and log outputs
        AnimatedVisibility(
            visible = !isWideScreen && activeOverlayJobId != null && activeJob != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            activeJob?.let { job ->
                VideoJobOverlay(
                    job = job,
                    hazeState = hazeState,
                    onClose = { nav3Controller.closeOverlay() }
                )
            }
        }
    }
}

private fun getScreenIcon(screen: Screen): androidx.compose.ui.graphics.vector.ImageVector {
    return when (screen) {
        Screen.Monitor -> Icons.Default.Home
        Screen.Gallery -> Icons.Default.PlayArrow
        Screen.Queue -> Icons.Default.Build
        Screen.Platforms -> Icons.Default.Share
        Screen.Config -> Icons.Default.Settings
    }
}
