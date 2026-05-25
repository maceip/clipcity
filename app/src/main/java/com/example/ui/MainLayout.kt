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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.VideoJob
import com.example.ui.navigation.Nav3Controller
import com.example.ui.navigation.Nav3Host
import com.example.ui.navigation.Screen
import com.example.ui.screens.*
import com.example.ui.viewmodel.VideoViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.HazeStyle

@OptIn(ExperimentalMaterial3Api::class)
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

    // Determine the active overlay job if selected
    val activeOverlayJobId = nav3Controller.activeJobOverlayId
    val activeJob = jobs.firstOrNull { it.id == activeOverlayJobId }

    // Root layout using BoxWithConstraints to support Pixel Fold & normal Android dynamically
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val isWideScreen = maxWidth >= 600.dp

        Row(
            modifier = Modifier
                .fillMaxSize()
                .haze(hazeState)
        ) {
            // Adaptive Navigation Component (Navigation Rail for unfolded foldables/tablets)
            if (isWideScreen) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxHeight()
                        .border(width = 2.dp, color = MaterialTheme.colorScheme.outline),
                    header = {
                        Box(
                            modifier = Modifier
                                .padding(vertical = 16.dp)
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "JD",
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 16.sp
                            )
                        }
                    }
                ) {
                    Spacer(modifier = Modifier.height(12.dp))
                    val mainScreens = listOf(Screen.Monitor, Screen.Gallery, Screen.Queue)
                    mainScreens.forEach { screen ->
                        val isSelected = nav3Controller.currentScreen == screen
                        NavigationRailItem(
                            selected = isSelected,
                            onClick = { nav3Controller.navigate(screen) },
                            icon = {
                                Icon(
                                    imageVector = getScreenIcon(screen),
                                    contentDescription = screen.title,
                                    tint = if (isSelected) getScreenColor(screen) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    color = if (isSelected) getScreenColor(screen) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = Color.Unspecified,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurface,
                                selectedTextColor = Color.Unspecified,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                            )
                        )
                    }
                }
            }

            // Main Scaffold Frame
            Scaffold(
                modifier = Modifier.weight(1f),
                topBar = {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = nav3Controller.currentScreen.title,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 20.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
                                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "V16.1",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        },
                        navigationIcon = {
                            if (nav3Controller.backstack.size > 1) {
                                IconButton(onClick = { nav3Controller.pop() }) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "Navigate back",
                                        tint = MaterialTheme.colorScheme.onBackground
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
                            // Shortcuts for secondary setups to completely offload primary menu clutter
                            IconButton(onClick = { nav3Controller.navigate(Screen.Platforms) }) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Social Channels Hub",
                                    tint = if (nav3Controller.currentScreen == Screen.Platforms) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                                )
                            }
                            IconButton(onClick = { nav3Controller.navigate(Screen.Config) }) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Engine Profiles",
                                    tint = if (nav3Controller.currentScreen == Screen.Config) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            // Display user avatar badge in header
                            Box(
                                modifier = Modifier
                                    .padding(end = 12.dp)
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "JD",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent
                        )
                    )
                },
                bottomBar = {
                    // Bottom Navigation Bar for standard portrait screens & folded Pixel Fold configs
                    if (!isWideScreen) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .height(80.dp)
                                .border(width = 2.dp, color = MaterialTheme.colorScheme.outline)
                        ) {
                            val mainScreens = listOf(Screen.Monitor, Screen.Gallery, Screen.Queue)
                            mainScreens.forEach { screen ->
                                val isSelected = nav3Controller.currentScreen == screen
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { nav3Controller.navigate(screen) },
                                    icon = {
                                        Icon(
                                            imageVector = getScreenIcon(screen),
                                            contentDescription = screen.title,
                                            tint = if (isSelected) getScreenColor(screen) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = screen.title,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 11.sp,
                                            color = if (isSelected) getScreenColor(screen) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.Unspecified,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        selectedTextColor = Color.Unspecified,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                                    )
                                )
                            }
                        }
                    }
                },
                containerColor = MaterialTheme.colorScheme.background
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (isWideScreen) {
                        // Supporting Pane layout strategy matching Material 3 Canonical layouts
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Left Column: Main Scene Content (60% width)
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
                            
                            // Right Column: Supporting Side Pane (40% width) showing details
                            Box(
                                modifier = Modifier
                                    .weight(0.4f)
                                    .fillMaxHeight()
                                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                                    .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
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
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold, 
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            )
                                            IconButton(onClick = { nav3Controller.closeOverlay() }) {
                                                Icon(
                                                    imageVector = Icons.Default.Close, 
                                                    contentDescription = "Close overlay pane",
                                                    tint = MaterialTheme.colorScheme.onSurface
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
                                            progress = inlineActiveJob.uploadProgressPercent / 100f,
                                            modifier = Modifier.fillMaxWidth(),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text(
                                            text = "CANONICAL PRESET STATE",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold, 
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        )
                                        Text(
                                            text = "Device Multi-Pane Console",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f))
                                        
                                        Text(text = "Default Crop: ${cropRatio}", fontSize = 13.sp)
                                        Text(text = "Rendering CoDec: ${compression}", fontSize = 13.sp)
                                        Text(text = "Active Filter: ${filter}", fontSize = 13.sp)
                                        
                                        Spacer(modifier = Modifier.height(14.dp))
                                        
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(MaterialTheme.colorScheme.background, RoundedCornerShape(12.dp))
                                                .padding(12.dp)
                                        ) {
                                            Column {
                                                Text(
                                                    text = "BUS HARNESS BUS RATE", 
                                                    fontWeight = FontWeight.Bold, 
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.secondary
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
                        // Standard viewport navigation host
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
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(12.dp),
                                shadowElevation = 4.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                                    .clickable { nav3Controller.openOverlay(job.id) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            progress = { job.uploadProgressPercent / 100f },
                                            color = MaterialTheme.colorScheme.primary,
                                            strokeWidth = 2.5.dp,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "BACKGROUND TRANSCODING & PORTING STATUS",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            letterSpacing = 0.8.sp
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "${job.status}: ${job.originalName}",
                                                color = MaterialTheme.colorScheme.onSurface,
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
                                                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(6.dp))
                                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                                ) {
                                                    Text(
                                                        text = "+${ongoingJobs.size - 1} more",
                                                        fontSize = 8.sp,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "VIEW HUD",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Overlay element containing detailed WorkManager tracking and log outputs
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

private fun getScreenColor(screen: Screen): Color {
    return when (screen) {
        Screen.Monitor -> Color(0xFFE91E63)
        Screen.Gallery -> Color(0xFF9C27B0)
        Screen.Queue -> Color(0xFFFF9800)
        Screen.Platforms -> Color(0xFF2196F3)
        Screen.Config -> Color(0xFF4CAF50)
    }
}
