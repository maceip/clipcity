package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

enum class Screen(val title: String, val icon: String) {
    Monitor("Monitor", "🖥️"),
    Gallery("Gallery", "🎞️"),
    Queue("Queue", "📋"),
    Platforms("Platforms", "📱"),
    Config("Config", "⚙️")
}

@Stable
class Nav3Controller(initial: Screen = Screen.Monitor) {
    var currentScreen by mutableStateOf(initial)
    val backstack = mutableStateListOf<Screen>(initial)

    // Secondary stack for details or modal overlays
    var activeJobOverlayId by mutableStateOf<Int?>(null)

    fun navigate(screen: Screen) {
        if (currentScreen != screen) {
            currentScreen = screen
            backstack.add(screen)
        }
    }

    fun pop(): Boolean {
        if (activeJobOverlayId != null) {
            activeJobOverlayId = null
            return true
        }
        if (backstack.size > 1) {
            backstack.removeAt(backstack.lastIndex)
            currentScreen = backstack.last()
            return true
        }
        return false
    }

    fun openOverlay(jobId: Int) {
        activeJobOverlayId = jobId
    }

    fun closeOverlay() {
        activeJobOverlayId = null
    }
}

@Composable
fun Nav3Host(
    controller: Nav3Controller,
    modifier: Modifier = Modifier,
    content: @Composable (Screen) -> Unit
) {
    val current = controller.currentScreen

    // Predictive back for overlay first, then primary screens
    val hasOverlay = controller.activeJobOverlayId != null
    val canPopStack = controller.backstack.size > 1

    BackHandler(enabled = hasOverlay || canPopStack) {
        if (hasOverlay) {
            controller.closeOverlay()
        } else {
            controller.pop()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = current,
            transitionSpec = {
                fadeIn(animationSpec = spring()) + scaleIn(initialScale = 0.95f) togetherWith
                        fadeOut(animationSpec = spring()) + scaleOut(targetScale = 0.95f)
            },
            label = "Nav3Transition"
        ) { targetScreen ->
            content(targetScreen)
        }
    }
}
