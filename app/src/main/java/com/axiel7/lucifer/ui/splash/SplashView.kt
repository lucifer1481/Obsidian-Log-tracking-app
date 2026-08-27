package com.axiel7.lucifer.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashView(onSplashFinished: () -> Unit) {
    // Animation progress from 0f (hidden/left) to 1f (fully visible)
    val alphaAnim = remember { Animatable(0f) }
    val slideAnim = remember { Animatable(-50f) } // Starts slightly shifted to the left

    LaunchedEffect(key1 = true) {
        // Smooth fade-in and slide-in from left to right over 1 second
        alphaAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000)
        )
    }

    LaunchedEffect(key1 = true) {
        slideAnim.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 1000)
        )
        // Hold the screen for 1.5 seconds so it can be admired
        delay(1500L)
        onSplashFinished()
    }

    // Cinematic Gradient: Deep cinematic blood-red fading smoothly into pure pitch black
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF2A080A), // Deep dark red tint at the top
            Color(0xFF000000), // Pure pitch black at the bottom
            Color(0xFF000000)
        )
    )

    // Gradient on the text itself: Shifting from a bright crimson red to a fiery orange-red
    val textGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFFFF1E1E), // Bright Cinematic Red
            Color(0xFFFF8A8A)  // Soft glowing highlight red
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient), // Applied the smooth gradient background
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "OBSIDIAN LOG",
            fontSize = 52.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 10.sp,
            // Applies the gradient directly to the font fill
            style = androidx.compose.ui.text.TextStyle(brush = textGradient),
            modifier = Modifier
                .alpha(alphaAnim.value) // Fades in smoothly
                .graphicsLayer(translationX = slideAnim.value) // Slides smoothly from left to right
        )
    }
}
